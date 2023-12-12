/*
 * (c) 2022 Collibra Inc. This software is protected under international copyright law.
 * You may only install and use this software subject to the license agreement available at https://marketplace.collibra.com/binary-code-license-agreement/.
 * If such an agreement is not in place, you may not use the software.
 */
package com.collibra.marketplace.atscale.controller;

import com.collibra.marketplace.atscale.service.MainProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "/api")
@RequestMapping("/api")
public class EntryPointController {

	private static final Logger LOGGER = LoggerFactory.getLogger(EntryPointController.class);

	private final MainProcessor mainProcessor;

	@Autowired
	public EntryPointController(MainProcessor mainProcessor) {
		this.mainProcessor = mainProcessor;
	}

	@PostMapping("/sync")
	@ResponseBody
	@ApiOperation(value = "Synchronize the metadata")
	public JsonNode syncTriggeredByApiRequest(@RequestParam(required = false) String orgName, String orgId) {
		LOGGER.info("Synchronization triggered via API request");
		return this.mainProcessor.start(orgName, orgId);
	}

	// HTTP POST Endpoint
	@PostMapping("/setup")
	@ApiOperation(value = "Create needed GUIDs")
	public JsonNode setupTriggeredByApiRequest() {
		LOGGER.info("Setup triggered via API request");
		return this.mainProcessor.setup();
	}

	// HTTP POST Endpoint
	@PostMapping("/removeassets")
	@ApiOperation(value = "Remove all AtScale assets from both domains")
	public JsonNode rmAssetsTriggeredByApiRequest() {
		LOGGER.info("Remove all AtScale assets triggered via API request");
		return this.mainProcessor.removeAssets();
	}

	// HTTP POST Endpoint
	@PostMapping("/removetypes")
	@ApiOperation(value = "Remove all AtScale types (assets, attributes, relations and domains)")
	public JsonNode rmTypesTriggeredByApiRequest() {
		LOGGER.info("Remove all AtScale types triggered via API request");
		return this.mainProcessor.removeTypes();
	}

	// HTTP POST Endpoint
	@PostMapping("/removeobsolete")
	@ApiOperation(value = "Remove AtScale assets with obsolete status")
	public JsonNode rmObsTriggeredByApiRequest() {
		LOGGER.info("Remove obsolete assets triggered via API request");
		return this.mainProcessor.removeObsolete();
	}
	@PostMapping("/syncCollibraToAtscale")
	@ApiOperation(value = "syncCollibra")
	public JsonNode syncCollibraToAtscale() {
		LOGGER.info("In syncCollibraToAtscale");
		return this.mainProcessor.syncCollibraToAtscale();
	}
}
