FROM alpine:3.12  
COPY entrypoint.sh /
RUN set -xe && \
    apk upgrade --no-cache && \
    apk add --no-cache \
        curl \
        git \
        bash && \
    # git clone https://github.com/vishnubob/wait-for-it.git /wait-for-it/ && \
    # chmod +x /wait-for-it/wait-for-it.sh /entrypoint.sh
    chmod +x /entrypoint.sh

#MALLI: https://github.com/camunda/docker-camunda-bpm-platform/blob/next/Dockerfile