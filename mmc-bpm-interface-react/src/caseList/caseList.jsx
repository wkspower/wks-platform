import React, { useState, useEffect } from "react";
import Button from '@mui/material/Button';
import { DataGrid, GridColDef } from '@mui/x-data-grid';
import { CaseForm } from "../caseForm/caseForm";

export const CaseList = (casesParam) => {
    const [cases, setCases] = useState([]);
    const [aCase, setACase] = useState([]);
    const [open, setOpen] = useState(false);

    useEffect(() => {

        if (Object.keys(casesParam).length > 0) {
            setCases(casesParam.cases);
        } else {
            fetch('http://localhost:8081/case')
                .then((response) => response.json())
                .then((data) => {
                    setCases(data);
                })
                .catch((err) => {
                    console.log(err.message);
                });
        }
    }, [open, casesParam]);

    const columns: GridColDef[] = [
        { field: 'id', headerName: 'Id', width: 300 },
        { field: 'status', headerName: 'Status', width: 220 },
        {
            field: "action",
            headerName: "Action",
            sortable: false,
            renderCell: (params) => {
                const onClick = (e) => {
                    setACase(params.row);
                    e.stopPropagation(); // don't select this row after clicking
                    setOpen(true);
                };

                return <Button onClick={onClick}>Details</Button>;
            }
        }
    ];

    const handleClose = () => {
        setOpen(false);
    };

    return (
        <div style={{ height: 650, width: '100%' }}>
            <DataGrid
                rows={cases}
                columns={columns}
                pageSize={10}
                rowsPerPageOptions={[10]}
                checkboxSelection
            />
            <CaseForm aCase={aCase} handleClose={handleClose} open={open} />
        </div >
    );
}