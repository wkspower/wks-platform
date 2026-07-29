#!/bin/sh

# Fail fast. If the runtime-config substitution below doesn't land, index.html
# still carries the literal $__SERVER_*__ placeholders: the browser then builds
# a nonsense issuer URL and the portal redirect-loops until nginx answers 414.
# That reads as "the portal doesn't open" while the container looks healthy, so
# refuse to serve rather than serve an unconfigured page.
set -e

envsubst < /usr/share/nginx/html/index.html > /tmp/index.html

# Overwrite via redirection (owner O_TRUNC), not cp: under the unprivileged
# image the html directory isn't writable by the runtime UID, so cp's
# unlink-and-recreate replace fails with "File exists" while an in-place
# truncate of the (owned, writable) file succeeds.
cat /tmp/index.html > /usr/share/nginx/html/index.html

# Belt and braces: a partial write or a future refactor could leave placeholders
# behind without any command returning non-zero.
if grep -q '\$__SERVER_' /usr/share/nginx/html/index.html; then
    echo "startup.sh: runtime config substitution failed - index.html still contains \$__SERVER_* placeholders" >&2
    exit 1
fi

nginx
