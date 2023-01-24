import { Box, Button } from '@mui/material';
import { GridColDef } from '@mui/x-data-grid';
import { Kanban } from 'components/Kanban/kanban';
import MainCard from 'components/MainCard';
import React, { useEffect, useState } from 'react';
import { CaseForm } from '../caseForm/caseForm';
import { NewCaseForm } from '../caseForm/newCaseForm';
import { DataGrid } from '@mui/x-data-grid';

import ViewListIcon from '@mui/icons-material/ViewList';
import ViewKanbanIcon from '@mui/icons-material/ViewKanban';
import ToggleButton from '@mui/material/ToggleButton';
import ToggleButtonGroup from '@mui/material/ToggleButtonGroup';

import Snackbar from '@mui/material/Snackbar';
import IconButton from '@mui/material/IconButton';
import CloseIcon from '@mui/icons-material/Close';

export const CaseList = ({ status, caseDefId, keycloak }) => {
    const [stages, setStages] = useState([]);

    const [cases, setCases] = useState([]);
    const [aCase, setACase] = useState(null);
    const [newCaseDefId, setNewCaseDefId] = useState(null);
    const [lastCreatedCase, setLastCreatedCase] = useState(null);


    const [openCaseForm, setOpenCaseForm] = useState(false);
    const [openNewCaseForm, setOpenNewCaseForm] = useState(false);
    const [view, setView] = React.useState('list');
    const [snackOpen, setSnackOpen] = useState(false);

    useEffect(() => {
        fetch(process.env.REACT_APP_API_URL + '/case-definition' + (caseDefId ? '/' + caseDefId : ''))
            .then((response) => response.json())
            .then((data) => {
                setStages(data.stages);
                return fetch(process.env.REACT_APP_API_URL + '/case/?'
                    + (status ? 'status=' + status : '')
                    + (caseDefId ? '&caseDefinitionId=' + caseDefId : ''))
            })
            .then((response) => response.json())
            .then((data) => {
                let cases = data.map(
                    function (element) {
                        element.date = "11/12/2022"
                        element.statusDescription = getStatus(element.status);
                        return element;
                    }
                )
                setCases(cases);
            })
            .catch((err) => {
                console.log(err.message);
            });

    }, [caseDefId, openNewCaseForm, openCaseForm, status]);

    const [caseDefs, setCaseDefs] = useState([]);
    useEffect(() => {
        fetch(process.env.REACT_APP_API_URL + '/case-definition/')
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
        { field: 'statusDescription', headerName: 'Status', width: 220 },
        { field: 'stage', headerName: 'Stage', width: 220 },
        { field: 'date', headerName: 'Created At', width: 220 },
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
        setSnackOpen(true);
    };

    const handleNewCaseAction = () => {
        setLastCreatedCase(null);
        setNewCaseDefId(caseDefId);
        setOpenNewCaseForm(true);
    };

    const handleChangeView = (event, nextView) => {
        if (nextView !== null) {
            setView(nextView);
        }
    };

    const getStatus = (status) => {
        if (status === "WIP_CASE_STATUS")
            return "Work In Progress";
        if (status === "CLOSED_CASE_STATUS")
            return "Closed";
        if (status === "ARCHIVED_CASE_STATUS")
            return "Archived";
        return "";
    };

    const fetchKanbanConfig = () => {
        return caseDefs.find(o => o.id === caseDefId).kanbanConfig;
    }

    const handleCloseSnack = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }

        setSnackOpen(false);
    };

    const snackAction = (
        lastCreatedCase && <React.Fragment>
            <Button color="primary" size="small" onClick={() => {
                setACase({
                    businessKey: lastCreatedCase.businessKey,
                    caseDefinitionId: caseDefId,
                });
                setOpenCaseForm(true);
                handleCloseSnack();
            }}
            >
                {lastCreatedCase.businessKey}
            </Button>
            <IconButton
                size="small"
                aria-label="close"
                color="inherit"
                onClick={handleCloseSnack}
            >
                <CloseIcon fontSize="small" />
            </IconButton>
        </React.Fragment>
    );

    return (
        <div style={{ height: 650, width: '100%' }}>
            {caseDefId && <div>
                <Button id="basic-button" onClick={handleNewCaseAction} variant="contained">
                    New Case
                </Button>
            </div>
            }

            {caseDefId &&
                <ToggleButtonGroup
                    orientation="horizontal"
                    value={view}
                    exclusive
                    onChange={handleChangeView}
                >
                    <ToggleButton value="list" aria-label="list">
                        <ViewListIcon />
                    </ToggleButton>
                    <ToggleButton value="kanban" aria-label="kanban">
                        <ViewKanbanIcon />
                    </ToggleButton>
                </ToggleButtonGroup>
            }

            <MainCard sx={{ mt: 2 }} content={false}>
                <Box>
                    {(view === 'list') && <DataGrid
                        sx={{ height: 650, width: '100%', backgroundColor: '#ffffff', mt: 1 }}
                        rows={cases}
                        columns={columns}
                        pageSize={10}
                        rowsPerPageOptions={[10]}
                        getRowId={(row) => row.businessKey}
                    />}
                    {(view === 'kanban') && <Kanban stages={stages} cases={cases} caseDefId={caseDefId} kanbanConfig={fetchKanbanConfig()} setACase={setACase} setOpenCaseForm={setOpenCaseForm} />}
                </Box>
            </MainCard>

            {openCaseForm && <CaseForm aCase={aCase} handleClose={handleCloseCaseForm} open={openCaseForm} keycloak={keycloak} />}

            {openNewCaseForm && <NewCaseForm handleClose={handleCloseNewCaseForm} open={openNewCaseForm} caseDefId={newCaseDefId} setLastCreatedCase={setLastCreatedCase} />}

            {lastCreatedCase && <Snackbar
                open={snackOpen}
                autoHideDuration={6000}
                message="Case created"
                onClose={handleCloseSnack}
                action={snackAction}
            />}

        </div>
    );
};
