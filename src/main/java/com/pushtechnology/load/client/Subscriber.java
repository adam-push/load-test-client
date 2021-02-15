/*
 * Push Technology Ltd. ("Push") CONFIDENTIAL
 * Unpublished Copyright © 2017 Push Technology Ltd., All Rights Reserved.
 */
package com.pushtechnology.load.client;

import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.SessionId;
import com.pushtechnology.load.client.config.ConfigLoader;
import com.pushtechnology.load.client.executors.ActionExecutor;
import com.pushtechnology.load.client.stats.StatsCollector;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A scriptable Diffusion subscriber.
 * <p>
 * Reads a list of actions from a configuration file and performs those actions at the user-specified times.
 *
 * @author adam
 */
public class Subscriber {

    // Do we want human or machine-readable statistics output?
    // Machine-readable is in the style of nmon, ultimately processed by a tool
    // such as Kabana.
    public enum OutputFormat {
	HUMAN,
	MACHINE
    }

    public static final Logger LOGGER = LoggerFactory.getLogger("subscriber");
    public static final Random RANDOM = new Random();
    public static final ConcurrentHashMap<SessionId, Session> ACTIVE_SESSIONS = new ConcurrentHashMap<>();
    public static final ScheduledExecutorService DEFAULT_SCHEDULED_EXECUTOR
	    = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    public static StatsCollector STATS_COLLECTOR;
    private final ExecutorService actionExecutorPool = Executors.newCachedThreadPool();
    private String configFile;
    private OptionSet options;

    public Subscriber() {
	RANDOM.setSeed(System.currentTimeMillis());
    }



    /**
     * Main entry point.
     *
     * @param args command-line arguments
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

	final int rc;

	final Subscriber subscriber = new Subscriber();
	subscriber.parseOptions(args);
	subscriber.start();
	rc = subscriber.waitUntilDone();

	Subscriber.STATS_COLLECTOR.stop();
	Subscriber.STATS_COLLECTOR.printSummary();
	Subscriber.STATS_COLLECTOR.terminate();

	LOGGER.info("Subscriber has been stopped");
	System.exit(rc);
    }

    /**
     * Load configuration and start the appropriate number of threads for reading and performing actions.
     */
    private void start() {
	STATS_COLLECTOR.getStatistics().setStartTime();

	// Load config file and get a list of Executors, one per Action.
	ConfigLoader cfgLoader = new ConfigLoader(configFile, options);
	List<ActionExecutor> actionExecutors = cfgLoader.loadActionExecutors();

	LOGGER.info("Starting action executors");
	// Start up all the action executors.
	actionExecutors.forEach(actionExecutorPool::execute);

	// Don't allow any more action executors to start.
	actionExecutorPool.shutdown();
    }

    /**
     * Blocks until all action executors have terminated.
     *
     * @return 0 on controlled shutdown, -1 on unexpected shutdown.
     */
    private int waitUntilDone() {

	try {
	    do {
	    } while (actionExecutorPool.awaitTermination(60, TimeUnit.SECONDS) == false);
	} catch (InterruptedException ex) {
	    LOGGER.trace("Interrupted while waiting for action executors to terminate");
	    return -1;
	}
	return 0;
    }

    private void parseOptions(String args[]) throws IOException {
	OptionParser optionParser = new OptionParser();

	optionParser.acceptsAll(Arrays.asList("f", "filename"),
		"Write stats to the named file. Implies -m unless overridden."
		+ " A value of \"-\" writes to stdout."
		+ " If no value is specified, a unique filename is automatically generated.")
		.withRequiredArg()
		.ofType(String.class);
	optionParser.acceptsAll(Arrays.asList("o", "url"), "Default URL of Diffusion server")
		.withRequiredArg()
		.ofType(String.class)
		.defaultsTo("ws://localhost:8080");
	optionParser.acceptsAll(Arrays.asList("p", "principal"), "Default principal (username)")
		.withRequiredArg()
		.ofType(String.class);
	optionParser.acceptsAll(Arrays.asList("s", "password"), "Default credentials (password)")
		.withRequiredArg()
		.ofType(String.class);
	optionParser.acceptsAll(Arrays.asList("h", "human"), "Human-readable output");
	optionParser.acceptsAll(Arrays.asList("m", "machine"), "Machine-readable output");
	optionParser.acceptsAll(Arrays.asList("c", "config"), "Filename containing test configuration")
		.withRequiredArg()
		.ofType(String.class)
		.defaultsTo("script.json");
	optionParser.acceptsAll(Arrays.asList("u", "updaterate"), "Rate at which the statistics are output, in milliseconds")
		.withRequiredArg()
		.ofType(Long.class)
		.defaultsTo(1000L);
	optionParser.acceptsAll(Arrays.asList("?", "help"), "Show help")
		.forHelp();

	options = optionParser.parse(args);

	if (options.has("help") || args.length == 0) {
	    optionParser.printHelpOn(System.out);
	    System.exit(0);
	}

	OutputFormat outputFormat = OutputFormat.HUMAN;

	if (options.has("filename")) {
	    outputFormat = OutputFormat.MACHINE;
	}
	if (options.has("human")) {
	    outputFormat = OutputFormat.HUMAN;
	} else if (options.has("machine")) {
	    outputFormat = OutputFormat.MACHINE;
	}

	configFile = (String) options.valueOf("config");

	STATS_COLLECTOR = new StatsCollector((Long) options.valueOf("updaterate"), (String) options.valueOf("filename"), outputFormat);
    }
}
