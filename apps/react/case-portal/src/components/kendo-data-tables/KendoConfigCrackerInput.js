// CrackerConfig.jsx
import { Box, Tab, Tabs } from '@mui/material'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { useSelector } from 'react-redux'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'
import getEnhancedAOPColDefs from 'components/data-tables/CommonHeader/kendo_ConfigHeader'
import KendoDataTables from './index'
import { DataService } from 'services/DataService'

const CrackerConfig = ({ keycloak }) => {
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange, oldYear } = dataGridStore
  const isOldYear = oldYear?.oldYear
  const vertName = verticalChange?.selectedVertical || ''
  const lowerVertName = vertName.toLowerCase() || 'meg'
  //   const [snackbarData, setSnackbarData] = useState({
  //     message: '',
  //     severity: 'info',
  //   })
  // const [snackbarOpen, setSnackbarOpen] = useState(false)
  const plantId = JSON.parse(localStorage.getItem('selectedPlant'))?.id
    // if (!row?.isEditable) return

  // ── Tab names & “static” data placeholders ──
  // const [allProducts, setAllProducts] = useState([])

  const rawTabs = [
    'feed',
    'composition',
    'hydrogenation',
    'recovery',
    'optimizing',
    'furnace',
  ]
  const allModes = ['5F', '4F', '4F+D']
  const [rawTabIndex, setRawTabIndex] = useState(0)

    // —— C2/C3 (existing) ——

    // —— Hexene Purge Gas ——

    // —— Import Propane ——
  const [remarkDialogOpen, setRemarkDialogOpen] = useState(false)
  const [currentRemark, setCurrentRemark] = useState('')
  const [currentRowId, setCurrentRowId] = useState(null)
  const handleRemarkCellClick = (row) => {
    setCurrentRemark(row.remarks || '')
    setCurrentRowId(row.id)
    setRemarkDialogOpen(true)
  }

    // —— BPCL Kochi Propylene ——
  const [selectMode, setSelectMode] = useState(allModes[0])

    // —— FCC C3 ——

    // —— LDPE Off Gas ——

    // —— Additional Feed (Default Composition) ——
  const [rowsByTab, setRowsByTab] = useState({
    feed: [],
    composition: [],

    hydrogenation: [],

    recovery: [],

    optimizing: [],
    furnace: [],
  })

  // ── Helper to update only one slice of rowsByTab ──
  const setRowsForTab = useCallback((tabName, newRows) => {
    setRowsByTab((prev) => ({ ...prev, [tabName]: newRows }))
  }, [])

  // ── Single “fetch” function that fills rowsByTab.feed … etc. ──
  const fetchCrackerRows = useCallback(
    async (tabName, mode) => {
      if (tabName) {
        try {
          // Call the backend once; assume it returns an object like:
          // { feed: [...], composition: [...], hydrogenation: [...], …, furnace: [...] }
          const spyroVM = await DataService.getSpyroInputData(keycloak, mode)

          // If spyroVM.data doesn't exist or is missing, default to empty arrays:
          const payload = spyroVM.data || {}

          setRowsByTab({
            feed: payload || [],
            composition: payload || [],
            hydrogenation: payload || [],
            recovery: payload || [],
            optimizing: payload || [],
            furnace: payload || [],
          })
        } catch (err) {
          console.error('Error loading Spyro‑Input data:', err)
          // Fallback: leave “feed” unchanged or set to empty
          setRowsForTab('feed', rowsByTab.feed)
        }
      } else {


    // Simulate network delay
    setTimeout(() => {
          const staticData = {
            composition: [
              /* … your compositionRows here … */
            ],
            hydrogenation: [
              /* … your hydrogenationRows here … */
            ],
            recovery: [
              /* … your recoveryRows here … */
            ],
            optimizing: [
              /* … your optimizingRows here … */
            ],
            furnace: [
              /* … your furnaceRows here … */
            ],
      }
          setRowsForTab(tabName, staticData[tabName] || [])
    }, 500) // 500ms delay to mimic async
      }
    },
    [keycloak, rowsByTab, setRowsByTab],
  )

  // 5️⃣ Whenever the selected tab changes, reload that tab’s rows
  useEffect(() => {
    const activeTab = rawTabs[rawTabIndex]
    fetchCrackerRows(activeTab, selectMode)
    console.log(selectMode)
  }, [rawTabIndex, selectMode, plantId])
  const getAdjustedPermissions = (permissions, isOldFlag) => {
    if (isOldFlag !== 1) return permissions
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
      isOldYear: true,
      allAction: false,
    }
  }

  const adjustedPermissions = getAdjustedPermissions(
    {
      showAction: false,
      addButton: true,
      deleteButton: false,
      editButton: false,
      showUnit: false,
      showModes: lowerVertName === 'cracker',
      saveWithRemark: true,
      saveBtn: true,
      allAction: lowerVertName === 'cracker',
      modes: allModes,
    },
    isOldYear,
  )
  const headerMap = generateHeaderNames(localStorage.getItem('year'))
  const productionColumns = useMemo(() => {
    // console.log(props)
    // const productId = props.dataItem.normParameterFKId
    // const product = allProducts.find((p) => p.id === productId)
    // const displayName = product?.displayName || ''
    // console.log(displayName)

    return getEnhancedAOPColDefs({
    // allGradesReciepes,
    // allProducts,
    headerMap,
    handleRemarkCellClick,
    configType:
      rawTabs[rawTabIndex] === 'composition'
        ? 'cracker_composition'
        : 'cracker', // columnConfig,
  })
  }, [headerMap, rawTabIndex])
  const NormParameterIdCell = (props) => <td>{props.dataItem.particulars}</td>
  return (
    <Box>
      {/* ─── Tabs ─── */}
      <Tabs
        sx={{
          borderBottom: '1px solid #ccc',
          '.MuiTabs-indicator': { display: 'none' },
          // margin: '-35px 0px -8px 0%',
        }}
        textColor='primary'
        indicatorColor='primary'
        value={rawTabIndex}
        onChange={(_, newIndex) => setRawTabIndex(newIndex)}
      >
        {rawTabs.map((tabId) => (
          <Tab
            key={tabId}
            label={tabId}
            sx={{
              textTransform: 'capitalize',
              border: '1px solid #ADD8E6',
              borderBottom: '1px solid #ADD8E6',
            }}
          />
        ))}
      </Tabs>

      {/* ─── Only one KendoDataTables instance, depending on active tab ─── */}
      <Box sx={{ marginTop: 2 }}>
        {rawTabs.map((tabName, idx) => {
          if (idx !== rawTabIndex) return null
              return (
                  <KendoDataTables
              key={tabName}
              rows={rowsByTab[tabName]}
              setRows={(newArr) => setRowsForTab(tabName, newArr)}
              fetchData={() => fetchCrackerRows(tabName, selectMode)}
              configType={
                tabName === 'composition' ? 'cracker_composition' : 'cracker'
              }
              groupBy={
                tabName === 'composition' ? 'ParticularsType' : undefined
              }
                    handleRemarkCellClick={handleRemarkCellClick}
                    NormParameterIdCell={NormParameterIdCell}
                    columns={productionColumns}
                    remarkDialogOpen={remarkDialogOpen}
                    setRemarkDialogOpen={setRemarkDialogOpen}
                    currentRemark={currentRemark}
                    setCurrentRemark={setCurrentRemark}
                    currentRowId={currentRowId}
                    permissions={adjustedPermissions}
              selectMode={selectMode}
              setSelectMode={setSelectMode}
                  />
          )
        })}
                </Box>
    </Box>
              )
}

