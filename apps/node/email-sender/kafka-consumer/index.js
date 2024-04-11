const { Kafka } = require("kafkajs");
const config = require("../config");
const pino = require("pino");
const axios = require("axios");

const logger = pino({ level: config.LogLevel });

const kafka = new Kafka({
  clientId: "email-sender-app",
  brokers: [config.KafkaUrl],
});

const axiosInstance = axios.create();

axiosInstance.interceptors.request.use(
  function (config) {
    logger.debug("HTTP Request:");
    logger.debug(`Method: ${config.method.toUpperCase()}`);
    logger.debug(`URL: ${config.url}`);
    logger.debug(`Headers: ${JSON.stringify(config.headers)}`);
    logger.debug(`Body: ${JSON.stringify(config.data)}`);

    return getToken()
      .then((token) => {
        config.headers.Authorization = `Bearer ${token}`;
        return config;
      })
      .catch((error) => {
        logger.error(error, "Error while getting token");
        return Promise.reject(error);
      });
  },
  function (error) {
    return Promise.reject(error);
  }
);
async function createConsumer(topic) {
  logger.debug(`Initiating Kafka consumer for topic: ${topic}`);
  const consumer = kafka.consumer({ groupId: `email-sender-${topic}` });

  await consumer.connect();
  await consumer.subscribe({ topic, fromBeginning: true });
  logger.debug(`Kafka consumer connected and subscribed: ${topic}`);

  await consumer.run({
    eachMessage: async ({ message }) => {
      const kafkaMessage = message.value.toString();
      logger.debug(`Kafka message received: ${kafkaMessage}`);
      try {
        logger.info(`Sending email...`);
        sendHttpRequest(kafkaMessage);
      } catch (error) {
        logger.error(error, `Error sending HTTP request`);
      }
    },
  });
}

async function sendHttpRequest(caseEmail) {
  logger.debug(`Preparing Email Acknowledgement patch request`);

  const json = JSON.parse(caseEmail);
  try {
    logger.debug(`Sending Email Acknowledgement patch request`);
    await axiosInstance.patch(
      `${config.CaseEngineApiBaseUrl}/case-email/${json.caseEmailId}/sent`,
      { receivedDateTime: new Date() },
      {
        headers: {
          Authorization: `Bearer ${config}`,
          "Content-Type": "application/merge-patch+json",
        },
      }
    );
  } catch (error) {
    logger.debug(error, "Error sending request");
    throw error;
  }
}

async function getToken() {
  try {
    const response = await axios.post(
      `http://localhost:8082/realms/localhost/protocol/openid-connect/token`,
      {
        client_id: "wks-email-to-case",
        client_secret: "replaceme",
        grant_type: "client_credentials",
      },
      {
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
      }
    );
    return response.data.access_token;
  } catch (error) {
    logger.error(error, "Error while getting token");
    throw error;
  }
}

async function startConsumers() {
  await createConsumer(config.KafkaTopic);
}

module.exports = {
  startConsumers,
};
