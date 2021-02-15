/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
*/
package com.pushtechnology.load.client.callbacks;

import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.load.client.Subscriber;

/**
 * Log some statistics when a session changes state.
 *
 * @author adam
 */
public class StateChangeListener implements Session.Listener {

    @Override
    public void onSessionStateChanged(Session session, Session.State oldState, Session.State newState) {
	Subscriber.LOGGER.debug("Session "
                + session.getSessionId()
                + " state changed from "
                + oldState.name()
                + " to "
                + newState.name());

        if (oldState.isConnected() && newState.isClosed()) {
            switch(session.getState()) {
                case CLOSED_BY_CLIENT:
                    Subscriber.STATS_COLLECTOR.getStatistics().incClosedByClientCount();
                    break;
                case CLOSED_BY_SERVER:
                    Subscriber.STATS_COLLECTOR.getStatistics().incClosedByServerCount();
                    break;
                case CLOSED_FAILED:
                    // Already accounted for in session open handler
                    break;
                default:
                    break;
            }
        }
    }
}
