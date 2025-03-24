FROM nginx:1.15.8-alpine
COPY deployments/nginx.conf /etc/nginx/nginx.conf
COPY dist /usr/share/nginx/html
# Copy offline css, js, and libs
COPY ./public/css /usr/share/nginx/html/css
COPY ./public/js /usr/share/nginx/html/js
COPY ./public/libs /usr/share/nginx/html/libs
COPY deployments/startup.sh /startup.sh
CMD ["/bin/sh", "startup.sh"]