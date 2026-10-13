import React, { useEffect, useState, useCallback, useMemo } from 'react'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { useGridApiRef } from '@mui/x-data-grid'
import moment from 'moment'
import { DocumentUploadApiService } from 'components/aop-phase-two/services/crude/documentUplaodApiService'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'
import { getRoleName } from 'services/role-service'
import {
    Box,
    Snackbar,
    Alert,
    IconButton,
    Tooltip,
    TextField,
    Typography,
    Button,
} from '@mui/material'
import { styled } from '@mui/material/styles'
import SendIcon from '@mui/icons-material/Send'
import 'kendo-data-grid.css'
import AddIcon from '@mui/icons-material/Add'
import UploadIcon from '@mui/icons-material/Upload'
import CloudDownloadIcon from '@mui/icons-material/CloudDownload'
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import KendoDataTables from 'components/kendo-data-tables'
import { SaveIcon } from 'assets/images/icons'
import UploadDocumentDialog from 'components/kendo-data-tables/components/UploadDocumentDialog'
import DeleteConfirmationDialog from 'components/kendo-data-tables/components/DeleteConfirmationDialog'
import { TextArea } from '@progress/kendo-react-inputs'
import { formatToIST } from 'components/aop-phase-two/common/commonUtilityFunctions'

// ─── Helpers ──────────────────────────────────────────────────────────────────

const formatDateTime = (dateVal) => {
    if (!dateVal) return ''
    const m = moment(dateVal)
    if (m.isValid()) {
        return m.format('DD-MM-YYYY HH:mm a')
    }
    return dateVal
}

