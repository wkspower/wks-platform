import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import React, { useState } from 'react';
import { Grid } from '@mui/material';
import MainCard from 'components/MainCard';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemAvatar from '@mui/material/ListItemAvatar';
import ListItemText from '@mui/material/ListItemText';
import ListItemButton from '@mui/material/ListItemButton';
import Avatar from '@mui/material/Avatar';
import FilePdfOutlined from '@ant-design/icons/FilePdfOutlined';
import FileExcelOutlined from '@ant-design/icons/FileExcelOutlined';
import FileOutlined from '@ant-design/icons/FileOutlined';
import FileImageOutlined from '@ant-design/icons/FileImageOutlined';
import Fade from '@mui/material/Fade';
import CircularProgress from '@mui/material/CircularProgress';
import Snackbar from '@mui/material/Snackbar';
import MuiAlert from '@mui/material/Alert';
import { useSession } from 'SessionStoreContext';
import { FileService } from '../../services';
import CaseStore from './store';
import Files from 'react-files';

function Attachments({ aCase }) {
    const keycloak = useSession();
    const [fetching, setFetching] = useState(false);
    const [percent, setPercent] = useState(0);
    const [messageError, setMessageError] = useState(null);
    const [filesUploaded, setFilesUploaded] = useState(aCase.attachments || []);

    const handleChange = (files) => {
        setFetching(true);

        CaseStore.saveAttachmentsFromFiles(keycloak, files, aCase, setPercent)
            .then((data) => {
                setFilesUploaded([...filesUploaded, ...data]);
            })
            .catch((e) => {
                console.log(e);
                setMessageError(e);
            })
            .finally(() => {
                const timer = setTimeout(() => {
                    setPercent(0);
                    setFetching(false);
                    clearTimeout(timer);
                }, 800);
            });
    };

    const handleError = (error, file) => {
        console.log('error code ' + error.code + ': ' + error.message);
    };

    const handleCloseMesssage = () => {
        setMessageError(null);
    };

    const AnimatedCircularProgress = React.forwardRef((props, ref) => {
        return (
            <div ref={ref} {...props}>
                <Box sx={{ position: 'relative', display: 'inline-flex', top: 10 }}>
                    <CircularProgress variant="determinate" {...props} size={60} />
                    <Box
                        sx={{
                            top: 0,
                            left: 0,
                            bottom: 0,
                            right: 0,
                            position: 'absolute',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center'
                        }}
                    >
                        <Typography
                            variant="caption"
                            component="div"
                            color="text.secondary"
                        >{`${Math.round(props.value)}%`}</Typography>
                    </Box>
                </Box>
            </div>
        );
    });

    const Alert = React.forwardRef((props, ref) => {
        return <MuiAlert elevation={6} ref={ref} variant="filled" {...props} />;
    });

    return (
        <Grid container spacing={2} sx={{ display: 'flex', flexDirection: 'column' }}>
            <Grid item xs={12}>
                <MainCard sx={{ mb: 1 }}>
                    <Box sx={{ border: '1px dashed #d9d9d9', padding: 5 }}>
                        <Grid
                            container
                            direction="column"
                            justifyContent="center"
                            alignItems="center"
                        >
                            <Avatar
                                style={{
                                    backgroundColor: '#27CDF2',
                                    fontSize: 40,
                                    height: 60,
                                    width: 60,
                                    opacity: 0.5
                                }}
                            >
                                <FilePdfOutlined />
                            </Avatar>

                            <br />

                            <Files
                                className="files-dropzone"
                                onChange={handleChange}
                                onError={handleError}
                                multiple
                                clickable
                            >
                                <Typography variant="h4" color="textSecondary" sx={{ pr: 0.5 }}>
                                    Drop files here or click to upload
                                </Typography>
                            </Files>

                            <Fade in={fetching}>
                                <AnimatedCircularProgress value={percent} />
                            </Fade>
                        </Grid>
                    </Box>

                    <div style={{ paddingTop: 15 }}>
                        <Typography variant="h5" color="textSecondary" sx={{ pr: 0.2 }}>
                            Your files will be shown here
                        </Typography>
                        <hr />
                    </div>

                    <List>
                        {filesUploaded.map((file, index) => {
                            return (
                                <ListItem key={index}>
                                    <ListItemAvatar>
                                        {file.type === 'application/pdf' && (
                                            <Avatar style={{ backgroundColor: 'red' }}>
                                                <FilePdfOutlined />
                                            </Avatar>
                                        )}

                                        {file.type === 'application/xls' && (
                                            <Avatar style={{ backgroundColor: 'green' }}>
                                                <FileExcelOutlined />
                                            </Avatar>
                                        )}

                                        {file.type && file.type.includes('image/') && (
                                            <Avatar style={{ backgroundColor: 'lightblue' }}>
                                                <FileImageOutlined />
                                            </Avatar>
                                        )}

                                        {file.type !== 'application/xls' &&
                                            file.type !== 'application/pdf' &&
                                            file.type &&
                                            !file.type.includes('image/') && (
                                                <Avatar style={{ backgroundColor: 'grey' }}>
                                                    <FileOutlined />
                                                </Avatar>
                                            )}
                                    </ListItemAvatar>
                                    <ListItemText
                                        primary={file.name}
                                        secondary={file.size + 'KB'}
                                        style={{ maxWidth: '80%' }}
                                    />
                                    <ListItemButton
                                        style={{ maxWidth: '10%' }}
                                        component="button"
                                        onClick={() => downloadFile(file, keycloak)}
                                    >
                                        <ListItemText primary="Download" />
                                    </ListItemButton>
                                </ListItem>
                            );
                        })}
                    </List>
                </MainCard>
            </Grid>

            <Snackbar open={!!messageError} autoHideDuration={6000} onClose={handleCloseMesssage}>
                <Alert onClose={handleCloseMesssage} severity="error" sx={{ width: '100%' }}>
                    {messageError}
                </Alert>
            </Snackbar>
        </Grid>
    );
}

const downloadFile = (file, keycloak) => {
    return FileService.download(file, keycloak);
};

export default Attachments;
