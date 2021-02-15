/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
 */
package com.pushtechnology.load.client.action;

/**
 * This action indicates the end of the test, and it should terminate.
 *
 * @author adam
 */
public class EndAction extends Action {

    public EndAction(String name) {
        super(name);
    }

    @Override
    public void run() {
        System.exit(0); // TODO: Find a better way
    }
}
