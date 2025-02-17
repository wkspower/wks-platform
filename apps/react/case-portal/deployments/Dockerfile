FROM nginx:1.15.8-alpine
COPY deployments/nginx.conf /etc/nginx/nginx.conf
COPY dist /usr/share/nginx/html
ADD startup.sh /startup.sh
CMD ["/bin/sh", "startup.sh"]