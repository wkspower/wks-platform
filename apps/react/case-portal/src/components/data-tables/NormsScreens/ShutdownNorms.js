import DataGridTable from '../ASDataGrid'

const shutdownNormsColumns = [
  {
    field: 'srNo',
    headerName: 'Sr. No',
    minWidth: 50,
    maxWidth: 70,
    editable: false,
  },
  { field: 'particular', headerName: 'Particular', width: 200, editable: true },
  {
    field: 'unit',
    headerName: 'Unit',
    minWidth: 50,
    maxWidth: 70,
    editable: true,
  },
  {
    field: 'norms',
    headerName: 'Norms',
    minWidth: 50,
    maxWidth: 70,
    editable: true,
  },
]

const shutdownNormsData = [
  { id: 1, srNo: 1, particular: 'Equipment A', unit: 'Hours', norms: 10 },
  { id: 2, srNo: 2, particular: 'Equipment B', unit: 'Days', norms: 2 },
  { id: 3, srNo: 3, particular: 'Material C', unit: 'Kg', norms: 50 },
  { id: 4, srNo: 4, particular: 'Tool D', unit: 'Pcs', norms: 5 },
  { id: 5, srNo: 5, particular: 'Machine E', unit: 'Hours', norms: 20 },
  { id: 6, srNo: 6, particular: 'Component F', unit: 'Litres', norms: 15 },
  { id: 7, srNo: 7, particular: 'System G', unit: 'Units', norms: 3 },
  { id: 8, srNo: 8, particular: 'Gear H', unit: 'Sets', norms: 8 },
  { id: 9, srNo: 9, particular: 'Pump I', unit: 'Hours', norms: 12 },
  { id: 10, srNo: 10, particular: 'Sensor J', unit: 'Pcs', norms: 6 },
]

const ShutdownNorms = () => (
  <div>
    <DataGridTable
      columns={shutdownNormsColumns}
      rows={shutdownNormsData}
      title='Shutdown Norms'
      onAddRow={(newRow) => console.log('New Row Added:', newRow)}
      onDeleteRow={(id) => console.log('Row Deleted:', id)}
      onRowUpdate={(updatedRow) => console.log('Row Updated:', updatedRow)}
      paginationOptions={[100, 200, 300]}
      permissions={{
        showAction: true,
        addButton: false,
        deleteButton: false,
        editButton: true,
        showUnit: true,
        saveWithRemark: true,
      }}
    />
  </div>
)

export default ShutdownNorms
