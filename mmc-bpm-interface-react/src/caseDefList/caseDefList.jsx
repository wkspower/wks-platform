import React, { useState, useEffect } from "react";
import Button from '@mui/material/Button';
import { DataGrid, GridColDef } from '@mui/x-data-grid';
import { CaseDefForm } from '../caseDefForm/caseDefForm'

export const CaseDefList = () => {
    const [caseDefs, setCaseDefs] = useState([]);
    const [aCaseDef, setACaseDef] = useState(null);
    const [openCaseDefForm, setOpenCaseDefForm] = useState(false);

    useEffect(() => {
        fetch('http://localhost:8081/case-definition')
            .then((response) => response.json())
            .then((data) => {
                setCaseDefs(data);
            })
            .catch((err) => {
                console.log(err.message);
            });
    }, []);

    const columns: GridColDef[] = [
        { field: 'id', headerName: 'Id', width: 300 },
        { field: 'name', headerName: 'Name', width: 220 },
        {
            field: "action",
            headerName: "Action",
            sortable: false,
            renderCell: (params) => {
                const onClick = (e) => {
                    setACaseDef(params.row);
                    e.stopPropagation(); // don't select this row after clicking
                    setOpenCaseDefForm(true);
                };

                return <Button onClick={onClick}>Details</Button>;
            }
        }

    ];

    const handleCloseCaseDefForm = () => {
        setOpenCaseDefForm(false);
    };

    return (
        <div style={{ height: 650, width: '100%' }}>
            <DataGrid
                rows={caseDefs}
                columns={columns}
                pageSize={10}
                rowsPerPageOptions={[10]}
                checkboxSelection
            />
            {aCaseDef && <CaseDefForm aCaseDef={aCaseDef} handleClose={handleCloseCaseDefForm} open={openCaseDefForm} />}
        </div >
    );
}