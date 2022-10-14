import React, { useState, useEffect } from 'react';

import { Box } from '@mui/material';
import Button from '@mui/material/Button';
import { DataGrid } from '@mui/x-data-grid';
import MainCard from 'components/MainCard';

import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';

export const BpmEngineList = () => {
    const [list, setList] = useState([]);
    const [bpmEngineTypes, setBpmEngineTypes] = useState([]);
    const [bpmEngineTypeSelected, setBpmEngineTypeSelected] = useState(null);

    useEffect(() => {
        fetch('http://localhost:8081/bpm-engine/')
            .then((response) => response.json())
            .then((data) => {
                setList(data);
            })
            .catch((err) => {
                console.log(err.message);
            });

        fetch('http://localhost:8081/bpm-engine-type/')
            .then((response) => response.json())
            .then((data) => {
                setBpmEngineTypes(data);
            })
            .catch((err) => {
                console.log(err.message);
            });
    }, []);

    const columns: GridColDef[] = [
        { field: 'id', headerName: 'Id', width: 300 },
        { field: 'name', headerName: 'Name', width: 220 },
        { field: 'type', headerName: 'Type', width: 220 },
        {
            field: 'action',
            headerName: '',
            sortable: false,
            renderCell: (params) => {
                const onClick = (e) => {
                    e.stopPropagation(); // don't select this row after clicking
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
        setBpmEngineTypeSelected(bpmEngineTypeCode);
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
                        <MenuItem key={bpmEngineType.code} onClick={() => handleNewBpmEngineAction(bpmEngineType.code)}>
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
        </div>
    );
};
