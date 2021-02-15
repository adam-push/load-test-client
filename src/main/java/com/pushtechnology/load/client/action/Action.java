/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
 */
package com.pushtechnology.load.client.action;

/**
 * Base class for all Actions.
 *
 * @author adam
 */
public abstract class Action implements Runnable {

    private final String name;

    public Action(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
