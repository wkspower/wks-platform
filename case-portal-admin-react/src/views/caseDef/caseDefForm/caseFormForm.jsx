import FormControl from '@mui/material/FormControl';
import FormHelperText from '@mui/material/FormHelperText';
import TextField from '@mui/material/TextField';

export const CaseDefFormForm = ({ formKey }) => {
    return (
        <div style={{ display: 'grid', padding: '10px' }}>
            <FormControl key="ctrlId" style={{ padding: '5px' }}>
                <TextField id="txtId" aria-describedby="my-helper-text" value={formKey} />
                <FormHelperText id="my-helper-text">Form Key</FormHelperText>
            </FormControl>
        </div>
    );
};
