import { CaseService, FileService } from '../../../services';

const CaseStore = {
    saveAttachmentsFromFiles
};

async function saveAttachmentsFromFiles(keycloak, files, aCase, progressCallback) {
    return Promise.all(
        files.map((file) => {
            const args = {
                dir: 'cases',
                file: file,
                keycloak,
                progress: (e, percent) => {
                    progressCallback(percent);
                }
            };

            return FileService.upload(args)
                .then((data) => saveAttachment(keycloak, aCase, data))
                .catch((e) => {
                    return Promise.reject(
                        `Could't upload this file "${file.name}", try again with other file.`
                    );
                });
        })
    );
}

async function saveAttachment(keycloak, aCase, attachment) {
    try {
        const data = await CaseService.addAttachment(keycloak, aCase, attachment);
        if (!data.ok) {
            return Promise.reject(data);
        }

        return Promise.resolve(attachment);
    } catch (e) {
        return Promise.reject(e);
    }
}

export default CaseStore;
