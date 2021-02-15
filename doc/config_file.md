---
title: Load testing tool configuration file
geometry: margin=2cm
---

# Introduction

The load testing tool is driven by a configuration file containing
directives (actions) in JSON format. There is a top level JSON array
named `actions`, which is a list of actions to be performed at certain
times. The order in which the actions are given is not important for
the execution of the test, but for readability you should consider
listing them in execution order.

# Actions

All actions are an object with three common entries:

| Name    | Type    | Description                                                                                |
|:--------|:-------:|:-------------------------------------------------------------------------------------------|
| name    | string  | A string containing a user-friendly name for the action.                                   |
| at_time | integer | The time in seconds after the start of the test that this action will begin.               |
| action  | string  | A string naming the type of action that will be performed. Valid actions are listed below. |


In addition, many actions will have a `params` object containing
configuration parameters specific to the type of action being
performed.

## connect

Requests that one or more connections is made to a server.

### Parameters

| Name           | Type    | Required? | Description                                                                                            |
|:---------------|:-------:|:---------:|:-------------------------------------------------------------------------------------------------------|
| url            | string  | yes       | The URL of the Diffusion server to connect to.                                                         |
| principal      | string  | no        | The username to use when connecting to the server.                                                     |
| password       | string  | no        | The password to use when connecting to the server.                                                     |
| num_sessions   | integer | yes       | The number of sessions that this action will attempt to open with the server.                          |
| topic_selector | string  | no        | Specifies the topics that each new session will subscribe to, if they connect successfully.            |
| lifespan       | object  | no        | If present, this has further configuration defining how long each connected session will be alive for. |

The `lifespan` parameter is defined as:

| Name       | Type    | Required? | Description                                                                                                  |
|:-----------|:-------:|:---------:|:-------------------------------------------------------------------------------------------------------------|
| active_for | integer | yes       | Sessions will close after this many seconds have elapsed from when they were opened.                         |
| active_for | array   | yes       | An array containing two integers; the minimum and maximum number of seconds for which session will be alive. |

### Example

```JSON
    {
        "name"    : "Make 1000 connections at 100/sec",
        "at_time" : 0,
        "action"  : "connect",
        "params"  : {
            "url"            : "ws://localhost:8080",
            "principal"      : "myuser",
            "password"       : "mypass",
            "num_sessions"   : 1000,
            "rate"           : 100,
            "topic_selector" : "?foo/bar//",
            "lifespan"       : {
                "active_for" : [ 5, 15 ]
            }
        }
    }
    
```

## close

Close one or more randomly selected sessions.

### Parameters

| Name         | Type    | Required? | Description                                               |
|:-------------|:-------:|:----------|:----------------------------------------------------------|
| num_sessions | integer | yes       | The number of sessions that will be selected for closure. |
| rate         | float   | yes       | The number of sessions to close, per second.              |

 
### Example

```JSON
{
    "name"    : "Close 100 connections over 5 seconds",
    "at_time" : "30",
    "action"  : "close",
    "params"  : {
        "num_sessions" : 100,
        "rate"         : 20
    }
}
```

## ping

Requests that ping requests are sent to the server. For each request,
a random session is chosen. The server round trip time is recorded and
added to the statistical output.

### Parameters

| Name | Type  | Required? | Description                                        |
|:-----|:-----:|:---------:|:---------------------------------------------------|
| rate | float | yes       | The number of ping requests to be sent per second. |

### Example

```JSON
{
    "name"    : "Ping 10 times per second",
    "at_time" : 0,
    "action"  : "ping",
    "params"  : {
        "rate" : 10
    }
}
```

## end

Causes the test to end at the requested time. If this action is
omitted, the test will continue indefinitely.

### Parameters

None.

### Example

```JSON
{
    "name"    : "End test",
    "at_time" : 120,
    "action"  : "end"
}
```

# A complete example

This example waits for 5 seconds, and then ramps up 1000 anonymous
users at 50 users/second, and on connection, subscribes them to the
`foo/bar` topic hierarchy.

After 60 seconds, there will be a spike of 500 additional connections
joining at a rate of 25/second subscribing to the `foo/baz` topic
hierarchy, but they automatically disconnect after 5 to 15 seconds.

After 120 seconds, all remaining sessions are closed down as quickly
as possible.

After 130 seconds, the test ends and the load testing tool exits.

During the entire test run, and starting immediately, server pings
will be sent from random connected sessions at a rate of 10 per
second.

```JSON
{
    "actions": [
        {
            "name": "Background ping",
            "at_time": 0,
            "action": "ping",
            "params": {
                "rate": 10
            }
        },
        {
            "name": "Ramp up users",
            "at_time": 5,
            "action": "connect",
            "params": {
                "url": "ws://localhost:8080",
                "num_sessions": 1000,
                "rate": 50,
                "topic_selector": "?foo/bar//"
            }
        },
        {
            "name": "Spike",
            "at_time": 60,
            "action": "connect",
            "params": {
                "url": "ws://localhost:8080",
                "num_sessions": 500,
                "rate": 25,
                "session_lifecycle": {
                    "active_for": [ 5, 15 ]
                },
                "topic_selector": "?foo/baz//"
            }
        },
        {
            "name": "Close down",
            "at_time": 120,
            "action": "close",
            "params": {
                "num_sessions": 99999,
                "rate": 0
            }
        },
        {
            "name": "End test",
            "at_time": 130,
            "action": "end"
        }
    ]
}

```
