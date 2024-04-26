import React, { useEffect, useState, lazy } from 'react';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import { DataGrid } from '@mui/x-data-grid';
import MainCard from 'components/MainCard';
import { useSession } from 'SessionStoreContext';
import { ProcessDefService } from 'services/ProcessDefService';

const BPMNModeler = lazy(() =>
  import('./bpmnModeler').then((module) => ({
    default: module.BPMNModeler,
  })),
)

export const ProcessDefList = () => {
  const [processDefs, setProcessDefs] = useState([]);
  const [processDef, setProcessDef] = useState(null);
  const [openBPMNModeler, setOpenBPMNModeler] = useState(false);
  const [fetching, setFetching] = useState(false);
  const keycloak = useSession();

  useEffect(() => {
    setFetching(true);

    ProcessDefService.find(keycloak)
      .then((data) => {
        setProcessDefs(data);
      })
      .finally(() => {
        setFetching(false);
      });
  }, [openBPMNModeler]);

  const columns = [
    { field: 'id', headerName: 'Id', width: 300 },
    { field: 'key', headerName: 'Key', width: 300 },
    { field: 'name', headerName: 'Name', width: 220 },
    { field: 'version', headerName: 'Version', width: 220 },
    {
      field: 'action',
      headerName: '',
      sortable: false,
      renderCell: (params) => {
        const onClick = (e) => {
          setProcessDef(params.row);
          e.stopPropagation(); // don't select this row after clicking
          setOpenBPMNModeler(true);
        };

        return (
          <React.Fragment>
            <Button onClick={onClick}>Edit</Button>
          </React.Fragment>
        );
      },
    },
  ];

  const handleCloseBPMNModeler = () => {
    setOpenBPMNModeler(false);
  };

  const handleNewProcessDef = () => {
    setProcessDef({
      id: '',
      name: '',
      key: '',
    });
    setOpenBPMNModeler(true);
  };

  return (
    <div style={{ height: 650, width: '100%' }}>
      <Button
        id='basic-button'
        variant='contained'
        onClick={handleNewProcessDef}
      >
        New
      </Button>
      <MainCard sx={{ mt: 2 }} content={false}>
        <Box>
          <DataGrid
            sx={{ height: 650, width: '100%', backgroundColor: '#ffffff' }}
            rows={processDefs}
            columns={columns}
            pageSize={10}
            loading={fetching}
            rowsPerPageOptions={[10]}
          />
        </Box>
      </MainCard>
      {processDef && (
        <BPMNModeler
          open={openBPMNModeler}
          keycloak={keycloak}
          processDef={processDef}
          handleClose={handleCloseBPMNModeler}
        />
      )}
    </div>
  );
};
