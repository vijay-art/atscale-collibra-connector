package com.collibra.marketplace.atscale.api;

import com.collibra.marketplace.atscale.config.ApplicationConfig;
import com.collibra.marketplace.atscale.exception.AtScaleServerClientException;
import com.collibra.marketplace.atscale.util.Constants;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.utils.Base64Coder;
import lombok.Data;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.collibra.marketplace.atscale.util.Constants.*;

@Data
@Component
public class AtScaleServerClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtScaleServerClient.class);

    private String token;
    private String urlAuthorization;
    private String urlQuery;
    private final String engineHost;
    private final String dcHost;
    private final String authHost;
    private final int authorizationPort;
    private final int designCenterPort;
    private final int enginePort;
    private final String username;
    private final boolean disableSSL;
    private final String password;
    private String orgName;
    private String orgGUID;

    public String getUrlAuthorization() {
        return urlAuthorization;
    }

    public String getUrlQuery() {
        return urlQuery;
    }

    @Autowired
    public AtScaleServerClient(ApplicationConfig appConfig) {
        engineHost = appConfig.getAtscaleApiHost();
        dcHost = appConfig.getAtscaleDcHost();
        authHost = appConfig.getAtscaleAuthHost();
        authorizationPort = Integer.parseInt(appConfig.getAtscaleAuthPort());
        designCenterPort = Integer.parseInt(appConfig.getAtscaleDcPort());
        enginePort = Integer.parseInt(appConfig.getAtscaleApiPort());
        username = appConfig.getAtscaleUsername();
        password = appConfig.getAtscalePassword();
        disableSSL = appConfig.getAtscaleDisableSsl();
        orgName = appConfig.getAtscaleOrganizationFilterName();
        orgGUID = appConfig.getAtscaleOrganizationFilterGUID();

        try {
            buildAuthorizationURL();
            buildQueryURL();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * Builds the URL to the AtScale Server for Authorization
     *
     */
    public void buildAuthorizationURL() {
        try {
            String protocol = HTTPS;
            if (disableSSL) {
                protocol = HTTP;
            }
            String organization = this.orgGUID;
            if (this.authorizationPort > 0) {
                this.urlAuthorization = String.format("%s://%s:%d/%s/auth", protocol, authHost, authorizationPort, organization);
            } else {
                this.urlAuthorization = String.format("%s://%s/%s/auth", protocol, authHost, organization);
            }
            LOGGER.info("Connection URL: {}", this.urlAuthorization);
        } catch (Exception ex) {
            LOGGER.error("Error while creating Atscale Authorization URL", ex);
            throw new AtScaleServerClientException("Unable to create AtScale Authorization URL");
        }
    }

    /**
     * Builds the URL to the AtScale Server for Queries
     *
     */
    public void buildQueryURL() {
        try {
            String protocol = HTTPS;
            if (disableSSL) {
                protocol = HTTP;
            }
            String organization = this.orgGUID;
            if (this.enginePort > 0 && this.enginePort != 443) {
                this.urlQuery = String.format("%s://%s:%d/xmla/%s", protocol, engineHost, enginePort, organization);
            } else {
                this.urlQuery = String.format("%s://%s/xmla/%s", protocol, engineHost, organization);
            }

            LOGGER.info("Query URL: {}", this.urlQuery);
        } catch (Exception ex) {
            LOGGER.error("Error while creating Atscale Query URL", ex);
            throw new AtScaleServerClientException("Unable to create AtScale Query URL");
        }
    }

    public String buildAPIURL(String endpoint, String projectUUID, String cubeUUID, String portType) {
        String retVal = Constants.EMPTY_STRING;
        try {
            String protocol = HTTPS;
            if (disableSSL) {
                protocol = Constants.HTTP;
            }
            String organization = this.orgGUID;
            if (endpoint.startsWith("/")) {
                endpoint = endpoint.substring(1);
            }
            endpoint = endpoint.replace("{orgId}", organization).replace("{projectId}", projectUUID).replace("{cubeId}", cubeUUID);
            if (portType.equals("engine")) {
                retVal = String.format("%s://%s:%d/%s", protocol, engineHost, enginePort, endpoint);
            } else {
                retVal = String.format("%s://%s:%d/%s", protocol, dcHost, designCenterPort, endpoint);
            }
            LOGGER.info("API Endpoint URL: {}", endpoint);
        } catch (Exception ex) {
            LOGGER.error("Unable to create AtScale API URL", ex);
            throw new AtScaleServerClientException("Unable to create AtScale API URL");
        }
        return retVal;
    }

    /**
     * Establish Connection to AtScale Server
     */
    public void connect() {
        try {
            HttpClient httpClient = HttpClients.custom()
                    .disableCookieManagement()
                    .build();
            Unirest.setHttpClient(httpClient);
            HttpResponse<String> responseAuth = Unirest.get(getUrlAuthorization())
                    .header(Constants.AUTHORIZATION, BASIC + SPACE + Base64Coder.encodeString(username + ":" + password))
                    .asString();

            this.token = responseAuth.getBody();
            LOGGER.info("Established Connection to AtScale Server");
        } catch (Exception ex) {
            LOGGER.error("Error while creating Atscale connection : ", ex);
            throw new AtScaleServerClientException("Unable to create AtScale connection");
        }
    }

    /**
     * To get the connection for creating statement and executing queries
     *
     * @return connection object
     */
    public String getConnection() {
        if (token.isEmpty()) {
            connect();
        }
        return token;
    }

    public void disconnect() {
        // Future Impl
    }

    /**
     * In case the connection is not working properly, disconnect from the server and establish a new
     * connection
     */
    public void reconnect() {
        disconnect();
        connect();
    }
}
