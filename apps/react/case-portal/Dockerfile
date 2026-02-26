# Stage 1: Build the application
FROM node:19.8 AS build

WORKDIR /app

COPY package.json yarn.lock ./
ADD deployments/startup.sh /app/startup.sh
ADD deployments/nginx.conf /app/nginx.conf
RUN yarn install --frozen-lockfile
COPY . .
RUN yarn build

# Stage 2: Create the production image
FROM nginx:1.15.8-alpine
    
COPY --from=build /app/dist /usr/share/nginx/html
COPY --from=build /app/startup.sh /startup.sh
COPY --from=build /app/nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

# Start nginx
CMD ["/bin/sh", "startup.sh"]
