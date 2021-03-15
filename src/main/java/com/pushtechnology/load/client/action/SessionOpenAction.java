/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
 */
package com.pushtechnology.load.client.action;

import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.SessionEstablishmentException;
import com.pushtechnology.diffusion.client.session.SessionFactory;
import com.pushtechnology.diffusion.client.session.SessionSecurityException;
import com.pushtechnology.load.client.Subscriber;
import com.pushtechnology.load.client.callbacks.SessionOpenCallback;
import com.pushtechnology.load.client.callbacks.SessionOpenCallbackParams;
import com.pushtechnology.load.client.callbacks.StateChangeListener;
import com.pushtechnology.load.client.config.SessionOpenConfig;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * This action attempts to open a new session with Diffusion.
 *
 * @author adam
 */
public class SessionOpenAction extends Action {

    private final SessionOpenCallback SESSION_OPEN_CALLBACK = new SessionOpenCallback();
    private final Session.ErrorHandler SESSION_ERROR_HANDLER = new Session.ErrorHandler.Default() {
        @Override
        public void onError(Session session, Session.SessionError error) {
            Subscriber.LOGGER.error("Error on session " + session.getSessionId() + ": " + error.getMessage());
        }
    };

    private final SessionOpenConfig config;

    private SessionFactory sessionFactory;

    private final Pattern sequencePattern = Pattern.compile("(.*)(%[0-9]*d)(.*)");
    private final AtomicInteger sessionSequence = new AtomicInteger(0);
    private boolean isDynamicPrincipal = false;

    private final ExecutorService openServicePool;

    public SessionOpenAction(String name, SessionOpenConfig cfg) {
        super(name);
        this.config = cfg;

        openServicePool = Executors.newFixedThreadPool((int) (cfg.getConnectRate() * 2));

        sessionFactory = Diffusion.sessions();
        if (cfg.getPrincipal() != null) {
            sessionFactory = sessionFactory.principal(cfg.getPrincipal());
            // Look for replaceable pattern in the principal
            if (sequencePattern.matcher(cfg.getPrincipal()).matches()) {
                isDynamicPrincipal = true;
            }
        }
        if (cfg.getPassword() != null) {
            sessionFactory = sessionFactory.password(cfg.getPassword());
        }

        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            sessionFactory = sessionFactory.sslContext(sslContext);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        sessionFactory = sessionFactory.reconnectionTimeout(10000);
        sessionFactory = sessionFactory.listener(new StateChangeListener());
        sessionFactory = sessionFactory.errorHandler(SESSION_ERROR_HANDLER);
    }

    @Override
    public void run() {

        long lifespan = -1;
        if (config.getActiveForMin() != null && config.getActiveForMax() != null) {
            long min = config.getActiveForMin();
            long max = config.getActiveForMax();
            if (min == max) {
                lifespan = min;
            } else {
                long t = Subscriber.RANDOM.nextLong();
                lifespan = (Math.abs(t) % (max - min)) + min;
            }
        }

        if(isDynamicPrincipal) {
            String principal = String.format(config.getPrincipal(), sessionSequence.getAndIncrement());
            sessionFactory = sessionFactory.principal(principal);
        }

        SessionOpenCallbackParams cbParams = new SessionOpenCallbackParams(lifespan, config.getTopicSelector());

        Runnable task = () -> {
            Session session;

            try {
                session = sessionFactory.open(config.getUrl());
                if(session == null) {
                    SESSION_OPEN_CALLBACK.onError(cbParams, ErrorReason.CALLBACK_EXCEPTION);
                } else {
                    SESSION_OPEN_CALLBACK.onOpened(cbParams, session);
                }
            }
            catch(IllegalArgumentException | IllegalStateException ex) {
                SESSION_OPEN_CALLBACK.onError(cbParams, ErrorReason.INCOMPATIBLE_STATE);
            }
            catch(SessionEstablishmentException ex) {
                SESSION_OPEN_CALLBACK.onError(cbParams, ErrorReason.COMMUNICATION_FAILURE);
            }
            catch(SessionSecurityException ex) {
                SESSION_OPEN_CALLBACK.onError(cbParams, ErrorReason.ACCESS_DENIED);
            }
        };

        openServicePool.execute(task);
    }

}
