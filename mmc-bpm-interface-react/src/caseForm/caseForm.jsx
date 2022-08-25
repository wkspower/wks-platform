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
import PropTypes from 'prop-types';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Box from '@mui/material/Box';

import { TaskList } from '../taskList/taskList';

const Transition = React.forwardRef(function Transition(
    props: TransitionProps & {
        children: React.ReactElement;
    },
    ref: React.Ref<unknown>,
) {
    return <Slide direction="up" ref={ref} {...props} />;
});

function a11yProps(index) {
    return {
        id: `simple-tab-${index}`,
        'aria-controls': `simple-tabpanel-${index}`,
    };
}

function TabPanel(props) {
    const { children, value, index, ...other } = props;

    return (
        <div
            role="tabpanel"
            hidden={value !== index}
            id={`simple-tabpanel-${index}`}
            aria-labelledby={`simple-tab-${index}`}
            {...other}
        >
            {value === index && (
                <Box sx={{ p: 3 }}>
                    <Typography>{children}</Typography>
                </Box>
            )}
        </div>
    );
}

TabPanel.propTypes = {
    children: PropTypes.node,
    index: PropTypes.number.isRequired,
    value: PropTypes.number.isRequired,
};

export const CaseForm = ({ open, handleClose, aCase, componentsParam }) => {
    const [formComponents, setFormComponents] = useState([]);

    const [value, setValue] = useState(0);

    useEffect(() => {
        if (componentsParam) {
            setFormComponents(componentsParam.components);
        } else if (aCase) {
            fetch('http://localhost:8081/case/' + aCase.businessKey)
                .then(response => response.json())
                .then(data => {
                    setFormComponents(data.attributes);
                })
                .catch((err) => {
                    console.log(err.message);
                });
        }
    }, [aCase, componentsParam]);

    const handleInputChange = function (event) {
        setFormComponents({ ...formComponents, [event.target.businessKey]: { value: event.target.value } });
    }

    const handleChange = (event, newValue) => {
        setValue(newValue);
    };

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
                            <div>{aCase?.businessKey}</div>
                        </Typography>
                        <Button autoFocus color="inherit">
                            Edit
                        </Button>
                    </Toolbar>
                </AppBar>

                <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                    <Tabs value={value} onChange={handleChange} aria-label="basic tabs example">
                        <Tab label="Case Details" {...a11yProps(0)} />
                        <Tab label="Tasks" {...a11yProps(1)} />
                    </Tabs>
                </Box>
                <TabPanel value={value} index={0}>
                    {/* Case Form */}
                    <div style={{ display: 'grid', padding: '10px' }}>
                        {(formComponents && formComponents.length) ? formComponents.map(component => {
                            if (component.type !== 'text') {
                                return (
                                    <FormControl key={component.name} style={{ padding: '5px' }}>
                                        <TextField id={component.name} aria-describedby="my-helper-text" value={component.value} onChange={handleInputChange} disabled />
                                        <FormHelperText id="my-helper-text">{component.name}</FormHelperText>
                                    </FormControl>
                                );
                            }
                        }) : <div>Empty form components</div>}
                    </div>
                </TabPanel>
                <TabPanel value={value} index={1}>
                    {/* Task List  */}
                    <div style={{ display: 'grid', padding: '10px' }}>
                        <TaskList businessKey={aCase.businessKey} />
                    </div>
                </TabPanel>
            </Dialog>
        </div>
    );
}