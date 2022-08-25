import { useState, useEffect } from "react";

import { DataGrid } from "@mui/x-data-grid";

const columns: GridColDef[] = [
    { field: 'code', headerName: 'Event', width: 300 },
    { field: 'description', headerName: 'Description', width: 220 },
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
        <div style={{ height: 650, width: '100%' }}>
            <DataGrid
                rows={eventTypes}
                columns={columns}
                pageSize={10}
                rowsPerPageOptions={[10]}
                getRowId={(row) => row.code}
            />
        </div>
    );
}