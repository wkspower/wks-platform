require('dotenv').config();

const config = {
    KafkaUrl: process.env.REACT_APP_KAFKA_URL,
    NovuTriggerUrl: process.env.REACT_APP_NOVU_TRIGGER_URL,
    NovuApiKey: process.env.REACT_APP_NOVU_API_KEY,
    NovuCaseCreateWorkflow: process.env.REACT_APP_NOVU_CASE_CREATE_WORKFLOW,
    NovuHumanTaskCreateWorkflow: process.env.REACT_APP_NOVU_HUMAN_TASK_CREATE_WORKFLOW,
};

module.exports = config;