export default CrackerConfig
// import { Box, Tab, Tabs } from '@mui/material'
// import { generateHeaderNames } from 'components/Utilities/generateHeaders'
// import KendoDataTables from './index'
//   //       case 'composition':
//   return (
//     <Box>
//                   <KendoDataTables
//                     rows={compositionRowsDummy}
//                     setRows={setCompositionRows}
//                     fetchData={() => fetchCrackerRows('composition')}
//                     configType='cracker_composition'
//                     groupBy='ParticularsType'
//                     handleRemarkCellClick={handleRemarkCellClick}
//                     NormParameterIdCell={NormParameterIdCell}
//                     columns={productionColumns}
//                     remarkDialogOpen={remarkDialogOpen}
//                     setRemarkDialogOpen={setRemarkDialogOpen}
//                     currentRemark={currentRemark}
//                     setCurrentRemark={setCurrentRemark}
//                     currentRowId={currentRowId}
//                     permissions={adjustedPermissions}
//                     selectMode={selectMode}
//                     setSelectMode={setSelectMode}
//                   />

//             case 'hydrogenation':
//               return (
//                 <Box>
//                   <KendoDataTables
//                     rows={hydrogenationRowsDummy}
//                     setRows={setHydrogenationRows}
//                     fetchData={() => fetchCrackerRows('hydrogenation')}
//                     configType='cracker'
//                     handleRemarkCellClick={handleRemarkCellClick}
//                     NormParameterIdCell={NormParameterIdCell}
//                     columns={productionColumns}
//                     remarkDialogOpen={remarkDialogOpen}
//                     setRemarkDialogOpen={setRemarkDialogOpen}
//                     currentRemark={currentRemark}
//                     setCurrentRemark={setCurrentRemark}
//                     currentRowId={currentRowId}
//                     permissions={adjustedPermissions}
//                     selectMode={selectMode}
//                     setSelectMode={setSelectMode}
//                   />

