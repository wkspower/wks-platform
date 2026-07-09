import CheckCircleOutlined from '@ant-design/icons/CheckCircleOutlined'
import FileExcelOutlined from '@ant-design/icons/FileExcelOutlined'
import FileImageOutlined from '@ant-design/icons/FileImageOutlined'
import FileOutlined from '@ant-design/icons/FileOutlined'
import FilePdfOutlined from '@ant-design/icons/FilePdfOutlined'
import { Grid } from '@mui/material'
import MuiAlert from '@mui/material/Alert'
import Avatar from '@mui/material/Avatar'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Chip from '@mui/material/Chip'
import CircularProgress from '@mui/material/CircularProgress'
import Fade from '@mui/material/Fade'
import List from '@mui/material/List'
import ListItem from '@mui/material/ListItem'
import ListItemAvatar from '@mui/material/ListItemAvatar'
import ListItemSecondaryAction from '@mui/material/ListItemSecondaryAction'
import ListItemText from '@mui/material/ListItemText'
import Snackbar from '@mui/material/Snackbar'
import Typography from '@mui/material/Typography'
import { useSession } from 'SessionStoreContext'
import React, { useState } from 'react'
import Files from 'react-files'
import { CaseService, FileService } from '../../services'
import { accountStore } from '../../store'
import DocumentStatusChip from './DocumentStatusChip'
import CaseStore from './store'

