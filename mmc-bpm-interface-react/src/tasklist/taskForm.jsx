import React from "react";
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
import Input from '@mui/material/Input';
import FormHelperText from '@mui/material/FormHelperText';
import { FormLabel } from "@mui/material";

const Transition = React.forwardRef(function Transition(
    props: TransitionProps & {
        children: React.ReactElement;
    },
    ref: React.Ref<unknown>,
) {
    return <Slide direction="up" ref={ref} {...props} />;
});

export const TaskForm = ({ open, handleClickOpen, handleClose, components }) => {
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
                            Task:
                        </Typography>
                        <Button autoFocus color="inherit" onClick={handleClose}>
                            Claim
                        </Button>
                        <Button autoFocus color="inherit" onClick={handleClose}>
                            Complete
                        </Button>
                    </Toolbar>
                </AppBar>
                <div>
                    {components?.map(component => {
                        if (component.type === 'text') {
                            return (
                                <h2 id={component.id}>{component.text}</h2>
                            );
                        } else {
                            return (
                                <FormControl>
                                    <Input id={component.id} aria-describedby="my-helper-text" />
                                    <FormHelperText id="my-helper-text">{component.label}</FormHelperText>
                                </FormControl>
                            );
                        }
                    })}
                </div>
            </Dialog>
        </div>
    );
}