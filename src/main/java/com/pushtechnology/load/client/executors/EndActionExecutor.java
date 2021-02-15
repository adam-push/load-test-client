/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
 */
package com.pushtechnology.load.client.executors;

import com.pushtechnology.load.client.action.Action;
import com.pushtechnology.load.client.action.EndAction;
import com.pushtechnology.load.client.config.EndConfig;
import java.util.concurrent.TimeUnit;

/**
 * An ActionExecutor that schedules an action to terminate the subscriber
 * process.
 *
 * @author adam
 */
public class EndActionExecutor extends ActionExecutor {

    private final Action action;

    public EndActionExecutor(String name, long startDelay, EndConfig cfg) {
        super(name, startDelay, cfg);
        this.action = new EndAction(name);
    }

    @Override
    public void run() {
        getExecutorService().schedule(action, getStartDelay(), TimeUnit.MILLISECONDS);
    }
}
