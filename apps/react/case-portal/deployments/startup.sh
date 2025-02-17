#!/bin/sh 

envsubst < /usr/share/nginx/html/index.html > /tmp/index.html

cp /tmp/index.html /usr/share/nginx/html/index.html

nginx -g "daemon off;"