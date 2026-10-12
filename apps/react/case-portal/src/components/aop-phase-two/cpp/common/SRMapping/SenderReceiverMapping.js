import { useState, useMemo, useCallback } from 'react'
import { useSelector } from 'react-redux'
import { useSession } from 'SessionStoreContext'
import { UtilityPlantApiServiceV2 } from 'components/aop-phase-two/services/cpp/jmd/utilityPlantApiServiceV2'
import { Box, Stack, Tooltip, IconButton } from '@mui/material'
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline'
import LoaderBackdrop from 'components/Utilities/LoaderBackdrop'
import AdvanceKendoTable from 'components/aop-phase-two/common/AdvanceKendoTable/index'
import DeleteDialog from 'components/aop-phase-two/common/AdvanceKendoTable/components/DeleteDialog'
import { useDebounce } from 'hooks/useDebounce'
import { validateRowDataWithRemarks } from 'components/aop-phase-two/common/commonUtilityFunctions'
import { generateExcelName } from 'components/aop-phase-two/common/utilities/excelNameUtil'

// ── Constants ────────────────────────────────────────────────────────────────
const SR_MAPPING_ROLE = 'sr_mapping'
const SR_MAPPING_EDIT_ROLE = 'sr_mapping_edit'

// Mandatory fields that must be filled for each row
const REQUIRED_FIELDS = [
  'cppPlantId',
  'senderCostCenterName',
  'senderPlantName',
  'senderUtilityName',
  'receiverCostCenterName',
  'receiverPlantName',
  'receiverUtilityName',
]

const FIELD_LABELS = {
  cppPlantId: 'CPP Plant',
  senderCostCenterName: 'Sender Cost Center',
  senderPlantName: 'Sender Plant',
  senderUtilityName: 'Sender Utility',
  receiverCostCenterName: 'Receiver Cost Center',
  receiverPlantName: 'Receiver Plant',
  receiverUtilityName: 'Receiver Utility',
}

const PREFIXES = ['sender', 'receiver']

const GROUP_FIELDS = {
  Plant: ['Name', 'Id', 'Code'],
  CostCenter: ['Name', 'Id', 'Code'],
  Utility: ['Name', 'Id', 'Code', 'UOM'],
}

const getFields = (prefix, group) =>
  GROUP_FIELDS[group].map((s) => `${prefix}${group}${s}`)

const getAllFields = (prefix) =>
  Object.keys(GROUP_FIELDS).flatMap((group) => getFields(prefix, group))

// Comprehensive dependency reset mapping
const DEPENDENCY_RESETS = {
  cppPlantId: PREFIXES.flatMap((p) => getAllFields(p)),
  senderPlantName: getFields('sender', 'Utility'),
  receiverPlantName: getFields('receiver', 'Utility'),
  senderCostCenterName: [],
  receiverCostCenterName: [],
  // When utility changes, no dependent fields to reset
}

const DROPDOWN_FIELD_CONFIG = {
  senderPlantName: {
    source: 'plantsDropdown',
    prefix: 'sender',
    group: 'Plant',
    valueField: 'value',
    labelField: 'label',
    codeField: 'code',
    idField: 'plantId',
  },
  receiverPlantName: {
    source: 'plantsDropdown',
    prefix: 'receiver',
    group: 'Plant',
    valueField: 'value',
    labelField: 'label',
    codeField: 'code',
    idField: 'plantId',
  },
  senderCostCenterName: {
    source: 'costCentersDropdown',
    prefix: 'sender',
    group: 'CostCenter',
    valueField: 'value',
    labelField: 'label',
    codeField: 'code',
    idField: 'costCenterId',
  },
  receiverCostCenterName: {
    source: 'costCentersDropdown',
    prefix: 'receiver',
    group: 'CostCenter',
    valueField: 'value',
    labelField: 'label',
    codeField: 'code',
    idField: 'costCenterId',
  },
  senderUtilityName: {
    source: 'normParameters',
    prefix: 'sender',
    group: 'Utility',
    normTypeFkId: 2,
    valueField: 'displayName',
    labelField: 'displayName',
    codeField: 'sapMaterialCode',
    idField: 'id',
    uomField: 'uom',
  },
  receiverUtilityName: {
    source: 'normParameters',
    prefix: 'receiver',
    group: 'Utility',
    normTypeFkId: 1,
    valueField: 'displayName',
    labelField: 'displayName',
    codeField: 'sapMaterialCode',
    idField: 'id',
    uomField: 'uom',
  },
}

// ── Action Cell (kept outside component to keep identity stable) ────────────

