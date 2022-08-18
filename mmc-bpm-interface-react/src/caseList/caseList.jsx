import React, { useState, useEffect } from "react";
import Button from '@mui/material/Button';
import { DataGrid, GridColDef } from '@mui/x-data-grid';
import { CaseForm } from "../caseForm/caseForm";
import { NewCaseForm } from "../caseForm/newCaseForm";
import Box from '@mui/material/Box';

export const CaseList = (casesParam) => {
    const [cases, setCases] = useState([]);
    const [aCase, setACase] = useState(null);
    const [openCaseForm, setOpenCaseForm] = useState(false);
    const [openNewCaseForm, setOpenNewCaseForm] = useState(false);

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
    }, [openCaseForm, casesParam]);

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
                    setOpenCaseForm(true);
                };

                return <Button onClick={onClick}>Details</Button>;
            }
        }
    ];

    const handleCloseCaseForm = () => {
        setOpenCaseForm(false);
    };

    const handleCloseNewCaseForm = () => {
        setOpenNewCaseForm(false);
    };

    const handleNewCaseAction = () => {
        setOpenNewCaseForm(true);
    }

    return (
        <div style={{ height: 650, width: '100%' }}>
            <Box component="span" sx={{ p: 2 }}>
                <Button onClick={handleNewCaseAction}>New Case</Button>
            </Box>
            <DataGrid
                rows={cases}
                columns={columns}
                pageSize={10}
                rowsPerPageOptions={[10]}
                checkboxSelection
            />
            { aCase && <CaseForm aCase={aCase} handleClose={handleCloseCaseForm} open={openCaseForm} />}

            <NewCaseForm handleClose={handleCloseNewCaseForm} open={openNewCaseForm} />
        </div >
    );
}