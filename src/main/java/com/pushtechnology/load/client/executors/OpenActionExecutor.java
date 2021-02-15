/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
 */
package com.pushtechnology.load.client.executors;

import com.pushtechnology.load.client.Subscriber;
import com.pushtechnology.load.client.action.Action;
import com.pushtechnology.load.client.action.LimitedAction;
import com.pushtechnology.load.client.action.SessionOpenAction;
import com.pushtechnology.load.client.config.SessionOpenConfig;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * An ActionExecutor that schedules some number of connections to Diffusion.
 *
 * @author adam
 */
public class OpenActionExecutor extends ActionExecutor {

    private final long openDelay;
    private final int limit;

    public OpenActionExecutor(String name, long startDelay, SessionOpenConfig cfg) {
        super(name, startDelay, cfg);

        if (cfg.getConnectRate() == 0) {
            openDelay = 0;
        } else {
            openDelay = (long) (1000 / cfg.getConnectRate());
        }

        if (cfg.getNumSessions() == null) {
            limit = 1;
        } else {
            limit = cfg.getNumSessions().intValue();
        }
    }

    @Override
    public void run() {

        Action action = new SessionOpenAction(getName(), (SessionOpenConfig) getConfig());

        // Limit to the given number of executions.
        LimitedAction limited = new LimitedAction(action, limit);

        Subscriber.LOGGER.debug("Starting open action executor \""
                + action.getName()
                + "\" in "
                + getStartDelay()
                + "ms. New action every "
                + openDelay
                + "ms, limited to "
                + limit
                + " actions");

        long startNanoDelay = TimeUnit.NANOSECONDS.convert(getStartDelay(), TimeUnit.MILLISECONDS);
        long nanoDelay = TimeUnit.NANOSECONDS.convert(openDelay, TimeUnit.MILLISECONDS);
        if (nanoDelay == 0) {
            nanoDelay = 1;
        }
        ScheduledFuture f = getExecutorService().scheduleAtFixedRate(limited, startNanoDelay, nanoDelay, TimeUnit.NANOSECONDS);
        try {
            limited.await();
        } catch (InterruptedException ex) {
            Subscriber.LOGGER.trace("Interrupted while waiting for all open actions to complete", ex);
        }

        f.cancel(false);
    }
}
