package com.collibra.marketplace.atscale.util;

import com.collibra.marketplace.atscale.model.Measure;
import com.collibra.marketplace.atscale.model.Project;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import static com.collibra.marketplace.atscale.util.Constants.HYPHEN;
import static com.collibra.marketplace.atscale.util.Constants.NEW_LINE_WITH_ASTERISK;

public class Tools {

    private static final Logger LOGGER = LoggerFactory.getLogger(Tools.class);

    /***
     * utility class pattern
     * The constructor is made private to prevent the class from being instantiated from outside the class.
     */
    private Tools() {
        throw new IllegalStateException("Tools Utility class");
    }


    public static void printMsg(String toPrint) {
        if (Constants.PRINT_MSGS) {
            LOGGER.info(toPrint);
        }
    }

    public static String printSet(Set<String> list, String prefix) {
        StringBuilder retVal = new StringBuilder();
        for (String item : list) {
            if (retVal.length() == 0) {
                retVal.append(prefix).append(item);
            } else {
                retVal.append(", ").append(item);
            }
        }
        return retVal.toString();
    }

    public static String printSetInLines(Set<String> list, String prefix) {
        StringBuilder retVal = new StringBuilder();
        for (String item : list) {
            if (retVal.length() == 0) {
                retVal.append(NEW_LINE_WITH_ASTERISK+prefix + item);
            } else {
                retVal.append(NEW_LINE_WITH_ASTERISK + item);
            }
        }
        return retVal.toString();
    }

    public static String printList(List<String> list, String prefix) {
        StringBuilder retVal = new StringBuilder();
        for (String item : list) {
            if (retVal.length() == 0) {
                retVal.append(prefix).append(item);
            } else {
                retVal.append(",  ").append(item);
            }
        }
        return retVal.toString();
    }

    public static String printListInLines(List<String> list, String prefix) {
        StringBuilder retVal = new StringBuilder();
        for (String item : list) {
            if (retVal.length() == 0) {
                retVal.append(NEW_LINE_WITH_ASTERISK + prefix + item);
            } else {
                retVal.append(NEW_LINE_WITH_ASTERISK + item);
            }
        }
        return retVal.toString();
    }

    public static String printFirstChars(String val, Integer numChars) {
        if (val.length() > numChars) {
            return val.substring(0,numChars);
        }
        return val;
    }

    public static <T> T coalesce(T ...items) {
        for(T i : items) {
            if (i != null && i.getClass().equals(String.class)) {
                if (((String) i).length() > 0) {
                    return i;
                }
            } else {
                if (i != null) return i;
            }
        }
        return null;
    }

    public static String printShort(String toPrint, Integer numChars) {
        toPrint = toPrint.replace("\n", " ");
        if (toPrint.length() > numChars) {
            return toPrint.substring(0,numChars) + "...";
        }
        return toPrint;
    }

    public static void printWithFilter(String filter, String toPrint) {
        for (String lookFor: Constants.FILTER) {
            if (lookFor.toLowerCase(Locale.ROOT).equals(filter.toLowerCase(Locale.ROOT))) {
                LOGGER.info("CCC {}", toPrint);

            }
        }
    }

    public static void printWithFilterAnySubset(String filter, String toPrint) {
        for (String lookFor: Constants.FILTER) {
            if (filter.toLowerCase(Locale.ROOT).contains(lookFor.toLowerCase(Locale.ROOT))) {
                LOGGER.info("CCC {}", toPrint);
            }
        }
    }

    public static void printHeader(String header, Integer depth) {
        if (Constants.PRINT_HEADERS) {
            StringBuilder indent = new StringBuilder();
            for (Integer i = 1; i < depth; i++) {
                indent.append("   ");
            }
            LOGGER.info("{}** Top of {}",indent, header);
        }
    }

    public static Integer hashStringToInt(String toHash) {
        return new HashCodeBuilder(17, 37).append(toHash).toHashCode();
    }

    public static boolean isEmpty(String in) {
        return (in == null || in.equals(""));
    }

    public static boolean inList(String in, List<String> list) {
        for (String item : list) {
            if (item.equals(in)) {
                return true;
            }
        }
        return false;
    }

    public static boolean inListSubset(String in, List<String> list) {
        for (String item : list) {
            if (in.contains(item)) {
                return true;
            }
        }
        return false;
    }

