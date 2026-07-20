FROM nginxinc/nginx-unprivileged:1.27-alpine
COPY deployments/nginx.conf /etc/nginx/nginx.conf
# uid 101 (nginx) + gid 0, group-writable: startup.sh rewrites index.html at
# boot, and OpenShift restricted-v2 runs the container as an arbitrary UID
# whose only guaranteed membership is gid 0.
COPY --chown=101:0 dist /usr/share/nginx/html
USER root
RUN chmod -R g=u /usr/share/nginx/html
USER 101
COPY deployments/startup.sh /startup.sh
EXPOSE 8080
CMD ["/bin/sh", "/startup.sh"]
