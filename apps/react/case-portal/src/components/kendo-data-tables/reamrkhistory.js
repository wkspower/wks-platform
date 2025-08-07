import React, { useState, useMemo } from "react";
import KendoDataTables from "./index"; 
import { useSelector } from 'react-redux'
import { DataService } from 'services/DataService'
import { useEffect } from "react";
// Dropdown options
const yesNoOptions = ["yes", "no"];
const plantOptions = ["meg1", "meg2", "meg3"];
const normParameterTypeOptions = [
  "dummy material",
  "raw material",
];
const normTypeOptions = [
  "Production",
  "Consumption",
  "CalculatedIntermediateValues",
];

// Initial rows
const  initialRows = [
  {
     id: 1,
    Name: "MIXED OIL",
    DisplayName: "MIXED OIL",
    UOM: "MT",
    Expression: "operations ",
    ExecuteQuery: "",
    NormParameterType_FK_Id: "raw material",
    Plant_FK_Id: "meg1",
    NormType_FK_Id: "Production",
    IsHistorical: "yes",
    DisplayOrder: 50,
    IsEditables: "yes",
    IsVisible: "no",
    CalculationType: "NULL",
  },
];

export default function RemarkHistoryReport() {
  const [rows, setRows] = useState(initialRows);
  const [modifiedCells, setModifiedCells] = useState({});
  const dataGridStore = useSelector((state) => state.dataGridStore)
    const { verticalChange, yearChanged, oldYear, plantID } = dataGridStore
    const year = yearChanged 
    const vertName = verticalChange?.selectedVertical
    const lowerVertName = vertName?.toLowerCase() || 'meg'
    const [loading, setLoading] = useState(false)
      const [snackbarData, setSnackbarData] = useState({
        message: '',
        severity: 'info',
      });
      const [snackbarOpen, setSnackbarOpen] = useState(false);
      //const keycloak = useSession();
      const [cellErrors, setCellErrors] = useState({});
  const [normParameterTypeOptions, setNormParameterTypeOptions] = useState([]);
  const [normTypeOptions, setNormTypeOptions] = useState([]);

  useEffect(() => {
    // Fetch NormParameterType options
    DataService.getNormParameterTypes?.(year, plantID)
      .then((data) => {
        setNormParameterTypeOptions(
          Array.isArray(data)
            ? data.map((item) => ({
                label: item.DisplayName || item.Name,
                value: item.Id,
              }))
            : []
        );
      })
      .catch(() => setNormParameterTypeOptions([]));

    // Fetch NormType options
    DataService.getNormTypes?.(year, plantID)
      .then((data) => {
        setNormTypeOptions(
          Array.isArray(data)
            ? data.map((item) => ({
                label: item.Name,
                value: item.Id,
              }))
            : []
        );
      })
      .catch(() => setNormTypeOptions([]));
  }, [year, plantID]);
  // ✅ Columns: Just pass `options` — index.js will handle dropdown rendering
  const columns = useMemo(
    () => [
      {
    field: 'Id',
    title: 'Id',
    editable: false,
    hidden: true,
  },
      { field: "Name", title: "Name", editable: true },
      { field: "DisplayName", title: "Display Name", editable: true },
      {
        field: "NormParameterType_FK_Id",
        title: "NormParameterType",
        editable: true,
        options: normParameterTypeOptions,
      },
      { field: "UOM", title: "UOM", editable: true },
      
      { field: "ExecuteQuery", title: "Execute Query", editable: true, hidden: true },
      {
      field: "Plant_FK_Id",
      title: "Plant",
      editable: true,
      options: plantOptions,
      hidden: true,
    },
      {
        field: "NormType_FK_Id",
        title: "NormType",
        editable: true,
        options: normTypeOptions,
      },
      {
        field: "IsHistorical",
        title: "Is Historical",
        editable: true,
        options: yesNoOptions,
        hidden: true,
      },
      { field: "DisplayOrder", title: "Display Order", editable: true,type: 'number', },
  
      {
        field: "IsVisible",
        title: "Is Visible",
        editable: true,
        options: yesNoOptions,
      },
      { field: "CalculationType", title: "Calculation Type", editable: true, hidden: true },
      { field: "Expression", title: "Expression", editable: true },
      {
        field: "IsEditables",
        title: "Is Editable",
        editable: true,
        options: yesNoOptions,
      },
    ],
    []
  );
function validateRow(row, columns) {
  const errors = {};
  for (const col of columns) {
    if (!col.editable) continue;
    
    const value = row[col.field];

    // Check empty values more reliably (covers "", null, undefined, NaN)
    if (value === "" || value === null || value === undefined || (typeof value === "number" && isNaN(value))) {
      errors[col.field] = true;
    }
  }
  return errors;
}

function handleSave() {
  let hasError = false;
  const newCellErrors = {};

  rows.forEach((row, index) => {
    const rowErrors = validateRow(row, columns);
    if (Object.keys(rowErrors).length > 0) {
      hasError = true;
      newCellErrors[row.id ?? index] = rowErrors; // fallback to index if id is missing
    }
  });

  setCellErrors(newCellErrors);

  if (hasError) {
    setSnackbarData({
      message: "Please fill all required fields.",
      severity: "error",
    });
    
    // Force snackbar to show (even if already open)
    setSnackbarOpen(false);
    setTimeout(() => setSnackbarOpen(true), 0);
    return;
  }

  setSnackbarOpen(true);
  setSnackbarData({
    message: "Saved successfully!",
    severity: "success",
  });
  }
  const permissions = {
    saveBtn: true,
    addButton: true,
    deleteButton: false,
    allAction: true,
    downloadExcelBtnFromUI: false,
    ExcelName: "RemarkHistory",
  };

  return (
    <KendoDataTables
      rows={rows}
      setRows={setRows}
      columns={columns}
      modifiedCells={modifiedCells}
      setModifiedCells={setModifiedCells}
      permissions={permissions}
      saveChanges={handleSave}
      cellErrors={cellErrors} 
    />
    
  );
}
