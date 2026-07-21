#!/bin/sh

envsubst < /usr/share/nginx/html/index.html > /tmp/index.html

# Overwrite via redirection (owner O_TRUNC), not cp: under the unprivileged
# image the html directory isn't writable by the runtime UID, so cp's
# unlink-and-recreate replace fails with "File exists" while an in-place
# truncate of the (owned, writable) file succeeds.
cat /tmp/index.html > /usr/share/nginx/html/index.html

nginx
