import { Box } from '@mui/material'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import Typography from '@mui/material/Typography'
import { useGridApiRef } from '@mui/x-data-grid'
import kendoGetEnhancedColDefs from 'components/data-tables/CommonHeader/kendoBusinessDemColDef'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import React, { useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { BusinessDemandDataApiService } from 'services/business-demand-data-api-service'
import { useSession } from 'SessionStoreContext'
import {
  CustomAccordion,
  CustomAccordionDetails,
  CustomAccordionSummary,
} from 'utils/CustomAccrodian'
import { validateFields } from 'utils/validationUtils'
import KendoDataTables from './index'
import ProductionvolumeData from './ProductionVoluemData'

const BusinessDemand = ({ permissions }) => {
  const [modifiedCells, setModifiedCells] = React.useState({})

  const keycloak = useSession()

  const [open1, setOpen1] = useState(false)
  const [deleteId, setDeleteId] = useState(null)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    verticalChange,
    yearChanged,
    oldYear,
    plantID,
    plantObject,
    siteObject,
    verticalObject,
    year,
  } = dataGridStore

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear

  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  const apiRef = useGridApiRef()
  const [rows, setRows] = useState()
  const headerMap = generateHeaderNames(AOP_YEAR)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [loading, setLoading] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const unsavedChangesRef = React.useRef({
    unsavedRows: {},
    rowsBeforeChange: {},
  })

  const fetchData = async () => {
    if (!PLANT_ID || !SITE_ID || !VERTICAL_ID || !AOP_YEAR) return
    setLoading(true)
    try {
      var data = await BusinessDemandDataApiService.getBDData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      const formattedData = data.map((item, index) => ({
        ...item,
        idFromApi: item.id,
        id: index,
        originalRemark: item.remark,
        inEdit: false,
        Particulars: item.normParameterTypeDisplayName,
        expanded: false,
      }))

      setRows(formattedData)

      setLoading(false)
    } catch (error) {
      console.error('Error fetching Business Demand data:', error)
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [plantID, oldYear, yearChanged, keycloak])

  const handleRemarkCellClick = (dataItem) => {
    // if (!dataItem?.isEditable) return
    setCurrentRemark(dataItem.remark || '')
    setCurrentRowId(dataItem.id)
    setRemarkDialogOpen(true)
  }

  const colDefs = kendoGetEnhancedColDefs({
    headerMap,
  })

  const saveChanges = React.useCallback(async () => {
    setLoading(true)

    try {
      if (Object.keys(modifiedCells).length === 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }

      var rawData = Object.values(modifiedCells)
      const data = rawData.filter((row) => row.inEdit)
      // var data = Object.values(unsavedChangesRef.current.unsavedRows)
      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }
      const isPPorPE_NMD =
        (lowerVertName === 'pp' || lowerVertName === 'pe') &&
        siteObject?.name?.toLowerCase() === 'nmd'
      //

      if (isPPorPE_NMD) {
        // Use all rows, not just edited ones
        const productionRows = (rows || []).filter(
          (row) => row.Particulars?.toLowerCase() === 'production',
        )
        if (productionRows.length > 0) {
          const months = [
            'april',
            'may',
            'june',
            'july',
            'aug',
            'sep',
            'oct',
            'nov',
            'dec',
            'jan',
            'feb',
            'march',
          ]
          for (const month of months) {
            const sumMonth = productionRows.reduce(
              (acc, row) => acc + (parseFloat(row[month]) || 0),
              0,
            )
            if (Math.abs(sumMonth - 100) > 0.01) {
              setSnackbarOpen(true)
              setSnackbarData({
                message: `Sum of '${month.charAt(0).toUpperCase() + month.slice(1)}' for Production must be exactly 100.00 (Current: ${sumMonth.toFixed(2)})`,
                severity: 'error',
              })
              setLoading(false)
              return
            }
          }
        }
      }
      const requiredFields = ['normParameterId', 'remark']

      const validationMessage = validateFields(data, requiredFields)

      if (validationMessage) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: validationMessage,
          severity: 'error',
        })
        setLoading(false)
        return
      }
      saveBusinessDemandData(data)
    } catch (error) {
      console.log('Error saving changes:', error)
    }
    // }, 400)
  }, [modifiedCells])

  const saveBusinessDemandData = async (newRows) => {
    try {
      const payloadData = newRows.map((row) => ({
        april: row.april || null,
        may: row.may || null,
        june: row.june || null,
        july: row.july || null,
        aug: row.aug || null,
        sep: row.sep || null,
        oct: row.oct || null,
        nov: row.nov || null,
        dec: row.dec || null,
        jan: row.jan || null,
        feb: row.feb || null,
        march: row.march || null,
        remark: row.remark || null,
        avgTph: row.avgTph || null,
        year: AOP_YEAR,
        plantId: PLANT_ID,
        siteFKId: SITE_ID,
        verticalFKId: VERTICAL_ID,
        normParameterId: row.normParameterId,
        id: row.idFromApi || null,
        inEdit: row.inEdit || false,
      }))

      const response =
        await BusinessDemandDataApiService.saveBusinessDemandData(
          payloadData,
          keycloak,
        )

      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Saved Successfully!',
        severity: 'success',
      })
      setModifiedCells({})

      unsavedChangesRef.current = {
        unsavedRows: {},
        rowsBeforeChange: {},
      }
      fetchData()
      return response
    } catch (error) {
      console.error('Error in saving data!', error)
    } finally {
      // fetchData()
    }
  }
  const deleteRowData = async (paramsForDelete) => {
    try {
      const { idFromApi, id } = paramsForDelete.row
      const deleteId = id

      if (!idFromApi) {
        setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
      }

      if (idFromApi) {
        await BusinessDemandDataApiService.deleteBusinessDemandData(
          idFromApi,
          keycloak,
        )
        setRows((prevRows) => prevRows.filter((row) => row.id !== deleteId))
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Record Deleted successfully!',
          severity: 'success',
        })
        fetchData()
      }
    } catch (error) {
      console.error('Error deleting Record!', error)
    }
  }

  const getAdjustedPermissions = (permissions, isOldYear) => {
    if (isOldYear != 1) return permissions
    return {
      ...permissions,
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      saveWithRemark: false,
      saveBtn: false,
      isOldYear: isOldYear,
      // showStepper:false,
    }
  }
  const percentageTitle =
    lowerVertName === 'pp' || lowerVertName === 'pe'
      ? 'Business Demand Data (%)'
      : 'Business Demand Data'

  const adjustedPermissions = getAdjustedPermissions(
    {
      showAction: permissions?.showAction ?? false,
      addButton: permissions?.addButton ?? false,
      deleteButton: permissions?.deleteButton ?? false,
      editButton: permissions?.editButton ?? false,
      showUnit: permissions?.showUnit ?? false,
      saveWithRemark: permissions?.saveWithRemark ?? true,
      saveBtn: permissions?.saveBtn ?? true,
      allAction: permissions?.allAction ?? true,
      units: ['TPH', 'TPD'],
      showTitleNameBusiness: true,
      titleName: percentageTitle,
      downloadExcelBtnFromUI:
        lowerVertName == 'cracker' ||
        (lowerVertName == 'pe' &&
          plantObject?.name?.toLowerCase() === 'ldpe' &&
          siteObject?.name?.toLowerCase() === 'nmd')
          ? false
          : true,
      ExcelName: `${lowerVertName}_Business Demand Data`,
      isHeight: lowerVertName !== 'meg' && rows?.length > 10,

      downloadExcelBtn:
        lowerVertName == 'cracker' ||
        (lowerVertName == 'pe' &&
          plantObject?.name?.toLowerCase() === 'ldpe' &&
          siteObject?.name?.toLowerCase() === 'nmd')
          ? true
          : false,
      uploadExcelBtn:
        lowerVertName == 'cracker' ||
        (lowerVertName == 'pe' &&
          plantObject?.name?.toLowerCase() === 'ldpe' &&
          siteObject?.name?.toLowerCase() === 'nmd')
          ? true
          : false,
    },
    isOldYear,
  )

  const uploadBusinessDemand = async (rawFile) => {
    setLoading(true)

    try {
      let response

      response = await BusinessDemandDataApiService.businessDemandImport(
        rawFile,
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Uploaded Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        fetchData()
      } else if (response?.code === 400 && response?.data) {
        const byteCharacters = atob(response.data)
        const byteNumbers = Array.from(byteCharacters, (char) =>
          char.charCodeAt(0),
        )
        const byteArray = new Uint8Array(byteNumbers)

        const blob = new Blob([byteArray], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        })

        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', 'Error File - Business Demand.xlsx')
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)

        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Partial data saved. Error file downloaded.',
          severity: 'warning',
        })
        fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Upload Failed!',
          severity: 'error',
        })
      }

      return response
    } catch (error) {
      console.error('Error uploading xcel:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Unexpected error occurred!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleExcelUpload = (rawFile) => {
    uploadBusinessDemand(rawFile)
  }

  const downloadExcelForConfiguration = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'success',
    })

    try {
      let response
      response = await BusinessDemandDataApiService.businessDemandExport(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
    } catch (error) {
      console.error('Error downloading Excel:', error)
      setSnackbarData({
        message: 'Failed to download Excel.',
        severity: 'error',
      })
    } finally {
      setSnackbarOpen(true)
    }
  }

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      {lowerVertName !== 'cracker' && (
        <>
          <CustomAccordion defaultExpanded disableGutters>
            <CustomAccordionSummary
              aria-controls='meg-grid-content'
              id='meg-grid-header'
            >
              <Typography component='span' className='grid-title'>
                Production Target (MT) (This is a reference for entering the
                Business Demand value)
              </Typography>
            </CustomAccordionSummary>
            <CustomAccordionDetails>
              <Box sx={{ width: '100%', margin: 0 }}>
                <ProductionvolumeData
                  permissions={{
                    allAction: true,
                    showAction: false,
                    addButton: false,
                    deleteButton: false,
                    editButton: false,
                    showUnit: true,
                    saveWithRemark: false,
                    showCalculate: false,
                    saveBtn: false,
                    hideSummary: true,
                    hideUploadExcel: true,
                    hideDownloadExcel: true,
                  }}
                />
              </Box>
            </CustomAccordionDetails>
          </CustomAccordion>
        </>
      )}

      <KendoDataTables
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        setRows={setRows}
        columns={colDefs}
        rows={rows || []}
        title='Business Demand'
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        apiRef={apiRef}
        deleteId={deleteId}
        setDeleteId={setDeleteId}
        setOpen1={setOpen1}
        open1={open1}
        fetchData={fetchData}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={setCurrentRowId}
        handleRemarkCellClick={handleRemarkCellClick}
        deleteRowData={deleteRowData}
        permissions={adjustedPermissions}
        groupBy='Particulars'
        handleExcelUpload={handleExcelUpload}
        downloadExcelForConfiguration={downloadExcelForConfiguration}
      />
    </div>
  )
}

export default BusinessDemand
