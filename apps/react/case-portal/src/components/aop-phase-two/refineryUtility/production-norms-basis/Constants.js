import { useEffect, useState, useMemo, useCallback } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { useSelector } from 'react-redux'
import { ProductionNormsApiService } from 'components/aop-phase-two/services/refineryUtility/productionNormsApiService'
import { useSession } from 'SessionStoreContext'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import AdvanceKendoTable from '../../common/AdvanceKendoTable/index'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'
import { generateExcelName } from 'components/aop-phase-two/common/utilities/excelNameUtil'

const Constants = ({ startDate, endDate }) => {
  const keycloak = useSession()

  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [isTwoColumnPlant, setIsTwoColumnPlant] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { plantObject, year, siteObject } = dataGridStore
  const EXCEL_NAME = generateExcelName(
    dataGridStore,
    'Production_Norms_Basis_Constants',
  )
  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const AOP_YEAR = year?.selectedYear
  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const formatUomValue = useCallback(
    (val, uom, itemOrName, displayName) => {
      if (val === null || val === undefined || val === '') return ''
      const num = parseFloat(val)
      if (isNaN(num)) return val

      const siteName = siteObject?.name?.toUpperCase() || ''
      const plantName = plantObject?.name?.toUpperCase() || ''
      const isSpecialPlant =
        (siteName === 'DTA' && plantName === 'PCG ASU') ||
        (siteName === 'SEZ' && plantName === 'PCG ASU') ||
        (siteName === 'DTA' && plantName === 'AIR & ASU') ||
        (siteName === 'SEZ' && plantName === 'AIR & ASU') ||
        (siteName === 'C2' && plantName === 'ASU') ||
        (siteName === 'C2' && plantName === 'AIR')

      const isDtaCtPlant =
        siteName === 'DTA' &&
        (plantObject?.name === 'CT4 (734)' || plantObject?.name === 'CT6 (736)')
      const cleanUom = String(uom ?? '')
        .trim()
        .toLowerCase()

      const rawName =
        typeof itemOrName === 'object' && itemOrName !== null
          ? itemOrName?.Name || itemOrName?.name || ''
          : String(itemOrName ?? '')

      const rawDisplayName =
        typeof itemOrName === 'object' && itemOrName !== null
          ? itemOrName?.DisplayName ||
            itemOrName?.displayName ||
            itemOrName?.productName ||
            ''
          : String(displayName ?? '')

      const cleanName = rawName.trim().toLowerCase()
      const cleanDisplayName = rawDisplayName.trim().toLowerCase()

      const isCoc =
        cleanName.toLowerCase() === 'coc' ||
        cleanName.toLowerCase().startsWith('coc') ||
        cleanDisplayName.toLowerCase() === 'coc' ||
        cleanDisplayName.toLowerCase().startsWith('coc') ||
        cleanUom.toLowerCase() === 'coc'

      const isDmdSite =
        siteName === 'DMD' ||
        siteName.trim() === 'DMD' ||
        siteName.includes('DMD')

      const isCoolingWaterDesign =
        cleanName.includes('cooling water design') ||
        cleanDisplayName.includes('cooling water design')

      // 1 decimal conditions:
      // 1) COC for DTA site and CT4 / CT6 plants
      // 2) Name like 'Cooling Water Design' for DMD site
      if ((isCoc && isDtaCtPlant) || (isDmdSite && isCoolingWaterDesign)) {
        return (Math.trunc(num * 10) / 10).toFixed(1)
      }

      const isPcgAsuPlant =
        (siteName === 'DTA' || siteName === 'SEZ') && plantName === 'PCG ASU'

      const isCompressedAirFixedRatio =
        cleanName === 'compressed air fixed ratio' ||
        cleanDisplayName === 'compressed air fixed ratio' ||
        cleanName.includes('compressed air fixed ratio') ||
        cleanDisplayName.includes('compressed air fixed ratio')

      // 3 decimal conditions:
      // SEZ and DTA sites with PCG ASU plant for Compressed air fixed ratio
      if (isPcgAsuPlant && isCompressedAirFixedRatio) {
        return (Math.trunc(num * 1000) / 1000).toFixed(3)
      }

      if (isSpecialPlant) {
        if (cleanUom === '%' || cleanUom === 'm3/hr') {
          return Math.trunc(num).toString()
        }
        return (Math.trunc(num * 100) / 100).toFixed(2)
      } else {
        return Math.trunc(num).toString()
      }
    },
    [siteObject?.name, plantObject?.name],
  )

  useEffect(() => {
    const fetchPlantColumnConfig = async () => {
      if (!PLANT_ID) return
      try {
        const res = await ProductionNormsApiService.checkIsSummerWinterPlant(
          keycloak,
          PLANT_ID,
        )
        setIsTwoColumnPlant(
          Boolean(res?.data?.isSummerWinter ?? res?.data?.isTwoColumn),
        )
      } catch (err) {
        console.error('Error checking plant column config:', err)
        setIsTwoColumnPlant(false)
      }
    }
    if (PLANT_ID) {
      fetchPlantColumnConfig()
    }
  }, [PLANT_ID, keycloak])

  const columns = useMemo(() => {
    const baseCols = [
      {
        field: 'productName',
        title: 'Particulars',
        width: 300,
        minWidth: 250,
        widthT: 300,
        type: 'text',
        editable: false,
        hidden: false,
      },
      {
        field: 'UOM',
        title: 'UOM',
        width: 100,
        minWidth: 80,
        widthT: 100,
        type: 'text',
        editable: false,
      },
    ]

    if (isTwoColumnPlant) {
      baseCols.push(
        {
          field: 'apr',
          title: 'Summer',
          editable: true,
          width: 150,
          minWidth: 120,
          widthT: 150,
          align: 'left',
          headerAlign: 'left',
          type: 'uomWholeNumber',
        },
        {
          field: 'oct',
          title: 'Winter',
          editable: true,
          width: 150,
          minWidth: 120,
          widthT: 150,
          align: 'left',
          headerAlign: 'left',
          type: 'uomWholeNumber',
        },
      )
    } else {
      baseCols.push({
        field: 'apr',
        title: 'Value',
        editable: true,
        width: 150,
        minWidth: 120,
        widthT: 150,
        align: 'left',
        headerAlign: 'left',
        type: 'uomWholeNumber',
      })
    }

    baseCols.push({
      field: 'remarks',
      title: 'Remark',
      width: 300,
      minWidth: 250,
      widthT: 300,
      type: 'textarea',
      editable: true,
    })

    return baseCols
  }, [isTwoColumnPlant])

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchConstantsData()
    }
  }, [PLANT_ID, AOP_YEAR])

  const fetchConstantsData = async () => {
    setLoading(true)
    try {
      const res = await ProductionNormsApiService.refinaryConstantData(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
      )

      if (res?.data?.length === 0) {
        setRows([])
        return
      }

      // console.log('Constants data:', res)
      const formattedData = res?.data?.map((item, index) => {
        const uom = item?.UOM || item?.uom || ''
        const name = item?.Name || item?.name || ''
        const displayName =
          item?.DisplayName ||
          item?.displayName ||
          name ||
          item?.productName ||
          ''
        const rawApr =
          item?.apr !== undefined && item?.apr !== null
            ? item?.apr
            : item?.value ?? ''
        const rawOct =
          item?.oct !== undefined && item?.oct !== null ? item?.oct : ''
        const formattedApr = formatUomValue(rawApr, uom, item)
        const formattedOct =
          rawOct !== '' && rawOct !== null && rawOct !== undefined
            ? formatUomValue(rawOct, uom, item)
            : ''

        return {
          ...item,
          productName: displayName,
          DisplayName: displayName,
          Name: name,
          value: formattedApr,
          apr: formattedApr,
          oct: formattedOct,
          remarks: item?.remarks || '',
          id: item?.normParameterFKId || item?.id || index + 1,
          isConstantsTab: true,
        }
      })
      setRows(formattedData)
      setOriginalRows(formattedData)
    } catch (error) {
      console.error('Error fetching constants data:', error)
      setRows([])
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
    showExport: true,
    ExcelName: `Production_Norms_Constants_${AOP_YEAR}`,
    showImport: true,
    showTitleNameBusiness: true,
    showTitle: true,
    titleName: 'Constants',
  }

  const formatDateForAPI = (date) => {
    if (!date) return ''
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  const saveChanges = async () => {
    setLoading(true)

    // Validate required parameters
    if (!startDate || !endDate) {
      setSnackbarOpen(true)
      setSnackbarData({
        message:
          'Period dates are required. Please ensure dates are loaded from AOP Period Basis.',
        severity: 'error',
      })
      setLoading(false)
      return
    }

    if (!SITE_ID) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Site ID is required.',
        severity: 'error',
      })
      setLoading(false)
      return
    }

    const modifiedData = Object.values(modifiedCells)
    if (modifiedData.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    const data = modifiedData.filter((row) => row.inEdit)
    if (data.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'No Records to Save!',
        severity: 'info',
      })
      setLoading(false)
      return
    }

    const fieldsToCheck = isTwoColumnPlant ? ['apr', 'oct'] : ['value']
    const validationError = validateRowDataWithRemarks(
      data,
      originalRows,
      fieldsToCheck,
      'productName',
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

    // const payload = modifiedData
    const payload = modifiedData.map((row) => ({
      ...row,
      normParameterFKId: row.normParameterFKId,
      apr: row.apr !== undefined && row.apr !== '' ? Number(row.apr) : null,
      oct: row.oct !== undefined && row.oct !== '' ? Number(row.oct) : null,
      remarks: row.remarks || '',
      auditYear: row.auditYear || AOP_YEAR,
    }))
    try {
      const periodFrom = formatDateForAPI(startDate)
      const periodTo = formatDateForAPI(endDate)

      console.log('Saving constants data:', payload)

      const response = await ProductionNormsApiService.saveConstantsData(
        keycloak,
        AOP_YEAR,
        PLANT_ID,
        SITE_ID,
        periodFrom,
        periodTo,
        payload,
      )

      setModifiedCells({})
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Successfully saved ${modifiedData.length} changes!`,
        severity: 'success',
      })
      await fetchConstantsData()
    } catch (error) {
      console.error('Error saving constants data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to save changes. Please try again.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleExcelUpload = async (file) => {
    if (!file) return

    if (!startDate || !endDate) {
      setSnackbarOpen(true)
      setSnackbarData({
        message:
          'Period dates are required. Please ensure dates are loaded from AOP Period Basis.',
        severity: 'error',
      })
      return
    }

    setLoading(true)
    try {
      const periodFrom = formatDateForAPI(startDate)
      const periodTo = formatDateForAPI(endDate)

      const response = await ProductionNormsApiService.importConstantsExcel(
        file,
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        periodFrom,
        periodTo,
      )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Excel file imported successfully!',
          severity: 'success',
        })
        await fetchConstantsData()
      } else if (response?.code === 400 && response?.data) {
        try {
          const base64Data = response.data
          const binaryString = window.atob(base64Data)
          const bytes = new Uint8Array(binaryString.length)
          for (let i = 0; i < binaryString.length; i++) {
            bytes[i] = binaryString.charCodeAt(i)
          }
          const blob = new Blob([bytes], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          })
          const url = window.URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.href = url
          link.download = `Constants_Errors_${new Date().getTime()}.xlsx`
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
          window.URL.revokeObjectURL(url)

          setSnackbarOpen(true)
          setSnackbarData({
            message:
              response?.message ||
              'Import failed with errors. Please check the downloaded file.',
            severity: 'error',
          })
          await fetchConstantsData()
        } catch (downloadError) {
          console.error('Error downloading error file:', downloadError)
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Import failed but could not download error file.',
            severity: 'error',
          })
        }
      } else {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Failed to import Excel file.',
          severity: 'error',
        })
      }
    } catch (error) {
      console.error('Error uploading Excel file:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Failed to import Excel file: ${error.message}`,
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleExport = async () => {
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'info',
    })

    try {
      await ProductionNormsApiService.exportConstantsExcel(
        keycloak,
        PLANT_ID,
        AOP_YEAR,
        EXCEL_NAME,
      )
      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting Constants data:', error)
      setSnackbarData({
        message: 'Excel download failed. Please try again.',
        severity: 'error',
      })
    }
  }

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  const handleItemChange = useCallback(
    (e, setRows, setModifiedCells, setCustomModifiedCells) => {
      const { dataItem, field, value } = e
      if (field === 'apr' || field === 'oct' || field === 'value') {
        const uom = dataItem?.UOM || dataItem?.uom || ''
        const formatted = formatUomValue(value, uom, dataItem)
        if (formatted !== value) {
          setRows((prev) =>
            prev.map((r) =>
              r.id === dataItem.id ? { ...r, [field]: formatted } : r,
            ),
          )
          setModifiedCells((prev) => {
            const rowMod = prev[dataItem.id] || {}
            return {
              ...prev,
              [dataItem.id]: {
                ...rowMod,
                [field]: formatted,
              },
            }
          })
          if (setCustomModifiedCells) {
            setCustomModifiedCells((prev) => {
              const rowMod = prev[dataItem.id] || {}
              return {
                ...prev,
                [dataItem.id]: {
                  ...rowMod,
                  [field]: formatted,
                },
              }
            })
          }
        }
      }
    },
    [formatUomValue],
  )

  return (
    <Box>
      <LoaderBackdrop open={!!loading} />
      <AdvanceKendoTable
        columns={columns}
        rows={rows}
        setRows={setRows}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title={permissions.showTitle ? permissions.titleName : ''}
        permissions={permissions}
        handleRemarkCellClick={handleRemarkCellClick}
        customItemChange={handleItemChange}
        remarkDialogOpen={remarkDialogOpen}
        setRemarkDialogOpen={setRemarkDialogOpen}
        currentRemark={currentRemark}
        setCurrentRemark={setCurrentRemark}
        currentRowId={currentRowId}
        setCurrentRowId={() => {}}
        saveChanges={saveChanges}
        handleExcelUpload={handleExcelUpload}
        handleExport={handleExport}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        groupBy={['normTypeName']}
        paginationConfig={{
          threshold: 100,
          buttonCount: 5,
          pageSizes: [10, 20, 50, 100],
          defaultPageSize: 100,
        }}
      />
    </Box>
  )
}

export default Constants
