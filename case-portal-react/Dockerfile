FROM node:19.8 AS build

ARG REACT_APP_KEYCLOAK_URL
ARG REACT_APP_API_URL
ARG REACT_APP_EMAIL_URL

ENV NODE_ENV=development

ADD src src
ADD public public
ADD package.json package.json
ADD yarn.lock yarn.lock
ADD jsconfig.json jsconfig.json
ADD .eslintrc .eslintrc
ADD .prettierrc .prettierrc
ADD .eslintrc .eslintrc

RUN yarn install
COPY . .
ENV REACT_APP_KEYCLOAK_URL=${REACT_APP_KEYCLOAK_URL} 
ENV REACT_APP_API_URL=${REACT_APP_API_URL}
ENV REACT_APP_EMAIL_URL=${REACT_APP_EMAIL_URL}

EXPOSE 3000
CMD ["yarn", "start"]