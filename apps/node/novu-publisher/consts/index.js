require('dotenv').config();

const config = {
    LogLevel: process.env.LOG_LEVEL || 'info', 
    KafkaUrl: process.env.KAFKA_URL,
    NovuTriggerUrl: process.env.NOVU_TRIGGER_URL,
    NovuApiKey: process.env.NOVU_API_KEY,
    NovuCaseCreateWorkflow: process.env.NOVU_CASE_CREATE_WORKFLOW,
    NovuHumanTaskCreateWorkflow: process.env.NOVU_HUMAN_TASK_CREATE_WORKFLOW,
};

module.exports = config;
