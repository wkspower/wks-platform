import React from 'react';
import Box from '@mui/material/Box';
import { DataGrid } from '@mui/x-data-grid';
import MainCard from 'components/MainCard';
import Button from '@mui/material/Button';
import { CaseDefStageProcesses } from './caseDefStageProcesses';

export const CaseDefFormStages = ({ caseDef, setCaseDef }) => {
    const [openProcessesList, setOpenProcessesList] = React.useState(false);
    const [currentStage, setCurrentStage] = React.useState(null);

    const [sortModel] = React.useState([
        {
            field: 'index',
            sort: 'asc'
        }
    ]);

    const columns = [
        {
            field: 'index',
            headerName: 'Index',
            width: 100,
            sortable: false,
            editable: true,
            type: 'number'
        },
        { field: 'name', headerName: 'Name', width: 220, sortable: false, editable: true },
        {
            field: 'processes',
            headerName: '',
            sortable: false,
            renderCell: (params) => {
                return (
                    <React.Fragment>
                        <Button onClick={() => openProcessesDialog(params.row)}>Processes</Button>
                    </React.Fragment>
                );
            }
        },
        {
            field: 'action',
            headerName: '',
            sortable: false,
            renderCell: (params) => {
                const onClick = (e) => {
                    const newCaseDefStages = caseDef.stages.filter(function (value, index, arr) {
                        return value.id !== params.row.id;
                    });
                    setCaseDef({ ...caseDef, stages: newCaseDefStages });
                    e.stopPropagation(); // don't select this row after clicking
                };

                return (
                    <React.Fragment>
                        <Button onClick={onClick}>Delete</Button>
                    </React.Fragment>
                );
            }
        },
    ];

    const handleNewStage = () => {
        const length = caseDef.stages.length;
        const lastStage = caseDef.stages[length - 1];
        setCaseDef({
            ...caseDef,
            stages: [
                ...caseDef.stages,
                {
                    id: parseInt(lastStage.id) + 1,
                    index: lastStage.index + 1,
                    name: 'Stage ' + (parseInt(lastStage.id) + 1)
                }
            ]
        });
    };

    const processRowUpdate = (newRow) => {
        let newCaseDefStages = [...caseDef.stages];
        newCaseDefStages[newRow.id] = newRow;
        setCaseDef({ ...caseDef, stages: newCaseDefStages });
        return newRow;
    };

    const openProcessesDialog = (stage) => {
        setCurrentStage(stage);
        setOpenProcessesList(true);
    };

    const closeProcessesDialog = () => {
        setOpenProcessesList(false);
        setCurrentStage(null);
    };

    const updateProcesses = (stageId, processes) => {
        let newStages = [...caseDef.stages];
        let stage = newStages.find(s => s.id === stageId);
        stage.processesDefinitions = processes;
        setCaseDef({ ...caseDef, stages: newStages });
    };

    return (
        <div style={{ height: 650, width: '100%' }}>
            <Button id="basic-button" variant="contained" onClick={handleNewStage}>
                New Stage
            </Button>

            {caseDef.stages && (
                <MainCard sx={{ mt: 2 }} content={false}>
                    <Box>
                        <DataGrid
                            sx={{ height: 650, width: '100%', backgroundColor: '#ffffff' }}
                            rows={caseDef.stages}
                            columns={columns}
                            pageSize={10}
                            rowsPerPageOptions={[10]}
                            sortModel={sortModel}
                            experimentalFeatures={{ newEditingApi: true }}
                            processRowUpdate={processRowUpdate}
                        />
                    </Box>
                    {currentStage && <CaseDefStageProcesses open={openProcessesList} handleClose={closeProcessesDialog} stage={currentStage} updateProcesses={updateProcesses} />}
                </MainCard>
            )}
        </div>
    );
};