    public static String getAggregationByKey(int key) {
        switch (key) {
            case 1:
                return "Sum";
            case 2:
                return "Non-Distinct Count";
            case 3:
                return "Minimum";
            case 4:
                return "Maximum";
            case 5:
                return "Average";
            case 7:
                return "Standard Deviation";
            case 8:
                return "Distinct Count";
            default:
                return "";
        }
    }

    public static void addToMapList(Map<String, List<String>> map, String key, String addVal) {
        map.computeIfAbsent(key, k -> new ArrayList<>()).add(addVal);
    }

    public static Document parseXmlFile(String in) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (Exception e) {
            LOGGER.error("Error parsing XML: ",e);
        }
        return null;
    }

    public static void printDebug(boolean debugFlag, String s) {
        if (debugFlag) LOGGER.debug(s);
    }


    public static boolean hasStringValue(String in) {
        return !(in == null || in.isEmpty());
    }

    public static Map<String, Measure> convertCollibraMeasureASMap(List<Measure> collibraProjectMeasureList) {
        return collibraProjectMeasureList.stream()
                .collect(Collectors.toMap(measure -> measure.getCubeName().trim() + HYPHEN + measure.getMeasureName(), measure -> measure));
    }

    public static boolean compareMeasureDescription(Map<String, Measure> collibraProjectMeasureMap, List<Measure> allMeasures, Map<String, Measure> updatedMeasureList) {
        boolean flag = false;
        for (Measure atscaleMeasure : allMeasures) {
            if (collibraProjectMeasureMap.containsKey(atscaleMeasure.getCubeName() + HYPHEN + atscaleMeasure.getMeasureCaption())) {
                Measure collibraMEasure = collibraProjectMeasureMap.get(atscaleMeasure.getCubeName() + HYPHEN + atscaleMeasure.getMeasureCaption());
                if (!collibraMEasure.getAttributeList().isEmpty() && !collibraMEasure.getAttributeList().get(0).getValue().equals(atscaleMeasure.getDescription())) {
                    atscaleMeasure.setDescription(collibraMEasure.getAttributeList().get(0).getValue());
                    updatedMeasureList.put(atscaleMeasure.getMeasureGUID(), atscaleMeasure);
                    flag = true;
                }
            }

        }
        return flag;
    }

    public static List<Measure> getCollibraProjectMeasureList(List<Project> projectList, String key) {
        List<Measure> measerList = null;
        for (Project project : projectList) {
            if (project.getName().contains(key)) {
                measerList = project.getMeasuresList();
            }
        }
        return measerList;
    }

    public static void modifySchema(NodeList parentNode, Document document1, Map<String, Measure> updatedMeasureMap) {
        if (parentNode != null) {
            for (int i = 0; i < parentNode.getLength(); i++) {
                Element attributeElement = (Element) parentNode.item(i);
                String attributeId = attributeElement.getAttribute("id");
                if (updatedMeasureMap.containsKey(attributeId)) {
                    NodeList propertyNodes = attributeElement.getElementsByTagName("properties");
                    if (propertyNodes.getLength() > 0) {
                        Element propertyElement = (Element) propertyNodes.item(0);
                        String measureDescription = getMeasureDescription(attributeId, updatedMeasureMap);
                        if (measureDescription != null) {
                            NodeList descriptionNodes = propertyElement.getElementsByTagName("description");
                            if (descriptionNodes.getLength() > 0) {
                                Element deescriptionElement = (Element) descriptionNodes.item(0);
                                deescriptionElement.setTextContent(measureDescription);
                            } else {
                                org.w3c.dom.Element descriptionElement = document1.createElement("description");
                                descriptionElement.setTextContent(measureDescription);
                                propertyElement.appendChild(descriptionElement);
                            }
                        }
                    }
                }
            }
        }
    }

    public static String formatdocumentToXml(Document document1) {
        StringWriter writer1 = new StringWriter();
        TransformerFactory transformerFactory1 = TransformerFactory.newInstance();
        Transformer transformer1 = null;
        try {
            transformer1 = transformerFactory1.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
        try {
            transformer1.transform(new DOMSource(document1), new StreamResult(writer1));
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
        return writer1.toString();
    }

    public static String getMeasureDescription(String attributeId, Map<String, Measure> updatedMeasureMap) {
        String description = null;
        Measure measure = updatedMeasureMap.get(attributeId);
        if (measure != null) {
            description = measure.getDescription();
        }
        return description;
    }
}
