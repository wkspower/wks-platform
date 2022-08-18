import React, { useState, useEffect } from "react";
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import IconButton from '@mui/material/IconButton';
import Typography from '@mui/material/Typography';
import CloseIcon from '@mui/icons-material/Close';
import Slide from '@mui/material/Slide';
import { TransitionProps } from '@mui/material/transitions';
import FormControl from '@mui/material/FormControl';
import TextField from '@mui/material/TextField';
import FormHelperText from '@mui/material/FormHelperText';

import { TaskList } from '../taskList/taskList';

const Transition = React.forwardRef(function Transition(
    props: TransitionProps & {
        children: React.ReactElement;
    },
    ref: React.Ref<unknown>,
) {
    return <Slide direction="up" ref={ref} {...props} />;
});

export const CaseDefForm = ({ open, handleClose, aCaseDef }) => {

    return (
        <div>
            <Dialog
                fullScreen
                open={open}
                onClose={handleClose}
                TransitionComponent={Transition}
            >
                <AppBar sx={{ position: 'relative' }}>
                    <Toolbar>
                        <IconButton
                            edge="start"
                            color="inherit"
                            onClick={handleClose}
                            aria-label="close"
                        >
                            <CloseIcon />
                        </IconButton>
                        <Typography sx={{ ml: 2, flex: 1 }} variant="h6" component="div">
                            <div>Case definition: {aCaseDef?.id}</div>
                        </Typography>
                        <Button autoFocus color="inherit">
                            Edit
                        </Button>
                    </Toolbar>
                </AppBar>

                {/* Case Definition Form */}
                <div style={{ display: 'grid', padding: '10px' }}>
                    <FormControl key='ctrlId' style={{ padding: '5px' }}>
                        <TextField id='txtId' aria-describedby="my-helper-text" value={aCaseDef.id} />
                        <FormHelperText id="my-helper-text">Case Definition Id</FormHelperText>
                    </FormControl>
                    <FormControl key='ctrlName' style={{ padding: '5px' }}>
                        <TextField id='txtName' aria-describedby="my-helper-text" value={aCaseDef.name} />
                        <FormHelperText id="my-helper-text">Case Definition Name</FormHelperText>
                    </FormControl>
                    <FormControl key='ctrlProcDefKeys' style={{ padding: '5px' }}>
                        <TextField id='txtProcDefKeys' aria-describedby="my-helper-text" value={aCaseDef.onCreateProcessDefinitions} />
                        <FormHelperText id="my-helper-text">Process Definition Keys</FormHelperText>
                    </FormControl>
                </div>
            </Dialog>
        </div >
    );
}