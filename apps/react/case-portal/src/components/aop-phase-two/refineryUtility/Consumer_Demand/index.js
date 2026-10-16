import React, { useState, useEffect, useMemo, useRef, useCallback } from 'react'
import { Box } from '@mui/material'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import { generateHeaderNames } from '../../common/utilities/generateHeaders'
import { customValueFormatterPhaseTwo } from '../../common/ValueFormatterPhaseTwo'
import { validateRowDataWithRemarks } from '../../common/commonUtilityFunctions'
import { ConsumerDemandApiService } from 'components/aop-phase-two/services/refineryUtility/consumerDemandApiService'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'
import { generateExcelName } from 'components/aop-phase-two/common/utilities/excelNameUtil'
import {
  downloadBase64Excel,
  downloadBlobExcel,
} from 'components/aop-phase-two/common/utilities/downloadBase64Excel'

/**
 * Format type string into a readable title (e.g. "Vendor" -> "Vendor", "Utility Consumption" -> "Utility Consumption")
 */
export const formatTypeTitle = (type) => {
  if (!type || typeof type !== 'string') return 'Consumer Demand'
  const trimmed = type.trim()
  if (!trimmed) return 'Consumer Demand'

  const acronyms = new Set([
    'DM',
    'RO',
    'LP',
    'HP',
    'MP',
    'HHP',
    'UOM',
    'AOP',
    'FY',
    'LPG',
    'LNG',
    'CNG',
    'N2',
    'O2',
    'H2',
  ])

  return trimmed
    .split(/[\s_-]+/)
    .map((word) => {
      const upper = word.toUpperCase()
      if (acronyms.has(upper)) return upper
      return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()
    })
    .join(' ')
}

const TYPE_PRIORITY = [
  'utility consumption',
  'utility',
  'vendor',
  'vendor consumption',
]

const getTypeSortIndex = (key) => {
  const lower = key.toLowerCase().trim()
  const idx = TYPE_PRIORITY.indexOf(lower)
  return idx !== -1 ? idx : 999
}

