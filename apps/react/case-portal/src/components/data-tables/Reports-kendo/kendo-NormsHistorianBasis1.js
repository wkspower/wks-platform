import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import { Box } from '@mui/material'
import MuiAccordion from '@mui/material/Accordion'
import MuiAccordionDetails from '@mui/material/AccordionDetails'
import MuiAccordionSummary from '@mui/material/AccordionSummary'
import Backdrop from '@mui/material/Backdrop'
import CircularProgress from '@mui/material/CircularProgress'
import { styled } from '@mui/material/styles'
import Typography from '@mui/material/Typography'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useEffect, useState, useRef } from 'react'
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import {
  ExcelExport,
  ExcelExportColumn,
} from '@progress/kendo-react-excel-export'

import KendoDataGrid from 'components/Kendo-Report-DataGrid/index'
import getKendoNormsHistorianColumns from '../CommonHeader/KendoNormHistoryHeader'
import { Button } from '../../../../node_modules/@mui/material/index'
import { ColumnDefaultProps } from '../../../../node_modules/@progress/kendo-react-data-tools/index'
import {
  Grid,
  GridColumn,
  isColumnMenuFilterActive,
  isColumnMenuSortActive,
} from '../../../../node_modules/@progress/kendo-react-grid/index'
import { getColumnMenu1, getColumnMenuCheckboxFilter } from './ColumnMenu1'

const NormsHistorianBasis1 = () => {
  const keycloak = useSession()

  const [rowsHistorianValues, setHistorianValues] = useState([])

  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { sitePlantChange, verticalChange, yearChanged, oldYear } =
    dataGridStore
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'

  const [loading, setLoading] = useState(false)

  const fetchData = async (reportType, setState) => {
    try {
      setLoading(true)
      var data = []
      data = await DataService.getNormsHistorianBasis(keycloak, reportType)

      if (data?.code === 200) {
        const rowsWithId = data?.data?.normHistoricBasisData?.map(
          (item, index) => ({
            ...item,
            id: index,
            isEditable: false,
          }),
        )
        setLoading(false)
        setState(rowsWithId)
      } else {
        console.error(`Error fetching ${reportType} data`)
        setLoading(false)
      }
    } catch (error) {
      console.error(`Error fetching ${reportType} data:`, error)
      setLoading(false)
    }
  }

  const year = localStorage.getItem('year')
  const headerMap = generateHeaderNames(year)

  const colsHistorianValues1 = getKendoNormsHistorianColumns({
    headerMap,
    type: 'HistorianValues',
  })

  const colsHistorianValues = [
    {
      field: 'name',
      title: 'Type',
    },
    {
      field: 'particulars',
      title: 'Particulars',
    },
    {
      field: 'april',
      title: 'Particulars',
    },
    {
      field: 'may',
      title: 5,
    },
    {
      field: 'june',
      title: 6,
    },
    {
      field: 'july',
      title: 7,
    },
    {
      field: 'august',
      title: 8,
    },
    {
      field: 'september',
      title: 9,
    },
    {
      field: 'october',
      title: 10,
    },
    {
      field: 'november',
      title: 11,
    },
    {
      field: 'december',
      title: 12,
    },
    {
      field: 'january',
      title: 1,
    },
    {
      field: 'february',
      title: 2,
    },
    {
      field: 'march',
      title: 3,
    },
  ]

  useEffect(() => {
    fetchData('HistorianValues', setHistorianValues)
  }, [sitePlantChange, oldYear, yearChanged, keycloak, lowerVertName])

  const [filter, setFilter] = useState({ logic: 'and', filters: [] })
  const [sort, setSort] = useState([])

  const handleItemChange = (e) => {
    const updated = [...rowsHistorianValues]
    const index = updated.findIndex((r) => r.id === e.dataItem.id)

    if (index !== -1) {
      updated[index] = { ...updated[index], [e.field]: e.value }
      onRowChange?.(updated, e)
    }
  }

  const ColumnMenuCheckboxFilter =
    getColumnMenuCheckboxFilter(rowsHistorianValues)

  const isColumnActive = (field, filter, sort) => {
    return (
      isColumnMenuFilterActive(field, filter) ||
      isColumnMenuSortActive(field, sort)
    )
  }

  return (
    <div>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>

      <Box display='flex' flexDirection='column' gap={2}>
        <Box sx={{ width: '100%' }}>
          <div className='kendo-data-grid'>
            <Grid
              style={{ flex: 1, overflow: 'auto' }}
              data={rowsHistorianValues}
              dataItemKey='id'
              autoProcessData={true}
              sortable={true}
              scrollable='scrollable'
              filter={filter}
              onFilterChange={(e) => setFilter(e.filter)}
              onItemChange={handleItemChange}
              resizable={true}
              defaultSkip={0}
              defaultTake={100}
              contextMenu={true}
              pageable={
                rowsHistorianValues?.length > 100
                  ? {
                      buttonCount: 4,
                      pageSizes: [10, 50, 100],
                    }
                  : false
              }
              sort={sort}
              onSortChange={(e) => setSort(e.sort)}
            >
              {colsHistorianValues.map(({ field, title }) => (
                <GridColumn
                  key={field}
                  field={field}
                  title={title}
                  columnMenu={ColumnMenuCheckboxFilter}
                  headerClassName={
                    isColumnActive(field, filter, sort) ? 'active-column' : ''
                  }
                />
              ))}
            </Grid>
          </div>
        </Box>
      </Box>
    </div>
  )
}

export default NormsHistorianBasis1
