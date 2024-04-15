import Checkbox from '@mui/material/Checkbox';
import FormControl from '@mui/material/FormControl';
import FormControlLabel from '@mui/material/FormControlLabel';
import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
import Select from '@mui/material/Select';
import TextField from '@mui/material/TextField';
import { useSession } from 'SessionStoreContext';
import React, { useEffect, useState } from 'react';
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
    setCaseDef({
      ...caseDef,
      deployed: event.target.checked,
      [event.target.name]: event.target.value,
    });
  };

  const handleDeployedChange = (event) => {
    setCaseDef({ ...caseDef, deployed: event.target.checked });
  };

  const handleProcessDefinitionChange = (event) => {
    setCaseDef({ ...caseDef, stagesLifecycleProcessKey: event.target.value });
  };

  return (
    <React.Fragment>
      <FormControl key='ctrlId'>
        <TextField
          id='txtId'
          label='Id'
          value={caseDef.id}
          name='id'
          onChange={handleInputChange}
          disabled={!(caseDef.status && caseDef.status === 'new')}
        />
      </FormControl>

      <FormControl key='ctrlName' sx={{ mt: 3 }}>
        <TextField
          label='Name'
          id='txtName'
          value={caseDef.name}
          name='name'
          onChange={handleInputChange}
        />
      </FormControl>

      {processesDefinitions && (
        <FormControl
          key='ctrlStagesLCProcess'
          variant='outlined'
          sx={{ mt: 3 }}
        >
          <InputLabel id='processDefinitionlId'>Process Definition</InputLabel>
          <Select
            labelId='processDefinitionId'
            label='Process Definition'
            id='selectProcessDefinition'
            value={caseDef.stagesLifecycleProcessKey}
            onChange={handleProcessDefinitionChange}
          >
            <MenuItem key='processDefEmptyOptionKey' value='null'>
              &nbsp;
            </MenuItem>
            {processesDefinitions.map((processDefinition) => {
              return (
                <MenuItem
                  key={processDefinition.key}
                  value={processDefinition.key}
                >
                  {processDefinition.name}
                </MenuItem>
              );
            })}
          </Select>
        </FormControl>
      )}

      <FormControl key='ctrlDeployed' sx={{ mt: 3 }}>
        <FormControlLabel
          control={<Checkbox checked={caseDef.deployed} />}
          label='Deployed'
          id='txtDeployed'
          name='deployed'
          onChange={handleDeployedChange}
        />
      </FormControl>
    </React.Fragment>
  );
};
