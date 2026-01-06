import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import NestedKendoTable from 'components/kendo-data-tables/NestedKendoTable/index'
import { InputApiService } from 'services/phase-two-services/inputApiService'
import { Stack } from '../../../../../../node_modules/@mui/material/index'
import { isEmptyNullUndefined, validateNestedRowDataWithRemarks } from 'components/Utilities/commonUtilityFunctions'

const STGShutdownAndOperationalHr = ({ hoursRows = [] }) => {
  const keycloak = useSession()
  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, siteObject, verticalObject, year, screenTitle } =
    dataGridStore
  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear
  const headerMap = generateHeaderNames(AOP_YEAR)
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const valueFormat = ValueFormatterProduction()

  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const dummySTGData = [
    // ===================== SHP Steam =====================
    {
      id: 1,
      assetName: 'NMD - Utility Plant',
      utilityDistributed: { name: 'SHP Steam_Dis', sapCode: '310027924' },
      utilityGenerated: { name: 'HRSG1_SHP STEAM', sapCode: '310027926' },
      assetType: 'HRSG',
      april: { shutdownHrs: 720, netOperationHrs: null },
      may: { shutdownHrs: null, netOperationHrs: 744 },
      june: { shutdownHrs: 720, netOperationHrs: null },
      july: { shutdownHrs: null, netOperationHrs: 744 },
      aug: { shutdownHrs: null, netOperationHrs: 744 },
      sep: { shutdownHrs: 720, netOperationHrs: null },
      oct: { shutdownHrs: null, netOperationHrs: 744 },
      nov: { shutdownHrs: 720, netOperationHrs: null },
      dec: { shutdownHrs: null, netOperationHrs: 744 },
      jan: { shutdownHrs: null, netOperationHrs: 744 },
      feb: { shutdownHrs: 672, netOperationHrs: null },
      march: { shutdownHrs: null, netOperationHrs: 744 },
      remarks: '',
      inEdit: false,
    },

    {
      id: 2,
      assetName: 'NMD - Utility Plant',
      utilityDistributed: { name: 'SHP Steam_Dis', sapCode: '310027924' },
      utilityGenerated: { name: 'HRSG2_SHP STEAM', sapCode: '310027929' },
      assetType: 'HRSG',
      april: { shutdownHrs: null, netOperationHrs: 720 },
      may: { shutdownHrs: null, netOperationHrs: 744 },
      june: { shutdownHrs: 720, netOperationHrs: null },
      july: { shutdownHrs: null, netOperationHrs: 744 },
      aug: { shutdownHrs: null, netOperationHrs: 744 },
      sep: { shutdownHrs: 720, netOperationHrs: null },
      oct: { shutdownHrs: null, netOperationHrs: 744 },
      nov: { shutdownHrs: 720, netOperationHrs: null },
      dec: { shutdownHrs: null, netOperationHrs: 744 },
      jan: { shutdownHrs: null, netOperationHrs: 744 },
      feb: { shutdownHrs: 672, netOperationHrs: null },
      march: { shutdownHrs: null, netOperationHrs: 744 },
      remarks: '',
      inEdit: false,
    },

    {
      id: 3,
      assetName: 'NMD - Utility Plant',
      utilityDistributed: { name: 'SHP Steam_Dis', sapCode: '310027924' },
      utilityGenerated: { name: 'HRSG3_SHP STEAM', sapCode: '310027930' },
      assetType: 'HRSG',
      april: { shutdownHrs: null, netOperationHrs: 720 },
      may: { shutdownHrs: 744, netOperationHrs: null },
      june: { shutdownHrs: null, netOperationHrs: 720 },
      july: { shutdownHrs: 744, netOperationHrs: null },
      aug: { shutdownHrs: null, netOperationHrs: 744 },
      sep: { shutdownHrs: null, netOperationHrs: 720 },
      oct: { shutdownHrs: null, netOperationHrs: 744 },
      nov: { shutdownHrs: 720, netOperationHrs: null },
      dec: { shutdownHrs: null, netOperationHrs: 744 },
      jan: { shutdownHrs: null, netOperationHrs: 744 },
      feb: { shutdownHrs: 672, netOperationHrs: null },
      march: { shutdownHrs: null, netOperationHrs: 744 },
      remarks: '',
      inEdit: false,
    },

    // ===================== HP Steam =====================
    {
      id: 4,
      assetName: 'NMD - Utility Plant',
      utilityDistributed: { name: 'HP Steam_Dis', sapCode: '310027939' },
      utilityGenerated: { name: 'HP Steam PRDS', sapCode: '310028161' },
      assetType: 'PRDS',
      april: { shutdownHrs: null, netOperationHrs: 720 },
      may: { shutdownHrs: null, netOperationHrs: 744 },
      june: { shutdownHrs: null, netOperationHrs: 720 },
      july: { shutdownHrs: null, netOperationHrs: 744 },
      aug: { shutdownHrs: null, netOperationHrs: 744 },
      sep: { shutdownHrs: null, netOperationHrs: 720 },
      oct: { shutdownHrs: null, netOperationHrs: 744 },
      nov: { shutdownHrs: null, netOperationHrs: 720 },
      dec: { shutdownHrs: null, netOperationHrs: 744 },
      jan: { shutdownHrs: null, netOperationHrs: 744 },
      feb: { shutdownHrs: null, netOperationHrs: 672 },
      march: { shutdownHrs: null, netOperationHrs: 744 },
      remarks: '',
      inEdit: false,
    },

    // ===================== MP Steam =====================
    {
      id: 5,
      assetName: 'NMD - Utility Plant',
      utilityDistributed: { name: 'MP Steam_Dis', sapCode: '310027940' },
      utilityGenerated: { name: 'STG1_MP STEAM', sapCode: '310027952' },
      assetType: 'Steam Turbine Generator',
      april: { shutdownHrs: null, netOperationHrs: 720 },
      may: { shutdownHrs: null, netOperationHrs: 744 },
      june: { shutdownHrs: 100, netOperationHrs: 620 },
      july: { shutdownHrs: 100, netOperationHrs: 644 },
      aug: { shutdownHrs: 120, netOperationHrs: 624 },
      sep: { shutdownHrs: 120, netOperationHrs: 600 },
      oct: { shutdownHrs: 120, netOperationHrs: 624 },
      nov: { shutdownHrs: 100, netOperationHrs: 620 },
      dec: { shutdownHrs: 120, netOperationHrs: 624 },
      jan: { shutdownHrs: 120, netOperationHrs: 624 },
      feb: { shutdownHrs: 120, netOperationHrs: 552 },
      march: { shutdownHrs: 120, netOperationHrs: 624 },
      remarks: '',
      inEdit: false,
    },

    {
      id: 6,
      assetName: 'NMD - Utility Plant',
      utilityDistributed: { name: 'MP Steam_Dis', sapCode: '310027940' },
      utilityGenerated: { name: 'MP Steam PRDS SHP', sapCode: '310028012' },
      assetType: 'PRDS',
      april: { shutdownHrs: null, netOperationHrs: 720 },
      may: { shutdownHrs: null, netOperationHrs: 744 },
      june: { shutdownHrs: null, netOperationHrs: 720 },
      july: { shutdownHrs: null, netOperationHrs: 744 },
      aug: { shutdownHrs: null, netOperationHrs: 744 },
      sep: { shutdownHrs: null, netOperationHrs: 720 },
      oct: { shutdownHrs: null, netOperationHrs: 744 },
      nov: { shutdownHrs: null, netOperationHrs: 720 },
      dec: { shutdownHrs: null, netOperationHrs: 744 },
      jan: { shutdownHrs: null, netOperationHrs: 744 },
      feb: { shutdownHrs: null, netOperationHrs: 672 },
      march: { shutdownHrs: null, netOperationHrs: 744 },
      remarks: '',
      inEdit: false,
    },

    // ===================== LP Steam =====================
    {
      id: 7,
      assetName: 'NMD - Utility Plant',
      utilityDistributed: { name: 'LP Steam_Dis', sapCode: '310027965' },
      utilityGenerated: { name: 'STG1_LP STEAM', sapCode: '310028010' },
      assetType: 'Steam Turbine Generator',
      april: { shutdownHrs: null, netOperationHrs: 720 },
      may: { shutdownHrs: null, netOperationHrs: 744 },
      june: { shutdownHrs: 100, netOperationHrs: 620 },
      july: { shutdownHrs: 100, netOperationHrs: 644 },
      aug: { shutdownHrs: 120, netOperationHrs: 624 },
      sep: { shutdownHrs: 120, netOperationHrs: 600 },
      oct: { shutdownHrs: 120, netOperationHrs: 624 },
      nov: { shutdownHrs: 100, netOperationHrs: 620 },
      dec: { shutdownHrs: 120, netOperationHrs: 624 },
      jan: { shutdownHrs: 120, netOperationHrs: 624 },
      feb: { shutdownHrs: 120, netOperationHrs: 552 },
      march: { shutdownHrs: 120, netOperationHrs: 624 },
      remarks: '',
      inEdit: false,
    },

    {
      id: 8,
      assetName: 'NMD - Utility Plant',
      utilityDistributed: { name: 'LP Steam_Dis', sapCode: '310027965' },
      utilityGenerated: { name: 'LP Steam PRDS', sapCode: '310028013' },
      assetType: 'PRDS',
      april: { shutdownHrs: null, netOperationHrs: 720 },
      may: { shutdownHrs: null, netOperationHrs: 744 },
      june: { shutdownHrs: null, netOperationHrs: 720 },
      july: { shutdownHrs: null, netOperationHrs: 744 },
      aug: { shutdownHrs: null, netOperationHrs: 744 },
      sep: { shutdownHrs: null, netOperationHrs: 720 },
      oct: { shutdownHrs: null, netOperationHrs: 744 },
      nov: { shutdownHrs: null, netOperationHrs: 720 },
      dec: { shutdownHrs: null, netOperationHrs: 744 },
      jan: { shutdownHrs: null, netOperationHrs: 744 },
      feb: { shutdownHrs: null, netOperationHrs: 672 },
      march: { shutdownHrs: null, netOperationHrs: 744 },
      remarks: '',
      inEdit: false,
    },
  ]

  const nestedColumns = [
    {
      field: 'assetName',
      title: 'Asset Name',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      locked: true,
    },

    {
      field: 'utilityDistributed.name',
      title: 'Utility Distributed',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'utilityDistributed.sapCode',
      title: 'Distributed SAP Code',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'utilityGenerated.name',
      title: 'Utility Generated',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      locked: true,
    },
    {
      field: 'utilityGenerated.sapCode',
      title: 'Generated SAP Code',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      locked: true,
    },

    {
      field: 'assetType',
      title: 'Asset Type',
      width: 150,
      minWidth: 150,
      type: 'text',
      editable: false,
      locked: true,
      hidden: true,
    },
    {
      title: headerMap[4],
      children: [
        {
          field: 'april.shutdownHrs',
          title: 'Shutdown Hrs',
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'april.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[5],
      children: [
        {
          field: 'may.shutdownHrs',
          title: 'Shutdown Hrs',
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'may.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[6],
      children: [
        {
          field: 'june.shutdownHrs',
          title: 'Shutdown Hrs',
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'june.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[7],
      children: [
        {
          field: 'july.shutdownHrs',
          title: 'Shutdown Hrs',
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'july.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[8],
      children: [
        {
          field: 'aug.shutdownHrs',
          title: 'Shutdown Hrs',
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'aug.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[9],
      children: [
        {
          field: 'sep.shutdownHrs',
          title: 'Shutdown Hrs',
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'sep.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[10],
      children: [
        {
          field: 'oct.shutdownHrs',
          title: 'Shutdown Hrs',
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'oct.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[11],
      children: [
        {
          field: 'nov.shutdownHrs',
          title: 'Shutdown Hrs',
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'nov.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[12],
      children: [
        {
          field: 'dec.shutdownHrs',
          title: 'Shutdown Hrs',
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'dec.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[1],
      children: [
        {
          field: 'jan.shutdownHrs',
          title: 'Shutdown Hrs',
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'jan.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[2],
      children: [
        {
          field: 'feb.shutdownHrs',
          title: 'Shutdown Hrs',
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'feb.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      title: headerMap[3],
      children: [
        {
          field: 'march.shutdownHrs',
          title: 'Shutdown Hrs',
          width: 80,
          editable: true,
          type: 'wholeNumber',
          format: valueFormat,
        },
        {
          field: 'march.netOperationHrs',
          title: 'Operational Hrs',
          width: 80,
          editable: false,
          type: 'wholeNumber',
          format: valueFormat,
        },
      ],
    },
    {
      field: 'remarks',
      title: 'Remarks',
      width: 250,
      type: 'textarea',
      editable: true,
      minWidth: 250,
    },
  ]

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchShutdownAndOperationalData()
      setModifiedCells({})
    }
  }, [PLANT_ID, AOP_YEAR])

  const fetchShutdownAndOperationalData = async () => {
    setLoading(true)
    try {
      // TODO: Replace with actual API call
      const res = await InputApiService.getOperationHoursData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      const rowsWithIds = res?.steamResponse?.map((row, index) => ({
        ...row,
        id: row.id || index + 1,
      }))

      setRows(rowsWithIds)
      setOriginalRows(rowsWithIds)
    } catch (error) {
      console.error('Error fetching shutdown and operational data:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }

  const permissions = {
    showAction: true,
    addButton: false,
    deleteButton: false,
    editButton: true,
    saveBtn: true,
    allAction: true,
    showImport: true,
    downloadExcelBtnFromUI: true,
    ExcelName: `STG Shutdown and Operational - ${AOP_YEAR}`,
    showTitleNameBusiness: true,
    showTitle: false,
  }

  const saveChanges = async () => {
    setLoading(true)
    const modifiedData = Object.values(modifiedCells)
    if (modifiedData.length == 0) {
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

    // Custom validation: If any row data is updated, remarks must be filled and different from original
    const fieldsToCheck = ['april.shutdownHrs', 'may.shutdownHrs', 'june.shutdownHrs', 'july.shutdownHrs', 'aug.shutdownHrs', 'sep.shutdownHrs', 'oct.shutdownHrs', 'nov.shutdownHrs', 'dec.shutdownHrs', 'jan.shutdownHrs', 'feb.shutdownHrs', 'march.shutdownHrs']
    const validationError = validateNestedRowDataWithRemarks(data, originalRows, fieldsToCheck)

    if (validationError) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: validationError,
        severity: 'error',
      })
      setLoading(false)
      return
    }

    const payload = modifiedData.map(({ id, inEdit, ...rest }) => {
      // Convert utilityGenerated and utilityDistributed back to arrays
      const transformed = { ...rest }
      return transformed
    })
    const tempPayload = {
      steamResponse: payload,
    }

    try {
      console.log('tempPayload', tempPayload)

      const response = await InputApiService.saveOperationHours(
        keycloak,
        AOP_YEAR,
        tempPayload,
      )

      setModifiedCells({})
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Successfully saved ${modifiedData.length} changes!`,
        severity: 'success',
      })
    } catch (error) {
      console.error('Error saving operational hours data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to save changes. Please try again.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }
  // Handle remark cell click
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      <Stack>
        <NestedKendoTable
          columns={nestedColumns}
          rows={rows}
          setRows={setRows}
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          permissions={permissions}
          handleRemarkCellClick={handleRemarkCellClick}
          remarkDialogOpen={remarkDialogOpen}
          setRemarkDialogOpen={setRemarkDialogOpen}
          currentRemark={currentRemark}
          setCurrentRemark={setCurrentRemark}
          currentRowId={currentRowId}
          setCurrentRowId={() => {}}
          saveChanges={saveChanges}
          snackbarData={snackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          groupBy={['assetType']}
          hoursRows={hoursRows}
        />
      </Stack>
    </Box>
  )
}

export default STGShutdownAndOperationalHr
