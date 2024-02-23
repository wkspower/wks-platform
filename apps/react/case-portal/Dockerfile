# Stage 1: Build the application
FROM node:19.8 AS build

# Set build arguments and environment variables
ARG REACT_APP_KEYCLOAK_URL
ARG REACT_APP_API_URL
ARG REACT_APP_EMAIL_URL
ARG GENERATE_SOURCEMAP
ARG REACT_APP_NOVU_ENABLED
ARG REACT_APP_NOVU_APP_ID
ARG REACT_APP_WEBSOCKETS_ENABLED
ARG REACT_APP_WEBSOCKETS_CASE_CREATED
ARG REACT_APP_WEBSOCKETS_HUMAN_TASK_CREATED
ENV NODE_ENV=development

# Set working directory
WORKDIR /app

# Copy only necessary files for dependencies installation
COPY package.json yarn.lock ./

# Install dependencies
RUN yarn install --frozen-lockfile

# Copy the entire application
COPY . .

# Set environment variables
ENV REACT_APP_KEYCLOAK_URL=$REACT_APP_KEYCLOAK_URL \
    REACT_APP_API_URL=$REACT_APP_API_URL \
    REACT_APP_EMAIL_URL=$REACT_APP_EMAIL_URL \
    GENERATE_SOURCEMAP=${GENERATE_SOURCEMAP} \
    REACT_APP_NOVU_ENABLED=${REACT_APP_NOVU_ENABLED} \
    REACT_APP_NOVU_APP_ID=${REACT_APP_NOVU_APP_ID} \
    REACT_APP_WEBSOCKETS_ENABLED=${REACT_APP_WEBSOCKETS_ENABLED} \
    REACT_APP_WEBSOCKETS_CASE_CREATED=${REACT_APP_WEBSOCKETS_CASE_CREATED} \
    REACT_APP_WEBSOCKETS_HUMAN_TASK_CREATED=${REACT_APP_WEBSOCKETS_HUMAN_TASK_CREATED}

# Build the application
RUN yarn build

# Stage 2: Create the production image
FROM nginx:alpine

# Copy built files from the previous stage
COPY --from=build /app/build /usr/share/nginx/html

# Expose port
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]