const formatBytes = (bytes) => {
    if (typeof bytes === 'string') return bytes
    if (!bytes || bytes === 0) return '0 Bytes'
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`
}

// ─── Main Component ───────────────────────────────────────────────────────────

const OtherDocumentUpload = ({ permissions }) => {
    // ── Core state ───────────────────────────────────────────────────────────
    const [loading, setLoading] = useState(false)
    const [rows, setRows] = useState([])
    const [modifiedCells, setModifiedCells] = useState({})

    // ── Dialog state ─────────────────────────────────────────────────────────
    const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
    const [currentRemark, setCurrentRemark] = useState('')
    const [currentRowId, setCurrentRowId] = useState(null)
    const [open1, setOpen1] = useState(false)

    // ── Upload dialog ────────────────────────────────────────────────────────
    const [openUploadDialog, setOpenUploadDialog] = useState(false)
    const [uploadDialogMode, setUploadDialogMode] = useState('add')
    const [selectedRowForUpdate, setSelectedRowForUpdate] = useState(null)
    const [uploadProgress, setUploadProgress] = useState(0)
    const [isUploading, setIsUploading] = useState(false)

    // ── Delete dialog ────────────────────────────────────────────────────────
    const [openDeleteConfirm, setOpenDeleteConfirm] = useState(false)
    const [selectedRowForDelete, setSelectedRowForDelete] = useState(null)
    const [isDeleting, setIsDeleting] = useState(false)

    // ── Notes textarea state ───────────────────────────────────────────────────
    const [notes, setNotes] = useState('')
    const [isSubmittingNotes, setIsSubmittingNotes] = useState(false)

    // ── Toast ────────────────────────────────────────────────────────────────
    const [snackbarOpen, setSnackbarOpen] = useState(false)
    const [snackbarData, setSnackbarData] = useState({
        message: '',
        severity: 'info',
    })

    const [summary, setSummary] = useState('')
    const [infoRecord, setInfoRecord] = useState(null)

    // ── Store / session ──────────────────────────────────────────────────────
    const apiRef = useGridApiRef()
    const dataGridStore = useSelector((state) => state.dataGridStore)
    const {
        plantObject,
        year,
        oldYear,
        yearChanged,
        verticalObject,
        siteObject,
    } = dataGridStore

    const PLANT_ID = plantObject?.id
    const AOP_YEAR = year?.selectedYear
    const VERTICAL_ID = verticalObject?.id
    const IS_OLD_YEAR = oldYear?.oldYear
    const keycloak = useSession()

    const valueFormat = ValueFormatterProduction()
    const { isReleased } = dataGridStore
    const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR, isReleased)

    const PLANT_NAME_NO_CASE = plantObject?.name?.toUpperCase()
    const SITE_NAME_NO_CASE = siteObject?.name?.toUpperCase()
    const VERTICAL_NAME_NO_CASE = verticalObject?.name?.toUpperCase()
    const EXCEL_EXPORT_TITLE = `${VERTICAL_NAME_NO_CASE}_${SITE_NAME_NO_CASE}_${PLANT_NAME_NO_CASE}_${AOP_YEAR}_Season_Month`

    // ─── Toast ────────────────────────────────────────────────────────────────

    const showMessage = useCallback((message, severity = 'info') => {
        setSnackbarData({ message, severity })
        setSnackbarOpen(true)
    }, [])
    // ─── Fetch data ───────────────────────────────────────────────────────────

    const fetchDocuments = useCallback(async () => {
        if (!VERTICAL_ID || !AOP_YEAR) return
        setModifiedCells({})
        try {
            setLoading(true)
            const response = await DocumentUploadApiService.getOtherDocuments(
                keycloak,
                VERTICAL_ID,
                AOP_YEAR,
            )
            if (response?.code === 200 && Array.isArray(response?.data)) {
                const cleaned = response.data.map((row) => {
                    const rawFileName =
                        row.fileName ||
                        row.file_name ||
                        row.originalFileName ||
                        ''
                    const rawSize =
                        row.fileSize ??
                        row.size ??
                        row.file_size ??
                        row.fileLength ??
                        row.bytes ??
                        row.contentLength

                    return {
                        ...row,
                        fileName: rawFileName,
                        fileSize:
                            typeof rawSize === 'number'
                                ? formatBytes(rawSize)
                                : rawSize || '',
                        uploadedDateTime: formatToIST(
                            row.uploadedDateTime || row.uploadedAt || row.UploadedDateTime,
                        ),
                        size: typeof row.size === 'number' ? formatBytes(row.size) : row.size,
                    }
                })
                setRows(cleaned)
            } else if (response?.message) {
                showMessage(response.message, 'error')
            }
        } catch (err) {
            console.error('Error loading documents:', err)
            showMessage('Failed to load document list.', 'error')
        } finally {
            setLoading(false)
        }
    }, [VERTICAL_ID, AOP_YEAR, keycloak, showMessage])

    const fetchAdditionalInfo = useCallback(async () => {
        if (!VERTICAL_ID || !AOP_YEAR) return
        try {
            const infoResp = await DocumentUploadApiService.getOtherDocumentInformation(
                keycloak,
                VERTICAL_ID,
                AOP_YEAR,
            ).catch((err) => {
                console.warn('Could not fetch additional document information:', err)
                return null
            })

            if (infoResp?.code === 200 && Array.isArray(infoResp?.data) && infoResp.data.length > 0) {
                const rec = infoResp.data[0]
                setInfoRecord(rec)
                setSummary(rec?.otherInformation || '')
            } else {
                setInfoRecord(null)
            }
        } catch (err) {
            console.error('Error loading additional information:', err)
        }
    }, [VERTICAL_ID, AOP_YEAR, keycloak])

    // ─── Upload (Add) ─────────────────────────────────────────────────────────

    const uploadFiles = useCallback(
        async (filesList, rowToUpdate) => {
            if (READ_ONLY) {
                showMessage('You do not have permission to upload files.', 'error')
                return
            }
            if (!filesList?.length) return

            setIsUploading(true)
            setUploadProgress(0)
            setLoading(true)

            try {
                const targetRow = rowToUpdate || selectedRowForUpdate
                const targetMasterId = targetRow?.masterId || targetRow?.MasterId
                const targetTransactionId =
                    targetRow?.transactionId || targetRow?.TransactionId

                if (!targetMasterId) {
                    throw new Error('Please select a valid document row to upload.')
                }

                for (let i = 0; i < filesList.length; i++) {
                    const file = filesList[i]
                    const response =
                        await DocumentUploadApiService.uploadOrUpdateDocument(keycloak, {
                            masterId: targetMasterId,
                            verticalId: VERTICAL_ID,
                            aopYear: AOP_YEAR,
                            file,
                        })

                    if (response?.code !== 200 && response?.status !== 'SUCCESS') {
                        throw new Error(response?.message || 'Upload failed.')
                    }
                }
                showMessage('Document uploaded successfully!', 'success')
                setOpenUploadDialog(false)
                setSelectedRowForUpdate(null)
                await fetchDocuments()
            } catch (err) {
                console.error('Upload error:', err)
                showMessage(err?.message || 'Upload failed. Please try again.', 'error')
            } finally {
                setIsUploading(false)
                setUploadProgress(0)
                setLoading(false)
            }
        },
        [
            READ_ONLY,
            selectedRowForUpdate,
            VERTICAL_ID,
            AOP_YEAR,
            keycloak,
            showMessage,
            fetchDocuments,
        ],
    )

    // ─── Update (Replace) ─────────────────────────────────────────────────────

    const updateFile = useCallback(
        async (filesList, rowToUpdate) => {
            if (READ_ONLY) {
                showMessage('You do not have permission to update files.', 'error')
                return
            }
            if (!filesList?.length) return

            setIsUploading(true)
            setUploadProgress(0)
            setLoading(true)

            try {
                const targetRow = rowToUpdate || selectedRowForUpdate
                const targetMasterId = targetRow?.masterId || targetRow?.MasterId
                const targetTransactionId =
                    targetRow?.transactionId || targetRow?.TransactionId || targetRow?.id

                if (!targetTransactionId) {
                    throw new Error('Transaction ID not found for document update.')
                }

                const file = filesList[0]
                const response = await DocumentUploadApiService.uploadOrUpdateDocument(
                    keycloak,
                    {
                        transactionId: targetTransactionId,
                        masterId: targetMasterId,
                        verticalId: VERTICAL_ID,
                        aopYear: AOP_YEAR,
                        file,
                    },
                )

                if (response?.code !== 200 && response?.status !== 'SUCCESS') {
                    throw new Error(response?.message || 'Update failed.')
                }

                showMessage('Document updated successfully!', 'success')
                setOpenUploadDialog(false)
                setSelectedRowForUpdate(null)
                await fetchDocuments()
            } catch (err) {
                console.error('Update error:', err)
                showMessage(err?.message || 'Update failed. Please try again.', 'error')
            } finally {
                setIsUploading(false)
                setUploadProgress(0)
                setLoading(false)
            }
        },
        [
            READ_ONLY,
            selectedRowForUpdate,
            VERTICAL_ID,
            AOP_YEAR,
            keycloak,
            showMessage,
            fetchDocuments,
        ],
    )

    // ─── Download ─────────────────────────────────────────────────────────────

    const handleDownload = useCallback(
        async (file) => {
            if (!file?.content) {
                showMessage('No document content available to download.', 'warning')
                return
            }
            try {
                setLoading(true)
                const base64Content = file.content
                const contentType = file.contentType || 'application/octet-stream'

                const getFileNameWithExtension = (name, mimeType) => {
                    if (!name) return 'download'
                    if (name.includes('.')) return name

                    const mimeMap = {
                        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': '.xlsx',
                        'application/vnd.ms-excel': '.xls',
                        'application/pdf': '.pdf',
                        'text/csv': '.csv',
                        'application/csv': '.csv',
                    }

                    const ext = mimeMap[mimeType] || ''
                    return `${name}${ext}`
                }

                const fileName = getFileNameWithExtension(
                    file.fileName || file.documentName || file.name,
                    contentType,
                )

                // Decode base64 to binary blob
                const byteCharacters = atob(base64Content)
                const byteNumbers = new Array(byteCharacters.length)
                for (let i = 0; i < byteCharacters.length; i++) {
                    byteNumbers[i] = byteCharacters.charCodeAt(i)
                }
                const byteArray = new Uint8Array(byteNumbers)
                const blob = new Blob([byteArray], { type: contentType })

                // Create download link and trigger click
                const url = URL.createObjectURL(blob)
                const link = document.createElement('a')
                link.href = url
                link.download = fileName
                document.body.appendChild(link)
                link.click()
                link.remove()
                URL.revokeObjectURL(url)

                showMessage('Download started successfully.', 'success')
            } catch (err) {
                console.error('Download error:', err)
                showMessage('Failed to download document content.', 'error')
            } finally {
                setLoading(false)
            }
        },
        [showMessage],
    )

    // ─── Delete ───────────────────────────────────────────────────────────────

    const handleDelete = useCallback(
        async (rowOrId) => {
            if (READ_ONLY) {
                showMessage('You do not have permission to delete files.', 'error')
                return
            }

            const targetRow =
                typeof rowOrId === 'object' && rowOrId !== null
                    ? rowOrId
                    : rows.find(
                        (r) =>
                            r.id === rowOrId ||
                            r.transactionId === rowOrId ||
                            r.TransactionId === rowOrId,
                    ) || selectedRowForDelete

            const transactionId =
                targetRow?.transactionId ||
                targetRow?.TransactionId ||
                targetRow?.id ||
                (typeof rowOrId === 'string' || typeof rowOrId === 'number'
                    ? rowOrId
                    : null)

            if (!transactionId) {
                showMessage('Transaction ID not found for document deletion.', 'error')
                return
            }

            setIsDeleting(true)
            try {
                const response = await DocumentUploadApiService.deleteDocument(
                    keycloak,
                    transactionId,
                )

                if (
                    response?.code &&
                    response.code !== 200 &&
                    response?.status !== 'SUCCESS'
                ) {
                    throw new Error(response?.message || 'Delete failed.')
                }

                showMessage(
                    response?.message || 'Document deleted successfully.',
                    'success',
                )
                await fetchDocuments()
            } catch (err) {
                console.error('Delete error:', err)
                showMessage(err?.message || 'Delete failed. Please try again.', 'error')
            } finally {
                setIsDeleting(false)
                setOpenDeleteConfirm(false)
                setSelectedRowForDelete(null)
            }
        },
        [READ_ONLY, rows, keycloak, showMessage, fetchDocuments, selectedRowForDelete],
    )

    // ─── ActionCell ───────────────────────────────────────────────────────────
    // Kendo's cells.data receives the full props object including `dataItem`.
    // We use direct onClick handlers here — no DOM event listener tricks needed.

    const ActionCell = useMemo(() => {
        const Cell = ({ dataItem }) => {
            const hasDocument = Boolean(
                dataItem?.transactionId || dataItem?.TransactionId,
            )

            const handleAddClick = (e) => {
                e.stopPropagation()
                if (!READ_ONLY) {
                    setUploadDialogMode('add')
                    setSelectedRowForUpdate(dataItem)
                    setOpenUploadDialog(true)
                }
            }

            const handleUpdateClick = (e) => {
                e.stopPropagation()
                if (!READ_ONLY) {
                    setSelectedRowForUpdate(dataItem)
                    setUploadDialogMode('update')
                    setOpenUploadDialog(true)
                }
            }

            const handleDownloadClick = (e) => {
                e.stopPropagation()
                handleDownload(dataItem)
            }

            const handleDeleteClick = (e) => {
                e.stopPropagation()
                if (!READ_ONLY) {
                    setSelectedRowForDelete(dataItem)
                    setOpenDeleteConfirm(true)
                }
            }

            return (
                <td
                    style={{
                        textAlign: 'center',
                        verticalAlign: 'middle',
                        whiteSpace: 'nowrap',
                    }}
                >
                    {!hasDocument && (
                        <Tooltip title='Add Document' arrow>
                            <span>
                                <IconButton
                                    size='small'
                                    disabled={READ_ONLY}
                                    onClick={handleAddClick}
                                >
                                    <AddIcon fontSize='small' />
                                </IconButton>
                            </span>
                        </Tooltip>
                    )}
                    {hasDocument && <Tooltip title='Update Document' arrow>
                        <span>
                            <IconButton
                                size='small'
                                disabled={READ_ONLY}
                                onClick={handleUpdateClick}
                            >
                                <UploadIcon fontSize='small' />
                            </IconButton>
                        </span>
                    </Tooltip>}

                    {hasDocument && <Tooltip title='Download Document' arrow>
                        <span>
                            <IconButton size='small' onClick={handleDownloadClick}>
                                <CloudDownloadIcon fontSize='small' />
                            </IconButton>
                        </span>
                    </Tooltip>}

                    {hasDocument && <Tooltip title='Delete Document' arrow>
                        <span>
                            <IconButton
                                size='small'
                                disabled={READ_ONLY}
                                onClick={handleDeleteClick}
                            >
                                <DeleteOutlineIcon fontSize='small' />
                            </IconButton>
                        </span>
                    </Tooltip>}
                </td>
            )
        }
        Cell.displayName = 'ActionCell'
        return Cell
    }, [READ_ONLY, handleDownload])

    // ─── Columns ──────────────────────────────────────────────────────────────

    const columns = useMemo(
        () => [
            {
                field: 'documentName',
                title: 'Document Name',
                editable: false,
                minWidth: 280,
            },
            {
                field: 'uploadedDateTime',
                title: 'Date & Time',
                editable: false,
                minWidth: 200,
            },
            {
                field: 'uploadedBy',
                title: 'Uploaded By',
                editable: false,
                minWidth: 160,
            },
            {
                field: 'fileName',
                title: 'File Name',
                editable: false,
                minWidth: 280
            },
            {
                field: 'fileSize',
                title: 'File Size',
                editable: false,
                minWidth: 120
            },
            {
                field: 'action',
                title: 'Action',
                editable: false,
                minWidth: 200,
                cell: ActionCell,
            },
        ],
        [ActionCell],
    )

    // ─── Effects ──────────────────────────────────────────────────────────────

    useEffect(() => {
        setModifiedCells({})
        fetchDocuments()
        fetchAdditionalInfo()
    }, [oldYear, yearChanged, PLANT_ID, AOP_YEAR, fetchDocuments, fetchAdditionalInfo])

    // ─── Misc ─────────────────────────────────────────────────────────────────

    const handleRemarkCellClick = (row) => {
        if (READ_ONLY) return
        setCurrentRemark(row.remarks || '')
        setCurrentRowId(row.id)
        setRemarkDialogOpen(true)
    }

    const getAdjustedPermissions = (perms, isOldYear) => {
        if (isOldYear !== 1) return perms
        return {
            ...perms,
            showAction: false,
            addButton: false,
            deleteButton: false,
            downloadExcelBtn: false,
            uploadExcelBtn: false,
            editButton: false,
            showUnit: false,
            saveWithRemark: false,
            saveBtn: true,
            isOldYear,
            allAction: false,
        }
    }

    const adjustedPermissionsManual = getAdjustedPermissions(
        {
            showAction: true,
            allAction: true,
            downloadExcelBtnFromUI: false,
            ExcelName: EXCEL_EXPORT_TITLE,
            showTitleNameBusiness: true,
            titleName: 'Document List',
        },
        IS_OLD_YEAR,
    )

    // ─── Upload dialog handler ────────────────────────────────────────────────

    const handleUploadDialogUpload = useCallback(
        (files) => {
            if (uploadDialogMode === 'add') {
                uploadFiles(files)
            } else {
                updateFile(files, selectedRowForUpdate)
            }
        },
        [uploadDialogMode, uploadFiles, updateFile, selectedRowForUpdate],
    )

    // ─── Delete confirm handler ───────────────────────────────────────────────

    const handleDeleteConfirm = useCallback(() => {
        if (selectedRowForDelete) {
            handleDelete(selectedRowForDelete)
        }
    }, [selectedRowForDelete, handleDelete])

    // ─── Notes submit ─────────────────────────────────────────────────────────

    const handleNotesSubmit = useCallback(async () => {
        if (!summary.trim() || READ_ONLY) return
        setIsSubmittingNotes(true)
        try {
            const payload = [
                {
                    id: infoRecord?.id || null,
                    otherInformation: summary,
                    verticalId: VERTICAL_ID,
                    aopYear: AOP_YEAR,
                },
            ]
            const response =
                await DocumentUploadApiService.saveOrUpdateOtherDocumentInformation(
                    keycloak,
                    VERTICAL_ID,
                    AOP_YEAR,
                    payload,
                )
            if (response?.code === 200) {
                setSnackbarOpen(true)
                setSnackbarData({
                    message: 'Additional Information saved successfully!',
                    severity: 'success',
                })
                await fetchAdditionalInfo()
            } else {
                throw new Error(
                    response?.message || 'Failed to save additional information.',
                )
            }
        } catch (err) {
            console.error('Additional Information save error:', err)
            showMessage(
                err?.message ||
                'Failed to save additional information. Please try again.',
                'error',
            )
        } finally {
            setIsSubmittingNotes(false)
        }
    }, [
        summary,
        READ_ONLY,
        infoRecord,
        VERTICAL_ID,
        AOP_YEAR,
        keycloak,
        showMessage,
        fetchAdditionalInfo,
    ])

    // ─────────────────────────────────────────────────────────────────────────

    return (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3, mt: 2 }}>
            {loading && <LoaderBackdrop />}

            {/* Document Grid */}
            <Box>
                <KendoDataTables
                    modifiedCells={modifiedCells}
                    setModifiedCells={setModifiedCells}
                    setRows={setRows}
                    columns={columns}
                    rows={rows}
                    paginationOptions={[100, 200, 300]}
                    snackbarData={snackbarData}
                    snackbarOpen={snackbarOpen}
                    apiRef={apiRef}
                    open1={open1}
                    setOpen1={setOpen1}
                    setSnackbarOpen={setSnackbarOpen}
                    setSnackbarData={setSnackbarData}
                    handleRemarkCellClick={handleRemarkCellClick}
                    fetchData={fetchDocuments}
                    remarkDialogOpen={remarkDialogOpen}
                    setRemarkDialogOpen={setRemarkDialogOpen}
                    currentRemark={currentRemark}
                    setCurrentRemark={setCurrentRemark}
                    currentRowId={currentRowId}
                    permissions={adjustedPermissionsManual}
                    plantID={PLANT_ID}
                />
            </Box>

            {/* ── Notes / Remarks Textarea ─────────────────────────────────── */}

            <Box sx={{ width: '100%' }}>
                {/* Label + Save button on same horizontal row */}
                <Box
                    sx={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        mb: 0.5,
                    }}
                >
                    <Typography variant='caption' className='aop-design-basis-label'>
                        Additional Information
                    </Typography>

                    <Button
                        variant='contained'
                        className='btn-save'
                        startIcon={
                            <Box component='img' src={SaveIcon} className='w16-icon' />
                        }
                        disabled={
                            !summary.trim() || Boolean(READ_ONLY) || isSubmittingNotes
                        }
                        onClick={handleNotesSubmit}
                    >
                        Save
                    </Button>
                </Box>

                <TextArea
                    className='vertical-resize-textarea'
                    disabled={Boolean(READ_ONLY)}
                    value={summary}
                    rows={2}
                    onChange={(e) => {
                        setSummary(e.target.value)
                    }}
                />
            </Box>

            {/* ── Upload Dialog (Add / Update) ──────────────────────────────── */}
            <UploadDocumentDialog
                open={openUploadDialog}
                onClose={() => {
                    if (!isUploading) {
                        setOpenUploadDialog(false)
                        setSelectedRowForUpdate(null)
                    }
                }}
                uploadDialogMode={uploadDialogMode}
                selectedRowForUpdate={selectedRowForUpdate}
                isUploading={isUploading}
                uploadProgress={uploadProgress}
                onUpload={handleUploadDialogUpload}
                readOnly={READ_ONLY}
            />

            {/* ── Delete Confirmation Dialog ────────────────────────────────── */}
            <DeleteConfirmationDialog
                open={openDeleteConfirm}
                onClose={() => {
                    if (!isDeleting) {
                        setOpenDeleteConfirm(false)
                        setSelectedRowForDelete(null)
                    }
                }}
                selectedRowForDelete={selectedRowForDelete}
                onConfirm={handleDeleteConfirm}
                readOnly={READ_ONLY}
                isDeleting={isDeleting}
            />

            {/* ── Toast Notifications ───────────────────────────────────────── */}
            {/* <Snackbar
        open={snackbarOpen}
        autoHideDuration={4000}
        onClose={() => setSnackbarOpen(false)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert
          onClose={() => setSnackbarOpen(false)}
          severity={snackbarData.severity}
          variant="filled"
          sx={{ width: '100%', fontSize: '0.82rem' }}
        >
          {snackbarData.message}
        </Alert>
      </Snackbar> */}
        </Box>
    )
}

export default OtherDocumentUpload
