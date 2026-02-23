import { useState, useEffect } from 'react'
import { useSelector } from 'react-redux'
import { generateHeaderNames } from 'components/aop-phase-two/common/utilities/generateHeaders'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import AdvanceKendoTable from 'components/aop-phase-two/common/AdvanceKendoTable/index'

const generateMonthHours = (aopYear) => {
  if (!aopYear) return {}

  const [startYear, endYear] = aopYear.split('-').map((y) => parseInt(y))
  const fullStartYear = startYear < 100 ? 2000 + startYear : startYear
  const fullEndYear = endYear < 100 ? 2000 + endYear : endYear

  const getDaysInMonth = (month, year) => new Date(year, month, 0).getDate()

  return {
    apr: getDaysInMonth(4, fullStartYear) * 24,
    may: getDaysInMonth(5, fullStartYear) * 24,
    jun: getDaysInMonth(6, fullStartYear) * 24,
    jul: getDaysInMonth(7, fullStartYear) * 24,
    aug: getDaysInMonth(8, fullStartYear) * 24,
    sep: getDaysInMonth(9, fullStartYear) * 24,
    oct: getDaysInMonth(10, fullStartYear) * 24,
    nov: getDaysInMonth(11, fullStartYear) * 24,
    dec: getDaysInMonth(12, fullStartYear) * 24,
    jan: getDaysInMonth(1, fullEndYear) * 24,
    feb: getDaysInMonth(2, fullEndYear) * 24,
    mar: getDaysInMonth(3, fullEndYear) * 24,
  }
}

const HoursGrid = ({ onHoursRowsChange }) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { year } = dataGridStore
  const AOP_YEAR = year?.selectedYear
  const headerMap = generateHeaderNames(AOP_YEAR)
  const valueFormat = ValueFormatterProduction()

  const [hoursRows, setHoursRows] = useState([])
  const [modifiedCells, setModifiedCells] = useState({})

  useEffect(() => {
    if (AOP_YEAR) {
      const hoursData = generateMonthHours(AOP_YEAR)
      const rows = [hoursData]
      setHoursRows(rows)
      if (onHoursRowsChange) onHoursRowsChange(rows)
    }
  }, [AOP_YEAR])

  const columns = [
    {
      field: 'apr',
      title: headerMap[4] || 'Apr',
      widthT: 60,
      minWidth: 60,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'may',
      title: headerMap[5] || 'May',
      widthT: 60,
      minWidth: 60,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'jun',
      title: headerMap[6] || 'Jun',
      widthT: 60,
      minWidth: 60,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'jul',
      title: headerMap[7] || 'Jul',
      widthT: 60,
      minWidth: 60,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'aug',
      title: headerMap[8] || 'Aug',
      widthT: 60,
      minWidth: 60,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'sep',
      title: headerMap[9] || 'Sep',
      widthT: 60,
      minWidth: 60,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'oct',
      title: headerMap[10] || 'Oct',
      widthT: 60,
      minWidth: 60,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'nov',
      title: headerMap[11] || 'Nov',
      widthT: 60,
      minWidth: 60,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'dec',
      title: headerMap[12] || 'Dec',
      widthT: 60,
      minWidth: 60,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'jan',
      title: headerMap[1] || 'Jan',
      widthT: 60,
      minWidth: 60,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'feb',
      title: headerMap[2] || 'Feb',
      widthT: 60,
      minWidth: 60,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
    {
      field: 'mar',
      title: headerMap[3] || 'Mar',
      widthT: 60,
      minWidth: 60,
      editable: false,
      type: 'wholeNumber',
      format: valueFormat,
    },
  ]

  const permissions = {
    showAction: false,
    addButton: false,
    deleteButton: false,
    editButton: false,
    saveBtn: false,
    allAction: true,
    showTitleNameBusiness: false,
    showTitle: true,
    titleName: 'Total available hours',
  }

  return (
    <AdvanceKendoTable
      columns={columns}
      rows={hoursRows}
      setRows={setHoursRows}
      title={permissions.titleName}
      permissions={permissions}
      modifiedCells={modifiedCells}
      setModifiedCells={setModifiedCells}
    />
  )
}

export { generateMonthHours }
export default HoursGrid
