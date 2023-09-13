package com.collibra.marketplace.atscale.model;

import com.collibra.marketplace.atscale.config.ApplicationConfig;
import lombok.Data;

@Data
public class AtScaleInstance {
    private String name;
    private String dcHost;
    private String dcPort;
    private String apiHost;
    private String apiPort;
    private String authHost;
    private String authPort;
    private String username;
    private String password;
    private Boolean disableSsl;

    public AtScaleInstance(ApplicationConfig appConfig) {
        this.name = appConfig.getAtscaleApiHost();
        this.authHost = appConfig.getAtscaleAuthHost();
        this.authPort = appConfig.getAtscaleAuthPort();
        this.apiHost = appConfig.getAtscaleApiHost();
        this.apiPort = appConfig.getAtscaleApiPort();
        this.dcHost = appConfig.getAtscaleDcHost();
        this.dcPort = appConfig.getAtscaleDcPort();
        this.username = appConfig.getAtscaleUsername();
        this.password = appConfig.getAtscalePassword();
        this.disableSsl = appConfig.getAtscaleDisableSsl();
    }
}
