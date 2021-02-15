/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
*/
package com.pushtechnology.load.client.config;

import org.json.simple.JSONObject;

/**
 * Read a JSON configuration for an "end" action and extract relevant
 * parameters.
 *
 * @author adam
 */
public class EndConfig extends ActionConfig {

    public EndConfig(String name, long timeOffset, JSONObject cfgObject) {
        super(name, timeOffset, cfgObject);
    }

}
