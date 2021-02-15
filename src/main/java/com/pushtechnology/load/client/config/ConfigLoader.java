/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
 */
package com.pushtechnology.load.client.config;

import com.pushtechnology.load.client.Subscriber;
import com.pushtechnology.load.client.executors.ActionExecutor;
import com.pushtechnology.load.client.executors.CloseActionExecutor;
import com.pushtechnology.load.client.executors.EndActionExecutor;
import com.pushtechnology.load.client.executors.OpenActionExecutor;
import com.pushtechnology.load.client.executors.PingActionExecutor;
import joptsimple.OptionSet;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Read a JSON configuration file containing a number of actions and schedule
 * them for execution at the appropriate time.
 *
 * @author adam
 */
public class ConfigLoader {

    private final File cfgFile;
    private final OptionSet options;

    public ConfigLoader(String cfgFileName, OptionSet options) {
        this.cfgFile = new File(cfgFileName);
        this.options = options;
    }

    /*
     * Each action in the config file is assigned an ActionExecutor that will
     * be run at the time offset specified by its "at_time" parameter.
     */
    public List<ActionExecutor> loadActionExecutors() {
        JSONParser parser = new JSONParser();

        JSONObject root;
        try {
            root = (JSONObject) parser.parse(new FileReader(cfgFile));
        } catch (IOException | ParseException ex) {
            Subscriber.LOGGER.error("Unable to read file " + cfgFile.getName(), ex);
            throw new RuntimeException(ex);
        }

        // For each action in the JSON configuration, make an ActionExecutor.
        JSONArray actionArray = (JSONArray) root.get("actions");
        List<ActionExecutor> executors = new ArrayList<>();
        actionArray.forEach((json) -> {
            ActionExecutor r = makeActionExecutor((JSONObject) json);
            if (r != null) {
                executors.add(r);
            }
        });
        return executors;
    }

    /*
     * Makes an ActionExecutor with the appropriate configuration.
     */
    private ActionExecutor makeActionExecutor(JSONObject action) {
        String name = (String) action.getOrDefault("name", "undefined");

        long timeOffset = (Long) action.getOrDefault("at_time", 0);
        timeOffset *= 1000; // seconds to milliseconds

        ActionExecutor executor = null;

        ActionConfig cfg;
        String type = (String) action.get("action");
        if (type.equals("connect")) {
            cfg = new SessionOpenConfig(name, timeOffset, (JSONObject) action.get("params"),
                    (String)options.valueOf("url"),
                    (String)options.valueOf("principal"),
                    (String)options.valueOf("password"));
            executor = new OpenActionExecutor(name, timeOffset, (SessionOpenConfig) cfg);
        }
        if (type.equals("close")) {
            cfg = new SessionCloseConfig(name, timeOffset, (JSONObject) action.get("params"));
            executor = new CloseActionExecutor(name, timeOffset, (SessionCloseConfig) cfg);
        }
        if (type.equals("end")) {
            cfg = new EndConfig(name, timeOffset, (JSONObject) action.get("params"));
            executor = new EndActionExecutor(name, timeOffset, (EndConfig) cfg);
        }
        if (type.equals("ping")) {
            cfg = new PingConfig(name, timeOffset, (JSONObject) action.get("params"));
            executor = new PingActionExecutor(name, timeOffset, (PingConfig) cfg);
        }
        return executor;
    }
}
