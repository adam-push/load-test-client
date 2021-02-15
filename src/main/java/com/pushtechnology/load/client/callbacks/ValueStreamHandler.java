/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pushtechnology.load.client.callbacks;

import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.client.features.Topics;
import com.pushtechnology.diffusion.client.features.Topics.ValueStream;
import com.pushtechnology.diffusion.client.topics.details.TopicSpecification;
import com.pushtechnology.diffusion.datatype.Bytes;
import com.pushtechnology.load.client.Subscriber;

/**
 *
 * @author adam
 */
public class ValueStreamHandler implements ValueStream<Bytes> {

   @Override
    public void onValue(String topicPath, TopicSpecification specification, Bytes oldValue, Bytes newValue) {
        Subscriber.STATS_COLLECTOR.getStatistics().incTopicBytesReceived(newValue.length());
        Subscriber.STATS_COLLECTOR.getStatistics().incTopicUpdatesReceived();
    }

    @Override
    public void onSubscription(String topicPath, TopicSpecification specification) {
        Subscriber.STATS_COLLECTOR.getStatistics().incTopicSubscriptions();
    }

    @Override
    public void onUnsubscription(String topicPath, TopicSpecification specification, Topics.UnsubscribeReason reason) {
        Subscriber.STATS_COLLECTOR.getStatistics().incTopicUnsubscriptions();
    }

    @Override
    public void onClose() {
        // Ignore
    }

    @Override
    public void onError(ErrorReason errorReason) {
        // Ignore
    }

}
