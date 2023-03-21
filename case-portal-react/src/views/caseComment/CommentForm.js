import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import { useState } from 'react';

const CommentForm = ({
    handleSubmit,
    submitLabel,
    hasCancelButton = false,
    handleCancel,
    initialText = ''
}) => {
    const [text, setText] = useState(initialText);
    const isTextareaDisabled = text.length === 0;
    const onSubmit = (event) => {
        event.preventDefault();
        handleSubmit(text);
        setText('');
    };
    return (
        <Box sx={{ flexDirection: 'column', m: 1 }}>
            <TextField
                sx={{ display: 'flex' }}
                multiline
                value={text}
                onChange={(e) => setText(e.target.value)}
            />
            <Button
                sx={{ mt: 1 }}
                variant="contained"
                onClick={onSubmit}
                disabled={isTextareaDisabled}
            >
                {submitLabel}
            </Button>
            {hasCancelButton && (
                <Button sx={{ ml: 1, mt: 1 }} variant="contained" onClick={handleCancel}>
                    Cancel
                </Button>
            )}
        </Box>
    );
};

export default CommentForm;
