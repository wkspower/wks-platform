import Config from '../consts'

export const FileService = {
  upload,
  download,
}

function upload({ dir, file, progress, keycloak }) {
  function doUpload(url, fd, storage) {
    return new Promise((resolve, reject) => {
      let request = new XMLHttpRequest()

      request.open('POST', url)

      if (storage === 'filesystem') {
        request.setRequestHeader('Authorization', `Bearer ${keycloak.token}`)
      }

      request.addEventListener('openAndSetHeaders', function (...params) {
        request.open(...params)
        request.setRequestHeader('Authorization', `Bearer ${keycloak.token}`)
      })

      request.upload.addEventListener('progress', function (e) {
        if (typeof progress === 'function') {
          progress(e, (e.loaded / e.total) * 100)
        }
      })

      request.addEventListener('load', function () {
        if (request.status >= 200 && request.status < 300) {
          resolve({
            storage: storage || 'minio',
            dir: dir,
            name: file.name,
            url: file.name,
            size: file.size,
            type: file.type,
          })
        } else {
          reject(request.response || 'Unable to upload file')
        }
      })

      request.addEventListener('error', function (e) {
        e.networkError = true
        reject(e)
      })

      request.addEventListener('abort', function (e) {
        e.networkError = true
        reject(e)
      })

      request.send(fd)
    })
  }

  if (Config.StorageMode === 'filesystem') {
    const bucket = dir || 'files'
    const fd = new FormData()
    fd.append('file', file)
    const url = `${Config.StorageUrl}/storage/filesystem/${encodeURIComponent(
      bucket,
    )}/uploads?object=${encodeURIComponent(file.name)}`
    return doUpload(url, fd, 'filesystem')
  }

  let goUploadToFileUrl = `${Config.StorageUrl}/storage/files/${dir}/uploads/${file.name}?content-type=${file.type}`
  if (!dir) {
    goUploadToFileUrl = `${Config.StorageUrl}/storage/files/uploads/${file.name}?content-type=${file.type}`
  }

  return fetch(goUploadToFileUrl, createHeaders(keycloak))
    .then((resp) => resp.json())
    .then((data) => {
      const form = new FormData()

      for (const key in data.formData) {
        form.append(key, data.formData[key])
      }

      if (!dir) {
        form.append('key', file.name)
      } else {
        form.append('key', dir + '/' + file.name)
      }

      form.append('content-type', file.type)
      form.append('file', file)

      return doUpload(data.url, form)
    })
}

function download(file, keycloak) {
  if (Config.StorageMode === 'filesystem') {
    const bucket = file.dir || 'files'
    const url = `${Config.StorageUrl}/storage/filesystem/${encodeURIComponent(
      bucket,
    )}/downloads?object=${encodeURIComponent(file.name)}`

    return fetch(url, createHeaders(keycloak)).then(async (resp) => {
      const blob = await resp.blob()
      const downloadUrl = window.URL.createObjectURL(blob)

      const anchor = document.createElement('a')
      document.body.appendChild(anchor)
      anchor.href = downloadUrl
      anchor.download = file.name

      anchor.click()

      setTimeout(() => {
        window.URL.revokeObjectURL(downloadUrl)
        document.body.removeChild(anchor)
      }, 0)
      return
    })
  }

  let getObjectForUrl = `${Config.StorageUrl}/storage/files/${file.dir}/downloads/${file.name}?content-type=${file.type}`
  if (!file.dir) {
    getObjectForUrl = `${Config.StorageUrl}/storage/files/downloads/${file.name}?content-type=${file.type}`
  }

  return fetch(getObjectForUrl, createHeaders(keycloak))
    .then((resp) => resp.json())
    .then(async (data) => {
      const resp = await fetch(data.url)
      const blob = await resp.blob()
      const downloadUrl = window.URL.createObjectURL(blob)

      const anchor = document.createElement('a')
      document.body.appendChild(anchor)
      anchor.href = downloadUrl

      const url = new URL(data.url)
      if (url.pathname) {
        anchor.download = url.pathname
          .slice(url.pathname.lastIndexOf('/') + 1)
          .replaceAll("'")
      } else {
        anchor.download = downloadUrl
      }

      anchor.click()

      setTimeout(() => {
        window.URL.revokeObjectURL(downloadUrl)
        document.body.removeChild(anchor)
      }, 0)
      return
    })
}

function createHeaders(keycloak) {
  return {
    headers: {
      Authorization: `Bearer ${keycloak.token}`,
    },
  }
}
