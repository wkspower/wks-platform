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
      console.log(`new message received: ${kafkaMessage}`);

      // Make HTTP request for each Kafka message
      try {
        await sendHttpRequest(kafkaMessage, topic);
        console.log("HTTP request sent successfully");
      } catch (error) {
        console.error("Error sending HTTP request:", error);
      }
    },
  });
};

// Function to send HTTP request
const sendHttpRequest = async (kafkaMessage, novuWorkflow) => {
  const json = JSON.parse(kafkaMessage);
  try {
    await axios.post(
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
