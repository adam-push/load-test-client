/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
 */
package com.pushtechnology.load.client.action;

import java.util.concurrent.CountDownLatch;

/**
 * Wraps an existing Action to provide the ability to terminate it once it has
 * been run a predetermined number of times.
 *
 * @author adam
 */
public class LimitedAction implements Runnable {

    private final Action action;
    private final CountDownLatch latch;

    public LimitedAction(Action action, int limit) {
        this.action = action;
        this.latch = new CountDownLatch(limit);
    }

    public void await() throws InterruptedException {
        latch.await();
    }

    @Override
    public void run() {
        synchronized (latch) {
            if (latch.getCount() > 0) {
                latch.countDown();
                action.run();
            }
        }
    }
}
