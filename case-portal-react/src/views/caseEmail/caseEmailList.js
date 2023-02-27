import { useState, useEffect } from "react";
import MainCard from "components/MainCard";
import Box from '@mui/material/Box';
import { DataGrid } from '@mui/x-data-grid';


export const CaseEmailsList = ({caseInstanceBusinessKey}) => {


    const [emails, setEmails] = useState([]);

    useEffect(() => {
        fetch(process.env.REACT_APP_EMAIL_URL + '/email/?caseInstanceBusinessKey=' + caseInstanceBusinessKey)
            .then((response) => response.json())
            .then((data) => {
                setEmails(data);
            })
            .catch((err) => {
                console.log(err.message);
            });

    }, [caseInstanceBusinessKey]);

    const columns: GridColDef[] = [
        { field: 'from', headerName: 'From', width: 300 },
        { field: 'to', headerName: 'To', width: 300 },
        { field: 'text', headerName: 'Text', width: 220 },
    ];

    return (
        <div style={{ height: 650, width: '100%' }}>
            <MainCard sx={{ mt: 2 }} content={false}>
                <Box>
                    <DataGrid
                        sx={{ height: 650, width: '100%', backgroundColor: '#ffffff', mt: 1 }}
                        rows={emails}
                        columns={columns}
                        pageSize={10}
                        rowsPerPageOptions={[10]}
                        getRowId={(row) => row._id.$oid}
                    />
                </Box>
            </MainCard>
        </div>
    );
}