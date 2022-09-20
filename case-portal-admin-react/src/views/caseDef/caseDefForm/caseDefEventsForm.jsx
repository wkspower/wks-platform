import { useState } from 'react';

import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import TreeItem from '@mui/lab/TreeItem';
import TreeView from '@mui/lab/TreeView';

import { DataGrid, GridColDef } from '@mui/x-data-grid';
import MainCard from 'components/MainCard';
import { Grid, Box } from '@mui/material';

const eventsColumns: GridColDef[] = [
    { field: 'name', headerName: 'Name', width: 200 },
    { field: 'type', headerName: 'Type', width: 250 }
];

export const CaseDefEventsForm = ({ caseDef }) => {
    const [hook, setHook] = useState(null);
    const handleHookChange = (event, nodeId) => {
        if (!nodeId.endsWith('Group')) {
            setHook(nodeId);
        }
    };

    return (
        <Grid container rowSpacing={4.5} columnSpacing={2.75}>
            <Grid item xs={12} md={7} lg={2}>
                <TreeView
                    aria-label="file system navigator"
                    defaultCollapseIcon={<ExpandMoreIcon />}
                    defaultExpandIcon={<ChevronRightIcon />}
                    onNodeSelect={handleHookChange}
                    sx={{ flexGrow: 1, maxWidth: 200 }}
                >
                    <div style={{ fontWeight: 'bold', padding: '5px' }}>Case Hooks</div>
                    <TreeItem nodeId="createGroup" label="Create">
                        <TreeItem nodeId="beforeCaseCreateHook" label="Before Create" />
                        <TreeItem nodeId="postCaseCreateHook" label="After Create" />
                    </TreeItem>
                    <TreeItem nodeId="closeGroup" label="Close">
                        <TreeItem nodeId="beforeCaseCloseHook" label="Before Close" />
                        <TreeItem nodeId="postCaseCloseHook" label="After Close" />
                    </TreeItem>
                    <TreeItem nodeId="stateUpdateGroup" label="State Update">
                        <TreeItem nodeId="beforeCaseStateUpdateHook" label="Before State Update" />
                        <TreeItem nodeId="postCaseStateUpdateHook" label="After State Update" />
                    </TreeItem>
                    <TreeItem nodeId="updateGroup" label="Update">
                        <TreeItem nodeId="beforeCaseUpdateHook" label="Before Update" />
                        <TreeItem nodeId="postCaseUpdateHook" label="After Update" />
                    </TreeItem>
                    <TreeItem nodeId="archiveGroup" label="Archive">
                        <TreeItem nodeId="beforeCaseArchiveHook" label="Before Archive" />
                        <TreeItem nodeId="postCaseArchiveHook" label="After Archive" />
                    </TreeItem>
                    <TreeItem nodeId="assignGroup" label="Assign">
                        <TreeItem nodeId="beforeCaseAssignHook" label="Before Assign" />
                        <TreeItem nodeId="postCaseAssignHook" label="After Assign" />
                    </TreeItem>
                </TreeView>
            </Grid>
            <Grid item xs={12} md={7} lg={10}>
                <MainCard sx={{ mt: 2 }} content={false}>
                    <Box>
                        <div style={{ height: 650, width: '100%' }}>
                            {hook && caseDef[hook].caseEvents && (
                                <DataGrid rows={caseDef[hook].caseEvents} columns={eventsColumns} pageSize={10} rowsPerPageOptions={[10]} />
                            )}
                        </div>
                    </Box>
                </MainCard>
            </Grid>
        </Grid>
    );
};