const ActionCell = ({ dataItem, tdProps, isSRMappingRole, onDelete }) => (
  <td
    {...tdProps}
    style={{
      ...tdProps?.style,
      textAlign: 'center',
      verticalAlign: 'middle',
    }}
  >
    <Box sx={{ display: 'flex', justifyContent: 'center' }}>
      <Tooltip title='Delete Row'>
        <span>
          <IconButton
            size='medium'
            color='error'
            disabled={!isSRMappingRole}
            onClick={() => onDelete(dataItem)}
          >
            <DeleteOutlineIcon fontSize='medium' />
          </IconButton>
        </span>
      </Tooltip>
    </Box>
  </td>
)

// ── Component ────────────────────────────────────────────────────────────────

const SenderReceiverMapping = () => {
  const [modifiedCells, setModifiedCells] = useState({})
  const [loading, setLoading] = useState(false)
  const [snackbarData, setSnackbarData] = useState({
    message: '',
    severity: 'info',
  })
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)

  const [rows, setRows] = useState([])
  const [originalRows, setOriginalRows] = useState([])
  const [plantsDropdown, setPlantsDropdown] = useState([])
  const [costCentersDropdown, setCostCentersDropdown] = useState([])
  const [normParameters, setNormParameters] = useState([])

  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [rowToDelete, setRowToDelete] = useState(null)

  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const {
    verticalObject,
    siteObject,
    plantObject,
    year,
    screenTitle,
    jmdSelectedPlants,
  } = dataGridStore

  const isSRMappingRole = useMemo(
    () => keycloak?.realmAccess?.roles?.includes(SR_MAPPING_ROLE) || false,
    [keycloak?.realmAccess?.roles],
  )

  // sr_mapping_edit role: can update cost centers (and remarks) on existing rows
  const isSRMappingEditRole = useMemo(
    () => keycloak?.realmAccess?.roles?.includes(SR_MAPPING_EDIT_ROLE) || false,
    [keycloak?.realmAccess?.roles],
  )

  const PLANT_ID = plantObject?.id
  const lowerSiteName = siteObject?.name?.toLowerCase()
  const AOP_YEAR = year?.selectedYear

  const PLANT_ID_LIST = useMemo(
    () =>
      lowerSiteName === 'jmd'
        ? jmdSelectedPlants?.map((plant) => plant.id) || []
        : [PLANT_ID],
    [jmdSelectedPlants, lowerSiteName, PLANT_ID],
  )

  const cppPlantList = useMemo(
    () =>
      lowerSiteName === 'jmd'
        ? jmdSelectedPlants?.map((plant) => ({
            value: plant.id?.toLowerCase(),
            label: plant.name,
          }))
        : [
            {
              value: plantObject?.id?.toLowerCase(),
              label: plantObject?.name,
            },
          ],
    [jmdSelectedPlants, plantObject, lowerSiteName],
  )

  const handleDeleteClick = useCallback((dataItem) => {
    setRowToDelete(dataItem)
    setDeleteDialogOpen(true)
  }, [])

  // ── Data Fetching ──────────────────────────────────────────────────────────

  const fetchNormParameters = useCallback(async () => {
    if (!PLANT_ID_LIST?.length) return
    try {
      const results = await Promise.all(
        PLANT_ID_LIST.map((plantId) =>
          UtilityPlantApiServiceV2.getNormParameters(keycloak, plantId),
        ),
      )
      const data = results.flatMap((res) => res?.data || [])
      setNormParameters(data)
    } catch (error) {
      console.error('Error fetching norm parameters:', error)
      setNormParameters([])
    }
  }, [keycloak, PLANT_ID_LIST])

  const fetchAssociatedFieldIds = useCallback(async () => {
    if (!PLANT_ID_LIST?.length) return
    try {
      const plantsResponse = await UtilityPlantApiServiceV2.getSRMappingPlants(
        keycloak,
        PLANT_ID_LIST,
      )
      const plantsData = plantsResponse?.data || []
      const plantsOptions = plantsData.map((plant) => ({
        plantId: plant.plantId?.toLowerCase() || '',
        value: plant.plantName,
        label: plant.plantName || plant.plantCode || 'Unknown Plant',
        code: plant.plantCode || '',
        sourceName: plant.sourceName?.toLowerCase() || '',
        siteId: plant.siteId || '',
        siteName: plant.siteName || '',
        siteDisplayName: plant.siteDisplayName || '',
        verticalId: plant.verticalId || '',
        verticalName: plant.verticalName || '',
        verticalDisplayName: plant.verticalDisplayName || '',
      }))
      setPlantsDropdown(plantsOptions)

      const costCentersResponse =
        await UtilityPlantApiServiceV2.getSRMappingCostCenters(
          keycloak,
          PLANT_ID_LIST,
        )
      const costCentersData = costCentersResponse?.data || []
      const costCentersOptions = costCentersData.map((cc) => ({
        costCenterId: cc.id?.toLowerCase() || '',
        value: cc.costCenterName,
        label: cc.costCenterName || '',
        code: cc.costCenterCode || '',
        cppPlantFkId: cc.cppPlantFkId?.toLowerCase() || '',
      }))
      setCostCentersDropdown(costCentersOptions)
    } catch (error) {
      console.error('Error fetching dropdown options:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Error loading dropdown options',
        severity: 'error',
      })
    }
  }, [keycloak, PLANT_ID_LIST])

  const fetchData = useCallback(async () => {
    setLoading(true)
    try {
      const res = await UtilityPlantApiServiceV2.getSRMappingByPlant(
        keycloak,
        PLANT_ID_LIST,
        AOP_YEAR,
      )

      const apiRows = res?.data?.SRMappingResponse || res?.data || []

      if (!apiRows || apiRows.length === 0) {
        setRows([])
        setOriginalRows([])
        setSnackbarOpen(true)
        setSnackbarData({ message: 'No data found', severity: 'info' })
        setLoading(false)
        return
      }

      const formattedData = apiRows.map((item, index) => ({
        ...item,
        remarks: item?.remarks || '',
        id: item?.id || index + 1,
        apiId: item?.id,
        // Existing rows are only editable by sr_mapping_edit role.
        // sr_mapping-only users cannot edit any cell on existing rows
        // (they can still add new rows and delete via the ActionCell).
        isEditable: isSRMappingEditRole,
      }))
      setRows(formattedData)
      setOriginalRows(formattedData)
    } catch (error) {
      console.error('Error fetching SR mapping data:', error)
      setSnackbarOpen(true)
      setSnackbarData({ message: 'Error fetching data', severity: 'error' })
    } finally {
      setLoading(false)
    }
  }, [keycloak, PLANT_ID_LIST, AOP_YEAR, isSRMappingEditRole])

  useDebounce(
    () => {
      setRows([])
      setOriginalRows([])
      setModifiedCells({})

      if (PLANT_ID_LIST?.length && AOP_YEAR) {
        fetchData()
        fetchAssociatedFieldIds()
        fetchNormParameters()
      }
    },
    1000,
    [
      PLANT_ID_LIST,
      AOP_YEAR,
      fetchData,
      fetchAssociatedFieldIds,
      fetchNormParameters,
    ],
  )

  // ── Filter Functions ──────────────────────────────────────────────────────

  const getFilteredSenderUtilities = useCallback(
    (dataItem) => {
      const selectedPlantId = dataItem?.senderPlantId
      if (!selectedPlantId) return []
      const filtered = normParameters.filter(
        (np) =>
          np.normTypeFkId === 2 &&
          np.plantFkId?.toLowerCase() === selectedPlantId?.toLowerCase(),
      )
      return filtered.map((np) => ({
        value: np.displayName || np.name,
        label: np.displayName || np.name,
        sapMaterialCode: np.sapMaterialCode || '',
        uom: np.uom || '',
      }))
    },
    [normParameters],
  )

  const getFilteredReceiverUtilities = useCallback(
    (dataItem) => {
      const selectedPlantId = dataItem?.receiverPlantId
      if (!selectedPlantId) return []
      const filtered = normParameters.filter(
        (np) =>
          np.normTypeFkId === 1 &&
          np.plantFkId?.toLowerCase() === selectedPlantId?.toLowerCase(),
      )
      return filtered.map((np) => ({
        value: np.displayName || np.name,
        label: np.displayName || np.name,
        sapMaterialCode: np.sapMaterialCode || '',
        uom: np.uom || '',
      }))
    },
    [normParameters],
  )

  const getFilteredSenderCostCenters = useCallback(
    (dataItem) => {
      // Sender Cost Center: show all fetched for the selected CPP plants
      // (no per-row cppPlantId filter)
      return costCentersDropdown
    },
    [costCentersDropdown],
  )

  const getFilteredReceiverCostCenters = useCallback(
    (dataItem) => {
      // Receiver Cost Center: filter by cppPlantFkId matching the row's cppPlantId
      const selectedCppPlantId = dataItem?.cppPlantId
      if (!selectedCppPlantId) return []

      const filtered = costCentersDropdown.filter(
        (cc) =>
          cc.cppPlantFkId?.toLowerCase() === selectedCppPlantId?.toLowerCase(),
      )
      return filtered
    },
    [costCentersDropdown],
  )

  const getFilteredPlants = useCallback(
    (dataItem) => {
      // Sender Plant: show all plants fetched for the selected CPP plants
      // (no per-row cppPlantId filter — sender can be any plant in the list)
      return plantsDropdown
    },
    [plantsDropdown],
  )
  const getFilteredReceiverPlants = useCallback(
    (dataItem) => {
      const selectedCppPlantId = dataItem?.cppPlantId
      if (!selectedCppPlantId) return []

      const filtered = plantsDropdown.filter(
        (p) =>
          p.sourceName?.toLowerCase() === selectedCppPlantId?.toLowerCase() &&
          p.verticalName?.toLowerCase() === 'cpp',
      )
      return filtered
    },
    [plantsDropdown],
  )

  // ── Core Update Logic ──────────────────────────────────────────────────────

  const applyRowUpdate = useCallback((rowId, changedField, updates) => {
    // Get fields to reset based on the changed field
    const resetFields = DEPENDENCY_RESETS[changedField] || []

    // Build reset object with empty strings
    const resetUpdates = Object.fromEntries(
      resetFields.map((field) => [field, '']),
    )

    // Combine updates and resets — resets first so actual updates override
    // any reset fields (prevents empty-string reset from clobbering a value
    // that is both reset and set in the same operation).
    const allUpdates = {
      ...resetUpdates,
      ...updates,
      // Track that this row has been modified
      inEdit: true,
    }

    // Update rows
    setRows((prevRows) =>
      prevRows.map((row) =>
        row.id === rowId ? { ...row, ...allUpdates } : row,
      ),
    )

    // Update modifiedCells - ensure id/apiId are tracked so save payload
    // can identify existing rows
    setModifiedCells((prev) => {
      const existing = prev[rowId] || {}
      const currentRow = { id: rowId }
      return {
        ...prev,
        [rowId]: {
          ...existing,
          ...currentRow,
          ...allUpdates,
        },
      }
    })
  }, [])

  // ── Custom Item Change Handler ───────────────────────────────────────────

  const handleCustomItemChange = useCallback(
    (e) => {
      const { dataItem, field, value } = e

      if (!dataItem || !field) return

      // Handle cppPlantId change - reset everything
      if (field === 'cppPlantId') {
        applyRowUpdate(dataItem.id, field, { cppPlantId: value })
        return
      }

      // Handle text field changes
      const config = DROPDOWN_FIELD_CONFIG[field]
      if (!config) {
        // It's a text field or remark
        applyRowUpdate(dataItem.id, field, { [field]: value })
        return
      }

      // Handle dropdown changes
      if (!value) {
        // Clear the field
        applyRowUpdate(dataItem.id, field, { [field]: value })
        return
      }

      // Read the CURRENT row from state. e.dataItem is captured at edit-start
      // and can be stale if the user changed a parent field (e.g. plant) and
      // immediately tabbed into this cell. Using the live row ensures the
      // plant/cost-center context used for filtering matches what the
      // dropdown actually showed the user.
      const currentRow = rows.find((r) => r.id === dataItem.id) || dataItem

      // Get the appropriate dropdown data source
      let dropdownSource
      switch (config.source) {
        case 'plantsDropdown':
          dropdownSource = plantsDropdown
          break
        case 'costCentersDropdown':
          dropdownSource = costCentersDropdown
          break
        case 'normParameters':
          dropdownSource = normParameters
          break
        default:
          return
      }

      let source = dropdownSource

      if (config.group === 'Utility') {
        const selectedPlantId = currentRow[`${config.prefix}PlantId`]
        source = dropdownSource.filter(
          (item) =>
            item.normTypeFkId === config.normTypeFkId &&
            item.plantFkId?.toLowerCase() === selectedPlantId?.toLowerCase(),
        )
      } else if (config.group === 'Plant') {
        if (config.prefix === 'sender') {
          // Sender Plant: search across all fetched plants (no cppPlantId filter)
          source = dropdownSource
        } else {
          // Receiver Plant: filter by sourceName matching the row's cppPlantId
          // and vertical === 'cpp'
          const selectedCppPlantId = currentRow.cppPlantId
          source = dropdownSource.filter(
            (item) =>
              item.sourceName?.toLowerCase() ===
                selectedCppPlantId?.toLowerCase() &&
              item.verticalName?.toLowerCase() === 'cpp',
          )
        }
      } else if (config.group === 'CostCenter') {
        if (config.prefix === 'sender') {
          // Sender Cost Center: search across all fetched cost centers
          source = dropdownSource
        } else {
          // Receiver Cost Center: filter by cppPlantFkId matching the row's cppPlantId
          const selectedCppPlantId = currentRow.cppPlantId
          source = dropdownSource.filter(
            (item) =>
              item.cppPlantFkId?.toLowerCase() ===
              selectedCppPlantId?.toLowerCase(),
          )
        }
      }

      // Find the selected item within the context-filtered source.
      // For utilities, the dropdown value may be displayName OR name (when
      // displayName is empty), so check both fields.
      const valueLower = value?.toLowerCase()
      let selectedItem = source.find((item) => {
        const primary = (item[config.valueField] || item.value || '')
          .toString()
          .toLowerCase()
        if (primary === valueLower) return true
        // Fallback for utilities where displayName was empty and the
        // dropdown used `name` instead.
        if (config.group === 'Utility') {
          const fallback = (item.name || '').toString().toLowerCase()
          if (fallback === valueLower) return true
        }
        return false
      })

      if (!selectedItem) return

      // Build updates based on field type
      let updates = {}

      if (config.group === 'Utility') {
        // Utility fields
        updates = {
          [`${config.prefix}UtilityName`]:
            selectedItem[config.labelField] || value,
          [`${config.prefix}UtilityId`]: selectedItem[config.idField] || '',
          [`${config.prefix}UtilityCode`]: selectedItem[config.codeField] || '',
          [`${config.prefix}UtilityUOM`]: selectedItem[config.uomField] || '',
        }
      } else {
        // Plant or Cost Center fields
        updates = {
          [`${config.prefix}${config.group}Name`]:
            selectedItem[config.labelField] || value,
          [`${config.prefix}${config.group}Id`]:
            selectedItem[config.idField] || '',
          [`${config.prefix}${config.group}Code`]:
            selectedItem[config.codeField] || '',
        }
      }

      applyRowUpdate(dataItem.id, field, updates)
    },
    [rows, plantsDropdown, costCentersDropdown, normParameters, applyRowUpdate],
  )

  // ── Column Definitions ────────────────────────────────────────────────────

  // Stable wrapper that injects role + delete handler into the hoisted
  // ActionCell so the column array identity only changes when needed.
  const actionCell = useMemo(() => {
    const BoundActionCell = (props) => (
      <ActionCell
        {...props}
        isSRMappingRole={isSRMappingRole}
        onDelete={handleDeleteClick}
      />
    )
    BoundActionCell.displayName = 'BoundActionCell'
    return BoundActionCell
  }, [isSRMappingRole, handleDeleteClick])

  const columns = useMemo(
    () => [
      {
        field: 'cppPlantId',
        title: 'CPP Plant',
        widthT: 200,
        minWidth: 200,
        editable: isSRMappingRole,
        conditionalEditable: { dependsOn: 'isNew', editableValues: [true] },
        type: 'select',
        searchable: true,
        displayMode: 'label',
        options: cppPlantList,
      },
      {
        field: 'senderCostCenterName',
        title: 'Sender Cost Center',
        widthT: 220,
        minWidth: 220,
        type: 'select',
        dynamicOptions: true,
        displayMode: 'label',
        getOptions: getFilteredSenderCostCenters,
        editable: isSRMappingRole || isSRMappingEditRole,
      },
      {
        field: 'senderCostCenterCode',
        title: 'Sender Cost Center Code',
        widthT: 200,
        minWidth: 180,
        type: 'text',
        editable: false,
      },
      {
        field: 'senderPlantName',
        title: 'Sender Plant',
        widthT: 220,
        minWidth: 220,
        type: 'select',
        dynamicOptions: true,
        displayMode: 'label',
        getOptions: getFilteredPlants,
        editable: isSRMappingRole,
        conditionalEditable: { dependsOn: 'isNew', editableValues: [true] },
      },
      {
        field: 'senderPlantCode',
        title: 'Sender Plant Code',
        widthT: 180,
        minWidth: 180,
        type: 'text',
        editable: false,
      },
      {
        field: 'senderUtilityName',
        title: 'Utility',
        widthT: 220,
        minWidth: 220,
        type: 'select',
        dynamicOptions: true,
        displayMode: 'label',
        getOptions: getFilteredSenderUtilities,
        editable: isSRMappingRole,
        conditionalEditable: { dependsOn: 'isNew', editableValues: [true] },
      },
      {
        field: 'senderUtilityCode',
        title: 'Utility Code',
        widthT: 150,
        minWidth: 150,
        type: 'text',
        editable: false,
      },
      {
        field: 'senderUtilityUOM',
        title: 'Utility UOM',
        widthT: 150,
        minWidth: 150,
        type: 'text',
        editable: false,
      },
      {
        field: 'receiverCostCenterName',
        title: 'Receiver Cost Center',
        widthT: 220,
        minWidth: 220,
        editable: isSRMappingRole || isSRMappingEditRole,
        type: 'select',
        dynamicOptions: true,
        displayMode: 'label',
        getOptions: getFilteredReceiverCostCenters,
      },
      {
        field: 'receiverCostCenterCode',
        title: 'Receiver Cost Center Code',
        widthT: 200,
        minWidth: 200,
        type: 'text',
        editable: false,
      },
      {
        field: 'receiverPlantName',
        title: 'Receiver Plant',
        widthT: 220,
        minWidth: 220,
        type: 'select',
        dynamicOptions: true,
        displayMode: 'label',
        getOptions: getFilteredReceiverPlants,
        editable: isSRMappingRole,
        conditionalEditable: { dependsOn: 'isNew', editableValues: [true] },
      },
      {
        field: 'receiverPlantCode',
        title: 'Receiver Plant Code',
        widthT: 180,
        minWidth: 180,
        type: 'text',
        editable: false,
      },
      {
        field: 'receiverUtilityName',
        title: 'Receiver Utility',
        widthT: 220,
        minWidth: 220,
        type: 'select',
        dynamicOptions: true,
        displayMode: 'label',
        getOptions: getFilteredReceiverUtilities,
        editable: isSRMappingRole,
        conditionalEditable: { dependsOn: 'isNew', editableValues: [true] },
        hidden: false,
      },
      {
        field: 'receiverUtilityCode',
        title: 'Receiver Utility Code',
        widthT: 180,
        minWidth: 180,
        type: 'text',
        editable: false,
        hidden: false,
      },
      {
        field: 'receiverUtilityUOM',
        title: 'Receiver Utility UOM',
        widthT: 180,
        minWidth: 180,
        type: 'text',
        editable: false,
      },
      {
        field: 'remarks',
        title: 'Remarks',
        widthT: 200,
        minWidth: 200,
        type: 'textarea',
        editable: isSRMappingRole || isSRMappingEditRole,
      },
      {
        field: 'customActions',
        title: 'Action',
        type: 'customAction',
        minWidth: 100,
        className: 'k-text-center',
        cell: actionCell,
        editable: isSRMappingRole,
        // locked: true,
        // lockPosition: 'right',
      },
    ],
    [
      isSRMappingRole,
      isSRMappingEditRole,
      cppPlantList,
      getFilteredSenderCostCenters,
      getFilteredReceiverCostCenters,
      getFilteredPlants,
      getFilteredReceiverPlants,
      getFilteredSenderUtilities,
      getFilteredReceiverUtilities,
      actionCell,
    ],
  )

  // ── Delete Logic ──────────────────────────────────────────────────────────

  const handleConfirmDelete = async () => {
    if (!rowToDelete) return

    setDeleteDialogOpen(false)
    setLoading(true)

    try {
      if (rowToDelete.apiId != null) {
        await UtilityPlantApiServiceV2.deleteSRMapping(
          keycloak,
          rowToDelete.apiId,
          AOP_YEAR,
        )
      }
      // Remove the row from local state (covers both saved + unsaved rows).
      // No server refetch — keeps other unsaved local rows intact.
      setRows((prev) => prev.filter((r) => r.id !== rowToDelete.id))
      setOriginalRows((prev) => prev.filter((r) => r.id !== rowToDelete.id))
      setModifiedCells((prev) => {
        const next = { ...prev }
        delete next[rowToDelete.id]
        return next
      })
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'SR Mapping deleted successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error deleting SR mapping:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to delete SR Mapping. Please try again.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
      setRowToDelete(null)
    }
  }

  // ── Permissions ───────────────────────────────────────────────────────────

  const permissions = useMemo(
    () => ({
      showAction: true,
      addButton: isSRMappingRole,
      deleteButton: false,
      editButton: true,
      saveBtn: true,
      allAction: true,
      disableActionButtons: !(isSRMappingRole || isSRMappingEditRole),
      downloadExcelBtnFromUI: false,
      showExport: true,
      ExcelName: generateExcelName(dataGridStore, 'Sender_Receiver_Mapping'),
      showImport: false,
      showTitleNameBusiness: true,
      showTitle: true,
      titleName:
        screenTitle?.title || 'Sender Receiver Mapping (Utility for Utility)',
    }),
    [isSRMappingRole, isSRMappingEditRole, screenTitle, dataGridStore],
  )

  // ── Save / Export / Import ───────────────────────────────────────────────

  const saveChanges = async () => {
    setLoading(true)

    // Merge each modified cell with its corresponding row so that
    // identifying fields (id, apiId) and unchanged fields are preserved
    // when building the save payload.
    const modifiedData = Object.values(modifiedCells).map((cell) => {
      const row = rows.find((r) => r.id === cell.id) || {}
      return { ...row, ...cell }
    })
    const dataToSave = modifiedData.filter((row) => row.inEdit || row.isNew)

    if (!dataToSave || dataToSave.length === 0) {
      setSnackbarOpen(true)
      setSnackbarData({ message: 'No Records to Save!', severity: 'info' })
      setLoading(false)
      return
    }

    // Split into new rows (full multi-table save) and existing rows
    // (lightweight cost-center-only update).
    const newRows = dataToSave.filter((row) => row.isNew)
    const existingRows = dataToSave.filter((row) => !row.isNew)

    // ── Validate new rows: all REQUIRED_FIELDS must be filled ──────────────
    if (newRows.length > 0) {
      const missingNew = newRows.find((row) =>
        REQUIRED_FIELDS.some(
          (field) => !row[field] || row[field].toString().trim() === '',
        ),
      )
      if (missingNew) {
        const emptyField = REQUIRED_FIELDS.find(
          (field) =>
            !missingNew[field] || missingNew[field].toString().trim() === '',
        )
        const rowIndex = rows.findIndex((r) => r.id === missingNew.id)
        const rowLabel = `Row ${rowIndex >= 0 ? rowIndex + 1 : missingNew.id}`
        setSnackbarOpen(true)
        setSnackbarData({
          message: `${FIELD_LABELS[emptyField]} is required for: ${rowLabel}`,
          severity: 'error',
        })
        setLoading(false)
        return
      }
    }

    // ── Validate existing rows: at least one cost-center field must be filled
    if (existingRows.length > 0) {
      const missingBoth = existingRows.find(
        (row) =>
          (!row.senderCostCenterName ||
            row.senderCostCenterName.toString().trim() === '') &&
          (!row.receiverCostCenterName ||
            row.receiverCostCenterName.toString().trim() === ''),
      )
      if (missingBoth) {
        const rowIndex = rows.findIndex((r) => r.id === missingBoth.id)
        const rowLabel = `Row ${rowIndex >= 0 ? rowIndex + 1 : missingBoth.id}`
        setSnackbarOpen(true)
        setSnackbarData({
          message: `At least one of Sender or Receiver Cost Center is required for: ${rowLabel}`,
          severity: 'error',
        })
        setLoading(false)
        return
      }
    }

    // Remarks validation: if any row data was updated, remarks must be filled
    // and different from the original
    const remarksError = validateRowDataWithRemarks(
      dataToSave,
      originalRows,
      REQUIRED_FIELDS,
      'senderPlantName',
    )
    if (remarksError) {
      setSnackbarOpen(true)
      setSnackbarData({ message: remarksError, severity: 'error' })
      setLoading(false)
      return
    }

    try {
      const messages = []
      let allSuccess = true

      // ── Save new rows via the full multi-table API ──────────────────────
      if (newRows.length > 0) {
        const newPayload = newRows.map((item) => {
          const {
            inEdit,
            isNew,
            saveStatus,
            errDescription,
            isEditable,
            apiId,
            ...rest
          } = item
          return {
            ...rest,
            aopYear: AOP_YEAR,
            id: null,
          }
        })

        const newResponse =
          await UtilityPlantApiServiceV2.updateSRMappingsByPlant(
            keycloak,
            newPayload,
            AOP_YEAR,
          )

        if (newResponse?.code === 200) {
          messages.push(
            newResponse?.message || `${newRows.length} new record(s) created.`,
          )
        } else {
          allSuccess = false
          messages.push(newResponse?.message || 'Failed to save new records.')
        }
      }

      // ── Update existing rows via the lightweight cost-center API ────────
      if (existingRows.length > 0) {
        const ccPayload = existingRows.map((item) => {
          const payload = {
            id: item.apiId || item.id,
            remarks: item.remarks,
          }
          if (item.senderCostCenterId) {
            payload.senderCostCenterId = item.senderCostCenterId
          }
          if (item.receiverCostCenterId) {
            payload.receiverCostCenterId = item.receiverCostCenterId
          }
          return payload
        })

        const ccResponse =
          await UtilityPlantApiServiceV2.updateSRMappingCostCenters(
            keycloak,
            ccPayload,
          )

        if (ccResponse?.code === 200) {
          messages.push(
            ccResponse?.message ||
              `${existingRows.length} cost-center record(s) updated.`,
          )
        } else {
          allSuccess = false
          messages.push(ccResponse?.message || 'Failed to update cost centers.')
        }
      }

      setSnackbarOpen(true)
      setSnackbarData({
        message: messages.join(' '),
        severity: allSuccess ? 'success' : 'error',
      })

      if (allSuccess) {
        setModifiedCells({})
        await fetchData()
      }
    } catch (error) {
      console.error('Error saving SR mapping:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: 'Failed to save changes. Please try again.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleExcelUpload = async (file) => {
    if (!file) return

    setLoading(true)
    try {
      const response = await UtilityPlantApiServiceV2.importSRMappingExcel(
        file,
        keycloak,
      )

      if (response?.code === 200) {
        setSnackbarOpen(true)
        setSnackbarData({
          message: response?.message || 'Excel file imported successfully!',
          severity: 'success',
        })
        await fetchData()
        return
      }

      if (response?.code === 400 && response?.data) {
        try {
          const base64Data = response.data
          const binaryString = window.atob(base64Data)
          const bytes = new Uint8Array(binaryString.length)
          for (let i = 0; i < binaryString.length; i++) {
            bytes[i] = binaryString.charCodeAt(i)
          }
          const blob = new Blob([bytes], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
          })
          const url = window.URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.href = url
          link.download = `SRMapping_Errors_${new Date().getTime()}.xlsx`
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
          window.URL.revokeObjectURL(url)

          setSnackbarOpen(true)
          setSnackbarData({
            message:
              response?.message ||
              'Import failed with errors. Please check the downloaded file.',
            severity: 'error',
          })
          await fetchData()
          return
        } catch (downloadError) {
          console.error('Error downloading error file:', downloadError)
          setSnackbarOpen(true)
          setSnackbarData({
            message: 'Import failed but could not download error file.',
            severity: 'error',
          })
          return
        }
      }

      setSnackbarOpen(true)
      setSnackbarData({
        message: response?.message || 'Failed to import Excel file.',
        severity: 'error',
      })
    } catch (error) {
      console.error('Error uploading Excel file:', error)
      setSnackbarOpen(true)
      setSnackbarData({
        message: `Failed to import Excel file: ${error?.message || 'Unknown error'}`,
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleExport = async () => {
    setLoading(true)
    setSnackbarOpen(true)
    setSnackbarData({
      message: 'Excel download started!',
      severity: 'info',
    })

    try {
      await UtilityPlantApiServiceV2.exportSRMappingExcel(
        keycloak,
        PLANT_ID_LIST,
        AOP_YEAR,
      )
      setSnackbarData({
        message: 'Excel download completed successfully!',
        severity: 'success',
      })
    } catch (error) {
      console.error('Error exporting SR mapping:', error)
      setSnackbarData({
        message: 'Excel download failed. Please try again.',
        severity: 'error',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

  // ── Render ─────────────────────────────────────────────────────────────────

  return (
    <Box>
      <LoaderBackdrop open={!!loading} />
      <Stack sx={{ mt: 2 }}>
        <AdvanceKendoTable
          columns={columns}
          rows={rows}
          setRows={setRows}
          modifiedCells={modifiedCells}
          setModifiedCells={setModifiedCells}
          title={'Sender Receiver Mapping'}
          permissions={permissions}
          handleExport={handleExport}
          handleExcelUpload={handleExcelUpload}
          saveChanges={saveChanges}
          fetchData={fetchData}
          snackbarData={snackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
          setSnackbarData={setSnackbarData}
          customHeight={70}
          customItemChange={handleCustomItemChange}
          paginationConfig={{
            threshold: 100,
            buttonCount: 5,
            pageSizes: [10, 20, 50, 100],
            defaultPageSize: 100,
          }}
          handleRemarkCellClick={handleRemarkCellClick}
          remarkDialogOpen={remarkDialogOpen}
          setRemarkDialogOpen={setRemarkDialogOpen}
          currentRemark={currentRemark}
          setCurrentRemark={setCurrentRemark}
          currentRowId={currentRowId}
          setCurrentRowId={setCurrentRowId}
        />
      </Stack>

      <DeleteDialog
        openDeleteDialogeBox={deleteDialogOpen}
        setOpenDeleteDialogeBox={setDeleteDialogOpen}
        deleteTheRecord={handleConfirmDelete}
        message='Are you sure you want to delete this SR Mapping record?'
        confirmButtonText='Delete'
      />
    </Box>
  )
}

export default SenderReceiverMapping
