# Stage 1: Build the application
FROM node:19.8 AS build



# Set working directory
WORKDIR /app

# Copy only necessary files for dependencies installation
COPY package.json yarn.lock ./
ADD deployments/startup.sh /app/startup.sh
ADD deployments/nginx.conf /app/nginx.conf
RUN yarn install --frozen-lockfile

# Copy the entire application
COPY . .


# Build the application
RUN yarn build

# Stage 2: Create the production image
FROM nginx:1.15.8-alpine

# Copy built files from the previous stage
COPY --from=build /app/dist /usr/share/nginx/html

COPY --from=build /app/startup.sh /startup.sh
# Copy offline css, js, and libs
COPY ./public/css /usr/share/nginx/html/css
COPY ./public/js /usr/share/nginx/html/js
COPY ./public/libs /usr/share/nginx/html/libs
COPY ./public/fonts /usr/share/nginx/html/fonts
COPY --from=build /app/nginx.conf /etc/nginx/nginx.conf

# Expose port
EXPOSE 80

# Start nginx
CMD ["/bin/sh", "startup.sh"]
