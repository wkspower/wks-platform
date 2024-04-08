require('dotenv').config();

const config = {
    LogLevel: process.env.LOG_LEVEL || 'info', 
    Port: process.env.PORT || '3002', 
    KafkaUrl: process.env.KAFKA_URL,
    KafkaTopic: process.env.KAFKA_TOPIC,
    CaseEngineApiBaseUrl: process.env.CASE_ENGINE_API_BASE_URL,
};

module.exports = config;
