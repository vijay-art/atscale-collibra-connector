/*
 * (c) 2022 Collibra Inc. This software is protected under international copyright law.
 * You may only install and use this software subject to the license agreement available at https://marketplace.collibra.com/binary-code-license-agreement/.
 * If such an agreement is not in place, you may not use the software.
 */
package com.collibra.marketplace.atscale.model;

import lombok.Data;

@Data
public class Credentials {

	private String clientType;
	private String password;
	private String auth;
	private String userName;
	
}
