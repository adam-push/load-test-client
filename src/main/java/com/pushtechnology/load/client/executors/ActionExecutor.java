/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
 */
package com.pushtechnology.load.client.executors;

import com.pushtechnology.load.client.config.ActionConfig;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 *
 * @author adam
 */
public abstract class ActionExecutor implements Runnable {

    private final String name;
    private final long startDelay;
    private final ActionConfig config;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public ActionExecutor(String name, long startDelay, ActionConfig config) {
        this.name = name;
        this.startDelay = startDelay;
        this.config = config;
    }

    public String getName() {
        return name;
    }

    public long getStartDelay() {
        return startDelay;
    }

    public ActionConfig getConfig() {
        return config;
    }

    public ScheduledExecutorService getExecutorService() {
        return executorService;
    }
}
