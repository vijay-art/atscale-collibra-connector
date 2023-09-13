/*
 * (c) 2022 Collibra Inc. This software is protected under international copyright law.
 * You may only install and use this software subject to the license agreement available at https://marketplace.collibra.com/binary-code-license-agreement/.
 * If such an agreement is not in place, you may not use the software.
 */
package com.collibra.marketplace.atscale.component;

import com.collibra.marketplace.atscale.util.Constants;
import lombok.Setter;

import java.util.List;

public class AssetNames {

    /***
     * utility class pattern
     * The constructor is made private to prevent the class from being instantiated from outside the class.
     */
    private AssetNames() {
        throw new IllegalStateException("Utility class");
    }

    @Setter
    private static String serverName;

    public static String prepareAssetName(List<String> names) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String name : names) {
            stringBuilder.append(Constants.ASSET_NAME_SEPARATOR);
            stringBuilder.append(name);
        }
        if (stringBuilder.toString().startsWith(" > ")) {
            return replaceSemiColons(stringBuilder.toString().substring(3));
        }
        return replaceSemiColons(stringBuilder.toString());
    }

    private static String replaceSemiColons(String name) {
        return name.replace(";", "&semi").replace("\"", "'");
    }
}
