import { Box, Button } from '@mui/material';
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
import { useTranslation } from 'react-i18next';
import { CaseService } from '../../services';
import { useSession } from 'SessionStoreContext';

export const CaseList = ({ status, caseDefId }) => {
    const [stages, setStages] = useState([]);
    const [cases, setCases] = useState([]);
    const [aCase, setACase] = useState(null);
    const [newCaseDefId, setNewCaseDefId] = useState(null);
    const [lastCreatedCase, setLastCreatedCase] = useState(null);
    const [openCaseForm, setOpenCaseForm] = useState(false);
    const [openNewCaseForm, setOpenNewCaseForm] = useState(false);
    const [view, setView] = React.useState('list');
    const [snackOpen, setSnackOpen] = useState(false);
    const { t } = useTranslation();
    const keycloak = useSession();
    const [caseDefs, setCaseDefs] = useState([]);
    const [fetching, setFetching] = useState(false);

    useEffect(() => {
        setFetching(true);

        CaseService.getCaseDefinitionsById(keycloak, caseDefId)
            .then((data) => {
                setStages(data.stages);
                return CaseService.filterCase(keycloak, caseDefId, status);
            })
            .then((data) => {
                setCases(data);
            })
            .finally(() => {
                setFetching(false);
            });
    }, [caseDefId, status]);

    useEffect(() => {
        CaseService.getCaseDefinitions(keycloak).then((data) => {
            setCaseDefs(data);
        });
    }, []);

    const makeColumns = () => {
        return [
            {
                field: 'businessKey',
                headerName: t('pages.caselist.datagrid.columns.businesskey'),
                width: 150
            },
            {
                field: 'statusDescription',
                headerName: t('pages.caselist.datagrid.columns.statusdescription'),
                width: 220
            },
            { field: 'stage', headerName: t('pages.caselist.datagrid.columns.stage'), width: 220 },
            {
                field: 'createdAt',
                headerName: t('pages.caselist.datagrid.columns.createdat'),
                width: 220
            },
            {
                field: 'action',
                headerName: '',
                sortable: false,
                renderCell: (params) => {
                    const onClick = (e) => {
                        setACase(params.row);
                        e.stopPropagation();
                        setOpenCaseForm(true);
                    };

                    return (
                        <Button onClick={onClick}>
                            {t('pages.caselist.datagrid.action.details')}
                        </Button>
                    );
                }
            }
        ];
    };

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

    const fetchKanbanConfig = () => {
        return caseDefs.find((o) => o.id === caseDefId).kanbanConfig;
    };

    const handleCloseSnack = (event, reason) => {
        if (reason === 'clickaway') {
            return;
        }

        setSnackOpen(false);
    };

    const snackAction = lastCreatedCase && (
        <React.Fragment>
            <Button
                color="primary"
                size="small"
                onClick={() => {
                    setACase({
                        businessKey: lastCreatedCase.businessKey,
                        caseDefinitionId: caseDefId
                    });
                    setOpenCaseForm(true);
                    handleCloseSnack();
                }}
            >
                {lastCreatedCase.businessKey}
            </Button>
            <IconButton size="small" aria-label="close" color="inherit" onClick={handleCloseSnack}>
                <CloseIcon fontSize="small" />
            </IconButton>
        </React.Fragment>
    );

    return (
        <div style={{ height: 650, width: '100%' }}>
            {caseDefId && (
                <div>
                    <Button id="basic-button" onClick={handleNewCaseAction} variant="contained">
                        {t('pages.caselist.action.newcase')}
                    </Button>
                </div>
            )}

            {caseDefId && (
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
            )}

            <MainCard sx={{ mt: 2 }} content={false}>
                <Box>
                    {view === 'list' && (
                        <DataGrid
                            sx={{ height: 650, width: '100%', backgroundColor: '#ffffff', mt: 1 }}
                            rows={cases}
                            columns={makeColumns()}
                            pageSize={10}
                            rowsPerPageOptions={[10]}
                            getRowId={(row) => row.businessKey}
                            loading={fetching}
                        />
                    )}
                    {view === 'kanban' && (
                        <Kanban
                            stages={stages}
                            cases={cases}
                            caseDefId={caseDefId}
                            kanbanConfig={fetchKanbanConfig()}
                            setACase={setACase}
                            setOpenCaseForm={setOpenCaseForm}
                        />
                    )}
                </Box>
            </MainCard>

            {openCaseForm && (
                <CaseForm
                    aCase={aCase}
                    handleClose={handleCloseCaseForm}
                    open={openCaseForm}
                    keycloak={keycloak}
                />
            )}

            {openNewCaseForm && (
                <NewCaseForm
                    handleClose={handleCloseNewCaseForm}
                    open={openNewCaseForm}
                    caseDefId={newCaseDefId}
                    setLastCreatedCase={setLastCreatedCase}
                />
            )}

            {lastCreatedCase && (
                <Snackbar
                    open={snackOpen}
                    autoHideDuration={6000}
                    message="Case created"
                    onClose={handleCloseSnack}
                    action={snackAction}
                />
            )}
        </div>
    );
};
