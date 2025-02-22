FROM nginx:1.15.8-alpine
COPY deployments/nginx.conf /etc/nginx/nginx.conf
COPY dist /usr/share/nginx/html
COPY deployments/startup.sh /startup.sh
CMD ["/bin/sh", "startup.sh"]