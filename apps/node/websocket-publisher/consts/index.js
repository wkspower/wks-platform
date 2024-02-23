require('dotenv').config();

const config = {
    WebsockerPort: process.env.REACT_APP_WEBSOCKET_PORT,
    KafkaUrl: process.env.REACT_APP_KAFKA_URL,
};

module.exports = config;
