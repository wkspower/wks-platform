# ==== CONFIGURE =====
# Use a Node 16 base image
FROM node:slim

ARG REACT_APP_KEYCLOAK_URL
ARG REACT_APP_API_URL

# Set the working directory to /app inside the container
WORKDIR /app

# Copy app files
COPY package.json .

# ==== BUILD =====
# Install dependencies (npm ci makes sure the exact versions in the lockfile gets installed)
RUN npm install
COPY . .

# Build the app
RUN REACT_APP_KEYCLOAK_URL=${REACT_APP_KEYCLOAK_URL} \ 
    REACT_APP_API_URL=${REACT_APP_API_URL} \
    npm run build

# ==== RUN =======
# Set the env to "production"
ENV NODE_ENV development
# Expose the port on which the app will be running (3000 is the default that `serve` uses)
EXPOSE 3000
# Start the app
CMD [ "npx", "serve", "build" ]