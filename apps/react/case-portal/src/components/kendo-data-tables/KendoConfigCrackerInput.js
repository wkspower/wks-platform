// CrackerConfig.jsx
import { Box, Tab, Tabs } from '@mui/material'
import { useCallback, useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import getEnhancedAOPColDefs from 'components/data-tables/CommonHeader/kendo_ConfigHeader'
import KendoDataTables from './index'

const CrackerConfig = () => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange, oldYear } = dataGridStore
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical
  const lowerVertName = vertName?.toLowerCase() || 'meg'
  //   const [snackbarData, setSnackbarData] = useState({
  //     message: '',
  //     severity: 'info',
  //   })
  // const [snackbarOpen, setSnackbarOpen] = useState(false)
  const headerMap = generateHeaderNames(localStorage.getItem('year'))
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const handleRemarkCellClick = (row) => {
    // if (!row?.isEditable) return

    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }
  // const [allProducts, setAllProducts] = useState([])

  const rawTabs = [
    'feed',
    'composition',
    'hydrogenation',
    'recovery',
    'optimizing',
    'furnace',
  ]
  const [rawTabIndex, setRawTabIndex] = useState(0)

  const feedRows = [
    {
      id: 1,
      particulars: 'Propane',
      uom: 'tpd',
      remarks: 'from planning team (1.8 TPH min is required)',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 2,
      particulars: 'C2/C3',
      uom: 'tpd',
      remarks: 'from planning team',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 3,
      particulars: 'Ethane',
      uom: 'tpd',
      remarks: 'from planning team',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 4,
      particulars: 'FCC C3',
      uom: 'tpd',
      remarks: 'from planning team',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 5,
      particulars: 'BPCL Kochi Propylene',
      uom: 'tpd',
      remarks: 'from planning team',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 6,
      particulars: 'LDPE off Gas',
      uom: 'tpd',
      remarks: 'PAS data for current financial year',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 7,
      particulars: 'PP off gas',
      uom: 'tpd',
      remarks: 'PAS data for current financial year',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 8,
      particulars: 'Hexene Purge Gas',
      uom: 'tpd',
      remarks: 'PAS data for current financial year',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 9,
      particulars: 'Additional feed (Future scope)',
      uom: '',
      remarks:
        '1) Add button will be required for new addition of rows.\n2) Option to be provided to get the export',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
  ]

  const compositionRows = [
    // —— C2/C3 (existing) ——
    {
      id: 1,
      ParticularsType: 'C2/C3',
      particulars: 'Methane',
      uom: 'Wt%',
      remarks: 'Manual Entry / Lab Tag for the current year',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 2,
      ParticularsType: 'C2/C3',
      particulars: 'Ethane',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 3,
      ParticularsType: 'C2/C3',
      particulars: 'Propane',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 4,
      ParticularsType: 'C2/C3',
      particulars: 'i-C4',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 5,
      ParticularsType: 'C2/C3',
      particulars: 'n-C4',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 6,
      ParticularsType: 'C2/C3',
      particulars: 'C5',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },

    // —— Hexene Purge Gas ——
    {
      id: 7,
      ParticularsType: 'Hexene Purge Gas',
      particulars: 'Ethane',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 8,
      ParticularsType: 'Hexene Purge Gas',
      particulars: 'Ethylene',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },

    // —— Import Propane ——
    {
      id: 9,
      ParticularsType: 'Import Propane',
      particulars: 'Methane',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 10,
      ParticularsType: 'Import Propane',
      particulars: 'Propylene',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 11,
      ParticularsType: 'Import Propane',
      particulars: 'Ethane',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 12,
      ParticularsType: 'Import Propane',
      particulars: 'Propane',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 13,
      ParticularsType: 'Import Propane',
      particulars: 'i-C4',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 14,
      ParticularsType: 'Import Propane',
      particulars: 'n-C4',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 15,
      ParticularsType: 'Import Propane',
      particulars: 'C5',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },

    // —— BPCL Kochi Propylene ——
    {
      id: 16,
      ParticularsType: 'BPCL Kochi Propylene',
      particulars: 'Propylene',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },

    // —— FCC C3 ——
    {
      id: 17,
      ParticularsType: 'FCC C3',
      particulars: 'Propane',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 18,
      ParticularsType: 'FCC C3',
      particulars: 'Propylene',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },

    // —— LDPE Off Gas ——
    {
      id: 19,
      ParticularsType: 'LDPE Off Gas',
      particulars: 'C2H4 Content',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 20,
      ParticularsType: 'LDPE Off Gas',
      particulars: 'C2H6 Content',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 21,
      ParticularsType: 'LDPE Off Gas',
      particulars: 'Propane',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 22,
      ParticularsType: 'LDPE Off Gas',
      particulars: 'Propylene',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },

    // —— Additional Feed (Default Composition) ——
    {
      id: 23,
      ParticularsType: 'Additional Feed (Default Composition)',
      particulars: 'H2',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 24,
      ParticularsType: 'Additional Feed (Default Composition)',
      particulars: 'C1',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 25,
      ParticularsType: 'Additional Feed (Default Composition)',
      particulars: 'C2',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 26,
      ParticularsType: 'Additional Feed (Default Composition)',
      particulars: 'C3',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 27,
      ParticularsType: 'Additional Feed (Default Composition)',
      particulars: 'C4',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
  ]

  const hydrogenationRows = [
    {
      id: 1,
      particulars: 'C3 Hdn selectivity',
      uom: '%',
      remarks: 'Manual Entry (remain same for each mode of operation)',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 2,
      particulars: 'MAPD Conversion',
      uom: '%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 3,
      particulars: 'C2 Hdn selectivity',
      uom: '%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 4,
      particulars: 'C2 Conversion',
      uom: '%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
  ]

  const recoveryRows = [
    {
      id: 1,
      particulars: 'Ethylene loss in RG',
      uom: 'ppmv',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 2,
      particulars: 'Ethylene in C2 spl bot',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 3,
      particulars: 'ethane in ethylene',
      uom: 'ppmw',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 4,
      particulars: 'C4 loss in DP O/h',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 5,
      particulars: 'Propylene in C3',
      uom: 'Wt%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 6,
      particulars: 'Propane in propylene Product',
      uom: 'ppmw',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 7,
      particulars: 'LDPE off gas to Fuel',
      uom: 'Kg/day',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
  ]

  const optimizingRows = [
    {
      id: 1,
      particulars: 'C2C3 feed',
      uom: 'tpd',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 2,
      particulars: 'Import Propane',
      uom: 'tpd',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 3,
      particulars: 'Ethane conversion',
      uom: '%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 4,
      particulars: 'Propane conversion',
      uom: '%',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 5,
      particulars: 'Propylene loss in Propane',
      uom: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 6,
      particulars: 'Zone on Ethane',
      uom: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 7,
      particulars: 'Additional Variable',
      uom: '',
      remarks: 'Add button will be required for new addition of rows.',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
  ]

  const furnaceRows = [
    {
      id: 1,
      particulars: 'Max Flow per Zone for Ethane',
      uom: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 2,
      particulars: 'Max Flow per Zone for Propane',
      uom: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 3,
      particulars: 'Max Ethane Conversion',
      uom: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 4,
      particulars: 'Max Propane Conversion',
      uom: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 5,
      particulars: 'Max CGC molar flow',
      uom: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 6,
      particulars: 'Max Caustic Tower rho *V2',
      uom: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 7,
      particulars: 'Max Furnace Duty',
      uom: '',
      remarks: 'Manual Entry',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
    {
      id: 8,
      particulars: 'Additional Constraint',
      uom: '',
      remarks: 'Add button will be required for new addition of rows.',
      jan: 100.0,
      feb: 100.0,
      march: 100.0,
      april: 200.0,
      may: 100.0,
      june: 100.0,
      july: 100.0,
      aug: 100.0,
      sep: 100.0,
      oct: 100.0,
      nov: 100.0,
      dec: 100.0,
    },
  ]

  const [feedRowsDummy, setFeedRows] = useState([])
  const [compositionRowsDummy, setCompositionRows] = useState([])
  const [hydrogenationRowsDummy, setHydrogenationRows] = useState([])
  const [recoveryRowsDummy, setRecoveryRows] = useState([])
  const [optimizingRowsDummy, setOptimizingRows] = useState([])
  const [furnaceRowsDummy, setFurnaceRows] = useState([])

  const fetchCrackerRows = useCallback((tab) => {
    // Simulate network delay
    setTimeout(() => {
      switch (tab) {
        case 'feed':
          setFeedRows(feedRows)
          break
        case 'composition':
          setCompositionRows(compositionRows)
          break
        case 'hydrogenation':
          setHydrogenationRows(hydrogenationRows)
          break
        case 'recovery':
          setRecoveryRows(recoveryRows)
          break
        case 'optimizing':
          setOptimizingRows(optimizingRows)
          break
        case 'furnace':
          setFurnaceRows(furnaceRows)
          break
        default:
          break
      }
    }, 500) // 500ms delay to mimic async
  }, [])

  // 5️⃣ Whenever the selected tab changes, reload that tab’s rows
  useEffect(() => {
    const currentTab = rawTabs[rawTabIndex]
    fetchCrackerRows(currentTab)
  }, [rawTabIndex, fetchCrackerRows])
  const getAdjustedPermissions = (permissions, isOldYear) => {
    if (isOldYear != 1) return permissions
    return {
      ...permissions,
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      showModes: false,
      saveWithRemark: false,
      saveBtn: false,
      isOldYear: isOldYear,
      allAction: false,
    }
  }

  const adjustedPermissions = getAdjustedPermissions(
    {
      showAction: false,
      addButton: false,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      showModes: lowerVertName === 'cracker' ? true : false,
      saveWithRemark: true,
      saveBtn: true,
      allAction: lowerVertName === 'cracker' ? true : false,
      modes: ['5F', '4F', '4F+D'],
    },
    isOldYear,
  )
  const NormParameterIdCell = (props) => {
    // console.log(props)
    // const productId = props.dataItem.normParameterFKId
    // const product = allProducts.find((p) => p.id === productId)
    // const displayName = product?.displayName || ''
    // console.log(displayName)
    return <td>{props?.dataItem?.particulars}</td>
  }

  const productionColumns = getEnhancedAOPColDefs({
    // allGradesReciepes,
    // allProducts,
    headerMap,
    handleRemarkCellClick,
    configType:
      rawTabs[rawTabIndex] === 'composition'
        ? 'cracker_composition'
        : 'cracker', // columnConfig,
  })
  return (
    <Box>
      <Tabs
        sx={{
          borderBottom: '0px solid #ccc',
          '.MuiTabs-indicator': { display: 'none' },
          // margin: '-35px 0px -8px 0%',
        }}
        textColor='primary'
        indicatorColor='primary'
        value={rawTabIndex}
        onChange={(e, newIndex) => setRawTabIndex(newIndex)}
      >
        {rawTabs.map((tabId) => (
          <Tab
            key={tabId}
            sx={{
              border: '1px solid #ADD8E6',
              borderBottom: '1px solid #ADD8E6',
              textTransform: 'capitalize',
            }}
            label={tabId}
          />
        ))}
      </Tabs>

      <Box>
        {(() => {
          switch (rawTabs[rawTabIndex]) {
            case 'feed':
              return (
                <Box>
                  <KendoDataTables
                    rows={feedRowsDummy}
                    setRows={setFeedRows}
                    fetchData={() => fetchCrackerRows('feed')}
                    configType='cracker'
                    handleRemarkCellClick={handleRemarkCellClick}
                    NormParameterIdCell={NormParameterIdCell}
                    columns={productionColumns}
                    remarkDialogOpen={remarkDialogOpen}
                    setRemarkDialogOpen={setRemarkDialogOpen}
                    currentRemark={currentRemark}
                    setCurrentRemark={setCurrentRemark}
                    currentRowId={currentRowId}
                    permissions={adjustedPermissions}
                  />
                </Box>
              )

            case 'composition':
              return (
                <Box>
                  <KendoDataTables
                    rows={compositionRowsDummy}
                    setRows={setCompositionRows}
                    fetchData={() => fetchCrackerRows('composition')}
                    configType='cracker_composition'
                    groupBy='ParticularsType'
                    handleRemarkCellClick={handleRemarkCellClick}
                    NormParameterIdCell={NormParameterIdCell}
                    columns={productionColumns}
                    remarkDialogOpen={remarkDialogOpen}
                    setRemarkDialogOpen={setRemarkDialogOpen}
                    currentRemark={currentRemark}
                    setCurrentRemark={setCurrentRemark}
                    currentRowId={currentRowId}
                    permissions={adjustedPermissions}
                  />
                </Box>
              )

            case 'hydrogenation':
              return (
                <Box>
                  <KendoDataTables
                    rows={hydrogenationRowsDummy}
                    setRows={setHydrogenationRows}
                    fetchData={() => fetchCrackerRows('hydrogenation')}
                    configType='cracker'
                    handleRemarkCellClick={handleRemarkCellClick}
                    NormParameterIdCell={NormParameterIdCell}
                    columns={productionColumns}
                    remarkDialogOpen={remarkDialogOpen}
                    setRemarkDialogOpen={setRemarkDialogOpen}
                    currentRemark={currentRemark}
                    setCurrentRemark={setCurrentRemark}
                    currentRowId={currentRowId}
                    permissions={adjustedPermissions}
                  />
                </Box>
              )

            case 'recovery':
              return (
                <Box>
                  <KendoDataTables
                    rows={recoveryRowsDummy}
                    setRows={setRecoveryRows}
                    fetchData={() => fetchCrackerRows('recovery')}
                    configType='cracker'
                    handleRemarkCellClick={handleRemarkCellClick}
                    NormParameterIdCell={NormParameterIdCell}
                    columns={productionColumns}
                    remarkDialogOpen={remarkDialogOpen}
                    setRemarkDialogOpen={setRemarkDialogOpen}
                    currentRemark={currentRemark}
                    setCurrentRemark={setCurrentRemark}
                    currentRowId={currentRowId}
                    permissions={adjustedPermissions}
                  />
                </Box>
              )

            case 'optimizing':
              return (
                <Box>
                  <KendoDataTables
                    rows={optimizingRowsDummy}
                    setRows={setOptimizingRows}
                    fetchData={() => fetchCrackerRows('optimizing')}
                    configType='cracker'
                    handleRemarkCellClick={handleRemarkCellClick}
                    NormParameterIdCell={NormParameterIdCell}
                    columns={productionColumns}
                    remarkDialogOpen={remarkDialogOpen}
                    setRemarkDialogOpen={setRemarkDialogOpen}
                    currentRemark={currentRemark}
                    setCurrentRemark={setCurrentRemark}
                    currentRowId={currentRowId}
                    permissions={adjustedPermissions}
                  />
                </Box>
              )

            case 'furnace':
              return (
                <Box>
                  <KendoDataTables
                    rows={furnaceRowsDummy}
                    setRows={setFurnaceRows}
                    fetchData={() => fetchCrackerRows('furnace')}
                    configType='cracker'
                    handleRemarkCellClick={handleRemarkCellClick}
                    NormParameterIdCell={NormParameterIdCell}
                    columns={productionColumns}
                    remarkDialogOpen={remarkDialogOpen}
                    setRemarkDialogOpen={setRemarkDialogOpen}
                    currentRemark={currentRemark}
                    setCurrentRemark={setCurrentRemark}
                    currentRowId={currentRowId}
                    permissions={adjustedPermissions}
                  />
                </Box>
              )

            default:
              return null
          }
        })()}
      </Box>
    </Box>
  )
}

export default CrackerConfig
