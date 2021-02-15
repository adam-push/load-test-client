/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
*/
package com.pushtechnology.load.client.config;

import com.pushtechnology.diffusion.utils.tuple.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Read a JSON configuration for an "open" action and extract relevant
 * parameters.
 *
 * @author adam
 */
public class SessionOpenConfig extends ActionConfig {

    private final String url;
    private final String principal;
    private final String password;
    private final Long numSessions;
    private final Double connectRate;
    private final Long createUntil;
    private final Long activeForMin;
    private final Long activeForMax;
    private final String topicSelector;

    public SessionOpenConfig(String name, long timeOffset, JSONObject cfgObject,
                             String defaultURL, String defaultPrincipal, String defaultCredentials) {
        super(name, timeOffset, cfgObject);

        url = (String) cfgObject.getOrDefault("url", defaultURL);
        principal = (String) cfgObject.getOrDefault("principal", defaultPrincipal);
        password = (String) cfgObject.getOrDefault("password", defaultCredentials);

        numSessions = (Long) cfgObject.get("num_sessions");
        connectRate = ((Number) cfgObject.getOrDefault("rate", 0)).doubleValue();
        createUntil = (Long) cfgObject.get("until_time");

        topicSelector = (String) cfgObject.get("topic_selector");

        JSONObject lifecycle = (JSONObject) cfgObject.get("session_lifecycle");
        if (lifecycle != null) {
            Object activeFor = lifecycle.get("active_for");
            if (activeFor != null) {
                Pair<Long, Long> range = getRange(activeFor);
                activeForMin = range.getFirst() * 1000;
                activeForMax = range.getSecond() * 1000;
            } else {
                activeForMin = null;
                activeForMax = null;
            }
        } else {
            activeForMin = null;
            activeForMax = null;
        }
    }

    /*
     * Helper class representing a min/max lifespan for a connection.
     */
    private Pair<Long, Long> getRange(Object obj) {
        if (obj instanceof JSONArray) {
            JSONArray arr = (JSONArray) obj;
            if (arr.isEmpty()) {
                return Pair.of(0L, 0L);
            }
            if (arr.size() == 1) {
                return Pair.of((Long) arr.get(0), (Long) arr.get(0));
            }
            return Pair.of((Long) arr.get(0), (Long) arr.get(1));
        }

        return Pair.of((Long) obj, (Long) obj);
    }

    public String getUrl() {
        return url;
    }

    public String getPrincipal() {
        return principal;
    }

    public String getPassword() {
        return password;
    }

    public Long getNumSessions() {
        return numSessions;
    }

    public Double getConnectRate() {
        return connectRate;
    }

    public Long getCreateUntil() {
        return createUntil;
    }

    public Long getActiveForMin() {
        return activeForMin;
    }

    public Long getActiveForMax() {
        return activeForMax;
    }

    public String getTopicSelector() {
        return topicSelector;
    }
}
