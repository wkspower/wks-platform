import { CaseService } from '../../../services/CaseService';

const CaseStore = {
    getAttachmentsById
};

async function getAttachmentsById(id) {
    try {
        const data = await CaseService.getAttachmentsById(id);
        if (!data.length) {
            return Promise.resolve([]);
        }

        return Promise.resolve(data);
    } catch (e) {
        return Promise.reject(e);
    }
}

export default CaseStore;
