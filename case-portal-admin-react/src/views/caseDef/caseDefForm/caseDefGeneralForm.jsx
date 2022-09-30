import FormControl from '@mui/material/FormControl';
import FormHelperText from '@mui/material/FormHelperText';
import TextField from '@mui/material/TextField';

export const CaseDefGeneralForm = ({ caseDef }) => {
    return (
        <div style={{ display: 'grid', padding: '10px' }}>
            <FormControl key="ctrlId" style={{ padding: '5px' }}>
                <TextField id="txtId" aria-describedby="my-helper-text" value={caseDef.id} />
                <FormHelperText id="my-helper-text">Id</FormHelperText>
            </FormControl>
            <FormControl key="ctrlName" style={{ padding: '5px' }}>
                <TextField id="txtName" aria-describedby="my-helper-text" value={caseDef.name} />
                <FormHelperText id="my-helper-text">Name</FormHelperText>
            </FormControl>
            <FormControl key="ctrlStagesLCProcess" style={{ padding: '5px' }}>
                <TextField id="txtStagesLCProcess" aria-describedby="my-helper-text" value={caseDef.stagesLifecycleProcessKey} />
                <FormHelperText id="my-helper-text">Stages Lifecycle Process Key</FormHelperText>
            </FormControl>
        </div>
    );
};
