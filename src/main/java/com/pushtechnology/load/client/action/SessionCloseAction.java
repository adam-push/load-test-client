/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
 */
package com.pushtechnology.load.client.action;

import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.SessionId;
import com.pushtechnology.load.client.Subscriber;
import java.util.NoSuchElementException;

/**
 * This action closes a session. If the session is supplied, that session is
 * targeted, otherwise a session is chosen at random from those currently open.
 *
 * @author adam
 */
public class SessionCloseAction extends Action {

    private final Session session;

    public SessionCloseAction(String name, Session session) {
        super(name);
        this.session = session;
    }

    private void addSessionCloseRecorder(Session session) {
        long when = System.currentTimeMillis();
        session.addListener((s, oldState, newState) -> {
            if (newState.isClosed()) {
                Subscriber.ACTIVE_SESSIONS.remove(s.getSessionId());
                Subscriber.STATS_COLLECTOR.getStatistics().recordCloseSessionLatency(System.currentTimeMillis() - when);
            }
        });
    }

    @Override
    public void run() {
        // If we've been passed a session, close it.
        if (session != null) {
            Subscriber.LOGGER.debug("Closing named session " + session.getSessionId());
            addSessionCloseRecorder(session);
            session.close();
        } else {
            // Else, choose random session to close
            Session sess = null;
            try {
                SessionId id = Subscriber.ACTIVE_SESSIONS.keys().nextElement();
                // Remove ASAP, minimises the chance that a future session
                // chosen at random will be this one.
                sess = Subscriber.ACTIVE_SESSIONS.remove(id);
            } catch (NoSuchElementException ignore) {
                // Set is empty, there are no sessions available to be closed.
            }
            if (sess != null) {
                Subscriber.LOGGER.debug("Closing session " + sess.getSessionId());
                addSessionCloseRecorder(sess);
                sess.close();
            }
        }
    }
}
