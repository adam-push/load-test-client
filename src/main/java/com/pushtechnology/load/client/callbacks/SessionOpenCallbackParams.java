/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
 */
package com.pushtechnology.load.client.callbacks;

/**
 * Contextual parameters passed to a session open callback.
 *
 * @author adam
 */
public class SessionOpenCallbackParams {

    private final long when;
    private final long lifespan;
    private final String topicSelector;

    public SessionOpenCallbackParams(long lifespan, String topicSelector) {
        this.when = System.currentTimeMillis();
        this.lifespan = lifespan;
        this.topicSelector = topicSelector;
    }

    /**
     * Get the timestamp (in milliseconds) of when the session open call
     * was invoked.
     *
     * @return
     */
    public long getWhen() {
        return when;
    }

    /**
     * Get the requested lifespan of this session.
     * @return The lifespan in milliseconds, or -1 if immortal.
     */
    public long getLifespan() {
        return lifespan;
    }

    /**
     * Get the topic selector to subscribe to when successfully connected.
     * @return A string.
     */
    public String getTopicSelector() {
        return topicSelector;
    }
}
