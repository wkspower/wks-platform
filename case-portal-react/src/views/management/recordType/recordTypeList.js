import { useEffect, useState } from 'react';
import Box from '@mui/material/Box';
import { DataGrid } from '@mui/x-data-grid';
import MainCard from 'components/MainCard';
import Button from '@mui/material/Button';
import { RecordTypeForm } from './recordTypeForm';
import { RecordTypeService } from 'services';
import { useSession } from 'SessionStoreContext';

export const RecordTypeList = () => {
    const [recordTypes, setRecordTypes] = useState([]);
    const [openForm, setOpenForm] = useState(false);
    const [recordType, setRecordType] = useState(null);
    const keycloack = useSession();

    useEffect(() => {
        RecordTypeService.getAll(keycloack)
            .then((data) => {
                setRecordTypes(data);
            })
            .catch((err) => {
                console.log(err.message);
            });
    }, [openForm]);

    const handleCloseForm = () => {
        setOpenForm(false);
    };

    const columns = [
        { field: 'id', headerName: 'Id', width: 300 },
        {
            field: 'action',
            headerName: '',
            sortable: false,
            renderCell: (params) => {
                const onDetailsClick = (e) => {
                    setRecordType(params.row);
                    e.stopPropagation(); // don't select this row after clicking
                    setOpenForm(true);
                };

                return <Button onClick={onDetailsClick}>Edit</Button>;
            }
        }
    ];

    const handleInputChange = (event) => {
        setRecordType({ ...recordType, [event.target.name]: event.target.value });
    };

    const handleNew = () => {
        setRecordType({ id: '', fields: { components: [], display: 'form' }, mode: 'new' });
        setOpenForm(true);
    };

    return (
        <div style={{ height: 650, width: '100%' }}>
            <Button id="basic-button" variant="contained" onClick={handleNew}>
                New
            </Button>
            <MainCard sx={{ mt: 2 }} content={false}>
                <Box>
                    <DataGrid
                        sx={{ height: 650, width: '100%', backgroundColor: '#ffffff' }}
                        rows={recordTypes}
                        columns={columns}
                        pageSize={10}
                        rowsPerPageOptions={[10]}
                    />
                </Box>
            </MainCard>
            {recordType && (
                <RecordTypeForm
                    recordType={recordType}
                    handleClose={handleCloseForm}
                    open={openForm}
                    handleInputChange={handleInputChange}
                />
            )}
        </div>
    );
};
