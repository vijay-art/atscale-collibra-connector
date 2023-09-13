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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.collibra.marketplace.atscale.config.ApplicationConfig;

@Component
@EnableScheduling
public class ScheduledProcess {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledProcess.class);

	private final ApplicationConfig appConfig;
	private final MainProcessor mainProcessor;

	@Autowired
	public ScheduledProcess(ApplicationConfig appConfig, MainProcessor mainProcessor) {
		this.appConfig = appConfig;
		this.mainProcessor = mainProcessor;
	}

	// CRON Scheduler
	@Scheduled(cron = "${trigger.scheduler.cron.expression}")
	public void syncTriggeredByCronScheduler() {

		LOGGER.info("CRON TRIGGERED ... ");
		if (this.appConfig.isCronIsEnabled()) {
			LOGGER.info("Synchronization triggered via CRON Scheduler");
			this.mainProcessor.start("{addOrgNameHere}", "{addOrgIDHere}"); // XXX How to set this?
		}
	}

}
