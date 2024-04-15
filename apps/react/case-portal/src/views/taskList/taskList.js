import { AddCircleOutline, PlayCircle } from '@mui/icons-material';
import { Box } from '@mui/material';
import Button from '@mui/material/Button';
import List from '@mui/material/List';
import {
  default as IconButton,
  default as ListItem,
} from '@mui/material/ListItem';
import { default as ListItemSecondaryAction } from '@mui/material/ListItemIcon';
import ListSubheader from '@mui/material/ListSubheader';
import Modal from '@mui/material/Modal';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import Config from 'consts/index';
import { format } from 'date-fns';
import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { TaskService } from 'services';
import { useSession } from '../../SessionStoreContext';
import { TaskForm } from '../taskForm/taskForm';
import './taskList.css';

export const TaskList = ({ businessKey, callback }) => {
  const [tasks, setTasks] = useState(null);
  const [open, setOpen] = useState(false);
  const [task, setTask] = useState(null);
  const { t } = useTranslation();
  const [fetching, setFetching] = useState(false);
  const keycloak = useSession();

  const [isModalOpen, setModalOpen] = useState(false);
  const [newTaskData, setNewTaskData] = useState({
    name: '',
    description: '',
    due: null,
    assignee: '',
    caseInstanceId: businessKey,
  });

  useEffect(() => {
    if (Config.WebsocketsEnabled) {
      const websocketUrl = Config.WebsocketUrl;
      const topic = Config.WebsocketsTopicHumanTaskCreated;
      const ws = new WebSocket(`${websocketUrl}/${topic}`);
      ws.onmessage = () => {
        fetchTasks(setFetching, keycloak, businessKey, setTasks);
      };
      return () => {
        ws.close(); // Close WebSocket connection when component unmounts
      };
    }
  }, []);

  const handleNewTaskSubmit = () => {
    // Perform any necessary validation on the new task data
    // ...
    TaskService.createNewTask(keycloak, newTaskData).then(() => {
      fetchTasks(setFetching, keycloak, businessKey, setTasks);
    });

    // Reset the new task form
    setNewTaskData({
      name: '',
      description: '',
      due: null,
      assignee: '',
      caseInstanceId: businessKey,
    });

    // Close the modal
    setModalOpen(false);
  };

  useEffect(() => {
    fetchTasks(setFetching, keycloak, businessKey, setTasks);
  }, [open, businessKey]);

  const handleClose = () => {
    setOpen(false);
    callback();
  };

  return (
    <React.Fragment>
      {fetching && <Typography>Loading...</Typography>}
      <Button
        variant='contained'
        startIcon={<AddCircleOutline />}
        onClick={() => setModalOpen(true)}
      >
        {t('pages.caseform.actions.newTask')}
      </Button>
      {tasks && tasks.length > 0 && (
        <React.Fragment>
          <List
            component='nav'
            aria-labelledby='task-list'
            subheader={
              <ListSubheader component='div' id='nested-list-subheader'>
                {t('pages.tasklist.upcoming')}
              </ListSubheader>
            }
          >
            {tasks.map((task) => (
              <ListItem
                key={task.id}
                disablePadding
                sx={{
                  borderRadius: '5px',
                  mb: 1,
                  boxShadow: '0px 2px 4px rgba(0, 0, 0, 0.1)', // Adding box shadow for differentiation
                  backgroundColor: 'rgba(255, 255, 255, 0.5)', // Adding background color for differentiation
                }}
              >
                <Box sx={{ p: 2, flex: 1 }}>
                  <Typography variant='subtitle1'>{task.name}</Typography>
                  {task.assignee && (
                    <Typography variant='body2'>
                      {t('pages.tasklist.datagrid.columns.assignee')}:{' '}
                      {task.assignee}
                    </Typography>
                  )}
                  {task.created && (
                    <Typography variant='body2'>
                      {t('pages.tasklist.datagrid.columns.created')}:{' '}
                      {task.created}
                    </Typography>
                  )}
                  {task.due && (
                    <Typography variant='body2'>
                      {t('pages.tasklist.datagrid.columns.due')}: {task.due}
                    </Typography>
                  )}
                  {task.followUp && (
                    <Typography variant='body2'>
                      {t('pages.tasklist.datagrid.columns.followup')}:{' '}
                      {task.followUp}
                    </Typography>
                  )}
                </Box>
                <ListItemSecondaryAction>
                  <IconButton
                    edge='end'
                    aria-label='complete'
                    onClick={() => {
                      setTask(task);
                      setOpen(true);
                    }}
                  >
                    <PlayCircle color='primary' />
                  </IconButton>
                </ListItemSecondaryAction>
              </ListItem>
            ))}
          </List>

          <Modal open={isModalOpen} onClose={() => setModalOpen(false)}>
            <Box
              sx={{
                position: 'absolute',
                top: '50%',
                left: '50%',
                transform: 'translate(-50%, -50%)',
                bgcolor: 'background.paper',
                boxShadow: 24,
                p: 4,
                minWidth: 400,
                maxWidth: 600,
              }}
            >
              <Typography variant='h6' component='h2' gutterBottom>
                {t('pages.caseform.actions.newTask')}
              </Typography>
              <Box
                sx={{ display: 'flex', flexDirection: 'column', gap: '10px' }}
              >
                <TextField
                  label={t('pages.tasklist.newTask.name')}
                  value={newTaskData.name}
                  onChange={(e) =>
                    setNewTaskData({ ...newTaskData, name: e.target.value })
                  }
                />
                <TextField
                  label={t('pages.tasklist.newTask.description')}
                  value={newTaskData.description}
                  onChange={(e) =>
                    setNewTaskData({
                      ...newTaskData,
                      description: e.target.value,
                    })
                  }
                />
                {/* <TextField
                                    label={t('pages.tasklist.newTask.dueDate')}
                                    value={newTaskData.due}
                                    onChange={(e) =>
                                        setNewTaskData({ ...newTaskData, due: e.target.value })
                                    }
                                /> */}
                <TextField
                  label={t('pages.tasklist.newTask.assignee')}
                  value={newTaskData.assignee}
                  onChange={(e) =>
                    setNewTaskData({
                      ...newTaskData,
                      assignee: e.target.value,
                    })
                  }
                />
                <Button
                  type='submit'
                  variant='contained'
                  onClick={handleNewTaskSubmit}
                >
                  Submit
                </Button>
              </Box>
            </Box>
          </Modal>
        </React.Fragment>
      )}

      {open && task && (
        <TaskForm
          task={task}
          handleClose={handleClose}
          open={open}
          keycloak={keycloak}
        />
      )}
    </React.Fragment>
  );
};

function fetchTasks(setFetching, keycloak, businessKey, setTasks) {
  setFetching(true);

  TaskService.filterTasks(keycloak, businessKey)
    .then((data) => {
      setTasks(
        data?.map(
          (o) =>
            (o = {
              ...o,
              created: o.created && format(new Date(o.created), 'P'),
              due: o.due && format(new Date(o.due), 'P'),
              followUp: o.followUp && format(new Date(o.followUp), 'P'),
            }),
        ),
      );
    })
    .finally(() => {
      setFetching(false);
    });
}
