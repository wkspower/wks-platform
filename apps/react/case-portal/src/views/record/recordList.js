import { useEffect, useState } from 'react'
import Box from '@mui/material/Box'
import { DataGrid } from '@mui/x-data-grid'
import MainCard from 'components/MainCard'
import Button from '@mui/material/Button'
import { RecordForm } from './recordForm'
import { useTranslation } from 'react-i18next'
import { RecordService } from '../../services'
import { useSession } from 'SessionStoreContext'

export const RecordList = ({ recordTypeId }) => {
  const [records, setRecords] = useState([])
  const [record, setRecord] = useState(null)
  const [recordType, setRecordType] = useState([])
  const [openForm, setOpenForm] = useState(false)
  const [columns, setColumns] = useState([])
  const [mode, setMode] = useState('new')
  const { t } = useTranslation()
  const keycloak = useSession()

  useEffect(() => {
    RecordService.getRecordTypeById(keycloak, recordTypeId)
      .then((data) => {
        let dynamicColumns = []

        data.fields.components
          .filter((element) => element.input === true)
          .forEach((element) => {
            const key = element.key
            const label = element.label

            dynamicColumns.push({ 
              field: key, 
              headerName: label, 
              width: 100, 
              flex: 1,
            })
          })

        dynamicColumns.push({
          field: 'action',
          headerName: '',
          sortable: false,
          renderCell: (params) => {
            const onDetailsClick = (e) => {
              setRecord(params.row)
              e.stopPropagation() // don't select this row after clicking
              setMode('update')
              setOpenForm(true)
            }

            return (
              <Button onClick={onDetailsClick}>
                {t('pages.recordlist.datagrid.action.details')}
              </Button>
            )
          },
        })
        setColumns(dynamicColumns)
        setRecordType(data)
      })
      .catch((err) => {
        console.log(err.message)
      })

    RecordService.getRecordById(keycloak, recordTypeId)
      .then((data) => {
        setRecords(data)
      })
      .catch((err) => {
        console.log(err.message)
      })
  }, [recordTypeId, openForm])

  const handleCloseForm = () => {
    setOpenForm(false)
  }

  const handleNew = () => {
    setMode('new')
    setRecord({})
    setOpenForm(true)
  }

  return (
    <div style={{ height: 650, width: '100%' }}>
      <Button id='basic-button' variant='contained' onClick={handleNew}>
        {t('pages.recordlist.action.newrecord')}
      </Button>
      <MainCard sx={{ mt: 2 }} content={false}>
        <Box>
          <DataGrid
            sx={{ height: 650, width: '100%', backgroundColor: '#ffffff' }}
            rows={records}
            columns={columns}
            pageSize={10}
            rowsPerPageOptions={[10]}
            getRowId={(row) => row._id.$oid}
          />
        </Box>
      </MainCard>
      {record && recordType && (
        <RecordForm
          record={record}
          recordType={recordType}
          open={openForm}
          handleClose={handleCloseForm}
          mode={mode}
        />
      )}
    </div>
  )
}