//             case 'recovery':
//               return (
//                 <Box>
//                   <KendoDataTables
//                     rows={recoveryRowsDummy}
//                     setRows={setRecoveryRows}
//                     fetchData={() => fetchCrackerRows('recovery')}
//                     configType='cracker'
//                     handleRemarkCellClick={handleRemarkCellClick}
//                     NormParameterIdCell={NormParameterIdCell}
//                     columns={productionColumns}
//                     remarkDialogOpen={remarkDialogOpen}
//                     setRemarkDialogOpen={setRemarkDialogOpen}
//                     currentRemark={currentRemark}
//                     setCurrentRemark={setCurrentRemark}
//                     currentRowId={currentRowId}
//                     permissions={adjustedPermissions}
//                     selectMode={selectMode}
//                     setSelectMode={setSelectMode}
//                   />

//             case 'optimizing':
//               return (
//                 <Box>
//                   <KendoDataTables
//                     rows={optimizingRowsDummy}
//                     setRows={setOptimizingRows}
//                     fetchData={() => fetchCrackerRows('optimizing')}
//                     configType='cracker'
//                     handleRemarkCellClick={handleRemarkCellClick}
//                     NormParameterIdCell={NormParameterIdCell}
//                     columns={productionColumns}
//                     remarkDialogOpen={remarkDialogOpen}
//                     setRemarkDialogOpen={setRemarkDialogOpen}
//                     currentRemark={currentRemark}
//                     setCurrentRemark={setCurrentRemark}
//                     currentRowId={currentRowId}
//                     permissions={adjustedPermissions}
//                     selectMode={selectMode}
//                     setSelectMode={setSelectMode}
//                   />

//             case 'furnace':
//               return (
//                 <Box>
//                   <KendoDataTables
//                     rows={furnaceRowsDummy}
//                     setRows={setFurnaceRows}
//                     fetchData={() => fetchCrackerRows('furnace')}
//                     configType='cracker'
//                     handleRemarkCellClick={handleRemarkCellClick}
//                     NormParameterIdCell={NormParameterIdCell}
//                     columns={productionColumns}
//                     remarkDialogOpen={remarkDialogOpen}
//                     setRemarkDialogOpen={setRemarkDialogOpen}
//                     currentRemark={currentRemark}
//                     setCurrentRemark={setCurrentRemark}
//                     currentRowId={currentRowId}
//                     permissions={adjustedPermissions}
//                     selectMode={selectMode}
//                     setSelectMode={setSelectMode}
//                   />

//             default:
//               return null
//           }
//         })()}
//       </Box>
//     </Box>
//   )
// }

// export default CrackerConfig
