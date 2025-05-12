import { Box } from '@mui/material'
// import DataGridTable from '../ASDataGrid'
import ReportDataGrid from 'components/data-tables-views/ReportDataGrid2'
import { useEffect, useMemo, useState } from 'react'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'
import {
  Backdrop,
  CircularProgress,
  Typography,
} from '../../../../node_modules/@mui/material/index'

const PlantContribution = () => {
  const keycloak = useSession()

  const thisYear = localStorage.getItem('year')

  let oldYear1 = ''
  if (thisYear && thisYear.includes('-')) {
    const [start, end] = thisYear.split('-').map(Number)
    oldYear1 = `${start - 1}-${(end - 1).toString().slice(-2)}`
  }
  let oldYear2 = ''
  if (oldYear1 && oldYear1.includes('-')) {
    const [start, end] = oldYear1.split('-').map(Number)
    oldYear2 = `${start - 1}-${(end - 1).toString().slice(-2)}`
  }
  let oldYear3 = ''
  if (oldYear2 && oldYear2.includes('-')) {
    const [start, end] = oldYear2.split('-').map(Number)
    oldYear2 = `${start - 1}-${(end - 1).toString().slice(-2)}`
  }

  const year4 = localStorage.getItem('year')
  const year3 = `${+year4.split('-')[0] - 1}-${+year4.split('-')[1] - 1}`
  const year2 = `${+year3.split('-')[0] - 1}-${+year3.split('-')[1] - 1}`
  const year1 = `${+year2.split('-')[0] - 1}-${+year2.split('-')[1] - 1}`

  const [loading, setLoading] = useState(false)
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
  const year = localStorage.getItem('year')

  const columnsProductMix = useMemo(
    () => [
      { field: 'sno', headerName: 'SL.No', align: 'right' },
      { field: 'productName', headerName: 'Product name', flex: 2 },
      { field: 'unit', headerName: 'Unit' },

      // These get grouped
      { field: 'rsPerMt', headerName: 'Rs/MT', flex: 2, align: 'right' },
      { field: 'prodBudget', headerName: 'Budget', flex: 2, align: 'right' },
      { field: 'prodActual', headerName: 'Actual', flex: 2, align: 'right' },
      { field: 'prodNext', headerName: 'Budget', flex: 2, align: 'right' },
    ],
    [],
  )

  // 2) Three-layer grouping model
  const columnGroupingModel = useMemo(
    () => [
      {
        groupId: 'costing', // TOP layer
        headerName: 'Costing',
        children: [
          { field: 'rsPerMt' }, // LEAF
        ],
      },
      {
        groupId: 'production', // TOP layer
        headerName: 'Production, MT',
        children: [
          {
            groupId: 'current', // MID layer
            headerName: 'Current',
            children: [{ field: 'prodBudget' }, { field: 'prodActual' }],
          },
          {
            groupId: 'forecast', // MID layer
            headerName: 'Forecast',
            children: [{ field: 'prodNext' }],
          },
        ],
      },
    ],
    [],
  )

  // 3) Sample rows
  const rowsProductMix = [
    {
      id: 1,
      sno: 1,
      productName: 'EOE',
      unit: 'MT',
      rsPerMt: 25000,
      prodBudget: 942,
      prodActual: 783,
      prodNext: 895,
    },
    {
      id: 2,
      sno: 2,
      productName: 'MEG',
      unit: 'MT',
      rsPerMt: 22000,
      prodBudget: 723,
      prodActual: 631,
      prodNext: 804,
    },
    {
      id: 3,
      sno: 3,
      productName: 'EO',
      unit: 'MT',
      rsPerMt: 23000,
      prodBudget: 657,
      prodActual: 523,
      prodNext: 642,
    },
    {
      id: 4,
      sno: 4,
      productName: 'NSR',
      unit: 'Rs/MT',
      rsPerMt: null, // summary row
      prodBudget: 2112,
      prodActual: 2056,
      prodNext: 2190,
    },
  ]

  // 1) Column definitions for By Products table
  const columnsByProducts = [
    { field: 'sno', headerName: 'SL.No', align: 'right', flex: 0.5 },
    { field: 'byProductName', headerName: 'By product name', flex: 2 },
    { field: 'unit', headerName: 'Unit', flex: 1 },

    // Price group
    { field: 'priceRsMt', headerName: 'Rs/MT', flex: 1, align: 'right' },

    // Norm Unit/MT group
    { field: 'normBudget', headerName: 'Budget', flex: 1, align: 'right' },
    { field: 'normActual', headerName: 'Actual', flex: 1, align: 'right' },
    { field: 'normForecast', headerName: 'Budget', flex: 1, align: 'right' },

    // Cost Rs/MT group
    { field: 'costBudget', headerName: 'Budget', flex: 1, align: 'right' },
    { field: 'costActual', headerName: 'Actual', flex: 1, align: 'right' },
    { field: 'costForecast', headerName: 'Budget', flex: 1, align: 'right' },
  ]

  // 2) Three-layer grouping model
  const columnGroupingModelByProducts = [
    {
      groupId: 'price',
      headerName: 'Price',
      children: [{ field: 'priceRsMt' }],
    },
    {
      groupId: 'norm',
      headerName: 'Norm Unit/MT',
      children: [
        {
          groupId: 'normDtl',
          headerName: 'Norm Details',
          children: [
            { field: 'normBudget' },
            { field: 'normActual' },
            // { field: 'normForecast' },
          ],
        },
        {
          groupId: 'normDtl2',
          headerName: 'Norm2 Details',
          children: [
            // { field: 'normBudget' },
            // { field: 'normActual' },
            { field: 'normForecast' },
          ],
        },
      ],
    },
    {
      groupId: 'cost',
      headerName: 'Cost Rs/MT',
      children: [
        {
          groupId: 'costDtl',
          headerName: 'Cost Details',
          children: [
            { field: 'costBudget' },
            { field: 'costActual' },
            // { field: 'costForecast' },
          ],
        },
        {
          groupId: 'costDtl2',
          headerName: 'Cost2 Details',
          children: [
            // { field: 'costBudget' },
            // { field: 'costActual' },
            { field: 'costForecast' },
          ],
        },
      ],
    },
  ]

  // 3) Sample rows with hard-coded “random” numbers
  const rowsByProducts = [
    {
      id: 1,
      sno: 1,
      byProductName: 'Methanol',
      unit: 'MT',
      priceRsMt: 21000,
      normBudget: 1.2,
      normActual: 1.1,
      normForecast: 1.3,
      costBudget: 25000,
      costActual: 24500,
      costForecast: 25500,
    },
    {
      id: 2,
      sno: 2,
      byProductName: 'Ethanol',
      unit: 'MT',
      priceRsMt: 18000,
      normBudget: 0.8,
      normActual: 0.9,
      normForecast: 0.85,
      costBudget: 22000,
      costActual: 21500,
      costForecast: 22500,
    },
    {
      id: 3,
      sno: 3,
      byProductName: 'Isopropanol',
      unit: 'MT',
      priceRsMt: 24000,
      normBudget: 1.0,
      normActual: 0.95,
      normForecast: 1.05,
      costBudget: 26000,
      costActual: 25500,
      costForecast: 26500,
    },
    {
      id: 4,
      sno: 4,
      byProductName: 'Acetone',
      unit: 'MT',
      priceRsMt: 20000,
      normBudget: 1.5,
      normActual: 1.4,
      normForecast: 1.45,
      costBudget: 23000,
      costActual: 22500,
      costForecast: 23500,
    },
    {
      id: 5,
      sno: 5,
      byProductName: 'Phenol',
      unit: 'MT',
      priceRsMt: 27000,
      normBudget: 0.6,
      normActual: 0.65,
      normForecast: 0.7,
      costBudget: 28000,
      costActual: 27500,
      costForecast: 28500,
    },
  ]

  // 2) Raw Material
  const columnsRawMaterial = columnsByProducts.map((col) => ({
    ...col,
    headerName: col.field === 'byProductName' ? 'Raw material' : col.headerName,
    field:
      col.field === 'byProductName' ? 'rawName' : col.field.replace(/^/, 'raw'),
  }))

  const columnGroupingRawMaterial = columnGroupingModelByProducts.map(
    (group) => ({
      ...group,
      children: group.children.map((child) => ({
        ...(child.field ? { field: child.field.replace(/^/, 'raw') } : child),
        ...(child.children
          ? {
              children: child.children.map((c2) => ({
                field: c2.field.replace(/^/, 'raw'),
              })),
            }
          : {}),
      })),
    }),
  )

  const rowsRawMaterial = [
    {
      id: 1,
      sno: 1,
      rawName: 'Naphtha',
      unit: 'MT',
      rawpriceRsMt: 28000,
      rawnormBudget: 1.1,
      rawnormActual: 1.0,
      rawnormForecast: 1.2,
      rawcostBudget: 30800,
      rawcostActual: 30000,
      rawcostForecast: 31500,
    },
    {
      id: 2,
      sno: 2,
      rawName: 'Propylene',
      unit: 'MT',
      rawpriceRsMt: 34000,
      rawnormBudget: 0.9,
      rawnormActual: 0.85,
      rawnormForecast: 0.95,
      rawcostBudget: 30600,
      rawcostActual: 28900,
      rawcostForecast: 32300,
    },
    {
      id: 3,
      sno: 3,
      rawName: 'Ethylene',
      unit: 'MT',
      rawpriceRsMt: 31000,
      rawnormBudget: 1.2,
      rawnormActual: 1.15,
      rawnormForecast: 1.25,
      rawcostBudget: 37200,
      rawcostActual: 35650,
      rawcostForecast: 38750,
    },
  ]

  // 3) Catalyst Chemicals
  const columnsCatChem = columnsByProducts.map((col) => ({
    ...col,
    headerName: col.field === 'byProductName' ? 'Catalyst' : col.headerName,
    field:
      col.field === 'byProductName'
        ? 'catalystName'
        : col.field.replace(/^/, 'cat'),
  }))

  const columnGroupingCatChem = columnGroupingModelByProducts.map((group) => ({
    ...group,
    children: group.children.map((child) => ({
      ...(child.field ? { field: child.field.replace(/^/, 'cat') } : child),
      ...(child.children
        ? {
            children: child.children.map((c2) => ({
              field: c2.field.replace(/^/, 'cat'),
            })),
          }
        : {}),
    })),
  }))

  const rowsCatChem = [
    {
      id: 1,
      sno: 1,
      catalystName: 'Zeolite-Y',
      unit: 'Kg',
      catpriceRsMt: 50000,
      catnormBudget: 0.005,
      catnormActual: 0.0045,
      catnormForecast: 0.0052,
      catcostBudget: 250,
      catcostActual: 225,
      catcostForecast: 260,
    },
    {
      id: 2,
      sno: 2,
      catalystName: 'FCC Cat',
      unit: 'Kg',
      catpriceRsMt: 60000,
      catnormBudget: 0.006,
      catnormActual: 0.0058,
      catnormForecast: 0.0061,
      catcostBudget: 360,
      catcostActual: 348,
      catcostForecast: 366,
    },
  ]

  // 4) Utilities
  const columnsUtilities = columnsByProducts.map((col) => ({
    ...col,
    headerName: col.field === 'byProductName' ? 'Utility' : col.headerName,
    field:
      col.field === 'byProductName'
        ? 'utilName'
        : col.field.replace(/^/, 'util'),
  }))

  const columnGroupingUtilities = columnGroupingModelByProducts.map(
    (group) => ({
      ...group,
      children: group.children.map((child) => ({
        ...(child.field ? { field: child.field.replace(/^/, 'util') } : child),
        ...(child.children
          ? {
              children: child.children.map((c2) => ({
                field: c2.field.replace(/^/, 'util'),
              })),
            }
          : {}),
      })),
    }),
  )

  const rowsUtilities = [
    {
      id: 1,
      sno: 1,
      utilName: 'Steam',
      unit: 'MT',
      utilpriceRsMt: 3500,
      utilnormBudget: 0.4,
      utilnormActual: 0.38,
      utilnormForecast: 0.42,
      utilcostBudget: 1400,
      utilcostActual: 1330,
      utilcostForecast: 1470,
    },
    {
      id: 2,
      sno: 2,
      utilName: 'Electricity',
      unit: 'kWh',
      utilpriceRsMt: 7,
      utilnormBudget: 25,
      utilnormActual: 24,
      utilnormForecast: 26,
      utilcostBudget: 175,
      utilcostActual: 168,
      utilcostForecast: 182,
    },
  ]
  const columnsOtherVars = [
    { field: 'sno', headerName: 'SL.No', align: 'right' },
    { field: 'description', headerName: 'Other costs', flex: 2 },
    { field: 'unit', headerName: 'Unit' },

    // single two‐layer group: “Cost Rs/MT”
    { field: 'ovcBudget', headerName: 'Budget', flex: 1, align: 'right' },
    { field: 'ovcActual', headerName: 'Actual', flex: 1, align: 'right' },
    { field: 'ovcBudget2', headerName: 'Budget2', flex: 1, align: 'right' },

    // { field: 'ovcForecast', headerName: 'Forecast', flex: 1, align: 'right' },
  ]

  const columnGroupingOtherVars = [
    {
      groupId: 'cost',
      headerName: 'Cost Rs/MT',
      children: [
        { field: 'ovcBudget' },
        { field: 'ovcActual' },
        // { field: 'ovcForecast' },
      ],
    },
    {
      groupId: 'cost2',
      headerName: 'Cost2 Rs/MT',
      children: [
        { field: 'ovcBudget2' },
        // { field: 'ovcActual' },
        // { field: 'ovcForecast' },
      ],
    },
  ]

  const rowsOtherVars = [
    {
      id: 1,
      sno: 1,
      description: 'Lab charges',
      unit: 'MT',
      ovcBudget: 15,
      ovcActual: 14,
      ovcBudget2: 15,
      ovcForecast: 16,
    },
    {
      id: 2,
      sno: 2,
      description: 'Maintenance spares',
      unit: 'MT',
      ovcBudget: 25,
      ovcActual: 27,
      ovcBudget2: 25,
      ovcForecast: 30,
    },
    {
      id: 3,
      sno: 3,
      description: 'One-time setup',
      unit: 'MT',
      ovcBudget: 100,
      ovcActual: 90,
      ovcBudget2: 100,
      ovcForecast: 120,
    },
  ]

  const columnsProdCalc = [
    { field: 'sno', headerName: 'SL.No', align: 'right' },
    {
      field: 'description',
      headerName: 'Production Cost Calculations',
      flex: 2,
    },

    // Rate Rs/MT
    { field: 'rateBudget', headerName: 'Budget', flex: 1, align: 'right' },
    { field: 'rateActual', headerName: 'Actual', flex: 1, align: 'right' },
    // { field: 'rateForecast', headerName: 'Forecast', flex: 1, align: 'right' },

    // // Value Rs C
    { field: 'valueBudget', headerName: 'Budget', flex: 1, align: 'right' },
    // { field: 'valueActual', headerName: 'Actual', flex: 1, align: 'right' },
    // {
    //   field: 'valueVariance',
    //   headerName: 'Var vs Budget',
    //   flex: 1,
    //   align: 'right',
    // },
  ]

  const columnGroupingProdCalc = [
    {
      groupId: 'rate',
      headerName: 'Rate Rs/MT',
      children: [
        { field: 'rateBudget' },
        { field: 'rateActual' },
        // { field: 'rateForecast' },
      ],
    },
    {
      groupId: 'value',
      headerName: 'Value Rs Cr',
      children: [
        { field: 'valueBudget' },
        // { field: 'valueActual' },
        // { field: 'valueVariance' },
      ],
    },
  ]

  const rowsProdCalc = [
    {
      id: 1,
      sno: 1,
      description: 'Total conversion cost',
      rateBudget: 250,
      rateActual: 255,
      rateForecast: 248,
      valueBudget: 500,
      valueActual: 510,
      valueVariance: -10,
    },
    {
      id: 2,
      sno: 2,
      description: 'Total variable cost',
      rateBudget: 400,
      rateActual: 390,
      rateForecast: 405,
      valueBudget: 800,
      valueActual: 780,
      valueVariance: 20,
    },
    {
      id: 3,
      sno: 3,
      description: 'Total contribution',
      rateBudget: 650,
      rateActual: 645,
      rateForecast: 653,
      valueBudget: 1300,
      valueActual: 1285,
      valueVariance: 15,
    },
  ]

  return (
    <Box sx={{ height: 'auto', width: '100%' }}>
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        A. Product mix and production{' '}
      </Typography>
      <ReportDataGrid
        rows={rowsProductMix}
        columns={columnsProductMix}
        columnGroupingModel={columnGroupingModel}
        permissions={{
          textAlignment: 'center',
        }}
      />
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        B. By products{' '}
      </Typography>
      <ReportDataGrid
        rows={rowsByProducts}
        columns={columnsByProducts}
        columnGroupingModel={columnGroupingModelByProducts}
        permissions={{
          textAlignment: 'center',
        }}
      />
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        C. Raw material{' '}
      </Typography>
      <ReportDataGrid
        rows={rowsRawMaterial}
        columns={columnsRawMaterial}
        columnGroupingModel={columnGroupingRawMaterial}
        permissions={{
          textAlignment: 'center',
        }}
      />
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        D. Cat chem{' '}
      </Typography>
      <ReportDataGrid
        rows={rowsCatChem}
        columns={columnsCatChem}
        columnGroupingModel={columnGroupingCatChem}
        permissions={{
          textAlignment: 'center',
        }}
      />
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        E. Utilities{' '}
      </Typography>
      <ReportDataGrid
        rows={rowsUtilities}
        columns={columnsUtilities}
        columnGroupingModel={columnGroupingUtilities}
        permissions={{
          textAlignment: 'center',
        }}
      />{' '}
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Other Variable Cost{' '}
      </Typography>
      <ReportDataGrid
        rows={rowsOtherVars}
        columns={columnsOtherVars}
        columnGroupingModel={columnGroupingOtherVars}
        permissions={{
          textAlignment: 'center',
        }}
      />
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Production Cost Calculation{' '}
      </Typography>
      <ReportDataGrid
        rows={rowsProdCalc}
        columns={columnsProdCalc}
        columnGroupingModel={columnGroupingProdCalc}
        permissions={{
          textAlignment: 'center',
        }}
      />
      {/*
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Calculation of Operating hours{' '}
      </Typography>
      <ReportDataGrid rows={rowsOperatingHrs} columns={columnsOperatingHrs} />
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Calculation of Average hourly rate{' '}
      </Typography>
      <ReportDataGrid
        rows={rowsAverageHourlyRate}
        columns={columnsAverageHourlyRate}
      />
      <Typography component='div' className='grid-title' sx={{ mt: 1 }}>
        Production performance comparision with last 3 years{' '}
      </Typography>
      <ReportDataGrid
        rows={rowsProductionPerformance}
        columns={columnsProductionPerformance}
        columnGroupingModel={columnGroupingModel}
        permissions={{
          textAlignment: 'center',
        }}
      /> */}
    </Box>
  )
}

export default PlantContribution