const ConsumerDemand = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, year } = dataGridStore

  const PLANT_ID = plantObject?.id
  const AOP_YEAR = year?.selectedYear || '2026-2027'
  const EXCEL_NAME = generateExcelName(dataGridStore, 'Consumer_Demand')

  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const [activeRemarkType, setActiveRemarkType] = useState(null)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)

  const modifiedCellsRef = useRef({})
  useEffect(() => {
    modifiedCellsRef.current = modifiedCells
  }, [modifiedCells])

  const valueFormat = customValueFormatterPhaseTwo(2)
  const headerMap = generateHeaderNames(AOP_YEAR)

  const columns = useMemo(
    () => [
      {
        field: 'id',
        title: 'Id',
        widthT: 80,
        minWidth: 60,
        type: 'text',
        editable: false,
        locked: true,
        hidden: true,
      },
      {
        field: 'consumerUnit',
        title: 'Consumers Unit',
        widthT: 260,
        minWidth: 200,
        type: 'text',
        editable: false,
        locked: true,
      },
      {
        field: 'actualConsumption',
        title: 'Yearly Actual Consumption',
        widthT: 260,
        minWidth: 230,
        type: 'number1',
        locked: true,
        editable: false,
        format: valueFormat,
      },
      {
        field: 'consumerType',
        title: 'Consumer Category',
        widthT: 200,
        minWidth: 150,
        type: 'text',
        editable: false,
        locked: true,
        hidden: true,
      },
      {
        field: 'type',
        title: 'Type',
        widthT: 150,
        minWidth: 100,
        type: 'text',
        editable: false,
        locked: true,
        hidden: true,
      },
      {
        field: 'UOM',
        title: 'UOM',
        widthT: 100,
        minWidth: 80,
        type: 'text',
        editable: false,
        locked: true,
      },
      {
        field: 'apr',
        title: headerMap[4] || 'Apr',
        widthT: 120,
        minWidth: 110,
        type: 'number1',
        editable: true,
        format: valueFormat,
      },
      {
        field: 'may',
        title: headerMap[5] || 'May',
        widthT: 120,
        minWidth: 110,
        type: 'number1',
        editable: true,
        format: valueFormat,
      },
      {
        field: 'jun',
        title: headerMap[6] || 'Jun',
        widthT: 120,
        minWidth: 110,
        type: 'number1',
        editable: true,
        format: valueFormat,
      },
      {
        field: 'jul',
        title: headerMap[7] || 'Jul',
        widthT: 120,
        minWidth: 110,
        type: 'number1',
        editable: true,
        format: valueFormat,
      },
      {
        field: 'aug',
        title: headerMap[8] || 'Aug',
        widthT: 120,
        minWidth: 110,
        type: 'number1',
        editable: true,
        format: valueFormat,
      },
      {
        field: 'sep',
        title: headerMap[9] || 'Sep',
        widthT: 120,
        minWidth: 110,
        type: 'number1',
        editable: true,
        format: valueFormat,
      },
      {
        field: 'oct',
        title: headerMap[10] || 'Oct',
        widthT: 120,
        minWidth: 110,
        type: 'number1',
        editable: true,
        format: valueFormat,
      },
      {
        field: 'nov',
        title: headerMap[11] || 'Nov',
        widthT: 120,
        minWidth: 110,
        type: 'number1',
        editable: true,
        format: valueFormat,
      },
      {
        field: 'dec',
        title: headerMap[12] || 'Dec',
        widthT: 120,
        minWidth: 110,
        type: 'number1',
        editable: true,
        format: valueFormat,
      },
      {
        field: 'jan',
        title: headerMap[1] || 'Jan',
        widthT: 120,
        minWidth: 110,
        type: 'number1',
        editable: true,
        format: valueFormat,
      },
      {
        field: 'feb',
        title: headerMap[2] || 'Feb',
        widthT: 120,
        minWidth: 110,
        type: 'number1',
        editable: true,
        format: valueFormat,
      },
      {
        field: 'mar',
        title: headerMap[3] || 'Mar',
        widthT: 120,
        minWidth: 110,
        type: 'number1',
        editable: true,
        format: valueFormat,
      },
      {
        field: 'remarks',
        title: 'Remarks',
        widthT: 180,
        minWidth: 140,
        type: 'textarea',
        editable: true,
      },
    ],
    [headerMap, valueFormat],
  )

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchData()
    } else {
      setRows([])
      setOriginalRows([])
    }
  }, [PLANT_ID, AOP_YEAR])

  const fetchData = async () => {
    setLoading(true)
    try {
      const response = await ConsumerDemandApiService.getConsumerDemand(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      let rawList = []
      if (Array.isArray(response?.data)) {
        rawList = response.data
      } else if (Array.isArray(response?.data?.consumerDemandDTOList)) {
        rawList = response.data.consumerDemandDTOList
      } else if (Array.isArray(response?.data?.mcuNormsValueDTOList)) {
        rawList = response.data.mcuNormsValueDTOList
      } else if (Array.isArray(response)) {
        rawList = response
      }

      const formattedData = (rawList || []).map((item, index) => {
        const category =
          item?.normTypeName ||
          item?.consumerType ||
          item?.category ||
          'Consumer Demand'

        const unitName =
          item?.displayName ||
          item?.name ||
          item?.consumerUnit ||
          item?.consumersUnit ||
          item?.productName ||
          item?.unitName ||
          ''

        const actualVal =
          item?.previousFYAvg !== undefined && item?.previousFYAvg !== null
            ? item.previousFYAvg
            : item?.actualConsumption ?? 0

        const aprVal =
          item?.apr !== undefined && item?.apr !== null
            ? item.apr
            : item?.april ?? 0
        const mayVal =
          item?.may !== undefined && item?.may !== null ? item.may : 0
        const junVal =
          item?.jun !== undefined && item?.jun !== null
            ? item.jun
            : item?.june ?? 0
        const julVal =
          item?.jul !== undefined && item?.jul !== null
            ? item.jul
            : item?.july ?? 0
        const augVal =
          item?.aug !== undefined && item?.aug !== null
            ? item.aug
            : item?.august ?? 0
        const sepVal =
          item?.sep !== undefined && item?.sep !== null
            ? item.sep
            : item?.september ?? item?.sept ?? 0
        const octVal =
          item?.oct !== undefined && item?.oct !== null
            ? item.oct
            : item?.october ?? 0
        const novVal =
          item?.nov !== undefined && item?.nov !== null
            ? item.nov
            : item?.november ?? 0
        const decVal =
          item?.dec !== undefined && item?.dec !== null
            ? item.dec
            : item?.december ?? 0
        const janVal =
          item?.jan !== undefined && item?.jan !== null
            ? item.jan
            : item?.january ?? 0
        const febVal =
          item?.feb !== undefined && item?.feb !== null
            ? item.feb
            : item?.february ?? 0
        const marVal =
          item?.mar !== undefined && item?.mar !== null
            ? item.mar
            : item?.march ?? 0

        const itemType =
          item?.type !== undefined && item?.type !== null && String(item.type).trim() !== ''
            ? String(item.type).trim()
            : 'Consumer Demand'

        const rowId = item?.id || item?.normParameterFKId || index + 1

        return {
          ...item,
          id: rowId,
          srNo:
            item?.srNo !== undefined && item?.srNo !== null
              ? item.srNo
              : index + 1,
          normParameterFKId: item?.normParameterFKId || null,
          name: item?.name || unitName,
          displayName: item?.displayName || unitName,
          consumerUnit: unitName,
          consumersUnit: unitName,
          productName: unitName,
          actualConsumption: actualVal,
          previousFYAvg: actualVal,
          UOM: item?.uom || item?.UOM || '',
          uom: item?.uom || item?.UOM || '',
          consumerType: category,
          normTypeName: category,
          remarks: item?.remarks || '',
          originalRemark: item?.remarks || '',
          apr: aprVal,
          april: aprVal,
          may: mayVal,
          jun: junVal,
          june: junVal,
          jul: julVal,
          july: julVal,
          aug: augVal,
          august: augVal,
          sep: sepVal,
          september: sepVal,
          oct: octVal,
          october: octVal,
          nov: novVal,
          november: novVal,
          dec: decVal,
          december: decVal,
          jan: janVal,
          january: janVal,
          feb: febVal,
          february: febVal,
          mar: marVal,
          march: marVal,
          auditYear: item?.auditYear || AOP_YEAR,
          displayOrder: item?.displayOrder ?? index + 1,
          isEditable: item?.isEditable !== undefined ? item.isEditable : true,
          type: itemType,
        }
      })

      // Retain any pending edits for unsaved grids
      const mergedData = formattedData.map((row) => {
        if (modifiedCellsRef.current && modifiedCellsRef.current[row.id]) {
          return { ...row, ...modifiedCellsRef.current[row.id] }
        }
        return row
      })

      setRows(mergedData)
      setOriginalRows(formattedData)
    } catch (error) {
      console.error('Error fetching consumer demand data:', error)
      setRows([])
      setOriginalRows([])
    } finally {
      setLoading(false)
    }
  }

  // Dynamically group rows by the 'type' field and order them
  const { distinctTypes, groupedRows } = useMemo(() => {
    const groups = {}
    const typesOrder = []

    rows.forEach((row, index) => {
      const rawType =
        row.type !== undefined && row.type !== null && String(row.type).trim() !== ''
          ? String(row.type).trim()
          : 'Consumer Demand'

      const key = rawType.toLowerCase()
      if (!groups[key]) {
        groups[key] = {
          key,
          rawType,
          title: formatTypeTitle(rawType),
          items: [],
          firstIndex: index,
        }
        typesOrder.push(groups[key])
      }
      groups[key].items.push(row)
    })

    // Sort types: Utility Consumption first, Vendor second, followed by any others
    typesOrder.sort((a, b) => {
      const prioA = getTypeSortIndex(a.key)
      const prioB = getTypeSortIndex(b.key)
      if (prioA !== prioB) return prioA - prioB
      return a.firstIndex - b.firstIndex
    })

    return {
      distinctTypes: typesOrder,
      groupedRows: groups,
    }
  }, [rows])

  // Get modified cells filtered for a specific type key
  const getModifiedCellsForType = useCallback(
    (typeKey) => {
      const filtered = {}
      Object.entries(modifiedCells).forEach(([id, row]) => {
        if ((row?.type || '').trim().toLowerCase() === typeKey) {
          filtered[id] = row
        }
      })
      return filtered
    },
    [modifiedCells],
  )

  // Safe rows updater handling both functional and array updates
  const handleSetRows = useCallback((updater) => {
    setRows((prev) => {
      if (typeof updater === 'function') {
        return updater(prev)
      } else if (Array.isArray(updater)) {
        const updaterMap = new Map(updater.map((r) => [r.id, r]))
        return prev.map((r) => updaterMap.get(r.id) || r)
      }
      return prev
    })
  }, [])

  // Safe modified cells updater
  const handleSetModifiedCells = useCallback((updater) => {
    setModifiedCells((prev) => {
      if (typeof updater === 'function') {
        return updater(prev)
      } else if (updater && typeof updater === 'object') {
        return { ...prev, ...updater }
      }
      return prev
    })
  }, [])

  const saveChanges = async (targetType) => {
    setLoading(true)

    const modifiedData = Object.values(modifiedCells)
    const dataForType = targetType
      ? modifiedData.filter(
          (row) =>
            (row?.type || '').trim().toLowerCase() ===
            targetType.trim().toLowerCase(),
        )
      : modifiedData

    if (dataForType.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    const data = dataForType.filter((row) => row.inEdit !== false)
    if (data.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    const fieldsToCheck = [
      'apr',
      'may',
      'jun',
      'jul',
      'aug',
      'sep',
      'oct',
      'nov',
      'dec',
      'jan',
      'feb',
      'mar',
      'april',
      'june',
      'july',
      'august',
      'september',
      'october',
      'november',
      'december',
      'january',
      'february',
      'march',
    ]

    const validationError = validateRowDataWithRemarks(
      data,
      originalRows,
      fieldsToCheck,
      'consumerUnit',
    )

    if (validationError) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: validationError,
        severity: 'error',
      })
      setLoading(false)
      return
    }

    const parseNum = (val) => {
      if (val === null || val === undefined || val === '') return null
      const n = Number(val)
      return isNaN(n) ? null : n
    }

    const payload = data.map((row) => ({
      normParameterFKId: row.normParameterFKId || null,
      name: row.name || row.consumerUnit,
      displayName: row.displayName || row.consumerUnit,
      uom: row.UOM || row.uom || '',
      normTypeName: row.normTypeName || row.consumerType || '',
      previousFYAvg: parseNum(row.previousFYAvg ?? row.actualConsumption),
      apr: parseNum(row.apr ?? row.april),
      may: parseNum(row.may),
      jun: parseNum(row.jun ?? row.june),
      jul: parseNum(row.jul ?? row.july),
      aug: parseNum(row.aug ?? row.august),
      sep: parseNum(row.sep ?? row.september ?? row.sept),
      oct: parseNum(row.oct ?? row.october),
      nov: parseNum(row.nov ?? row.november),
      dec: parseNum(row.dec ?? row.december),
      jan: parseNum(row.jan ?? row.january),
      feb: parseNum(row.feb ?? row.february),
      mar: parseNum(row.mar ?? row.march),
      auditYear: row.auditYear || AOP_YEAR,
      remarks: row.remarks || '',
      displayOrder: row.displayOrder ?? row.srNo ?? null,
      isEditable: row.isEditable ?? true,
      type: row.type || '',
    }))

    try {
      const response = await ConsumerDemandApiService.saveConsumerDemand(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        payload,
      )

      if (response?.code === 200 || response?.status === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Data saved successfully!',
          severity: 'success',
        })
        // Clear saved items of this type from modifiedCells
        setModifiedCells((prev) => {
          if (!targetType) return {}
          const next = { ...prev }
          Object.keys(next).forEach((key) => {
            if (
              (next[key]?.type || '').trim().toLowerCase() ===
              targetType.trim().toLowerCase()
            ) {
              delete next[key]
            }
          })
          return next
        })
        await fetchData()
      } else if (response?.code === 400 && response?.data) {
        const failedNames = Array.isArray(response.data)
          ? response.data
              .map((d) => d.displayName || d.name)
              .filter(Boolean)
              .join(', ')
          : ''
        setSnackbarOpen(true)
        setSnackbarData({
          message: failedNames
            ? `Partial Data Updated. Remarks required for: ${failedNames}`
            : 'Partial Data Updated. Please check remarks.',
          severity: 'warning',
        })
        await fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Error saving data!',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error saving consumer demand data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error saving data!',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleExport = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel export started!',
      severity: 'info',
    })

    try {
      const blob = await ConsumerDemandApiService.exportConsumerDemand(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )
      downloadBlobExcel(blob, EXCEL_NAME)

      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting consumer demand data:', error)
      setSnackbarData({
        message: 'Excel download failed. Please try again.',
        severity: 'error',
      })
    }
  }

  const handleImport = async (file) => {
    setLoading(true)
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Importing data...',
      severity: 'info',
    })

    try {
      const response = await ConsumerDemandApiService.importConsumerDemand(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        file,
      )
      if (response?.code === 200 || response?.status === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Uploaded Successfully!',
          severity: 'success',
        })
        setModifiedCells({})
        await fetchData()
      } else if (response?.code === 400 && response?.data) {
        downloadBase64Excel(response.data, 'Error File Consumer Demand.xlsx')
        setSnackbarOpen(true)
        setSnackbarData({
          message: 'Partial data saved. Error file downloaded.',
          severity: 'warning',
        })
        await fetchData()
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Import failed. Please try again.',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error importing consumer demand data:', error)
      setSnackbarData({
        message: 'Import failed. Please try again.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setActiveRemarkType((row.type || '').trim().toLowerCase())
    setRemarkDialogOpen(true)
  }

  const basePermissions = {
    showAction: true,
    addButton: false,
    deleteButton: false,
    editButton: true,
    saveBtn: true,
    allAction: true,
    showExport: false,
    showImport: false,
    showCalculate: false,
    showTitleNameBusiness: true,
    showTitle: true,
    showDropdown: false,
    remarksEditable: true,
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
      <LoaderBackdrop open={!!loading} />

      {distinctTypes.length === 0 ? (
        <AdvanceKendoTable
          columns={columns}
          rows={[]}
          setRows={handleSetRows}
          modifiedCells={{}}
          setModifiedCells={handleSetModifiedCells}
          title='Consumer Demand'
          permissions={{
            ...basePermissions,
            titleName: 'Consumer Demand',
            ExcelName: `Consumer_Demand_${AOP_YEAR}`,
          }}
          handleRemarkCellClick={handleRemarkCellClick}
          remarkDialogOpen={false}
          setRemarkDialogOpen={setRemarkDialogOpen}
          currentRemark=''
          setCurrentRemark={setCurrentRemark}
          currentRowId={null}
          setCurrentRowId={setCurrentRowId}
          saveChanges={() => saveChanges()}
          handleExport={handleExport}
          handleExcelUpload={handleImport}
          snackbarData={snackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          paginationConfig={{
            threshold: 100,
            buttonCount: 5,
            pageSizes: [10, 20, 50, 100],
            defaultPageSize: 100,
          }}
        />
      ) : (
        distinctTypes.map((group, index) => {
          const typeModifiedCells = getModifiedCellsForType(group.key)
          const gridPermissions = {
            ...basePermissions,
            titleName: group.title,
            ExcelName: `${group.title.replace(/\s+/g, '_')}_${AOP_YEAR}`,
          }

          return (
            <AdvanceKendoTable
              key={group.key}
              columns={columns}
              rows={group.items}
              setRows={handleSetRows}
              modifiedCells={typeModifiedCells}
              setModifiedCells={handleSetModifiedCells}
              title={gridPermissions.showTitle ? gridPermissions.titleName : ''}
              permissions={gridPermissions}
              handleRemarkCellClick={handleRemarkCellClick}
              remarkDialogOpen={
                remarkDialogOpen && activeRemarkType === group.key
              }
              setRemarkDialogOpen={setRemarkDialogOpen}
              currentRemark={currentRemark}
              setCurrentRemark={setCurrentRemark}
              currentRowId={currentRowId}
              setCurrentRowId={setCurrentRowId}
              saveChanges={() => saveChanges(group.rawType)}
              handleExport={handleExport}
              handleExcelUpload={handleImport}
              snackbarData={snackbarData}
              snackbarOpen={index === 0 ? snackbarOpen : false}
              setSnackbarOpen={setSnackbarOpen}
              setSnackbarData={setSnackbarData}
              paginationConfig={{
                threshold: 100,
                buttonCount: 5,
                pageSizes: [10, 20, 50, 100],
                defaultPageSize: 100,
              }}
            />
          )
        })
      )}
    </Box>
  )
}

export default ConsumerDemand
