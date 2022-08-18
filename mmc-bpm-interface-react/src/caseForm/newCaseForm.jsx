import React, { useEffect } from "react";
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

const Transition = React.forwardRef(function Transition(
    props: TransitionProps & {
        children: React.ReactElement;
    },
    ref: React.Ref<unknown>,
) {
    return <Slide direction="up" ref={ref} {...props} />;
});

export const NewCaseForm = ({ open, handleClose }) => {
    useEffect(() => {
        console.log("useEffect: new case form open")
    }, [open]);

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
                            <div>New Generic Case</div>
                        </Typography>
                        <Button autoFocus color="inherit">
                            Save
                        </Button>
                    </Toolbar>
                </AppBar>

                {/* New Case Form */}
                <div style={{ display: 'grid', padding: '10px' }}>
                    <FormControl key='key' style={{ padding: '5px' }}>
                        <TextField id='id' aria-describedby="my-helper-text" value='value' />
                        <FormHelperText id="my-helper-text">id</FormHelperText>
                    </FormControl>
                </div>
            </Dialog>
        </div >
    );
}