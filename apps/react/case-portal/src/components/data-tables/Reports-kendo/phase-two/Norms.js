import { useEffect, useState } from 'react'
import { Box, Backdrop, CircularProgress } from '@mui/material'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import ValueFormatterProduction from 'utils/ValueFormatterProduction'
import AdvanceKendoTable from 'components/kendo-data-tables/AdvanceKendoTable/index'
import KendoDataTables from 'components/kendo-data-tables/index'
import { min } from 'lodash'
import { nestedDummyRows } from './nestedDummyData'
import { flattenMonthObject, unflattenMonthObject } from 'components/Utilities/commonUtilityFunctions'
import { UtilityPlantApiServiceV2 } from 'services/phase-two-services/utilityPlantApiServiceV2'
import NestedKendoTable from 'components/kendo-data-tables/NestedKendoTable/index'

const Norms = () => {
  const keycloak = useSession()
  // State management

  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    verticalChange,
    yearChanged,
    oldYear,
    plantID,
    plantObject,
    siteObject,
    verticalObject,
    year,
    screenTitle,
  } = dataGridStore
  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const VERTICAL_NAME = verticalObject?.name
  const AOP_YEAR = year?.selectedYear
  const headerMap = generateHeaderNames(AOP_YEAR)
  const valueFormat = ValueFormatterProduction()
  

   // Column definitions
  const nestedColumns = [
    //Generating Plant
    {
      field: 'generatingPlantName',
      title: 'Generating Plant',
      width: 150,
      type: 'text',
      editable: false,
      locked: true,
      minWidth: 100,
    },
    //Utility
    {
      field: 'utilityName',
      title: 'Utility',
      width: 120,
      type: 'text',
      editable: false,
      locked: true,
      minWidth: 100,
    },
    // Utility ID
    {
      field: 'utilityId',
      title: 'Utility ID',
      width: 120,
      type: 'text',
      editable: false,
      locked: true,
      minWidth: 100,
    },
    //UOM
    {
      field: 'uom',
      title: 'UOM',
      width: 80,
      type: 'text',
      editable: false,
      minWidth: 80,
    },
    // Account
    {
      field: 'accountName',
      title: 'Account',
      width: 100,
      type: 'text',
      editable: false,
      minWidth: 100,
    },
    // SAP Code
    {
      field: 'sapCode',
      title: 'SAP Code',
      width: 100,
      type: 'text',
      editable: false,
      minWidth: 100,
    },
    // Material
    {
      field: 'materialName',
      title: 'Material',
      width: 100,
      type: 'text',
      editable: false,
      minWidth: 100,
    },
    // Issuing Plant
    {
      field: 'issuingPlantName',
      title: 'Issuing Plant',
      width: 100,
      type: 'text',
      editable: false,
      minWidth: 100,
    },
    {
      field: 'issuingUom',
      title: 'Issuing UOM',
      width: 80,
      type: 'text',
      editable: false,
      minWidth: 80,
    },
    // Apr
    {
      title: headerMap[4],
      children: [
        { field: 'apr.QTY', title: 'Qty', width: 80, editable: false, type: 'number1', format: valueFormat },
        { field: 'apr.generationUom', title: 'Generation Uom', width: 80, editable: false, type: 'number1', format: valueFormat },
        { field: 'apr.norms', title: 'Norms', width: 80, editable: true, type: 'number1', format: valueFormat },
        { field: 'apr.quantity', title: 'Quantity', width: 80, minWidth: 80, type: 'number', format: valueFormat },
        { field: 'apr.amount', title: 'Amount', width: 80, minWidth: 80, type: 'number', format: valueFormat },
        { field: 'apr.price', title: 'Price', width: 80, minWidth: 80, editable: true, type: 'number1', format: valueFormat },
      ],
    },
    // May
    {
      title: headerMap[5],
      children: [
        { field: 'may.QTY', title: 'Qty', width: 80, editable: false, type: 'number1', format: valueFormat },
        { field: 'may.generationUom', title: 'Generation Uom', width: 80, editable: false, type: 'number1', format: valueFormat },
        { field: 'may.norms', title: 'Norms', width: 80, editable: true, type: 'number1', format: valueFormat },
        { field: 'may.quantity', title: 'Quantity', width: 80, minWidth: 80, type: 'number', format: valueFormat },
        { field: 'may.amount', title: 'Amount', width: 80, minWidth: 80, type: 'number', format: valueFormat },
        { field: 'may.price', title: 'Price', width: 80, minWidth: 80, editable: true, type: 'number1', format: valueFormat },
      ],
    },
    // Jun
    {
      title: headerMap[6],
      children: [
        { field: 'jun.QTY', title: 'Qty', width: 80, editable: false, type: 'number1', format: valueFormat },
        { field: 'jun.generationUom', title: 'Generation Uom', width: 80, editable: false, type: 'number1', format: valueFormat },
        { field: 'jun.norms', title: 'Norms', width: 80, editable: true, type: 'number1', format: valueFormat },
        { field: 'jun.quantity', title: 'Quantity', width: 80, minWidth: 80, type: 'number', format: valueFormat },
        { field: 'jun.amount', title: 'Amount', width: 80, minWidth: 80, type: 'number', format: valueFormat },
        { field: 'jun.price', title: 'Price', width: 80, minWidth: 80, editable: true, type: 'number1', format: valueFormat },
      ],
    },
    // Jul
    {
      title: headerMap[7],
      children: [
        { field: 'jul.QTY', title: 'Qty', width: 80, editable: false, type: 'number1', format: valueFormat },
        { field: 'jul.generationUom', title: 'Generation Uom', width: 80, editable: false, type: 'number1', format: valueFormat },
        { field: 'jul.norms', title: 'Norms', width: 80, editable: true, type: 'number1', format: valueFormat },
        { field: 'jul.quantity', title: 'Quantity', width: 80, minWidth: 80, type: 'number', format: valueFormat },
        { field: 'jul.amount', title: 'Amount', width: 80, minWidth: 80, type: 'number', format: valueFormat },
        { field: 'jul.price', title: 'Price', width: 80, minWidth: 80, editable: true, type: 'number1', format: valueFormat },
      ],
    },
    // Aug
    {
      title: headerMap[8],
      children: [
        { field: 'aug.QTY', title: 'Qty', width: 80, editable: false, type: 'number1', format: valueFormat },
        { field: 'aug.generationUom', title: 'Generation Uom', width: 80, editable: false, type: 'number1', format: valueFormat },
        { field: 'aug.norms', title: 'Norms', width: 80, editable: true, type: 'number1', format: valueFormat },
        { field: 'aug.quantity', title: 'Quantity', width: 80, minWidth: 80, type: 'number', format: valueFormat },
        { field: 'aug.amount', title: 'Amount', width: 80, minWidth: 80, type: 'number', format: valueFormat },
        { field: 'aug.price', title: 'Price', width: 80, minWidth: 80, editable: true, type: 'number1', format: valueFormat },
      ],
    },
    // Sep
    {
      title: headerMap[9],
      children: [
        { field: 'sep.QTY', title: 'Qty', width: 80, editable: false, type: 'number1', format: valueFormat },
        { field: 'sep.generationUom', title: 'Generation Uom', width: 80, editable: false, type: 'number1', format: valueFormat },
        { field: 'sep.norms', title: 'Norms', width: 80, editable: true, type: 'number1', format: valueFormat },
        { field: 'sep.quantity', title: 'Quantity', width: 80, minWidth: 80, type: 'number', format: valueFormat },
        { field: 'sep.amount', title: 'Amount', width: 80, minWidth: 80, type: 'number', format: valueFormat },
        { field: 'sep.price', title: 'Price', width: 80, minWidth: 80, editable: true, type: 'number1', format: valueFormat },
      ],
    },
    // Oct
    {
      title: headerMap[10],
      children: [
        { field: 'oct.QTY', title: 'Qty', width: 80, editable: false, type: 'number1', format: valueFormat },
        { field: 'oct.generationUom', title: 'Generation Uom', width: 80, editable: false, type: 'number1', format: valueFormat },
        { field: 'oct.norms', title: 'Norms', width: 80, editable: true, type: 'number1', format: valueFormat },
        { field: 'oct.quantity', title: 'Quantity', width: 80, minWidth: 80, type: 'number', format: valueFormat },
        { field: 'oct.amount', title: 'Amount', width: 80, minWidth: 80, type: 'number', format: valueFormat },
        { field: 'oct.price', title: 'Price', width: 80, minWidth: 80, editable: true, type: 'number1', format: valueFormat },
      ],
    },
    // Nov
    {
      title: headerMap[11],
      children: [
        { field: 'nov.QTY', title: 'Qty', width: 80, editable: false, type: 'number1', format: valueFormat },
        { field: 'nov.generationUom', title: 'Generation Uom', width: 80, editable: false, type: 'number1', format: valueFormat },
        { field: 'nov.norms', title: 'Norms', width: 80, editable: true, type: 'number1', format: valueFormat },
        { field: 'nov.quantity', title: 'Quantity', width: 80, minWidth: 80, type: 'number', format: valueFormat },
        { field: 'nov.amount', title: 'Amount', width: 80, minWidth: 80, type: 'number', format: valueFormat },
        { field: 'nov.price', title: 'Price', width: 80, minWidth: 80, editable: true, type: 'number1', format: valueFormat },
      ],
    },
    //Dec
    {
      title: headerMap[12],
      children: [
        { field: 'dec.QTY', title: 'Qty', width: 80, editable: false, type: 'number1', format: valueFormat },
        { field: 'dec.generationUom', title: 'Generation Uom', width: 80, editable: false, type: 'number1', format: valueFormat },
        { field: 'dec.norms', title: 'Norms', width: 80, editable: true, type: 'number1', format: valueFormat },
        { field: 'dec.quantity', title: 'Quantity', width: 80, minWidth: 80, type: 'number', format: valueFormat },
        { field: 'dec.amount', title: 'Amount', width: 80, minWidth: 80, type: 'number', format: valueFormat },
        { field: 'dec.price', title: 'Price', width: 80, minWidth: 80, editable: true, type: 'number1', format: valueFormat },
      ],
    },
    //Jan
    {
      title: headerMap[1],
      children: [
        { field: 'jan.QTY', title: 'Qty', width: 80, editable: false, type: 'number1', format: valueFormat },
        { field: 'jan.generationUom', title: 'Generation Uom', width: 80, editable: false, type: 'number1', format: valueFormat },
        { field: 'jan.norms', title: 'Norms', width: 80, editable: true, type: 'number1', format: valueFormat, minWidth: 80 },
        { field: 'jan.quantity', title: 'Quantity', width: 80, minWidth: 80, type: 'number', format: valueFormat },
        { field: 'jan.amount', title: 'Amount', width: 80, minWidth: 80, type: 'number', format: valueFormat },
        { field: 'jan.price', title: 'Price', width: 80, minWidth: 80, editable: true, type: 'number1', format: valueFormat },
      ],
    },
    //Feb
    {
      title: headerMap[2],
      children: [
        { field: 'feb.QTY', title: 'Qty', width: 80, editable: false, type: 'number1', format: valueFormat },
        { field: 'feb.generationUom', title: 'Generation Uom', width: 80, editable: false, type: 'number1', format: valueFormat },
        { field: 'feb.norms', title: 'Norms', width: 80, editable: true, type: 'number1', format: valueFormat, minWidth: 80 },
        { field: 'feb.quantity', title: 'Quantity', width: 80, minWidth: 80, type: 'number', format: valueFormat },
        { field: 'feb.amount', title: 'Amount', width: 80, minWidth: 80, type: 'number', format: valueFormat },
        { field: 'feb.price', title: 'Price', width: 80, minWidth: 80, editable: true, type: 'number1', format: valueFormat },
      ],
    },
    //Mar
    {
      title: headerMap[3],
      minWidth: 320,
      children: [
        { field: 'mar.QTY', title: 'Qty', width: 80, editable: false, type: 'number1', format: valueFormat },
        { field: 'mar.generationUom', title: 'Generation Uom', width: 80, editable: false, type: 'number1', format: valueFormat },
        { field: 'mar.norms', title: 'Norms', width: 80, editable: true, type: 'number1', format: valueFormat, minWidth: 80 },
        { field: 'mar.quantity', title: 'Quantity', width: 80, minWidth: 80, type: 'number', format: valueFormat },
        { field: 'mar.amount', title: 'Amount', width: 80, minWidth: 80, type: 'number', format: valueFormat },
        { field: 'mar.price', title: 'Price', width: 80, minWidth: 80, editable: true, type: 'number1', format: valueFormat },
      ],
    },
  ]
  // Column definitions
  const columns = [
    //Generating Plant
    {
      field: 'generatingPlantName',
      title: 'Generating Plant',
      width: 150,
      type: 'text',
      editable: false,
      locked: true,
      minWidth: 100,
    },
    //Utility
    {
      field: 'utilityName',
      title: 'Utility',
      width: 120,
      type: 'text',
      editable: false,
      locked: true,
      minWidth: 100,
    },
    // Utility ID
    {
      field: 'utilityId',
      title: 'Utility ID',
      width: 120,
      type: 'text',
      editable: false,
      locked: true,
      minWidth: 100,
    },
    //UOM
    {
      field: 'uom',
      title: 'UOM',
      width: 80,
      type: 'text',
      editable: false,
      minWidth: 80,
    },
    // Account
    {
      field: 'accountName',
      title: 'Account',
      width: 100,
      type: 'text',
      editable: false,
      minWidth: 100,
    },
    // SAP Code
    {
      field: 'sapCode',
      title: 'SAP Code',
      width: 100,
      type: 'text',
      editable: false,
      minWidth: 100,
    },
    // Material
    {
      field: 'materialName',
      title: 'Material',
      width: 100,
      type: 'text',
      editable: false,
      minWidth: 100,
    },
    // Issuing Plant
    {
      field: 'issuingPlantName',
      title: 'Issuing Plant',
      width: 100,
      type: 'text',
      editable: false,
      minWidth: 100,
    },
    {
      field: 'issuingUom',
      title: 'Issuing UOM',
      width: 80,
      type: 'text',
      editable: false,
      minWidth: 80,
    },
    // Apr
    {
      title: headerMap[4],
      children: [
        { field: 'apr_QTY', title: 'Qty', width: 80, editable: false,type: 'number1',format: valueFormat},
        { field: 'apr_generationUom', title: 'Generation Uom', width: 80, editable: false,type: 'number1',format: valueFormat},
        { field: 'apr_norms', title: 'Norms', width: 80, editable: true,type: 'number1',format: valueFormat},
        { field: 'apr_quantity', title: 'Quantity', width: 80, minWidth: 80,type: 'number',format: valueFormat },
        { field: 'apr_amount', title: 'Amount', width: 80, minWidth: 80,type: 'number',format: valueFormat },
        { field: 'apr_price', title: 'Price', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat},
      ],
    },
    // May
    {
      title: headerMap[5],
      children: [
        { field: 'may_QTY', title: 'Qty', width: 80, editable: false,type: 'number1',format: valueFormat},
        { field: 'may_generationUom', title: 'Generation Uom', width: 80, editable: false,type: 'number1',format: valueFormat},
        { field: 'may_norms', title: 'Norms', width: 80, editable: true,type: 'number1',format: valueFormat },
        { field: 'may_quantity', title: 'Quantity', width: 80, minWidth: 80,type: 'number',format: valueFormat },
        { field: 'may_amount', title: 'Amount', width: 80, minWidth: 80,type: 'number',format: valueFormat },
        { field: 'may_price', title: 'Price', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat },
      ],
    },
    // Jun
    {
      title: headerMap[6],
      children: [
        { field: 'jun_QTY', title: 'Qty', width: 80, editable: false,type: 'number1',format: valueFormat},
        { field: 'jun_generationUom', title: 'Generation Uom', width: 80, editable: false,type: 'number1',format: valueFormat},
        { field: 'jun_norms', title: 'Norms', width: 80, editable: true,type: 'number1',format: valueFormat },
        { field: 'jun_quantity', title: 'Quantity', width: 80, minWidth: 80,type: 'number',format: valueFormat },
        { field: 'jun_amount', title: 'Amount', width: 80, minWidth: 80,type: 'number',format: valueFormat },
        { field: 'jun_price', title: 'Price', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat },
      ],
    },
    // Jul
    {
      title: headerMap[7],
      children: [
        { field: 'jul_QTY', title: 'Qty', width: 80, editable: false,type: 'number1',format: valueFormat},
        { field: 'jul_generationUom', title: 'Generation Uom', width: 80, editable: false,type: 'number1',format: valueFormat},
        { field: 'jul_norms', title: 'Norms', width: 80, editable: true,type: 'number1',format: valueFormat },
        { field: 'jul_quantity', title: 'Quantity', width: 80, minWidth: 80,type: 'number',format: valueFormat },
        { field: 'jul_amount', title: 'Amount', width: 80, minWidth: 80,type: 'number',format: valueFormat },
        { field: 'jul_price', title: 'Price', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat },
      ],
    },
    // Aug
    {
      title: headerMap[8],
      children: [
        { field: 'aug_QTY', title: 'Qty', width: 80, editable: false,type: 'number1',format: valueFormat},
        { field: 'aug_generationUom', title: 'Generation Uom', width: 80, editable: false,type: 'number1',format: valueFormat},
        { field: 'aug_norms', title: 'Norms', width: 80, editable: true,type: 'number1',format: valueFormat },
        { field: 'aug_quantity', title: 'Quantity', width: 80, minWidth: 80,type: 'number',format: valueFormat },
        { field: 'aug_amount', title: 'Amount', width: 80, minWidth: 80,type: 'number',format: valueFormat },
        { field: 'aug_price', title: 'Price', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat },
      ],
    },
    // Sep
    {
      title: headerMap[9],
      children: [
        { field: 'sep_QTY', title: 'Qty', width: 80, editable: false,type: 'number1',format: valueFormat},
        { field: 'sep_generationUom', title: 'Generation Uom', width: 80, editable: false,type: 'number1',format: valueFormat},
        { field: 'sep_norms', title: 'Norms', width: 80, editable: true,type: 'number1',format: valueFormat },
        { field: 'sep_quantity', title: 'Quantity', width: 80, minWidth: 80,type: 'number',format: valueFormat },
        { field: 'sep_amount', title: 'Amount', width: 80, minWidth: 80,type: 'number',format: valueFormat },
        { field: 'sep_price', title: 'Price', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat },
      ],
    },
    // Oct
    {
      title: headerMap[10],
      children: [
        { field: 'oct_QTY', title: 'Qty', width: 80, editable: false,type: 'number1',format: valueFormat},
        { field: 'oct_generationUom', title: 'Generation Uom', width: 80, editable: false,type: 'number1',format: valueFormat},
        { field: 'oct_norms', title: 'Norms', width: 80, editable: true,type: 'number1',format: valueFormat },
        { field: 'oct_quantity', title: 'Quantity', width: 80, minWidth: 80,type: 'number',format: valueFormat },
        { field: 'oct_amount', title: 'Amount', width: 80, minWidth: 80,type: 'number',format: valueFormat },
        { field: 'oct_price', title: 'Price', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat },
      ],
    },
    // Nov
    {
      title: headerMap[11],
      children: [
        { field: 'nov_QTY', title: 'Qty', width: 80, editable: false,type: 'number1',format: valueFormat},
        { field: 'nov_generationUom', title: 'Generation Uom', width: 80, editable: false,type: 'number1',format: valueFormat},
        { field: 'nov_norms', title: 'Norms', width: 80, editable: true,type: 'number1',format: valueFormat },
        { field: 'nov_quantity', title: 'Quantity', width: 80, minWidth: 80,type: 'number',format: valueFormat },
        { field: 'nov_amount', title: 'Amount', width: 80, minWidth: 80,type: 'number',format: valueFormat },
        { field: 'nov_price', title: 'Price', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat },
      ],
    },
    //Dec
    {
      title: headerMap[12],
      children: [
        { field: 'dec_QTY', title: 'Qty', width: 80, editable: false,type: 'number1',format: valueFormat},
        { field: 'dec_generationUom', title: 'Generation Uom', width: 80, editable: false,type: 'number1',format: valueFormat},
        { field: 'dec_norms', title: 'Norms', width: 80, editable: true,type: 'number1',format: valueFormat },
        { field: 'dec_quantity', title: 'Quantity', width: 80, minWidth: 80,type: 'number',format: valueFormat },
        { field: 'dec_amount', title: 'Amount', width: 80, minWidth: 80,type: 'number',format: valueFormat },
        { field: 'dec_price', title: 'Price', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat },
      ],
    },
    //Jan
    {
      title: headerMap[1],
      children: [
        { field: 'jan_QTY', title: 'Qty', width: 80, editable: false,type: 'number1',format: valueFormat},
        { field: 'jan_generationUom', title: 'Generation Uom', width: 80, editable: false,type: 'number1',format: valueFormat},
        { field: 'jan_norms', title: 'Norms', width: 80, editable: true,type: 'number1',format: valueFormat, minWidth: 80 },
        { field: 'jan_quantity', title: 'Quantity', width: 80, minWidth: 80,type: 'number',format: valueFormat },
        { field: 'jan_amount', title: 'Amount', width: 80, minWidth: 80,type: 'number',format: valueFormat },
        { field: 'jan_price', title: 'Price', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat },
      ],
    },
    //Feb
    {
      title: headerMap[2],
      children: [
        { field: 'feb_QTY', title: 'Qty', width: 80, editable: false,type: 'number1',format: valueFormat},
        { field: 'feb_generationUom', title: 'Generation Uom', width: 80, editable: false,type: 'number1',format: valueFormat},
        { field: 'feb_norms', title: 'Norms', width: 80, editable: true,type: 'number1',format: valueFormat, minWidth: 80 },
        { field: 'feb_quantity', title: 'Quantity', width: 80, minWidth: 80,type: 'number',format: valueFormat },
        { field: 'feb_amount', title: 'Amount', width: 80, minWidth: 80,type: 'number',format: valueFormat },
        { field: 'feb_price', title: 'Price', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat },
      ],
    },
    //mar
    {
      title: headerMap[3],
      minWidth: 320,
      children: [
        { field: 'mar_QTY', title: 'Qty', width: 80, editable: false,type: 'number1',format: valueFormat},
        { field: 'mar_generationUom', title: 'Generation Uom', width: 80, editable: false,type: 'number1',format: valueFormat},
        { field: 'mar_norms', title: 'Norms', width: 80, editable: true,type: 'number1',format: valueFormat, minWidth: 80 },
        { field: 'mar_quantity', title: 'Quantity', width: 80, minWidth: 80,type: 'number',format: valueFormat },
        { field: 'mar_amount', title: 'Amount', width: 80, minWidth: 80,type: 'number',format: valueFormat },
        { field: 'mar_price', title: 'Price', width: 80, minWidth: 80,editable: true,type: 'number1',format: valueFormat },
      ],
    },
  ]
  
  const [rows, setRows] = useState([])

  useEffect(() => {
    if (PLANT_ID && AOP_YEAR) {
      fetchPlantRequirementData()
      setModifiedCells({})
    }
  }, [PLANT_ID,AOP_YEAR])

  const fetchPlantRequirementData = async () => {
    setLoading(true)
    try {        
      const res = await UtilityPlantApiServiceV2.getNormBasedUtilityBudget(
        keycloak,
        PLANT_ID,
        AOP_YEAR
      )
      
      if (res?.data?.length === 0) {
        setRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        return
      }
      console.log('res', res)
      // setRows(flattenMonthObject(res?.data)) 
      setRows(res?.data) 
    } catch (error) {
      console.error('Error fetching fixed consumption data:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }

  // Permissions (adjust as needed)
  const permissions = {
    showAction: true,
    addButton: false,
    deleteButton: false,
    editButton: true,
    saveBtn: true,
    allAction: true,
    showTitleNameBusiness: true,
    titleName: screenTitle?.title,
    showExport: false,
    showImport: false,
    showTitle:true,
    // downloadExcelBtnFromUI:true,
    // ExcelName:'Norms'
  }

  // Save handler with API call
  const saveChanges = async () => {
    setLoading(true)
    console.log('modifiedCells',modifiedCells)
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

    const payload = modifiedData
    
    try {
      // Transform modifiedCells into the format expected by the API
     
      console.log('payload', payload)

      // Call the API to save changes
      // NOTE: Update this API call to expect nested format when ready
      const response = await UtilityPlantApiServiceV2.saveNormsData(
        keycloak,
        payload  // Now sending nested format: { apr: { norms, quantity, ... } }
      )

      // Update the local state with the saved data
      // setRows(updatedRows)
      setModifiedCells({})
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Successfully saved ${modifiedData.length} changes!`,
        severity: 'success',
      })
    } catch (error) {
      console.error('Error saving plant requirement data:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to save changes. Please try again.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  return (
    <Box>
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={!!loading}
      >
        <CircularProgress color='inherit' />
      </Backdrop>
      <NestedKendoTable
        columns={nestedColumns}
        rows={rows}
        setRows={setRows}
        modifiedCells={modifiedCells}
        setModifiedCells={setModifiedCells}
        title='Norms'
        permissions={permissions}
        saveChanges={saveChanges}
        snackbarData={snackbarData}
        snackbarOpen={snackbarOpen}
        setSnackbarOpen={setSnackbarOpen}
        setSnackbarData={setSnackbarData}
        groupBy={['generatingPlantName', 'accountName']}
      />
    </Box>
  )
}

export default Norms
