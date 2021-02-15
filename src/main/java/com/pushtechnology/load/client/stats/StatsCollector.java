/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
 */
package com.pushtechnology.load.client.stats;

import com.pushtechnology.load.client.Subscriber;
import com.pushtechnology.load.client.Subscriber.OutputFormat;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.HdrHistogram.Histogram;

/**
 * Output statistics in the requested format at configured intervals.
 *
 * @author adam
 */
public class StatsCollector implements Runnable {

    /*
     * Helper class to calculate common statistics one time only.
     */
    private class CommonStats {

        private final long now;
        private long runningTime;
        private final int currentSessions;

        public CommonStats(AccumulatedStatistics accumulated, long rate) {
            now = System.currentTimeMillis();
            long start = accumulated.getStartTime();
            runningTime = start > 0 ? now - start : 0;

            // Smooth out jitter in update time for presentation in output
            long smooth = rate / 100;
            long mod = runningTime % rate;
            if (mod < smooth) {
                runningTime -= mod;
            }
            if (mod > (rate - smooth)) {
                runningTime += (rate - mod);
            }

            currentSessions = accumulated.getOpenSessionCount()
                    - (accumulated.getClosedByClientCount() + accumulated.getClosedByServerCount());
        }

        public long getTime() {
            return now;
        }

        public long getRunningTime() {
            return runningTime;
        }

        public int getCurrentSessions() {
            return currentSessions;
        }
    }

    private final long rate;
    private final AtomicReference<Statistics> statsRef;
    private final AccumulatedStatistics accumulated;

    private final ScheduledExecutorService executor;

    private final PrintStream out;
    private OutputFormat outputFormat;

    private volatile boolean headersRequired = true;

    public StatsCollector(long rate) {
        this(rate, null, OutputFormat.MACHINE);
    }

    public StatsCollector(long rate, String filename, OutputFormat outputFormat) {
        this.rate = rate;
        this.outputFormat = outputFormat;

        if (filename == null || filename.equals("-")) {
            out = System.out;
        } else {
            try {
                out = new PrintStream(filename);
            } catch (IOException ex) {
                Subscriber.LOGGER.error("Unable to create log file " + filename);
                throw new RuntimeException(ex);
            }
        }

        this.statsRef = new AtomicReference<>(new Statistics());
        this.accumulated = new AccumulatedStatistics();

        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.executor.scheduleAtFixedRate(this, 0, rate, TimeUnit.MILLISECONDS);
    }

    public Statistics getStatistics() {
        return statsRef.get();
    }

    @Override
    public void run() {
        Statistics newStats = new Statistics();
        Statistics lastStats = statsRef.getAndSet(newStats);

        accumulated.setStartTime(lastStats.getStartTime());
        accumulated.setFirstMessageTime(lastStats.getFirstMessageTime());

        printSummary(lastStats);
    }

