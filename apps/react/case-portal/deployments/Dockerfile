FROM nginx:1.15.8-alpine
COPY deployments/nginx.conf /etc/nginx/nginx.conf
COPY build /usr/share/nginx/html
CMD ["nginx"]