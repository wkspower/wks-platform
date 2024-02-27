const { Kafka } = require("kafkajs");
const axios = require("axios");
const config = require("./consts");

const kafka = new Kafka({
  clientId: "novu-publisher-app",
  brokers: [config.KafkaUrl],
});

const createConsumer = async (topic) => {
  const consumer = kafka.consumer({ groupId: `novu-publisher-${topic}` });

  await consumer.connect();
  await consumer.subscribe({ topic, fromBeginning: true });

  await consumer.run({
    eachMessage: async ({ message }) => {
      const kafkaMessage = message.value.toString();

      // Make HTTP request for each Kafka message
      try {
        await sendHttpRequest(kafkaMessage, topic);
      } catch (error) {
        console.error("Error sending HTTP request:", error);
      }
    },
  });
};

const axiosInstance = axios.create();

// Add a request interceptor to log the request before sending
axiosInstance.interceptors.request.use(function (config) {
  console.log('HTTP Request:');
  console.log('Method:', config.method.toUpperCase());
  console.log('URL:', config.url);
  console.log('Headers:', config.headers);
  console.log('Body:', config.data);
  return config;
}, function (error) {
  return Promise.reject(error);
});

// Function to send HTTP request
const sendHttpRequest = async (kafkaMessage, novuWorkflow) => {
  const json = JSON.parse(kafkaMessage);
  try {
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
    throw error;
  }
};

// Create consumers for different topics
createConsumer(config.NovuCaseCreateWorkflow);
createConsumer(config.NovuHumanTaskCreateWorkflow);
