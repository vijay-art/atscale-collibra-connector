package com.collibra.marketplace.atscale.api;

import com.collibra.marketplace.atscale.exception.SOAPQueryException;
import com.collibra.marketplace.atscale.util.Constants;
import com.collibra.marketplace.atscale.util.Tools;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SOAPQuery {
    private static final Logger LOGGER = LoggerFactory.getLogger(SOAPQuery.class);

    /***
     * utility class pattern
     * The constructor is made private to prevent the class from being instantiated from outside the class.
     */
    private SOAPQuery() {
        throw new IllegalStateException("SOAPQuery Utility class");
    }

    public static SOAPResultSet runSOAPQuery(AtScaleServerClient atScaleServerClient, String query, Boolean debugFlag) {
        return runSOAPQuery(atScaleServerClient, query, null, debugFlag);
    }

    public static SOAPResultSet runSOAPQuery(AtScaleServerClient atScaleServerClient, String query, String extraProperty, Boolean debugFlag) {
        SOAPResultSet resultSet = new SOAPResultSet();

        try {
            String body = Constants.BEFORE_QUERY + query;


            // Check & see if we have to add an extra property to the SOAP call
            if (extraProperty != null) {
                String newAfterQuery = Constants.AFTER_QUERY;
                newAfterQuery = newAfterQuery.replace("<PropertyList>\n", "<PropertyList>\n" + extraProperty);
                body += newAfterQuery;
            }
            else {
                body += Constants.AFTER_CATALOG_QUERY; // AFTER_QUERY
            }

            atScaleServerClient.connect();
            String token = atScaleServerClient.getConnection();
            String url = atScaleServerClient.getUrlQuery();

            Tools.printDebug(debugFlag, "Token: '"+token+"'");
            Tools.printDebug(debugFlag, "URL: '"+url+"'");

            HttpClient httpClient = HttpClients.custom()
                    .disableCookieManagement()
                    .build();
            Unirest.setHttpClient(httpClient);

            Tools.printDebug(debugFlag, "Body:\n'"+body+"'");

            HttpResponse<String> response = Unirest.post(url)
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/xml")
                    .body(body)
                    .asString();

            if (response.getBody().contains("Schema not found")) {
                LOGGER.error("DMV call failed with 'Schema not found'. Make sure project is published");
            } else if (!response.getBody().toLowerCase(Locale.ROOT).contains("envelope")) {
                throw new SOAPQueryException("DMV call failed with: " + response.getBody());
            } else {
                    resultSet = processSOAPResponse(response);
            }
        } catch (Exception ex) {
            LOGGER.error("Error while running SOAP query: ", ex);
        }
        return resultSet;
    }

    public static SOAPResultSet processSOAPResponse(HttpResponse<String> response) {
        SOAPResultSet resultSet = new SOAPResultSet();

        try {
            Document doc = parseSOAPResponse(response);

            // Check for a SOAP fault to return error
            hasSOAPFault(doc);

            // Find query ID and log it
            logQueryID(doc);

            // Let's get the list of Fields (fields metadata)
            extractFieldsMetadata(doc, resultSet);

            // Now let's get read the data & create the ResultSet
            readDataAndCreateResultSet(doc, resultSet);
        } catch (Exception e) {
            LOGGER.error("Error while generating SOAP response: ", e);
        }
        return resultSet;
    }

    public static Document parseSOAPResponse(HttpResponse<String> response) throws ParserConfigurationException, IOException, SAXException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(response.getBody()));
        return db.parse(is);
    }

    public static void hasSOAPFault(Document doc) throws SOAPQueryException, XPathExpressionException {
        XPathExpression expr = XPathFactory.newInstance().newXPath().compile("/Envelope/Body/Fault/faultstring");
        Object obj = expr.evaluate(doc, XPathConstants.NODESET);
        if (obj instanceof NodeList && ((NodeList) obj).getLength() > 0) {
            throw new SOAPQueryException("SOAP Fault: "+((NodeList) obj).item(0).getFirstChild().getTextContent());
        }
    }

    public static void logQueryID(Document doc) throws XPathExpressionException {
        XPathExpression expr = XPathFactory.newInstance().newXPath().compile("/Envelope/Header/queryId");
        Object obj = expr.evaluate(doc, XPathConstants.NODESET);
        if (obj instanceof NodeList && ((NodeList) obj).getLength() > 0) {
            LOGGER.info("queryID: {}", ((NodeList) obj).item(0).getFirstChild().getTextContent());
        }
    }

    public static void extractFieldsMetadata(Document doc, SOAPResultSet resultSet) throws XPathExpressionException {
        XPathExpression exprMetadata = XPathFactory.newInstance().newXPath().compile("/Envelope/Body/ExecuteResponse/return/root/schema/complexType/sequence/element");

        Object hitsMetadata = exprMetadata.evaluate(doc, XPathConstants.NODESET);
        if (hitsMetadata instanceof NodeList) {
            NodeList list = (NodeList) hitsMetadata;
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                NamedNodeMap attrs = node.getAttributes();
                Node nameNode = attrs.getNamedItem("name");
                Node typeNode = attrs.getNamedItem("type");
                resultSet.insertColumn(nameNode.getTextContent(), typeNode.getTextContent());
            }
        }
    }

    public static void readDataAndCreateResultSet(Document doc, SOAPResultSet resultSet) throws XPathExpressionException {
        XPathExpression exprData =
                XPathFactory.newInstance().newXPath().compile("/Envelope/Body/ExecuteResponse/return/root/row");

        // Evaluate expression result on XML document
        Object hitsData = exprData.evaluate(doc, XPathConstants.NODESET);
        if (hitsData instanceof NodeList) {
            NodeList list = (NodeList) hitsData;

            // Move through each row
            for (int i = 0; i < list.getLength(); i++) {
                Node nodeRow = list.item(i);
                NodeList children = nodeRow.getChildNodes();

                // Add each column value to an array
                List<String> values = new ArrayList<>();
                for (int j = 0; j < children.getLength(); j++) {
                    Node nodeData = children.item(j);
                    values.add(nodeData.getTextContent());
                }
                resultSet.insertRow(values);
            }
        }
    }

    public static void printDocument(Document doc, OutputStream out) throws  TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(new DOMSource(doc),
                new StreamResult(new OutputStreamWriter(out, StandardCharsets.UTF_8)));
    }

    public static String getStringFromDocument(Document doc)
    {
        try
        {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            return writer.toString();
        }
        catch(TransformerException ex)
        {
            ex.printStackTrace();
            return null;
        }
    }
}
