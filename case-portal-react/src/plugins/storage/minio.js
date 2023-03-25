import { Formio } from 'formiojs';
import Config from '../../consts';

export function minio(formio) {
    function createHeaders() {
        return {
            headers: {
                Authorization: `Bearer ${Formio.getToken()}`
            }
        };
    }

    return {
        uploadFile(
            file,
            fileName,
            dir,
            progressCallback,
            url,
            options,
            fileKey,
            groupPermissions,
            groupId,
            abortCallback
        ) {
            function doUpload(url, fd) {
                return new Promise((resolve, reject) => {
                    let request = new XMLHttpRequest();

                    request.open('POST', url);

                    request.addEventListener('openAndSetHeaders', function (...params) {
                        request.open(...params);
                        request.setRequestHeader('Authorization', `Bearer ${Formio.getToken()}`);
                    });

                    request.upload.addEventListener('progress', function (e) {
                        if (typeof progressCallback === 'function') {
                            progressCallback(e);
                        }

                        if (typeof abortCallback === 'function') {
                            abortCallback(() => request.abort());
                        }
                    });

                    request.addEventListener('load', function (e) {
                        if (request.status >= 200 && request.status < 300) {
                            resolve({
                                storage: 'minio',
                                dir: dir,
                                name: file.name,
                                url: file.name,
                                size: file.size,
                                type: file.type
                            });
                        } else {
                            reject(request.response || 'Unable to upload file');
                        }
                    });

                    request.addEventListener('error', function (e) {
                        e.networkError = true;
                        reject(e);
                    });

                    request.addEventListener('abort', function (e) {
                        e.networkError = true;
                        reject(e);
                    });

                    request.send(fd);
                });
            }

            let goUploadToFileUrl = `${Config.StorageUrl}/storage/files/${dir}/uploads/${file.name}?content-type=${file.type}`;
            if (!dir) {
                goUploadToFileUrl = `${Config.StorageUrl}/storage/files/uploads/${file.name}?content-type=${file.type}`;
            }

            return fetch(goUploadToFileUrl, createHeaders())
                .then((resp) => resp.json())
                .then((data) => {
                    const form = new FormData();

                    for (const key in data.formData) {
                        form.append(key, data.formData[key]);
                    }

                    if (!dir) {
                        form.append('key', file.name);
                    } else {
                        form.append('key', dir + '/' + file.name);
                    }

                    form.append('content-type', file.type);
                    form.append('file', file);

                    return doUpload(data.url, form);
                });
        },
        downloadFile(file) {
            let getObjectForUrl = `${Config.StorageUrl}/storage/files/${file.dir}/downloads/${file.name}?content-type=${file.type}`;
            if (!file.dir) {
                getObjectForUrl = `${Config.StorageUrl}/storage/files/downloads/${file.name}?content-type=${file.type}`;
            }

            return fetch(getObjectForUrl, createHeaders())
                .then((resp) => resp.json())
                .then((data) => {
                    const anchor = document.createElement('a');
                    anchor.href = data.url;
                    anchor.download = data.url;
                    anchor.target = '_blank';
                    document.body.appendChild(anchor);
                    anchor.click();
                    return;
                });
        }
    };
}

minio.title = 'Minio';
