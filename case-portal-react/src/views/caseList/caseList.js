import { useTheme } from '@mui/material';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import { Kanban } from 'components/Kanban/kanban';
import MainCard from 'components/MainCard';
import React, { createContext, useEffect, useState, useContext } from 'react';
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
import { KeyboardArrowLeft } from '@mui/icons-material';
import { KeyboardArrowRight } from '@mui/icons-material';
import TablePagination from '@mui/material/TablePagination';

export const CaseList = ({ status, caseDefId }) => {
    const PaginationContext = createContext();
    const { t } = useTranslation();
    const [stages, setStages] = useState([]);
    const [cases, setCases] = useState([]);
    const [aCase, setACase] = useState(null);
    const [newCaseDefId, setNewCaseDefId] = useState(null);
    const [lastCreatedCase, setLastCreatedCase] = useState(null);
    const [openCaseForm, setOpenCaseForm] = useState(false);
    const [openNewCaseForm, setOpenNewCaseForm] = useState(false);
    const [view, setView] = React.useState('list');
    const [snackOpen, setSnackOpen] = useState(false);
    const keycloak = useSession();
    const [caseDefs, setCaseDefs] = useState([]);
    const [fetching, setFetching] = useState(false);
    const [filter, setFilter] = useState({
        sort: '',
        limit: 10,
        after: '',
        before: '',
        cursors: {},
        hasPrevious: false,
        hasNext: false
    });

    useEffect(() => {
        fetchCases(setFetching, keycloak, caseDefId, setStages, status, filter, setCases, setFilter);
    }, [caseDefId, status, openNewCaseForm]);

    useEffect(() => {
        CaseService.getCaseDefinitions(keycloak).then((resp) => {
            setCaseDefs(resp);
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
                field: 'caseOwnerName',
                headerName: t('pages.caselist.datagrid.columns.caseOwnerName'),
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
        fetchCases(setFetching, keycloak, caseDefId, setStages, status, filter, setCases, setFilter);
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

    const handlerNextPage = () => {
        setFetching(true);

        const next = {
            sort: filter.sort,
            limit: filter.limit,
            after: filter.cursors.after
        };

        CaseService.filterCase(keycloak, caseDefId, status, next)
            .then((resp) => {
                const { data, paging } = resp;

                setCases(data);
                setFilter({
                    ...filter,
                    cursors: paging.cursors,
                    hasPrevious: paging.hasPrevious,
                    hasNext: paging.hasNext
                });
            })
            .finally(() => {
                setFetching(false);
            });
    };

    const handlerPriorPage = () => {
        setFetching(true);

        const prior = {
            sort: filter.sort,
            limit: filter.limit,
            before: filter.cursors.before
        };

        CaseService.filterCase(keycloak, caseDefId, status, prior)
            .then((resp) => {
                const { data, paging } = resp;

                setCases(data);
                setFilter({
                    ...filter,
                    cursors: paging.cursors,
                    hasPrevious: paging.hasPrevious,
                    hasNext: paging.hasNext
                });
            })
            .finally(() => {
                setFetching(false);
            });
    };

    function TablePaginationActions(props) {
        const theme = useTheme();
        const filter = useContext(PaginationContext);
        const { onPageChange } = props;

        const handleBackButtonClick = (event) => {
            onPageChange(event, 'back');
        };

        const handleNextButtonClick = (event) => {
            onPageChange(event, 'next');
        };

        const { hasPrevious, hasNext } = filter;

        return (
            <Box sx={{ flexShrink: 0, ml: 2.5 }}>
                <IconButton
                    onClick={handleBackButtonClick}
                    disabled={!hasPrevious}
                    aria-label="previous page"
                >
                    {theme.direction === 'rtl' ? <KeyboardArrowRight /> : <KeyboardArrowLeft />}
                </IconButton>
                <IconButton
                    onClick={handleNextButtonClick}
                    disabled={!hasNext}
                    aria-label="next page"
                >
                    {theme.direction === 'rtl' ? <KeyboardArrowLeft /> : <KeyboardArrowRight />}
                </IconButton>
            </Box>
        );
    }

    const CustomPagination = () => {
        return (
            <PaginationContext.Provider value={filter}>
                <TablePagination
                    component="div"
                    count={-1}
                    page={0}
                    labelRowsPerPage={<div style={{ paddingTop: 15 }}>Rows per page:</div>}
                    rowsPerPage={filter.limit}
                    rowsPerPageOptions={[5, 10, 25, 50]}
                    getItemAriaLabel={(type) => ''}
                    labelDisplayedRows={() => ''}
                    onPageChange={(e, type) => {
                        const action = {
                            next: handlerNextPage,
                            back: handlerPriorPage
                        };
                        action[type]();
                    }}
                    onRowsPerPageChange={(e) => {
                        setFetching(true);

                        CaseService.filterCase(keycloak, caseDefId, status, {
                            limit: e.target.value
                        })
                            .then((resp) => {
                                const { data, paging } = resp;

                                setCases(data);
                                setFilter({
                                    ...filter,
                                    limit: e.target.value,
                                    cursors: paging.cursors,
                                    hasPrevious: paging.hasPrevious,
                                    hasNext: paging.hasNext
                                });
                            })
                            .finally(() => {
                                setFetching(false);
                            });
                    }}
                    SelectProps={{
                        inputProps: {
                            'aria-label': 'rows per page'
                        },
                        native: true
                    }}
                    ActionsComponent={TablePaginationActions}
                />
            </PaginationContext.Provider>
        );
    };

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
                        <div>
                            <DataGrid
                                sx={{
                                    height: 500,
                                    width: '100%',
                                    backgroundColor: '#ffffff',
                                    mt: 1
                                }}
                                rows={cases}
                                columns={makeColumns()}
                                getRowId={(row) => row.businessKey}
                                loading={fetching}
                                components={{ Pagination: CustomPagination }}
                            />
                        </div>
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

            <br />

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
function fetchCases(setFetching, keycloak, caseDefId, setStages, status, filter, setCases, setFilter) {
    setFetching(true);

    CaseService.getCaseDefinitionsById(keycloak, caseDefId)
        .then((resp) => {
            resp.stages.sort((a, b) => a.index - b.index).map((o) => o.name)
            setStages(resp.stages);
            return CaseService.filterCase(keycloak, caseDefId, status, filter);
        })
        .then((resp) => {
            const { data, paging } = resp;
            setCases(data);
            setFilter({
                ...filter,
                cursors: paging.cursors,
                hasPrevious: paging.hasPrevious,
                hasNext: paging.hasNext
            });
        })
        .finally(() => {
            setFetching(false);
        });
}

