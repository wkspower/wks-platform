import FileExcelOutlined from '@ant-design/icons/FileExcelOutlined';
import FileImageOutlined from '@ant-design/icons/FileImageOutlined';
import FileOutlined from '@ant-design/icons/FileOutlined';
import FilePdfOutlined from '@ant-design/icons/FilePdfOutlined';
import { Grid } from '@mui/material';
import MuiAlert from '@mui/material/Alert';
import Avatar from '@mui/material/Avatar';
import Box from '@mui/material/Box';
import CircularProgress from '@mui/material/CircularProgress';
import Fade from '@mui/material/Fade';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemAvatar from '@mui/material/ListItemAvatar';
import ListItemText from '@mui/material/ListItemText';
import Snackbar from '@mui/material/Snackbar';
import Typography from '@mui/material/Typography';
import { useSession } from 'SessionStoreContext';
import React, { useState } from 'react';
import Files from 'react-files';
import { FileService } from '../../services';
import CaseStore from './store';

function Documents({ aCase, initialValue }) {
    const keycloak = useSession();
    const [fetching, setFetching] = useState(false);
    const [percent, setPercent] = useState(0);
    const [messageError, setMessageError] = useState(null);
    const [filesUploaded, setFilesUploaded] = useState(initialValue);

    const handleChange = (files) => {
        setFetching(true);

        CaseStore.saveDocumentsFromFiles(keycloak, files, aCase.businessKey, setPercent)
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
            <Box sx={{ padding: 5 }}>
                <Grid container direction="column" justifyContent="center" alignItems="center">
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

            {filesUploaded && filesUploaded.length > 0 && (
                <List sx={{ border: '1px dashed #d9d9d9' }}>
                    {filesUploaded.map((file, index) => {
                        return (
                            <ListItem key={index} onClick={() => downloadFile(file, keycloak)}>
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
                            </ListItem>
                        );
                    })}
                </List>
            )}

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

export default Documents;
