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
import PropaneBusiness from 'components/kendo-data-tables/PropaneBusiness'
import { getRoleName } from 'services/role-service'
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
    screenTitle,
  } = dataGridStore
  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id

  const AOP_YEAR = year?.selectedYear
  const isOldYear = false
  const IS_OLD_YEAR = oldYear?.oldYear

  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)

  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase()

  const IS_ELASTOMER_VERTICAL = lowerVertName === 'elastomer'
  const IS_PE_PP_VERTICAL = lowerVertName === 'pp' || lowerVertName === 'pe'
  const IS_PTA_VERTICAL = lowerVertName === 'pta'
  const IS_PET_VERTICAL = lowerVertName === 'pet'
  const IS_VCM_VERTICAL = lowerVertName === 'vcm'
  const IS_CRACKER_VERTICAL = lowerVertName == 'cracker'
  const PRODUCTION_TARGET_LABEL = IS_VCM_VERTICAL
    ? 'Production Target (This is a reference for entering the Business Demand value)'
    : 'Production Target (MT) (This is a reference for entering the Business Demand value)'

  const SCREEN_NAME = screenTitle?.title

  const PLANT_NAME = plantObject?.name?.toUpperCase()
  const SITE_NAME = siteObject?.name?.toUpperCase()
  const VERTICAL_NAME = verticalObject?.name?.toUpperCase()

  const EXCEL_NAME = `${VERTICAL_NAME}_${SITE_NAME}_${PLANT_NAME}_Business_Demand_${AOP_YEAR}`
  const EXCEL_NAME_GRID2 = `${VERTICAL_NAME}_${SITE_NAME}_${PLANT_NAME}_${SCREEN_NAME}_${AOP_YEAR}`

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

    setModifiedCells({})

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
        UOM: IS_VCM_VERTICAL ? '%' : item?.UOM,
      }))

      setRows(formattedData)

      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [PLANT_ID, AOP_YEAR, oldYear, yearChanged, keycloak])

  const handleRemarkCellClick = (dataItem) => {
    // if (!dataItem?.isEditable) return
    if (READ_ONLY) return
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
      if (data.length == 0) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'No Records to Save!',
          severity: 'info',
        })
        setLoading(false)
        return
      }
      //

      if (
        IS_VCM_VERTICAL ||
        IS_PE_PP_VERTICAL ||
        IS_PTA_VERTICAL ||
        IS_PET_VERTICAL ||
        IS_ELASTOMER_VERTICAL
      ) {
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

          const SCALE = 100

          const toPreciseInt = (num) => {
            if (num === null || num === undefined || num === '') return 0
            const n = Number(num)
            if (isNaN(n)) return 0
            return Math.round(n * SCALE)
          }

          const formatFromIntRobust = (intVal) => {
            const sign = intVal < 0 ? '-' : ''
            const abs = Math.abs(intVal)
            const integerPart = Math.floor(abs / SCALE)
            const remainder = abs % SCALE
            if (remainder === 0) return sign + String(integerPart)
            const scaleDigits = String(SCALE).length - 1
            let fracStr = String(remainder).padStart(scaleDigits, '0')
            fracStr = fracStr.replace(/0+$/, '')
            return sign + `${integerPart}.${fracStr}`
          }

          const expected = 100 * SCALE
          const failures = []

          for (const month of months) {
            const sumInt = productionRows.reduce(
              (acc, row) => acc + toPreciseInt(row[month]),
              0,
            )

            if (sumInt !== expected) {
              failures.push({ month, sumInt })
            }
          }

          if (failures.length > 0) {
            const parts = failures.map((f) => {
              const prettyMonth =
                f.month.charAt(0).toUpperCase() + f.month.slice(1)
              const prettySum = formatFromIntRobust(f.sumInt)
              return `${prettyMonth} - ${prettySum}`
            })

            setSnackbarOpen(true)
            setSnackbarData({
              message: `The production Sum should be exactly 100 - Current values (${parts.join(', ')})`,
              severity: 'error',
            })
            setLoading(false)
            return
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
    IS_PE_PP_VERTICAL || IS_PET_VERTICAL
      ? `${SCREEN_NAME} (%)`
      : `${SCREEN_NAME}`

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
      ExcelName: `${EXCEL_NAME_GRID2}`,
      isHeight: lowerVertName !== 'meg' && rows?.length > 10,
      isTotalFooterActive:
        // IS_VCM_VERTICAL ||
        IS_PE_PP_VERTICAL ||
        // FOR PTA IT IS NOT REQUIRED
        // IS_PTA_VERTICAL ||
        IS_PET_VERTICAL ||
        IS_ELASTOMER_VERTICAL
          ? true
          : false,

      downloadExcelBtn:
        IS_CRACKER_VERTICAL || IS_PE_PP_VERTICAL || IS_PET_VERTICAL
          ? true
          : false,
      uploadExcelBtn:
        IS_CRACKER_VERTICAL || IS_PE_PP_VERTICAL || IS_PET_VERTICAL
          ? true
          : false,

      downloadExcelBtnFromUI:
        IS_CRACKER_VERTICAL || IS_PE_PP_VERTICAL || IS_PET_VERTICAL
          ? false
          : true,
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

  const totalRowConfiguration = [
    { field: 'april', aggregate: 'sum' },
    { field: 'aug', aggregate: 'sum' },
    { field: 'dec', aggregate: 'sum' },
    { field: 'feb', aggregate: 'sum' },
    { field: 'jan', aggregate: 'sum' },
    { field: 'july', aggregate: 'sum' },
    { field: 'june', aggregate: 'sum' },
    { field: 'march', aggregate: 'sum' },
    { field: 'may', aggregate: 'sum' },
    { field: 'nov', aggregate: 'sum' },
    { field: 'oct', aggregate: 'sum' },
    { field: 'sep', aggregate: 'sum' },
  ]

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
        EXCEL_NAME,
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
              <Typography component='span' className='accordian-title'>
                {PRODUCTION_TARGET_LABEL}
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
        totalRowConfiguration={totalRowConfiguration}
      />

      {IS_CRACKER_VERTICAL && (
        <>
          <Box sx={{ width: '100%', margin: 0 }}>
            <PropaneBusiness permissions={adjustedPermissions} />
          </Box>
        </>
      )}
    </div>
  )
}

export default BusinessDemand
