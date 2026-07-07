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

  // Inline mode: the minimal, no-storage-api deployment. The bytes travel with
  // the case document as base64 — nothing is uploaded to storage-api.
  if (Config.StorageMode === 'inline') {
    return readAsBase64(file).then((base64) => ({
      storage: 'inline',
      dir: dir,
      name: file.name,
      url: file.name,
      size: file.size,
      type: file.type,
      base64: base64,
    }))
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
  // Inline document: bytes are carried on the document itself (base64), so there
  // is no storage-api round-trip. Keyed off the persisted `storage` marker (with
  // a defensive fallback to a present base64 payload).
  if (file.storage === 'inline' || (file.base64 && !file.storage)) {
    const blob = base64ToBlob(file.base64, file.type)
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
    return Promise.resolve()
  }

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

// Read a File into a bare base64 string (without the `data:...;base64,` prefix)
// for inline storage mode.
function readAsBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const result = reader.result || ''
      const comma = result.indexOf(',')
      resolve(comma >= 0 ? result.slice(comma + 1) : result)
    }
    reader.onerror = () => reject(reader.error || 'Unable to read file')
    reader.readAsDataURL(file)
  })
}

// Decode a bare base64 string back into a Blob for inline-mode download.
function base64ToBlob(base64, type) {
  const binary = atob(base64 || '')
  const bytes = new Uint8Array(binary.length)
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i)
  }
  return new Blob([bytes], { type: type || 'application/octet-stream' })
}
