require('dotenv').config();

const config = {
    LogLevel: process.env.LOG_LEVEL || 'info', 
    Port: process.env.PORT || '3002', 
    KafkaUrl: process.env.KAFKA_URL,
    KafkaTopic: process.env.KAFKA_TOPIC,
    CaseEngineApiBaseUrl: process.env.CASE_ENGINE_API_BASE_URL,

    JwtTokenUrl: process.env.JWT_TOKEN_URL,
    JwtTokenClientId: process.env.JWT_TOKEN_CLIENT_ID,
    JwtTokenClientSecret: process.env.JWT_TOKEN_CLIENT_SECRET,
    JwtTokenGrantType: process.env.JWT_TOKEN_GRANT_TYPE,
};

module.exports = config;