function Documents({ aCase, initialValue, requiredDocuments = [] }) {
  const keycloak = useSession()
  const [fetching, setFetching] = useState(false)
  const [percent, setPercent] = useState(0)
  const [messageError, setMessageError] = useState(null)
  const [filesUploaded, setFilesUploaded] = useState(initialValue)

  const isManager = accountStore.isManagerUser(keycloak)

  const handleUpdateStatus = (documentId, status) => {
    CaseService.updateDocumentStatus(
      keycloak,
      aCase.businessKey,
      documentId,
      status,
    )
      .then(() => {
        setFilesUploaded((current) =>
          current.map((file) =>
            file.id === documentId ? { ...file, status } : file,
          ),
        )
      })
      .catch((e) => {
        console.log(e)
        setMessageError(e)
      })
  }

  const handleChange = (files, requirementId) => {
    setFetching(true)

    const uploadPromise = requirementId
      ? saveDocumentsForRequirement(files, requirementId)
      : CaseStore.saveDocumentsFromFiles(
          keycloak,
          files,
          aCase.businessKey,
          setPercent,
        )

    uploadPromise
      .then((data) => {
        setFilesUploaded([...filesUploaded, ...data])
      })
      .catch((e) => {
        console.log(e)
        setMessageError(e)
      })
      .finally(() => {
        const timer = setTimeout(() => {
          setPercent(0)
          setFetching(false)
          clearTimeout(timer)
        }, 800)
      })
  }

  // Mirrors CaseStore.saveDocumentsFromFiles but tags each uploaded
  // document with the requirementId it satisfies so the backend persists
  // the link (CaseService.addDocuments JSON.stringifies the whole object).
  const saveDocumentsForRequirement = (files, requirementId) => {
    return Promise.all(
      files.map((file) => {
        const args = {
          dir: 'cases',
          file: file,
          keycloak,
          progress: (e, percent) => {
            setPercent(percent)
          },
        }

        return FileService.upload(args)
          .then((uploaded) => {
            const document = { ...uploaded, requirementId }
            return CaseService.addDocuments(
              keycloak,
              aCase.businessKey,
              document,
            ).then((resp) => {
              if (!resp.ok) {
                return Promise.reject(resp)
              }
              return document
            })
          })
          .catch(() => {
            return Promise.reject(
              `Could't upload this file "${file.name}", try again with other file.`,
            )
          })
      }),
    )
  }

  const requirements = requiredDocuments || []
  const isRequirementSatisfied = (requirement) =>
    (filesUploaded || []).some((doc) => doc.requirementId === requirement.id)
  const mandatoryRequirements = requirements.filter(
    (requirement) => requirement.required !== false,
  )
  const satisfiedMandatoryCount = mandatoryRequirements.filter(
    isRequirementSatisfied,
  ).length

  const handleError = (error) => {
    console.log('error code ' + error.code + ': ' + error.message)
  }

  const handleCloseMesssage = () => {
    setMessageError(null)
  }

  const AnimatedCircularProgress = React.forwardRef(
    function AnimatedCircularProgress(props, ref) {
      return (
        <div ref={ref} {...props}>
          <Box sx={{ position: 'relative', display: 'inline-flex', top: 10 }}>
            <CircularProgress variant='determinate' {...props} size={60} />
            <Box
              sx={{
                top: 0,
                left: 0,
                bottom: 0,
                right: 0,
                position: 'absolute',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <Typography
                variant='caption'
                component='div'
                color='text.secondary'
              >{`${Math.round(props.value)}%`}</Typography>
            </Box>
          </Box>
        </div>
      )
    },
  )

  const Alert = React.forwardRef(function Alert(props, ref) {
    return <MuiAlert elevation={6} ref={ref} variant='filled' {...props} />
  })

  return (
    <Grid
      container
      spacing={2}
      sx={{ display: 'flex', flexDirection: 'column' }}
    >
      {requirements.length > 0 && (
        <Box sx={{ pb: 1 }}>
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              pb: 1,
            }}
          >
            <Typography variant='h5' color='textSecondary'>
              Required documents
            </Typography>
            <Typography variant='caption' color='textSecondary'>
              {`${satisfiedMandatoryCount} of ${mandatoryRequirements.length} required documents provided`}
            </Typography>
          </Box>

          <List sx={{ border: '1px dashed #d9d9d9' }}>
            {requirements.map((requirement) => {
              const satisfied = isRequirementSatisfied(requirement)
              const isOptional = requirement.required === false
              return (
                <ListItem key={requirement.id}>
                  <ListItemAvatar>
                    <Avatar
                      style={{
                        backgroundColor: satisfied ? 'green' : '#bfbfbf',
                      }}
                    >
                      {satisfied ? <CheckCircleOutlined /> : <FileOutlined />}
                    </Avatar>
                  </ListItemAvatar>
                  <ListItemText
                    primary={
                      <Box
                        sx={{
                          display: 'flex',
                          alignItems: 'center',
                          gap: 1,
                        }}
                      >
                        {requirement.label}
                        <Chip
                          size='small'
                          variant='outlined'
                          color={isOptional ? 'default' : 'primary'}
                          label={isOptional ? 'Optional' : 'Required'}
                        />
                      </Box>
                    }
                    secondary={requirement.description}
                    style={{ maxWidth: '70%' }}
                  />
                  {satisfied ? (
                    <Chip
                      size='small'
                      color='success'
                      label='Provided'
                      icon={<CheckCircleOutlined />}
                    />
                  ) : (
                    <Files
                      onChange={(files) => handleChange(files, requirement.id)}
                      onError={handleError}
                      accepts={requirement.acceptedFileTypes}
                      maxFileSize={requirement.maxSizeBytes}
                      clickable
                    >
                      <Button size='small' variant='outlined'>
                        Upload
                      </Button>
                    </Files>
                  )}
                </ListItem>
              )
            })}
          </List>
        </Box>
      )}

      <Box sx={{ padding: 5 }}>
        <Grid
          container
          direction='column'
          justifyContent='center'
          alignItems='center'
        >
          <Avatar
            style={{
              backgroundColor: '#27CDF2',
              fontSize: 40,
              height: 60,
              width: 60,
              opacity: 0.5,
            }}
          >
            <FilePdfOutlined />
          </Avatar>

          <br />

          <Files
            className='files-dropzone'
            onChange={handleChange}
            onError={handleError}
            multiple
            clickable
          >
            <Typography variant='h4' color='textSecondary' sx={{ pr: 0.5 }}>
              Drop files here or click to upload
            </Typography>
          </Files>

          <Fade in={fetching}>
            <AnimatedCircularProgress value={percent} />
          </Fade>
        </Grid>
      </Box>

      {filesUploaded && filesUploaded.length > 0 && (
        <List sx={{ border: '1px dashed #d9d9d9' }}>
          {filesUploaded.map((file, index) => {
            return (
              <ListItem
                key={index}
                onClick={() => downloadFile(file, keycloak)}
              >
                <ListItemAvatar>
                  {file.type === 'application/pdf' && (
                    <Avatar style={{ backgroundColor: 'red' }}>
                      <FilePdfOutlined />
                    </Avatar>
                  )}

                  {file.type === 'application/xls' && (
                    <Avatar style={{ backgroundColor: 'green' }}>
                      <FileExcelOutlined />
                    </Avatar>
                  )}

                  {file.type && file.type.includes('image/') && (
                    <Avatar style={{ backgroundColor: 'lightblue' }}>
                      <FileImageOutlined />
                    </Avatar>
                  )}

                  {file.type !== 'application/xls' &&
                    file.type !== 'application/pdf' &&
                    file.type &&
                    !file.type.includes('image/') && (
                      <Avatar style={{ backgroundColor: 'grey' }}>
                        <FileOutlined />
                      </Avatar>
                    )}
                </ListItemAvatar>
                <ListItemText
                  primary={file.name}
                  secondary={file.size + 'KB'}
                  style={{ maxWidth: '80%' }}
                />
                <ListItemSecondaryAction>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <DocumentStatusChip status={file.status || 'received'} />
                    {isManager && file.id && (
                      <>
                        <Button
                          size='small'
                          color='success'
                          variant='outlined'
                          onClick={(event) => {
                            event.stopPropagation()
                            handleUpdateStatus(file.id, 'verified')
                          }}
                        >
                          Verify
                        </Button>
                        <Button
                          size='small'
                          color='error'
                          variant='outlined'
                          onClick={(event) => {
                            event.stopPropagation()
                            handleUpdateStatus(file.id, 'rejected')
                          }}
                        >
                          Reject
                        </Button>
                      </>
                    )}
                  </Box>
                </ListItemSecondaryAction>
              </ListItem>
            )
          })}
        </List>
      )}

      <Snackbar
        open={!!messageError}
        autoHideDuration={6000}
        onClose={handleCloseMesssage}
      >
        <Alert
          onClose={handleCloseMesssage}
          severity='error'
          sx={{ width: '100%' }}
        >
          {messageError}
        </Alert>
      </Snackbar>
    </Grid>
  )
}

const downloadFile = (file, keycloak) => {
  return FileService.download(file, keycloak)
}

export default Documents
