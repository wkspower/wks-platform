import { Box, Button } from '@mui/material';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import { DataGrid, GridColDef } from '@mui/x-data-grid';
import MainCard from 'components/MainCard';
import React, { useEffect, useState } from 'react';
import { CaseForm } from '../caseForm/caseForm';
import { NewCaseForm } from '../caseForm/newCaseForm';

export const CaseList = ({ status, keycloak }) => {
    const [cases, setCases] = useState([]);
    const [aCase, setACase] = useState(null);
    const [newCaseDefId, setNewCaseDefId] = useState(null);
    const [openCaseForm, setOpenCaseForm] = useState(false);
    const [openNewCaseForm, setOpenNewCaseForm] = useState(false);

    useEffect(() => {
        fetch('http://localhost:8081/case/' + (status ? '?status=' + status : ''))
            .then((response) => response.json())
            .then((data) => {
                setCases(data);
            })
            .catch((err) => {
                console.log(err.message);
            });
    }, [status, openNewCaseForm, openCaseForm]);

    const [caseDefs, setCaseDefs] = useState([]);
    useEffect(() => {
        fetch('http://localhost:8081/case-definition/')
            .then((response) => response.json())
            .then((data) => {
                setCaseDefs(data);
            })
            .catch((err) => {
                console.log(err.message);
            });
    }, []);

    const columns: GridColDef[] = [
        { field: 'businessKey', headerName: 'Business Key', width: 150 },
        { field: 'status', headerName: 'Status', width: 220 },
        { field: 'stage', headerName: 'Stage', width: 220 },
        { field: 'caseDefinitionId', headerName: 'Case Definition', width: 220 },
        {
            field: 'action',
            headerName: '',
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

    const handleNewCaseAction = (caseDefId) => {
        setNewCaseDefId(caseDefId);
        setOpenNewCaseForm(true);
    };

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
                <Button id="basic-button" onClick={handleClick} variant="contained">
                    New Case
                </Button>
                <Menu
                    id="basic-menu"
                    anchorEl={anchorEl}
                    open={open}
                    onClose={handleClose}
                    MenuListProps={{
                        'aria-labelledby': 'basic-button'
                    }}
                >
                    {caseDefs.map((caseDef) => {
                        return (
                            <MenuItem key={caseDef.name} onClick={() => handleNewCaseAction(caseDef.id)}>
                                {caseDef.name}
                            </MenuItem>
                        );
                    })}
                </Menu>
            </div>
            <MainCard sx={{ mt: 2 }} content={false}>
                <Box>
                    <DataGrid
                        sx={{ height: 650, width: '100%', backgroundColor: '#ffffff', mt: 1 }}
                        rows={cases}
                        columns={columns}
                        pageSize={10}
                        rowsPerPageOptions={[10]}
                        getRowId={(row) => row.businessKey}
                    />
                </Box>
            </MainCard>
            {openCaseForm && <CaseForm aCase={aCase} handleClose={handleCloseCaseForm} open={openCaseForm} keycloak={keycloak} />}

            {openNewCaseForm && <NewCaseForm handleClose={handleCloseNewCaseForm} open={openNewCaseForm} caseDefId={newCaseDefId} />}
        </div>
    );
};
