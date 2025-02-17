# Stage 1: Build the application
FROM node:19.8 AS build

ARG GENERATE_SOURCEMAP
ARG REACT_APP_KEYCLOAK_URL
ARG REACT_APP_API_URL
ARG REACT_APP_STORAGE_URL
ARG REACT_APP_NOVU_ENABLED
ARG REACT_APP_NOVU_PUBLISHER_API_URL
ARG REACT_APP_WEBSOCKETS_ENABLED
ARG REACT_APP_WEBSOCKETS_URL
ARG REACT_APP_WEBSOCKETS_CASE_CREATED
ARG REACT_APP_WEBSOCKETS_HUMAN_TASK_CREATED

WORKDIR /app

COPY package.json yarn.lock ./
ADD deployments/startup.sh /app/startup.sh
RUN yarn install --frozen-lockfile
COPY . .
RUN yarn build

# Stage 2: Create the production image
FROM nginx:alpine
    
COPY --from=build /app/dist /usr/share/nginx/html
COPY --from=build /app/startup.sh /startup.sh
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

# Start nginx
CMD ["/bin/sh", "startup.sh"]
