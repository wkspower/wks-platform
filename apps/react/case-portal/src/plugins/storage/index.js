import { Formio } from 'formiojs';
import Config from '../../consts';
import MemoryTokenManager from '../MemoryTokenManager';

export class StorageService {
    async uploadFile(storage, file, fileName, dir, evt, url, options) {
        return minio().uploadFile(file, fileName, dir, evt, null, null, null, null, () => null);
    }

    async deleteFile(fileInfo) {
        //do something
    }

    async downloadFile(fileInfo, options) {
        return minio().downloadFile(fileInfo);
    }
}

export function minio() {
    function createHeaders() {
        return {
            headers: {
                Authorization: `Bearer ${MemoryTokenManager.getToken()}`
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
                .then(async (data) => {
                    const resp = await fetch(data.url);
                    const blob = await resp.blob();
                    const downloadUrl = window.URL.createObjectURL(blob);

                    const anchor = document.createElement('a');
                    document.body.appendChild(anchor);
                    anchor.href = downloadUrl;

                    const url = new URL(data.url);
                    if (url.pathname) {
                        anchor.download = url.pathname
                            .slice(url.pathname.lastIndexOf('/') + 1)
                            .replaceAll("'");
                    } else {
                        anchor.download = downloadUrl;
                    }

                    anchor.click();

                    setTimeout(() => {
                        window.URL.revokeObjectURL(downloadUrl);
                        document.body.removeChild(anchor);
                    }, 0);
                    return;
                });
        }
    };
}

minio.title = 's3';
