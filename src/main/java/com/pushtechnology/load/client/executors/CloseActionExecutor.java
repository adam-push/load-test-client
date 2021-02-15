/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
 */
package com.pushtechnology.load.client.executors;

import com.pushtechnology.load.client.Subscriber;
import com.pushtechnology.load.client.action.Action;
import com.pushtechnology.load.client.action.LimitedAction;
import com.pushtechnology.load.client.action.SessionCloseAction;
import com.pushtechnology.load.client.config.SessionCloseConfig;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * An ActionExecutor that schedules actions to close sessions.
 *
 * @author adam
 */
public class CloseActionExecutor extends ActionExecutor {

    private final long closeDelay;
    private final Action action;
    private final int limit;

    public CloseActionExecutor(String name, long startDelay, SessionCloseConfig cfg) {
        super(name, startDelay, cfg);

        if (cfg.getCloseRate() == 0) {
            closeDelay = 0;
        } else {
            closeDelay = (long) (1000 / cfg.getCloseRate());
        }

        if(cfg.getNumSessions() == null) {
            limit = 1;
        }
        else {
            limit = cfg.getNumSessions().intValue();
        }

        action = new SessionCloseAction(name, null);
    }

    @Override
    public void run() {
        LimitedAction limited = new LimitedAction(action, limit);

        Subscriber.LOGGER.debug("Starting close action executor \""
                + action.getName()
                + "\" in "
                + getStartDelay()
                + "ms. New action every "
                + closeDelay
                + "ms, limited to "
                + limit
                + " actions");

        long startNanoDelay = TimeUnit.NANOSECONDS.convert(getStartDelay(), TimeUnit.MILLISECONDS);
        long nanoDelay = TimeUnit.NANOSECONDS.convert(closeDelay, TimeUnit.MILLISECONDS);
        if(nanoDelay == 0) {
            nanoDelay = 1;
        }
        ScheduledFuture f = getExecutorService().scheduleAtFixedRate(limited, startNanoDelay, nanoDelay, TimeUnit.NANOSECONDS);
        try {
            limited.await();
        } catch (InterruptedException ex) {
            Subscriber.LOGGER.trace("Interrupted while waiting for all close actions to complete", ex);
        }

        f.cancel(false);
    }

}
