import { useState } from 'react';
import { TextField, Button, Grid, Typography } from '@mui/material';
import { makeStyles } from '@mui/styles';
import { useTranslation } from 'react-i18next';

const useStyles = makeStyles(() => ({
  form: {
    width: '100%',
  },
  submitButton: {
    marginTop: '16px',
  },
}));

export const EmailForm = ({ onSubmit }) => {
  const [recipient, setRecipient] = useState('');
  const [subject, setSubject] = useState('');
  const [body, setBody] = useState('');
  const { t } = useTranslation();
  const classes = useStyles();

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit({ to: recipient, subject, body });
    // Clear the form after submission
    setRecipient('');
    setSubject('');
    setBody('');
  };

  return (
    <>
      <Typography variant='h5' gutterBottom>
        {t('pages.emails.form.title')}
      </Typography>
      <form onSubmit={handleSubmit}>
        <Grid container spacing={2}>
          <Grid item xs={12}>
            <TextField
              fullWidth
              required
              label={t('pages.emails.form.recipient')}
              value={recipient}
              onChange={(e) => setRecipient(e.target.value)}
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              required
              label={t('pages.emails.form.subject')}
              value={subject}
              onChange={(e) => setSubject(e.target.value)}
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              required
              multiline
              rows={6} // Set number of rows
              label={t('pages.emails.form.body')}
              value={body}
              onChange={(e) => setBody(e.target.value)}
            />
          </Grid>
          <Grid item xs={12}>
            <Button
              type='submit'
              variant='contained'
              color='primary'
              className={classes.submitButton}
            >
              {t('pages.emails.form.send')}
            </Button>
          </Grid>
        </Grid>
      </form>
    </>
  );
};
