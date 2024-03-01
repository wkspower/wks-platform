const expressServer = require('./server');
const kafkaConsumers = require('./kafka-consumer');

async function main() {
    await kafkaConsumers.startConsumers();
    expressServer.startServer();
}

main().catch(console.error);