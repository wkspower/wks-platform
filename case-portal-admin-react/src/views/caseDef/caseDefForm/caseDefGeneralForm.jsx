import FormControl from '@mui/material/FormControl';
import FormHelperText from '@mui/material/FormHelperText';
import TextField from '@mui/material/TextField';

export const CaseDefGeneralForm = ({ caseDef, setCaseDef }) => {
    const handleInputChange = (event) => {
        setCaseDef({ ...caseDef, [event.target.name]: event.target.value });
    };

    return (
        <div style={{ display: 'grid', padding: '10px' }}>
            <FormControl key="ctrlId" style={{ padding: '5px' }}>
                <TextField
                    id="txtId"
                    aria-describedby="id-helper-text"
                    value={caseDef.id}
                    name="id"
                    onChange={handleInputChange}
                    disabled={!(caseDef.status && caseDef.status === 'new')}
                />
                <FormHelperText id="id-helper-text">Id</FormHelperText>
            </FormControl>
            <FormControl key="ctrlName" style={{ padding: '5px' }}>
                <TextField id="txtName" aria-describedby="name-helper-text" value={caseDef.name} name="name" onChange={handleInputChange} />
                <FormHelperText id="name-helper-text">Name</FormHelperText>
            </FormControl>
            <FormControl key="ctrlStagesLCProcess" style={{ padding: '5px' }}>
                <TextField
                    id="txtStagesLCProcess"
                    aria-describedby="stagesLCProcess-helper-text"
                    value={caseDef.stagesLifecycleProcessKey}
                    name="stagesLifecycleProcessKey"
                    onChange={handleInputChange}
                />
                <FormHelperText id="stagesLCProcess-helper-text">Stages Lifecycle Process Key</FormHelperText>
            </FormControl>
        </div>
    );
};
