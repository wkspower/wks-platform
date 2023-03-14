import React, { useEffect, useState } from 'react';
import { Box } from '@mui/material';
import Button from '@mui/material/Button';
import { DataGrid } from '@mui/x-data-grid';
import MainCard from 'components/MainCard';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import { BpmEngineC7Form } from '../bpmEngineForm/bpmEngineC7Form';
import { BpmEngineC8Form } from '../bpmEngineForm/bpmEngineC8Form';
import { BpmService } from 'services';
import { useSession } from 'SessionStoreContext';

export const BpmEngineList = () => {
    const [list, setList] = useState([]);
    const [bpmEngineTypes, setBpmEngineTypes] = useState([]);
    const [bpmEngine, setBpmEngine] = useState(null);
    const [openForm, setOpenForm] = useState(false);
    const keycloak = useSession();

    useEffect(() => {
        BpmService.getAll(keycloak)
            .then((data) => {
                setList(data);
            })
            .catch((err) => {
                console.log(err.message);
            });

        BpmService.getAllTypes(keycloak)
            .then((data) => {
                setBpmEngineTypes(data);
            })
            .catch((err) => {
                console.log(err.message);
            });
    }, [openForm]);

    const columns = [
        { field: 'id', headerName: 'Id', width: 300 },
        { field: 'name', headerName: 'Name', width: 220 },
        { field: 'type', headerName: 'Type', width: 220 },
        {
            field: 'action',
            headerName: '',
            sortable: false,
            renderCell: (params) => {
                const onClick = (e) => {
                    setBpmEngine(params.row);
                    e.stopPropagation(); // don't select this row after clicking
                    setOpenForm(true);
                };

                return (
                    <React.Fragment>
                        <Button onClick={onClick}>Edit</Button>
                    </React.Fragment>
                );
            }
        }
    ];

    const [anchorEl, setAnchorEl] = React.useState(null);
    const open = Boolean(anchorEl);
    const handleClick = (event) => {
        setAnchorEl(event.currentTarget);
    };
    const handleClose = () => {
        setAnchorEl(null);
    };

    const handleNewBpmEngineAction = (bpmEngineTypeCode) => {
        if (bpmEngineTypeCode === 'BPM_ENGINE_CAMUNDA7') {
            setBpmEngine({
                id: '',
                name: '',
                type: bpmEngineTypeCode,
                parameters: { url: '' },
                mode: 'new'
            });
        } else if (bpmEngineTypeCode === 'BPM_ENGINE_CAMUNDA8') {
            setBpmEngine({
                id: '',
                name: '',
                type: bpmEngineTypeCode,
                parameters: {
                    zeebeEndpoint: '',
                    zeebeEndpointPort: '',
                    clientId: '',
                    clientSecret: ''
                },
                mode: 'new'
            });
        }
        setOpenForm(true);
    };

    const handleCloseForm = () => {
        setBpmEngine(null);
        setOpenForm(false);
    };

    return (
        <div style={{ height: 650, width: '100%' }}>
            <Button id="basic-button" variant="contained" onClick={handleClick}>
                New
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
                {bpmEngineTypes.map((bpmEngineType) => {
                    return (
                        <MenuItem
                            key={bpmEngineType.code}
                            onClick={() => handleNewBpmEngineAction(bpmEngineType.code)}
                        >
                            {bpmEngineType.description}
                        </MenuItem>
                    );
                })}
            </Menu>
            <MainCard sx={{ mt: 2 }} content={false}>
                <Box>
                    <DataGrid
                        sx={{ height: 650, width: '100%', backgroundColor: '#ffffff' }}
                        rows={list}
                        columns={columns}
                        pageSize={10}
                        rowsPerPageOptions={[10]}
                    />
                </Box>
            </MainCard>
            {bpmEngine && bpmEngine.type === 'BPM_ENGINE_CAMUNDA7' && (
                <BpmEngineC7Form
                    bpmEngine={bpmEngine}
                    setBpmEngine={setBpmEngine}
                    open={openForm}
                    handleClose={handleCloseForm}
                />
            )}
            {bpmEngine && bpmEngine.type === 'BPM_ENGINE_CAMUNDA8' && (
                <BpmEngineC8Form
                    bpmEngine={bpmEngine}
                    setBpmEngine={setBpmEngine}
                    open={openForm}
                    handleClose={handleCloseForm}
                />
            )}
        </div>
    );
};
