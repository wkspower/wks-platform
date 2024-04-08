const kafkaConsumers = require('./kafka-consumer');

async function main() {
    await kafkaConsumers.startConsumers();
}

main().catch(console.error);