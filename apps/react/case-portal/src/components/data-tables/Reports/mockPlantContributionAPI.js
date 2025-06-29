// src/services/MockReportService.js

import { Tooltip } from '../../../../node_modules/@mui/material/index'

//  current FY
// const currFY = localStorage.getItem('year') || ''

// Compute previous FY (prevFY)

export const MockReportService = {
  async getReport({ category, year }) {
    const currFY = year || ''
    const formatValueToThreeDecimalsTwo = (params) => {
      return params === 0 ? 0 : params ? parseFloat(params).toFixed(2) : ''
    }
    const formatValueToThreeDecimalsZero = (params) => {
      return params === 0 ? 0 : params ? parseFloat(params).toFixed(0) : ''
    }
    const formatValueToThreeDecimalsOne = (params) => {
      return params === 0 ? 0 : params ? parseFloat(params).toFixed(1) : ''
    }
    const formatValueToThreeDecimalsFour = (params) => {
      return params === 0 ? 0 : params ? parseFloat(params).toFixed(4) : ''
    }

    let prevFY = ''
    if (currFY.includes('-')) {
      const [start, end] = currFY.split('-').map(Number)
      prevFY = `${start - 1}-${(end - 1).toString().padStart(2, '0')}`
    }
    switch (category) {
      // ==== A. Product mix and production ====
      case 'ProductMixAndProduction':
        return {
          columns: [
            { field: 'SrNo', headerName: 'SL.No', align: 'right', widthT: 50 },
            { field: 'ByProductName', headerName: 'Product name', flex: 2 },
            { field: 'Unit', headerName: 'Unit' },
            {
              field: 'Price',
              headerName: 'Rs/MT',
              flex: 2,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsTwo,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsTwo(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'PrevYearNormBudget',
              headerName: 'Budget',
              flex: 2,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsTwo,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsTwo(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'PrevYearNormActual',
              headerName: 'Actual',
              flex: 2,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsTwo,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsTwo(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'NextYearCostBudget',
              headerName: 'Budget',
              flex: 2,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsZero,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsZero(params.value)}</span>
                </Tooltip>
              ),
            },
          ],
          columnGrouping: [
            {
              groupId: 'costing',
              headerName: 'Price',
              children: [{ field: 'Price' }],
            },
            {
              groupId: 'production',
              headerName: 'Production, MT',
              children: [
                {
                  groupId: 'current',
                  // lone "Current"  use prevFY
                  headerName: prevFY,
                  children: [
                    { field: 'PrevYearNormBudget' },
                    { field: 'PrevYearNormActual' },
                  ],
                },
                {
                  groupId: 'forecast',
                  // lone "Forecast" use currFY
                  headerName: currFY,
                  children: [{ field: 'NextYearCostBudget' }],
                },
              ],
            },
          ],
        }

      // ==== B. By products ====
      case 'ByProducts':
        return {
          columns: [
            {
              field: 'SrNo',
              headerName: 'SL.No',
              align: 'right',
              flex: 0.5,
              widthT: 50,
            },
            { field: 'ByProductName', headerName: 'By product name', flex: 2 },
            { field: 'Unit', headerName: 'Unit', flex: 1 },
            {
              field: 'Price',
              headerName: 'Rs/MT',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsZero,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsZero(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'PrevYearNormBudget',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsFour,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsFour(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'PrevYearNormActual',
              headerName: 'Actual',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsFour,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsFour(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'NextYearNormActual',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsFour,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsFour(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'PrevYearCostBudget',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsOne,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsOne(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'PrevYearCostActual',
              headerName: 'Actual',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsOne,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsOne(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'NextYearCostActual',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsOne,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsOne(params.value)}</span>
                </Tooltip>
              ),
            },
          ],
          columnGrouping: [
            {
              groupId: 'price',
              headerName: 'Price',
              children: [{ field: 'Price' }],
            },
            {
              groupId: 'norm',
              // "Norm Unit/MT" unchanged
              headerName: 'Norm Unit/MT',
              children: [
                {
                  groupId: 'normDtl',
                  headerName: prevFY, // was "Current"
                  children: [
                    { field: 'PrevYearNormBudget' },
                    { field: 'PrevYearNormActual' },
                  ],
                },
                {
                  groupId: 'normDtl2',
                  headerName: currFY, // was "Forecast"
                  children: [{ field: 'NextYearNormActual' }],
                },
              ],
            },
            {
              groupId: 'cost',
              headerName: 'Cost Rs/MT',
              children: [
                {
                  groupId: 'costDtl',
                  headerName: prevFY, // "Current"  prevFY
                  children: [
                    { field: 'PrevYearCostBudget' },
                    { field: 'PrevYearCostActual' },
                  ],
                },
                {
                  groupId: 'costDtl2',
                  headerName: currFY, // "Forecast"  currFY
                  children: [{ field: 'NextYearCostActual' }],
                },
              ],
            },
          ],
        }

      // ==== C. Raw material ====
      case 'RawMaterial':
        return {
          columns: [
            {
              field: 'SrNo',
              headerName: '',
              align: 'right',
              flex: 0.5,
              widthT: 50,
            },
            {
              field: 'ByProductName',
              headerName: 'Raw material name',
              flex: 2,
            },
            { field: 'Unit', headerName: 'Unit', flex: 1 },
            {
              field: 'Price',
              headerName: 'Rs/MT',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsZero,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsZero(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'PrevYearNormBudget',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsFour,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsFour(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'PrevYearNormActual',
              headerName: 'Actual',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsFour,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsFour(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'NextYearNormActual',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsFour,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsFour(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'PrevYearCostBudget',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsOne,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsOne(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'PrevYearCostActual',
              headerName: 'Actual',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsOne,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsOne(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'NextYearCostActual',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsOne,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsOne(params.value)}</span>
                </Tooltip>
              ),
            },
          ],
          columnGrouping: [
            {
              groupId: 'price',
              headerName: 'Price',
              children: [{ field: 'Price' }],
            },
            {
              groupId: 'norm',
              headerName: 'Norm Unit/MT', // was "Norm Unit/MT" but contains "Previous Year"
              children: [
                {
                  groupId: 'normDtl',
                  headerName: prevFY,
                  children: [
                    { field: 'PrevYearNormBudget' },
                    { field: 'PrevYearNormActual' },
                  ],
                },
                {
                  groupId: 'normDtl2',
                  headerName: currFY,
                  children: [{ field: 'NextYearNormActual' }],
                },
              ],
            },
            {
              groupId: 'cost',
              headerName: 'Cost Rs/MT', // "Previous Year Cost"
              children: [
                {
                  groupId: 'costDtl',
                  headerName: prevFY,
                  children: [
                    { field: 'PrevYearCostBudget' },
                    { field: 'PrevYearCostActual' },
                  ],
                },
                {
                  groupId: 'costDtl2',
                  headerName: currFY,
                  children: [{ field: 'NextYearCostActual' }],
                },
              ],
            },
          ],
        }

      // ==== D. Cat chem ====
      case 'CatChem':
        return {
          columns: [
            {
              field: 'SrNo',
              headerName: 'SL.No',
              align: 'right',
              flex: 0.5,
              widthT: 50,
            },
            { field: 'ByProductName', headerName: 'Catalyst name', flex: 2 },
            { field: 'Unit', headerName: 'Unit', flex: 1 },
            {
              field: 'Price',
              headerName: 'Rs/MT',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsZero,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsZero(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'PrevYearNormBudget',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsFour,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsFour(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'PrevYearNormActual',
              headerName: 'Actual',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsFour,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsFour(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'NextYearNormActual',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsFour,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsFour(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'PrevYearCostBudget',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsOne,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsOne(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'PrevYearCostActual',
              headerName: 'Actual',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsOne,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsOne(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'NextYearCostActual',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsOne,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsOne(params.value)}</span>
                </Tooltip>
              ),
            },
          ],
          columnGrouping: [
            {
              groupId: 'price',
              headerName: 'Price',
              children: [{ field: 'Price' }],
            },
            {
              groupId: 'norm',
              headerName: 'Norm Unit/MT',
              children: [
                {
                  groupId: 'normDtl',
                  headerName: prevFY,
                  children: [
                    { field: 'PrevYearNormBudget' },
                    { field: 'PrevYearNormActual' },
                  ],
                },
                {
                  groupId: 'normDtl2',
                  headerName: currFY,
                  children: [{ field: 'NextYearNormActual' }],
                },
              ],
            },
            {
              groupId: 'cost',
              headerName: 'Cost Rs/MT',
              children: [
                {
                  groupId: 'costDtl',
                  headerName: prevFY,
                  children: [
                    { field: 'PrevYearCostBudget' },
                    { field: 'PrevYearCostActual' },
                  ],
                },
                {
                  groupId: 'costDtl2',
                  headerName: currFY,
                  children: [{ field: 'NextYearCostActual' }],
                },
              ],
            },
          ],
        }

      // ==== E. Utilities ====
      case 'Utilities':
        return {
          columns: [
            {
              field: 'SrNo',
              headerName: 'SL.No',
              align: 'right',
              flex: 0.5,
              widthT: 50,
            },
            { field: 'ByProductName', headerName: 'Utility name', flex: 2 },
            { field: 'Unit', headerName: 'Unit', flex: 1 },
            {
              field: 'Price',
              headerName: 'Rs/MT',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsZero,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsZero(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'PrevYearNormBudget',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsFour,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsFour(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'PrevYearNormActual',
              headerName: 'Actual',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsFour,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsFour(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'NextYearNormActual',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsFour,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsFour(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'PrevYearCostBudget',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsOne,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsOne(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'PrevYearCostActual',
              headerName: 'Actual',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsOne,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsOne(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'NextYearCostActual',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsOne,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsOne(params.value)}</span>
                </Tooltip>
              ),
            },
          ],
          columnGrouping: [
            {
              groupId: 'price',
              headerName: 'Price',
              children: [{ field: 'Price' }],
            },
            {
              groupId: 'norm',
              headerName: 'Norm Unit/MT',
              children: [
                {
                  groupId: 'normDtl',
                  headerName: prevFY,
                  children: [
                    { field: 'PrevYearNormBudget' },
                    { field: 'PrevYearNormActual' },
                  ],
                },
                {
                  groupId: 'normDtl2',
                  headerName: currFY,
                  children: [{ field: 'NextYearNormActual' }],
                },
              ],
            },
            {
              groupId: 'cost',
              headerName: 'Cost Rs/MT',
              children: [
                {
                  groupId: 'costDtl',
                  headerName: prevFY,
                  children: [
                    { field: 'PrevYearCostBudget' },
                    { field: 'PrevYearCostActual' },
                  ],
                },
                {
                  groupId: 'costDtl2',
                  headerName: currFY,
                  children: [{ field: 'NextYearCostActual' }],
                },
              ],
            },
          ],
        }

      // ==== F. Other Variable Cost ====
      case 'OtherVariableCost':
        return {
          columns: [
            { field: 'SrNo', headerName: 'SL.No', align: 'right', widthT: 50 },
            { field: 'OtherCost', headerName: 'Other cost', flex: 2 },
            { field: 'Unit', headerName: 'Unit' },
            {
              field: 'PrevYearBudget',
              headerName: 'Budget',
            },
            {
              field: 'PrevYearActual',
              headerName: 'Actual',
            },
            {
              field: 'CurrentYearBudget',
              headerName: 'Budget',
            },
          ],
          columnGrouping: [
            {
              groupId: 'previous',
              // literal ?Previous Year? prevFY
              headerName: prevFY,
              children: [
                { field: 'PrevYearBudget' },
                { field: 'PrevYearActual' },
              ],
            },
            {
              groupId: 'current',
              // literal ?Current Year?  currFY
              headerName: currFY,
              children: [{ field: 'CurrentYearBudget' }],
            },
          ],
        }

      // ==== G. Production Cost Calculations ====
      case 'ProductionCostCalculations':
        return {
          columns: [
            { field: 'SrNo', headerName: 'SL.No', align: 'right', widthT: 50 },
            {
              field: 'ProductionCostCalculations',
              headerName: 'Production cost calculations',
              flex: 2,
            },
            {
              field: 'PrevYearBudget',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsZero,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsZero(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'PrevYearActual',
              headerName: 'Actual',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsZero,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsZero(params.value)}</span>
                </Tooltip>
              ),
            },
            {
              field: 'NextYearBudget',
              headerName: 'Budget',
              flex: 1,
              align: 'right',
              valueFormatter: formatValueToThreeDecimalsZero,
              renderCell: (params) => (
                <Tooltip
                  title={params.value != null ? params.value.toString() : ''}
                  arrow
                >
                  <span>{formatValueToThreeDecimalsZero(params.value)}</span>
                </Tooltip>
              ),
            },
          ],
          columnGrouping: [
            {
              groupId: 'previous',
              headerName: prevFY,
              children: [
                { field: 'PrevYearBudget' },
                { field: 'PrevYearActual' },
              ],
            },
            {
              groupId: 'next',
              headerName: currFY,
              children: [{ field: 'NextYearBudget' }],
            },
          ],
        }

      default:
        throw new Error(`Unknown category: ${category}`)
    }
  },
}
