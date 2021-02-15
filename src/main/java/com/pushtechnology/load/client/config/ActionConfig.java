/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
 */
package com.pushtechnology.load.client.config;

import org.json.simple.JSONObject;

/**
 * Base class for an action in the configuration file.
 *
 * @author adam
 */
public abstract class ActionConfig {

    private final String name;
    private final long timeOffset;
    private final JSONObject cfgObject;

    public ActionConfig(String name, long timeOffset, JSONObject cfgObject) {
        this.name = name;
        this.timeOffset = timeOffset;
        this.cfgObject = cfgObject;
    }

    public String getName() {
        return name;
    }

    public long getTimeOffset() {
        return timeOffset;
    }

    public JSONObject getCfgObject() {
        return cfgObject;
    }
}
