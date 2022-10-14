import FormControl from '@mui/material/FormControl';
import FormHelperText from '@mui/material/FormHelperText';
import TextField from '@mui/material/TextField';

export const CaseDefFormForm = ({ caseDef, setCaseDef }) => {
    const handleInputChange = (event) => {
        setCaseDef({ ...caseDef, [event.target.name]: event.target.value });
    };

    return (
        <div style={{ display: 'grid', padding: '10px' }}>
            <FormControl key="ctrlId" style={{ padding: '5px' }}>
                <TextField
                    id="txtId"
                    aria-describedby="key-helper-text"
                    value={caseDef.formKey}
                    name="formKey"
                    onChange={handleInputChange}
                />
                <FormHelperText id="key-helper-text">Form Key</FormHelperText>
            </FormControl>
        </div>
    );
};
