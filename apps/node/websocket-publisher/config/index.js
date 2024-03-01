require('dotenv').config();

const config = {
    LogLevel: process.env.LOG_LEVEL || 'info', 
    WebsockerPort: process.env.WEBSOCKET_PORT,
    KafkaUrl: process.env.KAFKA_URL,
};

module.exports = config;
