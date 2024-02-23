# Use an official Node.js runtime as the base image
FROM node:19.8

# Set the working directory in the container
WORKDIR /usr/src/app

# Copy package.json and package-lock.json to the container
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy the rest of the application code to the container
COPY . .

# Expose the port on which your application will run
# EXPOSE 8484

# Command to run the application
CMD ["node", "index.js"]
