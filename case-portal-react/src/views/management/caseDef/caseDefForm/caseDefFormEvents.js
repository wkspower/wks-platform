import React, { useState } from 'react';
import {
    Container,
    TextField,
    Button,
    List,
    ListItem,
    ListItemText,
    ListItemSecondaryAction,
    IconButton,
    Grid,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';

export const CaseDefFormEvents = ({ caseDef, setCaseDef }) => {

    const [inputProcessDefKey, setInputProcessDefKey] = useState('');
    const [inputTaskDefKey, setInputTaskDefKey] = useState('');
    const [inputNewStage, setInputNewStage] = useState('');

    // Function to handle input change
    const handleProcessDefKeyChange = (e) => {
        setInputProcessDefKey(e.target.value);
    };

    const handleTaskDefKeyChange = (e) => {
        setInputTaskDefKey(e.target.value);
    };

    const handleNewStageChange = (e) => {
        setInputNewStage(e.target.value);
    };

    // Function to handle form submission
    const handleSubmit = (e) => {
        e.preventDefault();
        // Replace this with your actual create logic
        // For example, you can send a request to create a new record
        // and update the "records" state with the updated list
        const newRecord = {
            processDefKey: inputProcessDefKey,
            taskDefKey: inputTaskDefKey,
            actions: [{ newStage: inputNewStage }]
        };
        setCaseDef({ ...caseDef, taskCompleteHooks: [...caseDef.taskCompleteHooks, newRecord] });
        setInputProcessDefKey('');
        setInputTaskDefKey('');
        setInputNewStage('');
    };

    // Function to handle record deletion
    const handleDelete = (index) => {
        // Replace this with your actual delete logic
        // For example, you can send a request to delete the record
        // and update the "records" state by filtering out the deleted record
        const updatedRecords = [...caseDef.taskCompleteHooks];
        updatedRecords.splice(index, 1);
        setCaseDef({ ...caseDef, taskCompleteHooks: updatedRecords });
    };

    return (
        <Container maxWidth="md">
            <form onSubmit={handleSubmit}>
                <Grid container spacing={2}>
                    <Grid item xs={12} sm={4}>
                        <TextField
                            fullWidth
                            label="Process Definition Key"
                            value={inputProcessDefKey}
                            onChange={handleProcessDefKeyChange}
                            placeholder="Enter process definition key"
                        />
                    </Grid>
                    <Grid item xs={12} sm={4}>
                        <TextField
                            fullWidth
                            label="Task Definition Key"
                            value={inputTaskDefKey}
                            onChange={handleTaskDefKeyChange}
                            placeholder="Enter task definition key" />
                    </Grid>
                    <Grid item xs={12} sm={4}>
                        <TextField
                            fullWidth
                            label="New Stage"
                            value={inputNewStage}
                            onChange={handleNewStageChange}
                            placeholder="Enter new stage"
                        />
                    </Grid>
                </Grid>
                <Button type="submit" variant="contained" color="primary">
                    Add Record
                </Button>
            </form>
            <List>
                {caseDef.taskCompleteHooks.map((record, index) => (
                    <ListItem key={index}>
                        <ListItemText
                            primary={`Process Definition Key: ${record.processDefKey}`}
                            secondary={`Task Definition Key: ${record.taskDefKey}`}
                        />
                        <ListItemText
                            primary="Actions"
                            secondary={
                                <ul>
                                    {record.actions.map((action, actionIndex) => (
                                        <li key={actionIndex}>{action.newStage}</li>
                                    ))}
                                </ul>
                            }
                        />
                        <ListItemSecondaryAction>
                            <IconButton
                                edge="end"
                                aria-label="delete"
                                onClick={() => handleDelete(index)}
                            >
                                <DeleteIcon />
                            </IconButton>
                        </ListItemSecondaryAction>
                    </ListItem>
                ))}
            </List>
        </Container>
    );
};
