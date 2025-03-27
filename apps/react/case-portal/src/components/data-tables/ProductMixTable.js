import React, { useEffect, useState } from 'react'
import ASDataGrid from './ASDataGrid'
import { DataService } from 'services/DataService'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { useSession } from 'SessionStoreContext'
import { useSelector } from 'react-redux'
import getEnhancedColDefsAOP from './CommonHeader/ProductionAopHeader'
import getEnhancedColDefs from './CommonHeader/index'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import getEnhancedProductionColDefs from './CommonHeader/ProductionVolumeHeader'
import NumericInputOnly from 'utils/NumericInputOnly'
import dayjs from 'dayjs'
import { truncateRemarks } from 'utils/remarksUtils'
import {
  Tooltip,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Stepper,
  Step,
  StepLabel,
} from '@mui/material'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'

// import { validateFields } from 'utils/validationUtils'

const headerMap = generateHeaderNames()

const FiveTables = () => {
  const keycloak = useSession()
  const [loading, setLoading] = useState(false)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  // State variables for each table's data
  const [businessDemandData, setBusinessDemandData] = useState([])
  const [productionVolumeData, setProductionVolume] = useState([])
  const [shutdownData, setShutdownData] = useState([])
  const [slowDownData, setSlowdownData] = useState([])
  const [allProducts, setAllProducts] = useState([])
  const [otherTableData1, setOtherTableData1] = useState([])
  const [otherTableData2, setOtherTableData2] = useState([])
  const [otherTableData3, setOtherTableData3] = useState([])
  const [otherTableData4, setOtherTableData4] = useState([])
  const [activeStep, setActiveStep] = useState(0)
  const steps = [
    'Draft',
    'Validated',
    'Approved',
    'Activated',
    'Completed',
    'Closed',
  ]
  useEffect(() => {
    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProducts(
          keycloak,
          // lowerVertName === 'meg' ? 'Production' : 'Grade',
          null,
        )
        const allowedIds = [
          '4D8E17F6-D6CB-407E-8C9C-4BEDBC422C57',
          '00DC05B1-9607-470E-A159-62497E0123E2',
          'A061E050-0281-421F-81C1-B136CE2ED3F3',
          '92E0AF06-9535-4B93-8998-E56A71354393',
        ]

        const productList = data
          .filter((product) => allowedIds.includes(product.id))
          .map((product) => ({
            id: product.id,
            displayName: product.displayName,
          }))

        setAllProducts(productList)
      } catch (error) {
        console.error('Error fetching product:', error)
      } finally {
        // handleMenuClose();
      }
    }
    // fetchData()
    getAllProducts()
  }, [sitePlantChange, keycloak, lowerVertName])
  const findSum = (value, row) => {
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
    const values = months.map((month) => Number(row[month]) || 0)
    const sum = values.reduce((acc, val) => acc + val, 0)
    const total = sum.toFixed(2)
    return total === '0.00' ? null : total
  }
  const findAvg = (value, row) => {
    const months = [
      'april',
      'may',
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

    const values = months.map((month) => row[month] || 0)
    const sum = values.reduce((acc, val) => acc + val, 0)
    const avg = (sum / values.length).toFixed(2)

    return avg === '0.00' ? null : avg
  }
  const findDuration = (value, row) => {
    if (row && row.maintStartDateTime && row.maintEndDateTime) {
      const start = new Date(row.maintStartDateTime)
      const end = new Date(row.maintEndDateTime)

      if (!isNaN(start.getTime()) && !isNaN(end.getTime())) {
        // Check if dates are valid
        const durationInMs = end - start

        // Calculate duration in hours and minutes
        const durationInHours = Math.floor(durationInMs / (1000 * 60 * 60))
        const remainingMs = durationInMs % (1000 * 60 * 60)
        const durationInMinutes = Math.floor(remainingMs / (1000 * 60))

        // Format the duration as "HH:MM"
        const formattedDuration = `${String(durationInHours).padStart(2, '0')}:${String(durationInMinutes).padStart(2, '0')}`
        return formattedDuration
      } else {
        return '' // Or handle invalid dates as needed
      }
    } else {
      return '' // Or handle missing dates as needed
    }
  }
  const businessDemandColumns = getEnhancedColDefs({
    allProducts,
    headerMap,
    // handleRemarkCellClick,
  })
  const productionColumns = getEnhancedColDefsAOP({
    allProducts,
    headerMap,
    // handleRemarkCellClick,
    findSum,
  })
  const productionVolume = getEnhancedProductionColDefs({
    headerMap,
    allProducts,
    // handleRemarkCellClick,
    findAvg,
  })
  const shutdownColumns = [
    {
      field: 'discription',
      headerName: 'Shutdown Desc',
      minWidth: 125,
      editable: true,
      flex: 3,
    },
    {
      field: 'maintenanceId',
      headerName: 'maintenanceId',
      editable: false,
      hide: true,
    },

    {
      field: 'maintStartDateTime',
      headerName: 'SD- From',
      type: 'dateTime',
      minWidth: 175,
      editable: true,
      valueGetter: (params) => {
        const value = params
        const parsedDate = value
          ? dayjs(value, 'D MMM, YYYY, h:mm:ss A').toDate()
          : null
        return parsedDate
      },
    },

    {
      field: 'maintEndDateTime',
      headerName: 'SD- To',
      type: 'dateTime',
      editable: true,
      minWidth: 175,
      valueGetter: (params) => {
        const value = params
        const parsedDate = value
          ? dayjs(value, 'D MMM, YYYY, h:mm:ss A').toDate()
          : null
        return parsedDate
      },
    },
    {
      field: 'durationInHrs',
      headerName: 'Duration (hrs)',
      editable: false,
      minWidth: 100,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
      // valueGetter: (params) => params?.durationInHrs || 0,
      valueGetter: findDuration,
    },
    {
      field: 'remark',
      headerName: 'Remark',
      minWidth: 250,
      editable: true,
      renderCell: (params) => {
        const displayText = truncateRemarks(params.value)
        const isEditable = !params.row.Particulars

        return (
          <Tooltip title={params.value || ''} arrow>
            <div
              style={{
                cursor: 'pointer',
                color: params.value ? 'inherit' : 'gray',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
                maxWidth: 140,
              }}
              onClick={() => handleRemarkCellClick(params.row)}
            >
              {displayText || (isEditable ? 'Click to add remark' : '')}
            </div>
          </Tooltip>
        )
      },
    },
  ]
  const slowdownColumns = [
    {
      field: 'discription',
      headerName: 'Slowdown Desc',
      minWidth: 250,
      editable: true,
      flex: 3,
    },

    {
      field: 'maintenanceId',
      headerName: 'maintenanceId',
      editable: false,
      hide: true,
    },

    {
      field: 'product',
      headerName: 'Particulars',
      editable: true,
      minWidth: 125,
      valueGetter: (params) => params || '',
      valueFormatter: (params) => {
        const product = allProducts.find((p) => p.id === params)
        return product ? product.displayName : ''
      },
      renderEditCell: (params) => {
        const { value, id, api } = params

        const existingValues = new Set(
          [...api.getRowModels().values()]
            .filter((row) => row.id !== id)
            .map((row) => row.product),
        )

        return (
          <select
            value={value || ''}
            onChange={(event) => {
              params.api.setEditCellValue({
                id: params.id,
                field: 'product',
                value: event.target.value,
              })
            }}
            style={{
              width: '100%',
              padding: '5px',
              border: 'none',
              outline: 'none',
              background: 'transparent',
            }}
          >
            <option value='' disabled>
              Select
            </option>
            {allProducts
              .filter(
                (product) =>
                  product.id === value || !existingValues.has(product.id),
              ) // Ensure selected value is included
              .map((product) => (
                <option key={product.id} value={product.id}>
                  {product.displayName}
                </option>
              ))}
          </select>
        )
      },
    },

    {
      field: 'maintStartDateTime',
      headerName: 'SD- From',
      type: 'dateTime',
      minWidth: 200,
      editable: true,
      valueGetter: (params) => {
        const value = params
        const parsedDate = value
          ? dayjs(value, 'D MMM, YYYY, h:mm:ss A').toDate()
          : null
        return parsedDate
      },
    },

    {
      field: 'maintEndDateTime',
      headerName: 'SD- To',
      type: 'dateTime',
      minWidth: 200,
      editable: true,
      valueGetter: (params) => {
        const value = params
        const parsedDate = value
          ? dayjs(value, 'D MMM, YYYY, h:mm:ss A').toDate()
          : null
        return parsedDate
      },
    },

    {
      field: 'durationInHrs',
      headerName: 'Duration (hrs)',
      editable: true,
      minWidth: 75,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
      // valueGetter: findDuration,
    },

    {
      field: 'rate',
      headerName: 'Rate',
      editable: true,
      minWidth: 75,
      renderEditCell: NumericInputOnly,
      align: 'left',
      headerAlign: 'left',
    },

    {
      field: 'remark',
      headerName: 'Remarks',
      editable: true,
      minWidth: 250,
      renderCell: (params) => {
        const displayText = truncateRemarks(params.value)
        const isEditable = !params.row.Particulars

        return (
          <Tooltip title={params.value || ''} arrow>
            <div
              style={{
                cursor: 'pointer',
                color: params.value ? 'inherit' : 'gray',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
                maxWidth: 140,
              }}
              onClick={() => handleRemarkCellClick(params.row)}
            >
              {displayText || (isEditable ? 'Click to add remark' : '')}
            </div>
          </Tooltip>
        )
      },
    },
  ]

  const otherTableColumns2 = [
    // Define columns specific to the third table
  ]

  const otherTableColumns3 = [
    // Define columns specific to the fourth table
  ]
  // ----- Production AOP States & Functions -----
  const [productionRows, setProductionRows] = useState([])
  const [selectedUnit, setSelectedUnit] = useState('Ton')
  //  const productionApiRef = useGridApiRef();

  // Helper function to calculate sum (as used in ProductionNorms)

  //  const fetchProductionData = async () => {

  //  };

  // Function to fetch data for Business Demand table
  const fetchBusinessDemandData = async () => {
    setLoading(true)
    try {
      const data = await DataService.getBDData(keycloak)
      // Process data if necessary
      setBusinessDemandData(data)
    } catch (error) {
      console.error('Error fetching Business Demand data:', error)
    }
    setLoading(false)
  }

  // Similar functions for other tables
  const fetchShutdownData = async () => {
    try {
      setLoading(true)
      const data = await DataService.getShutDownPlantData(keycloak)
      const formattedData = data.map((item, index) => ({
        ...item,
        idFromApi: item?.id,
        id: index,
      }))
      // setShutdownData(formattedData)
      setShutdownData(formattedData)
      setLoading(false)
    } catch (error) {
      console.error('Error fetching Shutdown data:', error)
      setLoading(false)
    }
  }

  const fetchOtherTableData2 = async () => {
    try {
      setLoading(true)
      const data = await DataService.getAOPMCCalculatedData(keycloak)
      // const data = data1.slice(0, 3)
      const formattedData = data.map((item, index) => {
        const isTPD = selectedUnit == 'TPD'
        return {
          ...item,
          idFromApi: item?.id,
          normParametersFKId: item?.materialFKId.toLowerCase(),
          id: index,

          ...(isTPD && {
            april: item.april
              ? (item.april / 24).toFixed(2)
              : item.april || null,
            may: item.may ? (item.may / 24).toFixed(2) : item.may || null,
            june: item.june ? (item.june / 24).toFixed(2) : item.june || null,
            july: item.july ? (item.july / 24).toFixed(2) : item.july || null,
            august: item.august
              ? (item.august / 24).toFixed(2)
              : item.august || null,
            september: item.september
              ? (item.september / 24).toFixed(2)
              : item.september || null,
            october: item.october
              ? (item.october / 24).toFixed(2)
              : item.october || null,
            november: item.november
              ? (item.november / 24).toFixed(2)
              : item.november || null,
            december: item.december
              ? (item.december / 24).toFixed(2)
              : item.december || null,
            january: item.january
              ? (item.january / 24).toFixed(2)
              : item.january || null,
            february: item.february
              ? (item.february / 24).toFixed(2)
              : item.february || null,
            march: item.march
              ? (item.march / 24).toFixed(2)
              : item.march || null,
          }),
        }
      })
      // setProductNormData(formattedData)
      setProductionVolume(formattedData)
      setLoading(false)
    } catch (error) {
      console.error('Error fetching data:', error)
      setLoading(false)
    }
  }

  const fetchOtherTableData3 = async () => {
    setLoading(true)
    try {
      const data = await DataService.getSlowDownPlantData(keycloak)

      const formattedData = data.map((item, index) => ({
        ...item,
        idFromApi: item?.maintenanceId || item?.id,
        id: index,
      }))
      // setSlowDownData(formattedData)
      setSlowdownData(formattedData)
      setLoading(false) // Hide loading
    } catch (error) {
      console.error('Error fetching SlowDown data:', error)
      setLoading(false) // Hide loading
    }
  }

  const fetchOtherTableData4 = async () => {
    try {
      setLoading(true)
      const data1 = await DataService.getAOPData(keycloak)
      // Re-map data as done in ProductionNorms component
      const data2 = data1
        ?.map((product) => ({
          ...product,
          normParametersFKId: product.materialFKId,
          idFromApi: product.id,
        }))
        .map((item, index) => {
          const isKiloTon = selectedUnit !== 'Ton'
          return {
            ...item,
            id: index,
            ...(isKiloTon && {
              jan: item.jan ? item.jan / 1000 : item.jan,
              feb: item.feb ? item.feb / 1000 : item.feb,
              march: item.march ? item.march / 1000 : item.march,
              april: item.april ? item.april / 1000 : item.april,
              may: item.may ? item.may / 1000 : item.may,
              june: item.june ? item.june / 1000 : item.june,
              july: item.july ? item.july / 1000 : item.july,
              aug: item.aug ? item.aug / 1000 : item.aug,
              sep: item.sep ? item.sep / 1000 : item.sep,
              oct: item.oct ? item.oct / 1000 : item.oct,
              nov: item.nov ? item.nov / 1000 : item.nov,
              dec: item.dec ? item.dec / 1000 : item.dec,
            }),
          }
        })
      // For demonstration, slice the data if needed
      setProductionRows(data2.slice(0, 3))
    } catch (error) {
      console.error('Error fetching Production AOP data:', error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchBusinessDemandData()
    fetchShutdownData()
    fetchOtherTableData2()
    fetchOtherTableData3()
    fetchOtherTableData4()
  }, [sitePlantChange, keycloak])

  // Array of table configurations
  const tables = [
    {
      title: 'Business Demand',
      columns: businessDemandColumns,
      rows: businessDemandData,
    },
    {
      title: 'Production Volume Data',
      columns: productionVolume,
      rows: productionVolumeData,
    },
    {
      title: 'Shutdown Activities',
      columns: shutdownColumns,
      rows: shutdownData,
    },
    {
      title: 'Slowdown Activities',
      columns: slowdownColumns,
      rows: slowDownData,
    },
    {
      title: 'Production AOP',
      columns: productionColumns,
      rows: productionRows,
    },
  ]
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      <Backdrop open={loading} style={{ zIndex: 1000 }}>
        <CircularProgress color='inherit' />
      </Backdrop>

      {/* Stepper at the top */}
      <Stepper activeStep={activeStep} alternativeLabel>
        {steps.map((label, index) => (
          <Step key={label} onClick={() => setActiveStep(index)}>
            <StepLabel>{label}</StepLabel>
          </Step>
        ))}
      </Stepper>

      {tables.map((table, index) => (
        <Accordion key={index}>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            {table.title}
          </AccordionSummary>
          <AccordionDetails>
            <ASDataGrid
              // title={table.title}
              columns={table.columns}
              rows={table.rows}
              isCellEditable={() => false}
              permissions={{
                showAction: false,
                addButton: false,
                deleteButton: false,
                editButton: false,
                saveBtn: false,
                allAction: false,
              }}
            />
          </AccordionDetails>
        </Accordion>
      ))}
    </div>
  )
}

export default FiveTables
