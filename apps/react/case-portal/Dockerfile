# Stage 1: Build the application
FROM node:19.8 AS build

# Set build arguments and environment variables

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
ENV NODE_ENV=development

# Set working directory
WORKDIR /app

# Copy only necessary files for dependencies installation
COPY package.json yarn.lock ./
ADD deployments/startup.sh /app/startup.sh
RUN yarn install --frozen-lockfile

# Copy the entire application
COPY . .


# Build the application
RUN yarn build

# Stage 2: Create the production image
FROM nginx:alpine

# Copy built files from the previous stage
COPY --from=build /app/dist /usr/share/nginx/html

COPY --from=build /app/startup.sh /startup.sh
# Copy offline css, js, and libs
COPY ./public/css /usr/share/nginx/html/css
COPY ./public/js /usr/share/nginx/html/js
COPY ./public/libs /usr/share/nginx/html/libs
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port
EXPOSE 80

# Start nginx
CMD ["/bin/sh", "startup.sh"]
