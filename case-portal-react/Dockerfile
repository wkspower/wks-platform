FROM node:19.8 AS build

# Needed since Miragon BPMN modeler makes dist buildle too big
ENV GENERATE_SOURCEMAP=false

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
RUN REACT_APP_KEYCLOAK_URL=${REACT_APP_KEYCLOAK_URL} \ 
    REACT_APP_API_URL=${REACT_APP_API_URL} \
    REACT_APP_EMAIL_URL=${REACT_APP_EMAIL_URL} \
    yarn build

FROM node:19.8
COPY --from=build /build /build
RUN yarn global add serve
EXPOSE 3001
CMD [ "serve", "-s", "build", "-p", "3001" ]