#!/bin/sh
PID=$(cat /var/run/FeishuBot.pid)
kill -9 $PID
