FROM node:19.8 as build

ARG REACT_APP_KEYCLOAK_URL
ARG REACT_APP_API_URL
ARG REACT_APP_EMAIL_URL

WORKDIR /app

ADD case-portal-react/package.json package.json
ADD case-portal-react/yarn.lock yarn.lock
ADD case-portal-react/jsconfig.json jsconfig.json
ADD case-portal-react/.eslintrc .eslintrc
ADD case-portal-react/.prettierrc .prettierrc
ADD case-portal-react/.eslintrc .eslintrc
ADD case-portal-react/src src
ADD case-portal-react/public public
ADD case-portal-react/deployments/nginx.conf nginx.conf
RUN yarn install
RUN REACT_APP_KEYCLOAK_URL=${REACT_APP_KEYCLOAK_URL} \ 
    REACT_APP_API_URL=${REACT_APP_API_URL} \
    REACT_APP_EMAIL_URL=${REACT_APP_EMAIL_URL} \
    yarn build

FROM nginx:1.15.8-alpine
WORKDIR /usr/share/nginx/html
RUN rm -rf ./*
COPY --from=build /app/build .
COPY --from=build /app/nginx.conf /etc/nginx/nginx.conf
CMD ["nginx"]