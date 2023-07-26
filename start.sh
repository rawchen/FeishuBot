#!/bin/sh
nohup java -Xmn48m -Xms128m -Xmx128m -Xss256k -jar FeishuBot.jar >> app.log &
echo $! > /var/run/FeishuBot.pid
