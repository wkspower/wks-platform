import React, { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Stack from '@mui/material/Stack'
import { DataGrid } from '@mui/x-data-grid'
import MainCard from 'components/MainCard'
import { CaseDefForm } from '../caseDefForm/caseDefForm'
import CmmnImportDialog from '../cmmnImport/cmmnImportDialog'
import { CaseDefService } from 'services'
import { useSession } from 'SessionStoreContext'

export const CaseDefList = () => {
  const [caseDefs, setCaseDefs] = useState([])
  const [aCaseDef, setACaseDef] = useState(null)
  const [openCaseDefForm, setOpenCaseDefForm] = useState(false)
  const [openCmmnImport, setOpenCmmnImport] = useState(false)
  const [fetching, setFetching] = useState(false)
  const keycloak = useSession()
  const { t } = useTranslation()

  useEffect(() => {
    setFetching(true)

    CaseDefService.getAll(keycloak)
      .then((data) => {
        setCaseDefs(data)
      })
      .finally(() => {
        setFetching(false)
      })
    // Refetch when either dialog closes — an import creates a case definition too.
  }, [openCaseDefForm, openCmmnImport])

  const columns = [
    { field: 'id', headerName: 'Id', width: 300 },
    { field: 'name', headerName: 'Name', width: 220 },
    {
      field: 'action',
      headerName: '',
      sortable: false,
      renderCell: (params) => {
        const onClick = (e) => {
          setACaseDef(params.row)
          e.stopPropagation() // don't select this row after clicking
          setOpenCaseDefForm(true)
        }

        return (
          <React.Fragment>
            <Button onClick={onClick}>Edit</Button>
          </React.Fragment>
        )
      },
    },
  ]

  const handleCloseCaseDefForm = () => {
    setOpenCaseDefForm(false)
  }

  const handleNewCaseDef = () => {
    setACaseDef({
      status: 'new',
      id: '',
      name: '',
      formKey: '',
      stagesLifecycleProcessKey: '',
      stages: [{ id: 0, index: 0, name: 'Stage 0' }],
      taskCompleteHooks: [],
    })
    setOpenCaseDefForm(true)
  }

  return (
    <div style={{ height: 650, width: '100%' }}>
      <Stack direction='row' spacing={1}>
        <Button
          id='basic-button'
          variant='contained'
          onClick={handleNewCaseDef}
        >
          New
        </Button>
        <Button variant='outlined' onClick={() => setOpenCmmnImport(true)}>
          {t('pages.cmmnImport.action')}
        </Button>
      </Stack>
      <MainCard sx={{ mt: 2 }} content={false}>
        <Box>
          <DataGrid
            sx={{ height: 650, width: '100%', backgroundColor: '#ffffff' }}
            rows={caseDefs}
            columns={columns}
            pageSize={10}
            loading={fetching}
            rowsPerPageOptions={[10]}
          />
        </Box>
      </MainCard>
      {aCaseDef && (
        <CaseDefForm
          caseDefParam={aCaseDef}
          handleClose={handleCloseCaseDefForm}
          open={openCaseDefForm}
        />
      )}
      <CmmnImportDialog
        open={openCmmnImport}
        handleClose={() => setOpenCmmnImport(false)}
      />
    </div>
  )
}
