FROM node:19.8

ARG REACT_APP_KEYCLOAK_URL
ARG REACT_APP_API_URL
ARG REACT_APP_EMAIL_URL

ENV NODE_ENV=development
ENV HOST=0.0.0.0 
ENV BROWSER=none 
ENV PORT=3001

EXPOSE 3001

ADD case-portal-react/src src
ADD case-portal-react/public public
ADD case-portal-react/package.json package.json
ADD case-portal-react/yarn.lock yarn.lock
ADD case-portal-react/jsconfig.json jsconfig.json
ADD case-portal-react/.eslintrc .eslintrc
ADD case-portal-react/.prettierrc .prettierrc
ADD case-portal-react/.eslintrc .eslintrc

RUN yarn install
RUN REACT_APP_KEYCLOAK_URL=${REACT_APP_KEYCLOAK_URL} \ 
    REACT_APP_API_URL=${REACT_APP_API_URL} \
    REACT_APP_EMAIL_URL=${REACT_APP_EMAIL_URL} \
    yarn start