const WebSocket = require('ws');
const { Kafka } = require('kafkajs');
const config = require('./consts');

const wss = new WebSocket.Server({ port: config.WebsockerPort });
const kafka = new Kafka({ clientId: 'websocket-publisher-app', brokers: [config.KafkaUrl] });

const kafkaConsumers = new Map();

wss.on('connection', async function connection(ws, req) {

    const topic = req.url.substring(1); // Extract topic from the WebSocket URL. (ex: ws://host/topicName)
    console.log(`WebSocket connected for topic: ${topic}`);

    if (!topic) {
        console.error('Topic not specified in WebSocket URL');
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
    const consumer = kafka.consumer({ groupId: `websocker-publisher-${topic}` });
    await consumer.connect();
    await consumer.subscribe({ topic, fromBeginning: true });

    await consumer.run({
        eachMessage: async ({ message }) => {
            const kafkaMessage = message.value.toString();
            const clients = kafkaConsumers.get(topic);
            if (clients) {
                clients.forEach(client => {
                    if (client.readyState === WebSocket.OPEN) {
                        client.send(kafkaMessage);
                    }
                });
            }
        },
    });

    kafkaConsumers.set(topic, new Set());
};
