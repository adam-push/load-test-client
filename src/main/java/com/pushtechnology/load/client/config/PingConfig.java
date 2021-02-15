/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
 */
package com.pushtechnology.load.client.config;

import org.json.simple.JSONObject;

/**
 * Read a JSON configuration for a "ping" action and extract relevant
 * parameters.
 *
 * @author adam
 */
public class PingConfig extends ActionConfig {

    // Rate of pings, in pings/sec.
    private final Double rate;

    public PingConfig(String name, long timeOffset, JSONObject cfgObject) {
        super(name, timeOffset, cfgObject);

        this.rate = ((Number)cfgObject.getOrDefault("rate", 0)).doubleValue();
    }

    public Double getRate() {
        return rate;
    }
}
