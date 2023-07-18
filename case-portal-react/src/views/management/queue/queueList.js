import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import { DataGrid } from '@mui/x-data-grid';
import { useSession } from 'SessionStoreContext';
import MainCard from 'components/MainCard';
import { useEffect, useState } from 'react';
import { QueueService } from 'services/QueueService';
import { QueueForm } from './queueForm';

export const QueueList = () => {
    const [queues, setQueues] = useState([]);
    const [openForm, setOpenForm] = useState(false);
    const [queue, setQueue] = useState(null);
    const keycloak = useSession();

    useEffect(() => {
        QueueService.find(keycloak)
            .then((data) => {
                setQueues(data);
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
        { field: 'name', headerName: 'Name', width: 300 },
        { field: 'description', headerName: 'Description', width: 300 },
        {
            field: 'action',
            headerName: '',
            sortable: false,
            renderCell: (params) => {
                const onDetailsClick = (e) => {
                    setQueue(params.row);
                    e.stopPropagation(); // don't select this row after clicking
                    setOpenForm(true);
                };

                return <Button onClick={onDetailsClick}>Edit</Button>;
            }
        }
    ];

    const handleInputChange = (event) => {
        setQueue({ ...queue, [event.target.name]: event.target.value });
    };

    const handleNew = () => {
        setQueue({ id: '', fields: { components: [], display: 'form' }, mode: 'new' });
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
                        rows={queues}
                        columns={columns}
                        pageSize={10}
                        rowsPerPageOptions={[10]}
                    />
                </Box>
            </MainCard>
            {queue && (
                <QueueForm
                    queue={queue}
                    handleClose={handleCloseForm}
                    open={openForm}
                    handleInputChange={handleInputChange}
                />
            )}
        </div>
    );
};
