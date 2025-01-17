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
    console.log("HTTP Request:");
    console.log(`Method: ${config.method.toUpperCase()}`);
    console.log(`URL: ${config.url}`);
    console.log(`Headers: ${JSON.stringify(config.headers)}`);
    console.log(`Body: ${JSON.stringify(config.data)}`);
    return config;
  },
  function (error) {
    return Promise.reject(error);
  }
);

async function createConsumer(topic) {
  console.log(`Initiating Kafka consumer for topic: ${topic}`);
  const consumer = kafka.consumer({ groupId: `novu-publisher-${topic}` });

  await consumer.connect();
  await consumer.subscribe({ topic, fromBeginning: true });
  console.log(`Kafka consumer connected and subscribed: ${topic}`);

  await consumer.run({
    eachMessage: async ({ message }) => {
      const kafkaMessage = message.value.toString();
      console.log(`Kafka message received: ${kafkaMessage}`);
      try {
        await sendHttpRequest(kafkaMessage, topic);
      } catch (error) {
        console.log(error, `Error sending HTTP request`);
      }
    },
  });
}

async function sendHttpRequest(kafkaMessage, novuWorkflow) {
  console.log(`Preparing Novu request to workflow: ${novuWorkflow}`);

  const json = JSON.parse(kafkaMessage);
  try {
    console.log(`Sending Novu request to workflow: ${novuWorkflow}`);
    console.log(`Sending Novu request to workflow json: ${json}`);
    await axiosInstance.post(
      config.NovuTriggerUrl,
      {
        name: "welcome-onboarding-email",
        //name:"demo-comment-on-task",
        to: {
           subscriberId: '677bc9f40a01aefb9af20f3f',
           email: 'rakeshittam27@gmail.com'
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
    console.log(error, "Error sending Novu request");
    throw error;
  }
}

async function startConsumers() {
  console.log("start Consumers Novu NovuCaseCreateWorkflow "+ config.NovuCaseCreateWorkflow);
  console.log("start Consumers Novu NovuHumanTaskCreateWorkflow"+ config.NovuHumanTaskCreateWorkflow);
  console.log("start Consumers Novu NovuCaseEmailOutboundflow"+ config.NovuCaseEmailOutboundflow);
  await createConsumer(config.NovuCaseCreateWorkflow);
  await createConsumer(config.NovuCaseEmailOutboundflow);

  await createConsumer(config.NovuHumanTaskCreateWorkflow);
}

module.exports = {
  startConsumers,
};
