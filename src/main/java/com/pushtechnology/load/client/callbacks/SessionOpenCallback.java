/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
*/
package com.pushtechnology.load.client.callbacks;

import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.client.features.Topics;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.SessionFactory;
import com.pushtechnology.diffusion.datatype.Bytes;
import com.pushtechnology.load.client.Subscriber;
import com.pushtechnology.load.client.action.SessionCloseAction;
import java.util.concurrent.TimeUnit;

/**
 * Callback on opened session. If successfully connected:
 * <ol>
 *   <li>Register this session in the collection of active sessions.</li>
 *   <li>Subscribe to topics as specified in the action's configuration.</li>
 *   <li>If the session has been configured with a lifetime, schedule a
 *       corresponding close action.</li>
 * </ol>
 * If an error occurred during the session open process, also log that
 * information.
 *
 * @author adam
 */
public class SessionOpenCallback implements SessionFactory.OpenContextCallback<SessionOpenCallbackParams> {

    public static Topics.CompletionCallback SUBSCRIBE_CALLBACK = new Topics.CompletionCallback.Default();

    @Override
    public void onOpened(SessionOpenCallbackParams params, Session session) {
        long now = System.currentTimeMillis();
        Subscriber.LOGGER.debug("Created session " + session.getSessionId());
        Subscriber.STATS_COLLECTOR.getStatistics().setOpenTime();
        Subscriber.STATS_COLLECTOR.getStatistics().incOpenSessionCount();

        Subscriber.STATS_COLLECTOR.getStatistics().recordOpenSessionLatency(now - params.getWhen());

        // Register the active session
        Subscriber.ACTIVE_SESSIONS.put(session.getSessionId(), session);

        // Did we specify a topic selector?
        if (params.getTopicSelector() != null) {
            final Topics topics = session.feature(Topics.class);
            // topics.addTopicStream(params.getTopicSelector(), new TopicStreamCallback());
            topics.addStream(params.getTopicSelector(), Bytes.class, new ValueStreamHandler());
            topics.subscribe(params.getTopicSelector(), SUBSCRIBE_CALLBACK);
        }

        // Do we need to schedule a close for this session too?
        if(params.getLifespan() != -1) {
            SessionCloseAction closeAction = new SessionCloseAction("Lifecycle close", session);
            Subscriber.DEFAULT_SCHEDULED_EXECUTOR.schedule(closeAction, params.getLifespan(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onError(SessionOpenCallbackParams params, ErrorReason reason) {
        Subscriber.LOGGER.debug("Unable to create session: " + reason);
        Subscriber.STATS_COLLECTOR.getStatistics().incOpenSessionErrorCount();
    }
}
