import FormControl from '@mui/material/FormControl';
import FormHelperText from '@mui/material/FormHelperText';
import TextField from '@mui/material/TextField';

export const CaseDefGeneralForm = ({ caseDef, setCaseDef }) => {
    const handleInputChange = (event) => {
        setCaseDef({ ...caseDef, [event.target.name]: event.target.value });
    };

    return (
        <div style={{ display: 'grid', padding: '10px' }}>
            <FormControl key="ctrlName" style={{ padding: '5px' }}>
                <TextField id="txtName" aria-describedby="my-helper-text" value={caseDef.name} name="name" onChange={handleInputChange} />
                <FormHelperText id="my-helper-text">Name</FormHelperText>
            </FormControl>
            <FormControl key="ctrlStagesLCProcess" style={{ padding: '5px' }}>
                <TextField
                    id="txtStagesLCProcess"
                    aria-describedby="my-helper-text"
                    value={caseDef.stagesLifecycleProcessKey}
                    name="stagesLifecycleProcessKey"
                    onChange={handleInputChange}
                />
                <FormHelperText id="my-helper-text">Stages Lifecycle Process Key</FormHelperText>
            </FormControl>
        </div>
    );
};
