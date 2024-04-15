import { useEffect, useState } from 'react';
import Box from '@mui/material/Box';
import { DataGrid } from '@mui/x-data-grid';
import MainCard from 'components/MainCard';
import Button from '@mui/material/Button';
import { FormDetail } from './formDetail';
import { FormNew as NewForm } from './formNew';
import { FormService } from 'services';
import { useSession } from 'SessionStoreContext';

export const FormList = () => {
  const [forms, setForms] = useState([]);
  const [openForm, setOpenForm] = useState(false);
  const [openNewForm, setOpenNewForm] = useState(false);
  const [form, setForm] = useState(null);
  const keycloak = useSession();

  useEffect(() => {
    FormService.getAll(keycloak)
      .then((data) => {
        setForms(data);
      })
      .catch((err) => {
        console.log(err.message);
      });
  }, [openForm, openNewForm]);

  const handleCloseForm = () => {
    setOpenForm(false);
  };

  const columns = [
    { field: 'key', headerName: 'Form Key', width: 300 },
    { field: 'title', headerName: 'Title', width: 300 },
    {
      field: 'action',
      headerName: '',
      sortable: false,
      renderCell: (params) => {
        const onDetailsClick = (e) => {
          setForm(params.row);
          e.stopPropagation(); // don't select this row after clicking
          setOpenForm(true);
        };

        return <Button onClick={onDetailsClick}>Edit</Button>;
      },
    },
  ];

  const handleNewForm = () => {
    setOpenNewForm(true);
  };

  const handleCloseNewForm = () => {
    setOpenNewForm(false);
  };

  const handleInputChange = (event) => {
    setForm({ ...form, [event.target.name]: event.target.value });
  };

  const handleSelectDisplay = (event) => {
    let structure = { ...form.structure };
    structure.display = event.target.value;
    setForm({ ...form, structure: structure });
  };

  return (
    <div style={{ height: 650, width: '100%' }}>
      <Button id='basic-button' onClick={handleNewForm} variant='contained'>
        New Form
      </Button>
      <MainCard sx={{ mt: 2 }} content={false}>
        <Box>
          <DataGrid
            sx={{ height: 650, width: '100%', backgroundColor: '#ffffff' }}
            rows={forms}
            columns={columns}
            pageSize={10}
            rowsPerPageOptions={[10]}
            getRowId={(row) => row.key}
          />
        </Box>
      </MainCard>
      {form && (
        <FormDetail
          form={form}
          handleClose={handleCloseForm}
          open={openForm}
          handleInputChange={handleInputChange}
          handleSelectDisplay={handleSelectDisplay}
        />
      )}
      {<NewForm handleClose={handleCloseNewForm} open={openNewForm} />}
    </div>
  );
};
