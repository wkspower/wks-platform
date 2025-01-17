require('dotenv').config();

const config = {
    LogLevel: process.env.LOG_LEVEL || 'info', 
    Port: process.env.PORT || '3002', 
    KafkaUrl: process.env.KAFKA_URL,
    NovuTriggerUrl: process.env.NOVU_TRIGGER_URL,
    NovuAppId: process.env.NOVU_APP_ID,
    NovuApiKey: process.env.NOVU_API_KEY,
    NovuCaseCreateWorkflow: process.env.NOVU_CASE_CREATE_WORKFLOW,
    NovuCaseEmailOutboundflow: process.env.NOVU_TOPIC_CASE_EMAIL_OUTBOUND,
    NovuHumanTaskCreateWorkflow: process.env.NOVU_HUMAN_TASK_CREATE_WORKFLOW,
};

module.exports = config;
