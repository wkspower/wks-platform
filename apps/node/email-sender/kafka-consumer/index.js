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
    console.log("HTTP Request:");
    console.log(`Method: ${config.method.toUpperCase()}`);
    console.log(`URL: ${config.url}`);
    console.log(`Headers: ${JSON.stringify(config.headers)}`);
    console.log(`Body: ${JSON.stringify(config.data)}`);

    return getToken()
      .then((token) => {
        config.headers.Authorization = `Bearer ${token}`;
        return config;
      })
      .catch((error) => {
        console.log(error, "Error while getting token");
        return Promise.reject(error);
      });
  },
  function (error) {
    return Promise.reject(error);
  }
);
async function createConsumer(topic) {
   
  console.log(`Initiating Kafka consumer for topic: ${topic}`);
  const consumer = kafka.consumer({ groupId: `email-sender-${topic}` });

  await consumer.connect();
  await consumer.subscribe({ topic, fromBeginning: true });
  console.log(`Kafka consumer connected and subscribed: ${topic}`);

  await consumer.run({
    eachMessage: async ({ message }) => {
      const kafkaMessage = message.value.toString();
      console.log(`Kafka message received: ${kafkaMessage}`);
      try {
        console.log(`Sending email...`);
        sendHttpRequest(kafkaMessage);
      } catch (error) {
        console.log(error, `Error sending HTTP request`);
      }
    },
  });
}

async function sendHttpRequest(caseEmail) {
  console.log(`Preparing Email Acknowledgement patch request`);
  

  const json = JSON.parse(caseEmail);
  try {
    console.log(`Sending Email Acknowledgement patch request`);
    await axiosInstance.patch(
      //`${config.CaseEngineApiBaseUrl}/case-email/${json.caseEmailId}/sent`,
      `${config.CaseEngineApiBaseUrl}/case-email/rakeshittam27@gmail.com/sent`,
      caseEmail,
      {
        headers: {
          "Content-Type": "application/merge-patch+json",
        },
      }
    );
  } catch (error) {
    console.log(error, "Error sending request");
    throw error;
  }
}

async function getToken() {
  try {
    const response = await axios.post(
      `${config.JwtTokenUrl}`,
      {
        client_id: config.JwtTokenClientId,
        client_secret: config.JwtTokenClientSecret,
        grant_type: config.JwtTokenGrantType,
      },
      {
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
      }
    );
    return response.data.access_token;
  } catch (error) {
    console.log(error, "Error while getting token");
    throw error;
  }
}

async function startConsumers() {
  await createConsumer(config.KafkaTopic);
}

module.exports = {
  startConsumers,
};
