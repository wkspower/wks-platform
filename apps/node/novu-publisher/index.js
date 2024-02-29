const { Kafka } = require("kafkajs");
const axios = require("axios");
const config = require("./consts");
const pino = require("pino");

const logger = pino({level: config.LogLevel});

logger.info(`Initing novu-publisher`);
const kafka = new Kafka({
  clientId: "novu-publisher-app",
  brokers: [config.KafkaUrl],
});

const createConsumer = async (topic) => {
  logger.debug(`Initing kafka consumer for topic: ${topic + ''}`);
  const consumer = kafka.consumer({ groupId: `novu-publisher-${topic}` });

  await consumer.connect();
  await consumer.subscribe({ topic, fromBeginning: true });
  logger.debug(`Kafka consumer connected and subscribed: ${topic + ''}`);

  await consumer.run({
    eachMessage: async ({ message }) => {
      const kafkaMessage = message.value.toString();
      logger.debug(`Kafka message received: ${kafkaMessage}`);

      // Make HTTP request for each Kafka message
      try {
        await sendHttpRequest(kafkaMessage, topic);
      } catch (error) {
        logger.error(`Error sending HTTP request: ${error}`);
      }
    },
  });
};

const axiosInstance = axios.create();

// Add a request interceptor to log the request before sending
axiosInstance.interceptors.request.use(function (config) {
  logger.debug('HTTP Request:');
  logger.debug(`Method: ${config.method.toUpperCase()}`);
  logger.debug(`URL: ${config.url}`);
  logger.debug(`Headers: ${config.headers}`);
  logger.debug(`Body: ${JSON.stringify(config.data)}`);
  return config;
}, function (error) {
  return Promise.reject(error);
});

// Function to send HTTP request
const sendHttpRequest = async (kafkaMessage, novuWorkflow) => {
  logger.debug(`Preparing novu request to workflow: ${novuWorkflow + ''}`);

  const json = JSON.parse(kafkaMessage);
  try {
    logger.debug(`Sending novu request to workflow: ${novuWorkflow + ''}`);
    await axiosInstance.post(
      config.NovuTriggerUrl,
      {
        name: novuWorkflow,
        to: {
          subscriberId: json.owner?.id,
          email: json.owner?.email,
          phone: json.owner?.phone,
        },
        payload: json
      },
      {
        headers: {
          Authorization: `ApiKey ${config.NovuApiKey}`,
          "Content-Type": "application/json",
        },
      }
    );
  } catch (error) {
    logger.debug(`Error sending novu request: ${error + ''}`);
    throw error;
  }
};

// Create consumers for different topics
createConsumer(config.NovuCaseCreateWorkflow);
createConsumer(config.NovuHumanTaskCreateWorkflow);
