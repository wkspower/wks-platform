import { useEffect, useState } from 'react';

import { Box } from '@mui/material';
import { DataGrid } from '@mui/x-data-grid';
import MainCard from 'components/MainCard';

const columns: GridColDef[] = [
    { field: 'code', headerName: 'Event', width: 300 },
    { field: 'description', headerName: 'Description', width: 220 }
];

export const EventTypeList = () => {
    const [eventTypes, setEventTypes] = useState([]);
    useEffect(() => {
        fetch('http://localhost:8081/case-event-type')
            .then((response) => response.json())
            .then((data) => {
                setEventTypes(data);
            })
            .catch((err) => {
                console.log(err.message);
            });
    }, []);

    return (
        <MainCard sx={{ mt: 2 }} content={false}>
            <Box>
                <DataGrid
                    sx={{ height: 650, width: '100%', backgroundColor: '#ffffff' }}
                    rows={eventTypes}
                    columns={columns}
                    pageSize={10}
                    rowsPerPageOptions={[10]}
                    getRowId={(row) => row.code}
                />
            </Box>
        </MainCard>
    );
};
