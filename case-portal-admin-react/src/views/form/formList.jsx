import { useEffect, useState } from 'react';

import { DataGrid } from '@mui/x-data-grid';

import Button from '@mui/material/Button';
import { FormDetail } from './formDetail';

export const FormList = () => {
    const [forms, setForms] = useState([]);
    useEffect(() => {
        fetch('http://localhost:8081/form')
            .then((response) => response.json())
            .then((data) => {
                setForms(data);
            })
            .catch((err) => {
                console.log(err.message);
            });
    }, []);

    const [openForm, setOpenForm] = useState(false);
    const handleCloseForm = () => {
        setOpenForm(false);
    };

    const [form, setForm] = useState(null);
    const columns: GridColDef[] = [
        { field: 'key', headerName: 'Event', width: 300 },
        { field: 'description', headerName: 'Description', width: 220 },
        {
            field: 'action',
            headerName: 'Action',
            sortable: false,
            renderCell: (params) => {
                const onClick = (e) => {
                    setForm(params.row);
                    e.stopPropagation(); // don't select this row after clicking
                    setOpenForm(true);
                };

                return <Button onClick={onClick}>Details</Button>;
            }
        }
    ];

    return (
        <div style={{ height: 650, width: '100%' }}>
            <DataGrid
                sx={{ height: 650, width: '100%', backgroundColor: '#ffffff' }}
                rows={forms}
                columns={columns}
                pageSize={10}
                rowsPerPageOptions={[10]}
                getRowId={(row) => row.key}
            />

            {form && <FormDetail form={form} handleClose={handleCloseForm} open={openForm} />}
        </div>
    );
};
