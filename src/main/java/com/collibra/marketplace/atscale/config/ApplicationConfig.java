/*
 * (c) 2022 Collibra Inc. This software is protected under international copyright law.
 * You may only install and use this software subject to the license agreement available at https://marketplace.collibra.com/binary-code-license-agreement/.
 * If such an agreement is not in place, you may not use the software.
 */
package com.collibra.marketplace.atscale.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Class used to retrieve the application properties.
 */
@Data
@Configuration
public class ApplicationConfig {

	@Value("${trigger.scheduler.cron.enabled}")
	private boolean cronIsEnabled;

	@Value("${atscale.api.dchost}")
	private String atscaleDcHost;

	@Value("${atscale.api.dcport}")
	private String  atscaleDcPort;

	@Value("${atscale.api.apihost}")
	private String atscaleApiHost;

	@Value("${atscale.api.apiport}")
	private String atscaleApiPort;

	@Value("${atscale.api.authhost}")
	private String atscaleAuthHost;

	@Value("${atscale.api.authport}")
	private String atscaleAuthPort;

	@Value("${atscale.api.username}")
	private String atscaleUsername;

	@Value("${atscale.api.password}")
	private String atscalePassword;

	@Value("${atscale.api.disablessl}")
	private Boolean atscaleDisableSsl;

	@Value("${atscale.organization.names}")
	private String atscaleOrganizationNames;

	@Value("${atscale.organization.filter.name}")
	private String atscaleOrganizationFilterName;

	@Value("${atscale.organization.ids}")
	private String atscaleOrganizationGUIDs;

	@Value("${atscale.organization.filter.id}")
	private String atscaleOrganizationFilterGUID;

	@Value("${atscale.community.id}")
	private String atscaleCommunityId;

	@Value("${atscale.business.asset.domainid}")
	private String atscaleBusinessAssetDomainId;

	@Value("${atscale.business.asset.domainname}")
	private String atscaleBusinessAssetDomainName;

	@Value("${atscale.business.asset.domaindescription}")
	private String atscaleBusinessAssetDomainDescription;

	@Value("${collibra.business.asset.domaintype}")
	private String collibraBusinessAssetDomainType;

	@Value("${atscale.data.asset.domainid}")
	private String atscaleDataAssetDomainId;

	@Value("${atscale.data.asset.domainname}")
	private String atscaleDataAssetDomainName;

	@Value("${atscale.data.asset.domaindescription}")
	private String atscaleDataAssetDomainDescription;

	@Value("${collibra.data.asset.domaintype}")
	private String collibraDataAssetDomainType;

	@Value("${collibra.physical.data.domaintype}")
	private String collibraPhysicalDataDomainType;

	@Value("${atscale.filter.project}")
	private String filterProjectString;

	@Value("${atscale.api.raylight-path}")
	private String atscaleRaylightPath;

	@Value("${atscale.api.page.limit}")
	private int atscalePageLimit;

	@Value("${atscale.debug}")
	private Boolean atscaleDebug;

	@Value("${collibra.business.analysts.community.id}")
	private String collibraBusinessAnalystsCommunityId;

	@Value("${collibra.schemas.community.id}")
	private String collibraSchemasCommunityId;

	@Value("${collibra.asset.type.schema}")
	private String collibraAssetTypeSchema;

	@Value("${collibra.asset.type.table}")
	private String collibraAssetTypeTable;

	@Value("${collibra.asset.type.column}")
	private String collibraAssetTypeColumn;

	public List<String> getFilterProjects() {
		if (!filterProjectString.isEmpty()) {
			return Arrays.asList(filterProjectString.split(","));
		}
		return Collections.emptyList();
	}
}