import { Box } from '@mui/material'
import { useCallback, useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import getEnhancedAOPColDefs from 'components/data-tables/CommonHeader/kendo_ConfigHeader'
import KendoDataTables from './index'

const CrackerConfigOutput = () => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange, oldYear } = dataGridStore
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical || ''
  const lowerVertName = vertName.toLowerCase()

  const headerMap = generateHeaderNames(localStorage.getItem('year'))
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  // Pre-filled composition data with random values
  const compositionRows = [
    {
      id: 1,
      ParticularsType: 'Total Feed',
      particulars: 'Shale Ethane',
      uom: 'TPH',
      remarks:
        'Manual Entry - Provision to be provided for excel download/upload',
      april: 150.3,
      may: 162.7,
      june: 148.1,
      july: 155.0,
      aug: 160.4,
      sep: 149.9,
      oct: 158.2,
      nov: 151.6,
      dec: 153.8,
      jan: 147.5,
      feb: 152.0,
      march: 159.1,
    },
    {
      id: 2,
      ParticularsType: 'Total Feed',
      particulars: 'C2C3',
      uom: 'TPH',
      remarks: 'Manual Entry',
      april: 120.0,
      may: 115.5,
      june: 122.8,
      july: 118.3,
      aug: 123.7,
      sep: 119.1,
      oct: 121.4,
      nov: 117.9,
      dec: 125.2,
      jan: 118.6,
      feb: 121.0,
      march: 124.5,
    },
    {
      id: 3,
      ParticularsType: 'Total Feed',
      particulars: 'Imported Propane',
      uom: 'TPH',
      remarks: 'Manual Entry',
      april: 90,
      may: 95.2,
      june: 92.4,
      july: 94.8,
      aug: 98.1,
      sep: 93.5,
      oct: 97.3,
      nov: 96.7,
      dec: 94.0,
      jan: 95.6,
      feb: 93.9,
      march: 96.2,
    },
    {
      id: 4,
      ParticularsType: 'Total Feed',
      particulars: 'LPG feed',
      uom: 'TPH',
      remarks: 'Manual Entry',
      april: 130.5,
      may: 132.1,
      june: 128.9,
      july: 131.4,
      aug: 129.7,
      sep: 133.0,
      oct: 130.2,
      nov: 132.5,
      dec: 129.0,
      jan: 131.8,
      feb: 128.3,
      march: 134.6,
    },
    {
      id: 5,
      ParticularsType: 'Total Feed',
      particulars: 'PE recycle',
      uom: 'TPH',
      remarks: 'Manual Entry',
      april: 105.7,
      may: 108.3,
      june: 107.5,
      july: 109.2,
      aug: 106.9,
      sep: 110.1,
      oct: 108.4,
      nov: 107.0,
      dec: 109.6,
      jan: 106.2,
      feb: 108.8,
      march: 107.3,
    },
    {
      id: 6,
      ParticularsType: 'Total Feed',
      particulars: 'FCC C3',
      uom: 'TPH',
      remarks: 'Manual Entry',
      april: 140.2,
      may: 138.4,
      june: 141.6,
      july: 139.1,
      aug: 142.5,
      sep: 137.8,
      oct: 140.9,
      nov: 139.7,
      dec: 141.0,
      jan: 138.5,
      feb: 142.3,
      march: 139.9,
    },
    {
      id: 7,
      ParticularsType: 'Total Feed',
      particulars: 'PP recycle',
      uom: 'TPH',
      remarks: 'Manual Entry',
      april: 115.9,
      may: 117.4,
      june: 116.2,
      july: 118.8,
      aug: 114.7,
      sep: 119.0,
      oct: 116.5,
      nov: 117.8,
      dec: 115.3,
      jan: 118.1,
      feb: 116.7,
      march: 117.2,
    },
    {
      id: 8,
      ParticularsType: 'Total Feed',
      particulars: 'Hexene',
      uom: 'TPH',
      remarks: 'Manual Entry',
      april: 95.8,
      may: 97.3,
      june: 96.1,
      july: 98.0,
      aug: 94.9,
      sep: 97.6,
      oct: 95.2,
      nov: 96.8,
      dec: 94.5,
      jan: 97.1,
      feb: 95.4,
      march: 98.3,
    },
    {
      id: 9,
      ParticularsType: 'Total Products',
      particulars: 'Residue gas',
      uom: 'TPH',
      remarks: 'Manual Entry',
      april: 210.4,
      may: 212.9,
      june: 209.7,
      july: 213.5,
      aug: 208.8,
      sep: 214.2,
      oct: 211.0,
      nov: 213.1,
      dec: 210.2,
      jan: 212.5,
      feb: 209.3,
      march: 214.8,
    },
    {
      id: 10,
      ParticularsType: 'Total Products',
      particulars: 'Fuel gas captive',
      uom: 'TPH',
      remarks: 'Manual Entry',
      april: 180.7,
      may: 182.5,
      june: 179.4,
      july: 183.1,
      aug: 178.9,
      sep: 184.6,
      oct: 181.3,
      nov: 183.8,
      dec: 180.2,
      jan: 182.0,
      feb: 179.8,
      march: 184.1,
    },
    {
      id: 11,
      ParticularsType: 'Total Products',
      particulars: 'Fuel gas export',
      uom: 'TPH',
      remarks: 'Manual Entry',
      april: 190.1,
      may: 191.8,
      june: 188.6,
      july: 192.4,
      aug: 187.2,
      sep: 193.9,
      oct: 190.5,
      nov: 192.7,
      dec: 189.3,
      jan: 191.6,
      feb: 188.2,
      march: 193.0,
    },
    {
      id: 12,
      ParticularsType: 'Total Products',
      particulars: 'Fuel gas import',
      uom: 'TPH',
      remarks: 'Manual Entry',
      april: 170.3,
      may: 172.0,
      june: 169.1,
      july: 173.2,
      aug: 168.5,
      sep: 174.0,
      oct: 170.8,
      nov: 173.4,
      dec: 169.9,
      jan: 172.2,
      feb: 169.5,
      march: 174.6,
    },
    {
      id: 13,
      ParticularsType: 'Total Products',
      particulars: 'CH4 product',
      uom: 'TPH',
      remarks: 'Manual Entry',
      april: 160.8,
      may: 162.3,
      june: 159.6,
      july: 163.7,
      aug: 158.2,
      sep: 164.1,
      oct: 161.0,
      nov: 163.5,
      dec: 160.4,
      jan: 162.9,
      feb: 159.2,
      march: 164.8,
    },
    {
      id: 14,
      ParticularsType: 'Total Products',
      particulars: 'Ethylene',
      uom: 'TPH',
      remarks: 'Manual Entry',
      april: 220.5,
      may: 222.7,
      june: 219.4,
      july: 223.8,
      aug: 218.1,
      sep: 224.5,
      oct: 221.3,
      nov: 223.9,
      dec: 220.6,
      jan: 222.0,
      feb: 219.7,
      march: 225.2,
    },
    {
      id: 15,
      ParticularsType: 'Total Products',
      particulars: 'Propylene',
      uom: 'TPH',
      remarks: 'Manual Entry',
      april: 200.2,
      may: 202.5,
      june: 199.3,
      july: 203.6,
      aug: 198.4,
      sep: 204.7,
      oct: 201.1,
      nov: 203.8,
      dec: 200.9,
      jan: 202.4,
      feb: 199.0,
      march: 205.5,
    },
    {
      id: 16,
      ParticularsType: 'Total Products',
      particulars: 'C4 mix',
      uom: 'TPH',
      remarks: 'Manual Entry',
      april: 230.1,
      may: 232.4,
      june: 229.0,
      july: 233.2,
      aug: 228.3,
      sep: 234.6,
      oct: 231.5,
      nov: 233.7,
      dec: 230.2,
      jan: 232.8,
      feb: 229.5,
      march: 235.9,
    },
    {
      id: 17,
      ParticularsType: 'Total Products',
      particulars: 'RARFS',
      uom: 'TPH',
      remarks: 'Manual Entry',
      april: 175.4,
      may: 177.2,
      june: 174.1,
      july: 178.5,
      aug: 173.8,
      sep: 179.0,
      oct: 175.9,
      nov: 178.3,
      dec: 175.0,
      jan: 177.6,
      feb: 174.2,
      march: 179.3,
    },
    {
      id: 18,
      ParticularsType: 'Total Products',
      particulars: 'Mixed oil',
      uom: 'TPH',
      remarks: 'Manual Entry',
      april: 195.7,
      may: 197.1,
      june: 194.0,
      july: 198.3,
      aug: 193.5,
      sep: 199.2,
      oct: 196.4,
      nov: 198.0,
      dec: 195.2,
      jan: 197.5,
      feb: 194.8,
      march: 199.6,
    },
    {
      id: 19,
      ParticularsType: 'Total Products',
      particulars: 'Acetylene',
      uom: 'TPH',
      remarks: 'Manual Entry',
      april: 185.0,
      may: 186.9,
      june: 183.7,
      july: 187.4,
      aug: 182.2,
      sep: 188.1,
      oct: 185.8,
      nov: 187.3,
      dec: 184.5,
      jan: 186.2,
      feb: 183.4,
      march: 188.7,
    },
    {
      id: 20,
      ParticularsType: 'Miscellaneous Parameters',
      particulars: 'Residue gas Calorific value',
      uom: 'Kcal/ton',
      remarks: 'Manual Entry',
      april: 10_500,
      may: 10_800,
      june: 10_600,
      july: 10_900,
      aug: 10_700,
      sep: 10_850,
      oct: 10_650,
      nov: 10_780,
      dec: 10_620,
      jan: 10_750,
      feb: 10_580,
      march: 10_900,
    },
  ]

  const [compositionRowsDummy, setCompositionRows] = useState([])

  // Fetch composition data once on mount
  const fetchCrackerRows = useCallback(() => {
    setTimeout(() => {
      const sorted = [...compositionRows].sort((a, b) => {
        const rankA = typeRank[a.ParticularsType] || 99
        const rankB = typeRank[b.ParticularsType] || 99
        return rankA - rankB
      })
      setCompositionRows(sorted)
    }, 500)
  }, [])

  useEffect(() => {
    fetchCrackerRows()
  }, [fetchCrackerRows])

  const getAdjustedPermissions = (permissions, isOldYear) => {
    if (isOldYear !== 1) return permissions
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
      isOldYear,
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
      showModes: lowerVertName === 'cracker',
      saveWithRemark: true,
      saveBtn: true,
      allAction: lowerVertName === 'cracker',
      modes: ['5F', '4F', '4F+D'],
    },
    isOldYear,
  )

  const NormParameterIdCell = (props) => {
    return <td>{props.dataItem.particulars}</td>
  }

  const productionColumns = getEnhancedAOPColDefs({
    headerMap,
    handleRemarkCellClick,
    configType: 'cracker_composition',
  })
  const typeRank = {
    'Total Feed': 1,
    'Total Products': 2,
    'Miscellaneous Parameters': 3,
  }

  return (
    <Box>
      <KendoDataTables
        rows={compositionRowsDummy}
        setRows={setCompositionRows}
        fetchData={fetchCrackerRows}
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
        typeRank={typeRank}
      />
    </Box>
  )
}

export default CrackerConfigOutput
