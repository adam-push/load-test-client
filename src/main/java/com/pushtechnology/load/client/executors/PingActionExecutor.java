/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
 */
package com.pushtechnology.load.client.executors;

import com.pushtechnology.load.client.Subscriber;
import com.pushtechnology.load.client.action.Action;
import com.pushtechnology.load.client.action.PingAction;
import com.pushtechnology.load.client.config.PingConfig;
import java.util.concurrent.TimeUnit;

/**
 * An ActionExecutor that schedules ping messages to measure roundtrip times
 * with the server.
 *
 * @author adam
 */
public class PingActionExecutor extends ActionExecutor {

    private final long pingDelay;
    private final Action action;

    public PingActionExecutor(String name, long startDelay, PingConfig cfg) {
        super(name, startDelay, cfg);

        if (cfg.getRate() == 0) {
            pingDelay = 1000;
        } else {
            pingDelay = (long) (1000 / cfg.getRate());
        }

        action = new PingAction(name);
    }

    @Override
    public void run() {
        Subscriber.LOGGER.debug("Starting ping executor \""
                + action.getName()
                + "\" in "
                + getStartDelay()
                + "ms, at "
                + (pingDelay/1000)
                + " ping(s)/sec");

        long startNanoDelay = TimeUnit.NANOSECONDS.convert(getStartDelay(), TimeUnit.MILLISECONDS);
        long nanoDelay = TimeUnit.NANOSECONDS.convert(pingDelay, TimeUnit.MILLISECONDS);
        if(nanoDelay == 0) {
            nanoDelay = 1;
        }
        getExecutorService().scheduleAtFixedRate(action, startNanoDelay, nanoDelay, TimeUnit.NANOSECONDS);
    }

}
