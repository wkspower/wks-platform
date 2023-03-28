import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
import Select from '@mui/material/Select';
import TextField from '@mui/material/TextField';
import React from 'react';
import { useEffect, useState } from 'react';
import { useSession } from 'SessionStoreContext';
import { ProcessDefService } from 'services/ProcessDefService';

export const CaseDefGeneralForm = ({ caseDef, setCaseDef }) => {
    const [processesDefinitions, setProcessesDefinitions] = useState();
    const keycloak = useSession();

    useEffect(() => {
        ProcessDefService.find(keycloak)
            .then((data) => {
                setProcessesDefinitions(data);
            })
            .catch((err) => {
                setProcessesDefinitions(null);
                console.log(err.message);
            });
    }, [caseDef]);

    const handleInputChange = (event) => {
        setCaseDef({ ...caseDef, [event.target.name]: event.target.value });
    };

    const handleProcessDefinitionChange = (event) => {
        setCaseDef({ ...caseDef, stagesLifecycleProcessKey: event.target.value });
    };

    return (
        <React.Fragment>
            <FormControl key="ctrlId">
                <TextField
                    id="txtId"
                    label="Id"
                    value={caseDef.id}
                    name="id"
                    onChange={handleInputChange}
                    disabled={!(caseDef.status && caseDef.status === 'new')}
                />
            </FormControl>

            <FormControl key="ctrlName" sx={{ mt: 3 }}>
                <TextField
                    label="Name"
                    id="txtName"
                    value={caseDef.name}
                    name="name"
                    onChange={handleInputChange}
                />
            </FormControl>

            {processesDefinitions && (
                <FormControl key="ctrlStagesLCProcess" variant="outlined" sx={{ mt: 3 }}>
                    <InputLabel id="processDefinitionlId">Process Definition</InputLabel>
                    <Select
                        labelId="processDefinitionId"
                        label="Process Definition"
                        id="selectProcessDefinition"
                        value={caseDef.stagesLifecycleProcessKey}
                        onChange={handleProcessDefinitionChange}
                    >
                        <MenuItem key="processDefEmptyOptionKey" value="null">
                            &nbsp;
                        </MenuItem>
                        {processesDefinitions.map((processDefinition) => {
                            return (
                                <MenuItem key={processDefinition.key} value={processDefinition.key}>
                                    {processDefinition.name}
                                </MenuItem>
                            );
                        })}
                    </Select>
                </FormControl>
            )}
        </React.Fragment>
    );
};
