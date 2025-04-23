import { KeyboardArrowLeft, KeyboardArrowRight } from '@mui/icons-material'
import CloseIcon from '@mui/icons-material/Close'
import ViewKanbanIcon from '@mui/icons-material/ViewKanban'
import ViewListIcon from '@mui/icons-material/ViewList'
import RefreshIcon from '@mui/icons-material/Refresh'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import IconButton from '@mui/material/IconButton'
import Snackbar from '@mui/material/Snackbar'
import ToggleButton from '@mui/material/ToggleButton'
import ToggleButtonGroup from '@mui/material/ToggleButtonGroup'
import Tooltip from '@mui/material/Tooltip'
import Menu from '@mui/material/Menu'
import MenuItem from '@mui/material/MenuItem'
import Typography from '@mui/material/Typography'
import { useSession } from 'SessionStoreContext'
import MainCard from 'components/MainCard'
import React, { Suspense, lazy, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { CaseService } from '../../services'

const DataGrid = lazy(() =>
  import('@mui/x-data-grid').then((module) => ({ default: module.DataGrid })),
)
const Kanban = lazy(() =>
  import('components/Kanban/kanban').then((module) => ({
    default: module.Kanban,
  })),
)
const CaseForm = lazy(() =>
  import('../caseForm/caseForm').then((module) => ({
    default: module.CaseForm,
  })),
)
const NewCaseForm = lazy(() =>
  import('../caseForm/newCaseForm').then((module) => ({
    default: module.NewCaseForm,
  })),
)

export const CaseList = ({ status, caseDefId }) => {
  const { t } = useTranslation()
  const [stages, setStages] = useState([])
  const [cases, setCases] = useState([])
  const [aCase, setACase] = useState(null)
  const [newCaseDefId, setNewCaseDefId] = useState(null)
  const [lastCreatedCase, setLastCreatedCase] = useState(null)
  const [openCaseForm, setOpenCaseForm] = useState(false)
  const [openNewCaseForm, setOpenNewCaseForm] = useState(false)
  const [view, setView] = React.useState('list')
  const [snackOpen, setSnackOpen] = useState(false)
  const keycloak = useSession()
  const [caseDefs, setCaseDefs] = useState([])
  const [fetching, setFetching] = useState(false)
  const [filter, setFilter] = useState({
    sort: '',
    limit: 10,
    after: '',
    before: '',
    cursors: {},
    hasPrevious: false,
    hasNext: false,
  })

  const [anchorEl, setAnchorEl] = useState(null)
  const open = Boolean(anchorEl)
  const pageSizeOptions = [5, 10, 25, 50]

  useEffect(() => {
    fetchCases(
      setFetching,
      keycloak,
      caseDefId,
      setStages,
      status,
      filter,
      setCases,
      setFilter,
    )
  }, [caseDefId, status, openNewCaseForm])

  useEffect(() => {
    CaseService.getCaseDefinitions(keycloak).then((resp) => {
      setCaseDefs(resp)
    })
  }, [])

  const handleRefresh = () => {
    fetchCases(
      setFetching,
      keycloak,
      caseDefId,
      setStages,
      status,
      filter,
      setCases,
      setFilter,
    )
  }

  const makeColumns = () => {
    return [
      {
        field: 'businessKey',
        headerName: t('pages.caselist.datagrid.columns.businesskey'),
        width: 150,
      },
      {
        field: 'statusDescription',
        headerName: t('pages.caselist.datagrid.columns.statusdescription'),
        width: 150,
      },
      {
        field: 'stage',
        headerName: t('pages.caselist.datagrid.columns.stage'),
        width: 220,
      },
      {
        field: 'createdAt',
        headerName: t('pages.caselist.datagrid.columns.createdat'),
        width: 220,
      },
      {
        field: 'ownerName',
        headerName: t('pages.caselist.datagrid.columns.caseOwnerName'),
        width: 150,
        valueGetter: (value, row) => row?.owner?.name,
      },
      {
        field: 'queueId',
        headerName: t('pages.caselist.datagrid.columns.queue'),
        width: 200,
      },
      {
        field: 'action',
        headerName: '',
        sortable: false,
        renderCell: (data) => {
          const onClick = (e) => {
            setACase(data.row)
            e.stopPropagation()
            setOpenCaseForm(true)
          }

          return (
            <Button onClick={onClick}>
              {t('pages.caselist.datagrid.action.details')}
            </Button>
          )
        },
      },
    ]
  }

  const handleCloseCaseForm = () => {
    setOpenCaseForm(false)
    fetchCases(
      setFetching,
      keycloak,
      caseDefId,
      setStages,
      status,
      filter,
      setCases,
      setFilter,
    )
  }

  const handleCloseNewCaseForm = () => {
    setOpenNewCaseForm(false)
    setSnackOpen(true)
  }

  const handleNewCaseAction = () => {
    setLastCreatedCase(null)
    setNewCaseDefId(caseDefId)
    setOpenNewCaseForm(true)
  }

  const handleChangeView = (event, nextView) => {
    if (nextView !== null) {
      setView(nextView)
    }
  }

  const fetchKanbanConfig = () => {
    return caseDefs.find((o) => o.id === caseDefId).kanbanConfig
  }

  const handleCloseSnack = (event, reason) => {
    if (reason === 'clickaway') {
      return
    }

    setSnackOpen(false)
  }

  const snackAction = lastCreatedCase && (
    <React.Fragment>
      <Button
        color='primary'
        size='small'
        onClick={() => {
          setACase({
            businessKey: lastCreatedCase.businessKey,
            caseDefinitionId: caseDefId,
          })
          setOpenCaseForm(true)
          handleCloseSnack()
        }}
      >
        {lastCreatedCase.businessKey}
      </Button>
      <IconButton
        size='small'
        aria-label='close'
        color='inherit'
        onClick={handleCloseSnack}
      >
        <CloseIcon fontSize='small' />
      </IconButton>
    </React.Fragment>
  )

  const handlerNextPage = () => {
    setFetching(true)

    const next = {
      sort: filter.sort,
      limit: filter.limit,
      after: filter.cursors.after,
    }

    CaseService.filterCase(keycloak, caseDefId, status, next)
      .then((resp) => {
        const { data, paging } = resp

        setCases(data)
        setFilter({
          ...filter,
          cursors: paging.cursors,
          hasPrevious: paging.hasPrevious,
          hasNext: paging.hasNext,
        })
      })
      .finally(() => {
        setFetching(false)
      })
  }

  const handlerPriorPage = () => {
    setFetching(true)

    const prior = {
      sort: filter.sort,
      limit: filter.limit,
      before: filter.cursors.before,
    }

    CaseService.filterCase(keycloak, caseDefId, status, prior)
      .then((resp) => {
        const { data, paging } = resp

        setCases(data)
        setFilter({
          ...filter,
          cursors: paging.cursors,
          hasPrevious: paging.hasPrevious,
          hasNext: paging.hasNext,
        })
      })
      .finally(() => {
        setFetching(false)
      })
  }

  const handlePageSizeClick = (event) => {
    setAnchorEl(event.currentTarget)
  }

  const handlePageSizeClose = () => {
    setAnchorEl(null)
  }

  const handlePageSizeSelect = (pageSize) => {
    setFetching(true)

    CaseService.filterCase(keycloak, caseDefId, status, { limit: pageSize })
      .then((resp) => {
        const { data, paging } = resp

        setCases(data)
        setFilter({
          ...filter,
          limit: pageSize,
          cursors: paging.cursors,
          hasPrevious: paging.hasPrevious,
          hasNext: paging.hasNext,
        })
      })
      .finally(() => {
        setFetching(false)
      })

    handlePageSizeClose()
  }

  return (
    <div style={{ height: 650, width: '100%' }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <Box>
          {caseDefId && (
            <Button
              id='basic-button'
              onClick={handleNewCaseAction}
              variant='contained'
            >
              {t('pages.caselist.action.newcase')}
            </Button>
          )}
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          {caseDefId && (
            <ToggleButtonGroup
              orientation='horizontal'
              value={view}
              exclusive
              onChange={handleChangeView}
              sx={{ mr: 2 }}
            >
              <ToggleButton value='list' aria-label='list'>
                <ViewListIcon />
              </ToggleButton>
              <ToggleButton value='kanban' aria-label='kanban'>
                <ViewKanbanIcon />
              </ToggleButton>
            </ToggleButtonGroup>
          )}
          <Tooltip title={t('pages.caselist.action.refresh')}>
            <IconButton
              onClick={handleRefresh}
              color='primary'
              disabled={fetching}
            >
              <RefreshIcon />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>

      <MainCard sx={{ mt: 2 }} content={false}>
        <Box>
          {view === 'list' && (
            <div>
              <Suspense fallback={<div>Loading...</div>}>
                <Box
                  sx={{
                    height: 500,
                    width: '100%',
                    display: 'flex',
                    flexDirection: 'column',
                  }}
                >
                  <DataGrid
                    sx={{
                      flex: 1,
                      width: '100%',
                      backgroundColor: '#ffffff',
                      mt: 1,
                      '& .MuiDataGrid-main': { flex: 1 },
                      '& .MuiDataGrid-footerContainer': {
                        display: 'none',
                      },
                    }}
                    rows={cases}
                    columns={makeColumns()}
                    getRowId={(row) => row.businessKey}
                    loading={fetching}
                    hideFooter={true}
                    disableSelectionOnClick
                  />

                  <Box
                    sx={{
                      padding: '16px',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      borderTop: '1px solid rgba(224, 224, 224, 1)',
                      backgroundColor: '#ffffff',
                    }}
                  >
                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                      <Typography variant='body2' sx={{ mr: 2 }}>
                        Rows per page:
                      </Typography>
                      <Button
                        aria-controls={open ? 'page-size-menu' : undefined}
                        aria-haspopup='true'
                        aria-expanded={open ? 'true' : undefined}
                        onClick={handlePageSizeClick}
                        variant='text'
                        size='small'
                        endIcon={<KeyboardArrowRight />}
                        sx={{ minWidth: '75px' }}
                      >
                        {filter.limit}
                      </Button>
                      <Menu
                        id='page-size-menu'
                        anchorEl={anchorEl}
                        open={open}
                        onClose={handlePageSizeClose}
                        MenuListProps={{
                          'aria-labelledby': 'page-size-button',
                        }}
                      >
                        {pageSizeOptions.map((option) => (
                          <MenuItem
                            key={option}
                            onClick={() => handlePageSizeSelect(option)}
                            selected={filter.limit === option}
                          >
                            {option}
                          </MenuItem>
                        ))}
                      </Menu>
                    </Box>

                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                      <Typography variant='body2' sx={{ mx: 2 }}>
                        {cases.length > 0
                          ? filter.hasNext
                            ? `1-${cases.length} of more than ${cases.length}`
                            : `1-${cases.length} of ${cases.length}`
                          : '0-0 of 0'}
                      </Typography>

                      <IconButton
                        onClick={handlerPriorPage}
                        disabled={!filter.hasPrevious || fetching}
                      >
                        <KeyboardArrowLeft />
                      </IconButton>
                      <IconButton
                        onClick={handlerNextPage}
                        disabled={!filter.hasNext || fetching}
                      >
                        <KeyboardArrowRight />
                      </IconButton>
                    </Box>
                  </Box>
                </Box>
              </Suspense>
            </div>
          )}
          {view === 'kanban' && (
            <Suspense fallback={<div>Loading...</div>}>
              <Kanban
                stages={stages}
                cases={cases}
                caseDefId={caseDefId}
                kanbanConfig={fetchKanbanConfig()}
                setACase={setACase}
                setOpenCaseForm={setOpenCaseForm}
              />
            </Suspense>
          )}
        </Box>
      </MainCard>

      <br />

      {openCaseForm && (
        <CaseForm
          aCase={aCase}
          handleClose={handleCloseCaseForm}
          open={openCaseForm}
          keycloak={keycloak}
        />
      )}

      {openNewCaseForm && (
        <NewCaseForm
          handleClose={handleCloseNewCaseForm}
          open={openNewCaseForm}
          caseDefId={newCaseDefId}
          setLastCreatedCase={setLastCreatedCase}
        />
      )}

      {lastCreatedCase && (
        <Snackbar
          open={snackOpen}
          autoHideDuration={6000}
          message='Case created'
          onClose={handleCloseSnack}
          action={snackAction}
        />
      )}
    </div>
  )
}

function fetchCases(
  setFetching,
  keycloak,
  caseDefId,
  setStages,
  status,
  filter,
  setCases,
  setFilter,
) {
  setFetching(true)

  CaseService.getCaseDefinitionsById(keycloak, caseDefId)
    .then((resp) => {
      resp.stages.sort((a, b) => a.index - b.index).map((o) => o.name)
      setStages(resp.stages)
      return CaseService.filterCase(keycloak, caseDefId, status, filter)
    })
    .then((resp) => {
      const { data, paging } = resp
      setCases(data)
      setFilter({
        ...filter,
        cursors: paging.cursors,
        hasPrevious: paging.hasPrevious,
        hasNext: paging.hasNext,
      })
    })
    .finally(() => {
      setFetching(false)
    })
}
