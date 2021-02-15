---
title: Statistics output by load test subscriber
geometry: margin=2cm
---

# Introduction

Statistics are gathered continually, and output at scheduled
intervals. Some of these are reported for the interval that they were
gathered in, and reset before the next interval starts, and some are
an accumulation of statistics over the lifespan of the test.

# Sessions

Statistics relating to sessions being open and closed.

| Name                                | Description                                                                                                       |
|:------------------------------------|:------------------------------------------------------------------------------------------------------------------|
| CurrentSessionCount                 | The total number of connected sessions.                                                                           |
| OpenedSessions                      | The number of sessions opened during this interval.                                                               |
| AccumulatedOpenSessions             | The total number of sessions opened during the test.                                                              |
| ClosedByClient                      | The number of sessions closed by the subscriber client during this interval.                                      |
| AccumulatedClosedByClient           | The total number of sessions closed by the client during the test.                                                |
| ClosedByServer                      | The number of sessions closed by the server (i.e., the close was not initiated by the test) during this interval. |
| AccumulatedClosedByServer           | The total number of sessions closed by the server during the test.                                                |

# Topics

Statistics related to topics; messages and bytes received.

| Name                             | Description                                             |
|:---------------------------------|:--------------------------------------------------------|
| TopicUpdatesReceived             | Number of topic updated received during this interval.  |
| AccumulatedTopicUpdatesReceieved | Total number of topic updates received during the test. |
| BytesReceived                    | Number of bytes received during this interval.          |
| AccumulatedBytesReceived         | Total number of bytes received during the test.         |

# Subscriptions

Statistics relating to topic subscriptions.

| Name                          | Description                                                                  |
|:------------------------------|:-----------------------------------------------------------------------------|
| Subscriptions                 | Number of topic subscription notifications received during this interval.    |
| AccumulatedSubscriptions      | Total number of topic subscription notifications received during the test.   |
| Unsubscriptions               | Number of topic unsubscription notifications received during this interval.  |
| AccumulatedUnsubscriptions    | Total number of topic unsubscription notifications received during the test. |

# Pings

Statistics related to pings from clients to the server.

| Name                                | Description                                                                     |
|:------------------------------------|:--------------------------------------------------------------------------------|
| PingRoundTrip                       | The total time in milliseconds accrued by ping roundtrips during this interval. |
| AccumulatedPingRoundTrip            | The total time in milliseconds accrued by ping roundtrips during the test.      |
| AveragePingRoundTrip                | The average time in milliseconds of ping roundtrips during this interval.       |
| AccumulatedAveragePingRoundTrip     | The average time in milliseconds of ping roundtrips during the test.            |

# Histograms

Some statistics are gathered during the test and stored internally in
a histogram. Every interval, we output the values at the 0th, 50th,
75th, 95th, 99th and 100th percentiles.

| Name  | Description                                                                                |
|:------|:-------------------------------------------------------------------------------------------|
| Ping  | Percentiles for roundtrip times                                                            |
| Open  | Percentiles of session open latencies (from open request to session open callback).        |
| Close | Percentiles of session close latencies (from close request to session close notification). |
