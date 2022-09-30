// material-ui
import { Grid, Typography } from '@mui/material';
import DashBoardCard from 'components/cards/DashboardCard';

import { IconArchive, IconList, IconSquareAsterisk } from '@tabler/icons';

// ==============================|| DASHBOARD - DEFAULT ||============================== //

const DashboardDefault = () => {
    return (
        <Grid container rowSpacing={4.5} columnSpacing={2.75}>
            {/* row 1 */}
            <Grid item xs={12} sx={{ mb: -2.25 }}>
                <Typography variant="h5">My Workspace</Typography>
            </Grid>
            <Grid item xs={12} sm={6} md={4} lg={3}>
                <DashBoardCard title="Work in Progress" icon={<IconArchive />} to="/case-list/wip-cases" />
            </Grid>
            <Grid item xs={12} sm={6} md={4} lg={3}>
                <DashBoardCard title="All" icon={<IconSquareAsterisk />} to="/case-list/cases" />
            </Grid>
            <Grid item xs={12} sm={6} md={4} lg={3}>
                <DashBoardCard title="Tasks" icon={<IconList />} to="/task-list" />
            </Grid>

            <Grid item md={8} sx={{ display: { sm: 'none', md: 'block', lg: 'none' } }} />
        </Grid>
    );
};

export default DashboardDefault;