    public void stop() {
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException ignore) {
        }
    }

    public void terminate() {
        stop(); // May already have been stopped.
        if (out != System.out) {
            out.close();
        }
    }

    private double calculateAverage(long value, long count) {
        double average = 0;
        if (count > 0) {
            average = (double) value / count;
        }
        return average;
    }

    private String getPercentilesFromHistogram(Histogram hist) {
        return getPercentilesFromHistogram(hist, new int[]{0, 50, 75, 95, 99, 100});
    }

    private String getPercentilesFromHistogram(Histogram hist, int[] percentiles) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (int p : percentiles) {
            if (!first) {
                builder.append(" ");
            }
            builder.append(String.format("%d", hist.getValueAtPercentile(p)));
            first = false;
        }
        return builder.toString();
    }

    /*
     * Human-readable statistics
     */
    private void printSummaryHuman(Statistics statistics) {
        StatsAccumulator.accumulate(accumulated, statistics);
        CommonStats common = new CommonStats(accumulated, rate);

        out.println("Statistics at " + Statistics.DATE_FORMAT.format(common.getTime()));
        out.println("Time now                          : " + common.getTime() + ", +" + common.getRunningTime());
        out.println("Current sessions                  : " + common.getCurrentSessions());
        out.println("                                          Last / Accumulated");
        out.format("Open sessions                     : %10d /  %10d%n", statistics.getOpenSessionCount(), accumulated.getOpenSessionCount());
        out.format("Sessions closed by client         : %10d /  %10d%n", statistics.getClosedByClientCount(), accumulated.getClosedByClientCount());
        out.format("Sessions closed by server         : %10d /  %10d%n", statistics.getClosedByServerCount(), accumulated.getClosedByServerCount());
        out.format("Open session errors               : %10d /  %10d%n", statistics.getOpenSessionErrorCount(), accumulated.getOpenSessionErrorCount());
        out.format("Topic updates received            : %10d /  %10d%n", statistics.getTopicUpdatesReceived(), accumulated.getTopicUpdatesReceived());
        out.format("Bytes received                    : %10d /  %10d%n", statistics.getTopicBytesReceived(), accumulated.getTopicBytesReceived());
        out.format("Topics subscribed                 : %10d /  %10d%n", statistics.getTopicSubscriptions(), accumulated.getTopicSubscriptions());
        out.format("Topics unsubscribed               : %10d /  %10d%n", statistics.getTopicUnsubscriptions(), accumulated.getTopicUnsubscriptions());
        out.format("Ping delay                        : %10d /  %10d (avg: %10.1f/%10.1f)%n",
                statistics.getPingRoundtripTime(), accumulated.getPingRoundtripTime(),
                calculateAverage(statistics.getPingRoundtripTime(), statistics.getPingCount()),
                calculateAverage(accumulated.getPingRoundtripTime(), accumulated.getPingCount()));

        out.println("Ping roundtrip percentiles        : " + getPercentilesFromHistogram(statistics.getPingHistogram()));
        out.println("Open session latency percentiles  : " + getPercentilesFromHistogram(statistics.getOpenSessionLatencyHistogram()));
        out.println("Close session latency percentiles : " + getPercentilesFromHistogram(statistics.getCloseSessionLatencyHistogram()));

        out.println("----------------------------------------------------");
    }

    /*
     * Machine-readable statistics
     */
    private void printSummaryMachine(Statistics statistics) {
        StatsAccumulator.accumulate(accumulated, statistics);
        CommonStats common = new CommonStats(accumulated, rate);

        int percentiles[] = {0, 50, 75, 95, 99, 100};

        if (headersRequired) {
            headersRequired = false;

            out.println("HEADERS SESSIONS"
                    + " CurrentSessionCount"
                    + " OpenedSessions AccumulatedOpenedSessions"
                    + " ClosedByClient AccumulatedClosedByClient"
                    + " ClosedByServer AccumulatedClosedByServer"
                    + " OpenSessionErrorCount AccumulatedOpenSessionErrorCount");
            out.println("HEADERS TOPICS"
                    + " TopicUpdatesReceived AccumulatedTopicUpdatesReceived"
                    + " BytesReceived AccumulatedBytesReceived");
            out.println("HEADERS SUBS"
                    + " Subscriptions AccumulatedSubscriptions"
                    + " Unsubscriptions AccumulatedUnsubscriptions");
            out.println("HEADERS PINGS"
                    + " PingRoundTrip AccumulatedPingRoundTrip"
                    + " AveragePingRoundTrip AccumulatedAveragePingRoundTrip");
            StringBuilder pingHisto = new StringBuilder();
            pingHisto.append("HEADERS PING_PCTILE");
            for (int p : percentiles) {
                pingHisto.append(String.format(" Ping%dPercentile", p));
            }
            out.println(pingHisto.toString());

            StringBuilder openHisto = new StringBuilder();
            openHisto.append("HEADERS OPEN_SESSION_LATENCY_PCTILE");
            for (int p : percentiles) {
                openHisto.append(String.format(" OpenSession%dPercentile", p));
            }
            out.println(openHisto.toString());

            StringBuilder closeHisto = new StringBuilder();
            closeHisto.append("HEADERS CLOSE_SESSION_LATENCY_PCTILE");
            for (int p : percentiles) {
                closeHisto.append(String.format(" CloseSession%dPercentile", p));
            }
            out.println(closeHisto.toString());
        }

        String times = "" + common.getTime() + " " + common.getRunningTime();

        out.println(times
                + " SESSIONS"
                + " " + common.getCurrentSessions()
                + " " + statistics.getOpenSessionCount()
                + " " + accumulated.getOpenSessionCount()
                + " " + statistics.getClosedByClientCount()
                + " " + accumulated.getClosedByClientCount()
                + " " + statistics.getClosedByServerCount()
                + " " + accumulated.getClosedByServerCount()
                + " " + statistics.getOpenSessionErrorCount()
                + " " + accumulated.getOpenSessionErrorCount());
        out.println(times
                + " TOPICS"
                + " " + statistics.getTopicUpdatesReceived()
                + " " + accumulated.getTopicUpdatesReceived()
                + " " + statistics.getTopicBytesReceived()
                + " " + accumulated.getTopicBytesReceived());
        out.println(times
                + " SUBS"
                + " " + statistics.getTopicSubscriptions()
                + " " + accumulated.getTopicSubscriptions()
                + " " + statistics.getTopicUnsubscriptions()
                + " " + accumulated.getTopicUnsubscriptions());
        out.println(times
                + " PINGS"
                + " " + statistics.getPingRoundtripTime()
                + " " + accumulated.getPingRoundtripTime()
                + " " + calculateAverage(statistics.getPingRoundtripTime(), statistics.getPingCount())
                + " " + calculateAverage(accumulated.getPingRoundtripTime(), accumulated.getPingCount()));

        out.println(times
                + " PING_PCTILE "
                + getPercentilesFromHistogram(statistics.getPingHistogram()));
        out.println(times
                + " OPEN_SESSION_LATENCY_PCTILE "
                + getPercentilesFromHistogram(statistics.getOpenSessionLatencyHistogram()));
        out.println(times
                + " CLOSE_SESSION_LATENCY_PCTILE "
                + getPercentilesFromHistogram(statistics.getCloseSessionLatencyHistogram()));
    }

    /*
     * Print out the current statistics in the requested output format.
     */
    public void printSummary(Statistics statistics) {
        if (outputFormat == OutputFormat.MACHINE) {
            printSummaryMachine(statistics);
        }
        else {
            printSummaryHuman(statistics);
        }
    }

    public void printSummary() {
        printSummary(statsRef.get());
    }
}
