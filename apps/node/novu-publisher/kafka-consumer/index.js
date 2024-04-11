const { Kafka } = require("kafkajs");
const config = require("../config");
const pino = require("pino");
const axios = require("axios");

const logger = pino({ level: config.LogLevel });

const kafka = new Kafka({
  clientId: "novu-publisher-app",
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
    return config;
  },
  function (error) {
    return Promise.reject(error);
  }
);

async function createConsumer(topic) {
  logger.debug(`Initiating Kafka consumer for topic: ${topic}`);
  const consumer = kafka.consumer({ groupId: `novu-publisher-${topic}` });

  await consumer.connect();
  await consumer.subscribe({ topic, fromBeginning: true });
  logger.debug(`Kafka consumer connected and subscribed: ${topic}`);

  await consumer.run({
    eachMessage: async ({ message }) => {
      const kafkaMessage = message.value.toString();
      logger.debug(`Kafka message received: ${kafkaMessage}`);
      try {
        await sendHttpRequest(kafkaMessage, topic);
      } catch (error) {
        logger.error(error, `Error sending HTTP request`);
      }
    },
  });
}

async function sendHttpRequest(kafkaMessage, novuWorkflow) {
  logger.debug(`Preparing Novu request to workflow: ${novuWorkflow}`);

  const json = JSON.parse(kafkaMessage);
  try {
    logger.debug(`Sending Novu request to workflow: ${novuWorkflow}`);
    await axiosInstance.post(
      config.NovuTriggerUrl,
      {
        name: novuWorkflow,
        to: {
          subscriberId: json.owner?.email,
          email: json.owner?.email,
          phone: json.owner?.phone,
        },
        payload: json,
      },
      {
        headers: {
          Authorization: `ApiKey ${config.NovuApiKey}`,
          "Content-Type": "application/json",
        },
      }
    );
  } catch (error) {
    logger.debug(error, "Error sending Novu request");
    throw error;
  }
}

async function startConsumers() {
  await createConsumer(config.NovuCaseCreateWorkflow);
  await createConsumer(config.NovuHumanTaskCreateWorkflow);
}

module.exports = {
  startConsumers,
};
