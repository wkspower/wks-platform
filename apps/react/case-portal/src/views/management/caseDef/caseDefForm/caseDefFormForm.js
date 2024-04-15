import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
import Select from '@mui/material/Select';
import React from 'react';
import { useEffect, useState } from 'react';
import { FormService } from 'services';
import { useSession } from 'SessionStoreContext';

export const CaseDefFormForm = ({ caseDef, setCaseDef }) => {
  const [forms, setForms] = useState();
  const keycloak = useSession();

  useEffect(() => {
    FormService.getAll(keycloak)
      .then((data) => {
        setForms(data);
      })
      .catch((err) => {
        console.log(err.message);
      });
  }, [caseDef]);

  const handleFormChange = (event) => {
    setCaseDef({ ...caseDef, formKey: event.target.value });
  };

  return (
    <React.Fragment>
      {forms && (
        <FormControl key='ctrlForm' variant='outlined' sx={{ mt: 3 }}>
          <InputLabel id='formLabelId'>Form</InputLabel>
          <Select
            labelId='formLabelId'
            label='Form'
            id='selectForm'
            value={caseDef.formKey}
            onChange={handleFormChange}
          >
            {forms.map((form) => {
              return (
                <MenuItem key={form.key} value={form.key}>
                  {form.title}
                </MenuItem>
              );
            })}
          </Select>
        </FormControl>
      )}
    </React.Fragment>
  );
};
