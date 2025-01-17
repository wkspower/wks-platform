const WebSocket = require('ws');
const { Kafka } = require('kafkajs');
const config = require('./config');
const pino = require("pino");

const logger = pino({level: config.LogLevel});

console.log('Initing websocket-publisher');
const wss = new WebSocket.Server({ port: config.WebsockerPort });
const kafka = new Kafka({ clientId: 'websocket-publisher-app', brokers: [config.KafkaUrl] });
const kafkaConsumers = new Map();

wss.on('connection', async function connection(ws, req) {

    console.log(`Connection request received to: ${req.url}`);

    const topic = req.url.substring(1); // Extract topic from the WebSocket URL. (ex: ws://host/topicName)
    if (!topic) {
        console.log('Topic not specified at connection request');
        ws.close();
        return;
    }

    if (!kafkaConsumers.has(topic)) {
        await createConsumer(topic);
    }

    kafkaConsumers.get(topic).add(ws);

    ws.on('close', function close() {
        console.log(`WebSocket connection closed for topic: ${topic}`);
        const clients = kafkaConsumers.get(topic);
        if (clients) {
            clients.delete(ws);
            if (clients.size === 0) {
                kafkaConsumers.delete(topic);
            }
        }
    });
});

const createConsumer = async (topic) => {
    console.log(`Initing kafka consumer for topic: ${topic}`);

    const consumer = kafka.consumer({ groupId: `websocker-publisher-${topic}` });
    await consumer.connect();
    await consumer.subscribe({ topic, fromBeginning: true });
    console.log(`Kafka consumer connected and subscribed: ${topic}`);

    await consumer.run({
        eachMessage: async ({ message }) => {
            const kafkaMessage = message.value.toString();
            console.log(`Kafka message received: ${kafkaMessage}`);
            const clients = kafkaConsumers.get(topic);
            if (clients) {
                clients.forEach(client => {
                    if (client.readyState === WebSocket.OPEN) {
                        console.log(`Message sent to websocket client: ${client}`);
                        client.send(kafkaMessage);
                    }
                });
            }
        },
    });

    kafkaConsumers.set(topic, new Set());
    console.log(`New kafka consumer created: ${topic}`);
};
