/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
 */
package com.pushtechnology.load.client.config;

import org.json.simple.JSONObject;

/**
 * Read a JSON configuration for a "close" action and extract relevant
 * parameters.
 *
 * @author adam
 */
public class SessionCloseConfig extends ActionConfig {

    private final Long numSessions;
    private final Double closeRate;

    public SessionCloseConfig(String name, long timeOffset, JSONObject cfgObject) {
        super(name, timeOffset, cfgObject);

        this.numSessions = (Long) cfgObject.get("num_sessions");
        this.closeRate = ((Number) cfgObject.getOrDefault("rate", 0)).doubleValue();
    }

    public Long getNumSessions() {
        return numSessions;
    }

    public Double getCloseRate() {
        return closeRate;
    }
}
