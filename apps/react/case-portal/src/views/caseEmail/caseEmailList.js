import EmailIcon from '@mui/icons-material/Email';
import {
    Avatar,
    Box,
    Button,
    Collapse,
    List,
    ListItem,
    ListItemAvatar,
    ListItemText,
    Modal,
    Typography
} from '@mui/material';
import { useSession } from 'SessionStoreContext';
import DOMPurify from 'dompurify';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { EmailService } from '../../services';
import { EmailForm } from './emailForm';

export const CaseEmailsList = ({ caseInstanceBusinessKey }) => {
    const [emails, setEmails] = useState([]);
    const [expandedEmailId, setExpandedEmailId] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const { t } = useTranslation();
    const keycloak = useSession();

    useEffect(() => {
        EmailService.getAllByBusinessKey(keycloak, caseInstanceBusinessKey)
            .then((data) => {
                setEmails(data);
            })
            .catch((err) => {
                console.log(err.message);
            });
    }, [caseInstanceBusinessKey]);

    const handleExpandToggle = (emailId) => {
        setExpandedEmailId(expandedEmailId === emailId ? null : emailId);
    };

    const handleEmailSend = (emailData) => {
        EmailService.send(keycloak, {
            ...emailData,
            outbound: true,
            caseInstanceBusinessKey
        });
        setIsModalOpen(false);
    };

    const handleOpenModal = () => {
        setIsModalOpen(true);
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
    };

    return (
        <>
            <Button
                variant="contained"
                color="primary"
                onClick={handleOpenModal}
                style={{ marginBottom: '20px' }}
            >
                {t('pages.emails.datagrid.action.compose')}
            </Button>
            <Modal
                open={isModalOpen}
                onClose={handleCloseModal}
                aria-labelledby="email-modal-title"
                aria-describedby="email-modal-description"
            >
                <Box
                    sx={{
                        position: 'absolute',
                        top: '50%',
                        left: '50%',
                        transform: 'translate(-50%, -50%)',
                        width: '80%', // Adjust the width of the modal
                        maxWidth: 600, // Add a maximum width to the modal
                        bgcolor: 'background.paper',
                        boxShadow: 24,
                        p: 4
                    }}
                >
                    <EmailForm onSubmit={handleEmailSend} />
                </Box>
            </Modal>
            <List sx={{ width: '100%', bgcolor: 'background.paper' }}>
                {emails.map((email) => (
                    <>
                        <ListItem button onClick={() => handleExpandToggle(email._id)}>
                            <ListItemAvatar>
                                <Avatar alt={email.from}>
                                    <EmailIcon />
                                </Avatar>
                            </ListItemAvatar>
                            <ListItemText
                                primary={email.from}
                                secondary={
                                    <>
                                        <Typography variant="body2" color="text.secondary">
                                            {email.subject}
                                        </Typography>
                                        <Typography variant="body2" color="text.secondary">
                                            {t('pages.emails.datagrid.receivedDateTime')}:{' '}
                                            {new Date(email.receivedDateTime).toLocaleString()}
                                        </Typography>
                                        <Typography variant="body2" color="text.secondary">
                                            {t('pages.emails.datagrid.hasAttachments')}:{' '}
                                            {email.hasAttachments ? 'Yes' : 'No'}
                                        </Typography>
                                    </>
                                }
                            />
                        </ListItem>
                        <Collapse
                            in={expandedEmailId === email._id}
                            timeout="auto"
                            unmountOnExit
                        >
                            <Box margin={1}>
                                <Typography variant="body2" color="text.secondary">
                                    {t('pages.emails.datagrid.to')}: {email.to}
                                </Typography>
                                <Typography
                                    variant="body2"
                                    color="text.secondary"
                                    dangerouslySetInnerHTML={{
                                        __html: DOMPurify.sanitize(email.body)
                                    }}
                                />
                            </Box>
                        </Collapse>
                    </>
                ))}
            </List>
        </>
    );
};
