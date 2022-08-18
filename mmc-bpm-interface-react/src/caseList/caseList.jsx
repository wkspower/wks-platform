import React, { useState, useEffect } from "react";
import Button from '@mui/material/Button';
import { DataGrid, GridColDef } from '@mui/x-data-grid';
import { CaseForm } from "../caseForm/caseForm";
import { NewCaseForm } from "../caseForm/newCaseForm";
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';

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

    const [caseDefs, setCaseDefs] = useState([]);
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
        { field: 'status', headerName: 'Status', width: 220 },
        { field: 'caseDefinitionId', headerName: 'Case Definition', width: 220 },
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

    const [anchorEl, setAnchorEl] = React.useState(null);
    const open = Boolean(anchorEl);
    const handleClick = (event) => {
        setAnchorEl(event.currentTarget);
    };
    const handleClose = () => {
        setAnchorEl(null);
    };

    return (
        <div style={{ height: 650, width: '100%' }}>
            <div>
                <Button
                    id="basic-button"
                    aria-controls={open ? 'basic-menu' : undefined}
                    aria-haspopup="true"
                    aria-expanded={open ? 'true' : undefined}
                    onClick={handleClick}
                >
                    New Case
                </Button>
                <Menu
                    id="basic-menu"
                    anchorEl={anchorEl}
                    open={open}
                    onClose={handleClose}
                    MenuListProps={{
                        'aria-labelledby': 'basic-button',
                    }}
                >
                    {caseDefs.map((caseDef) => {
                        return (<MenuItem key={caseDef.name} onClick={handleNewCaseAction}>{caseDef.name}</MenuItem>);
                    })}
                </Menu>
            </div>
            <DataGrid
                rows={cases}
                columns={columns}
                pageSize={10}
                rowsPerPageOptions={[10]}
                checkboxSelection
            />
            {aCase && <CaseForm aCase={aCase} handleClose={handleCloseCaseForm} open={openCaseForm} />}

            <NewCaseForm handleClose={handleCloseNewCaseForm} open={openNewCaseForm} />
        </div >
    );
}