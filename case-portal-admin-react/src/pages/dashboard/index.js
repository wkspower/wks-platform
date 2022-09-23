// material-ui
import { Grid, Typography } from '@mui/material';
import DashBoardCard from 'components/cards/DashboardCard';

import {
    IconBriefcase,
    IconEngine,
    IconForms,
    IconHeartRateMonitor,
    IconIdBadge2,
    IconMail,
    IconPackgeExport,
    IconPalette,
    IconPencil,
    IconSpy,
    IconUsers,
    IconWebhook
} from '@tabler/icons';

// ==============================|| DASHBOARD - DEFAULT ||============================== //

const DashboardDefault = () => {
    return (
        <Grid container rowSpacing={4.5} columnSpacing={2.75}>
            {/* row 1 */}
            <Grid item xs={12} sx={{ mb: -2.25 }}>
                <Typography variant="h5">Case Life Cycle</Typography>
            </Grid>
            <Grid item xs={12} sm={6} md={4} lg={3}>
                <DashBoardCard
                    title="Case Definitions"
                    subtitle={'Create and edit Case Definitions'}
                    icon={<IconPencil />}
                    to="/case-life-cycle/case-definition"
                />
            </Grid>
            <Grid item xs={12} sm={6} md={4} lg={3}>
                <DashBoardCard
                    title="Forms"
                    subtitle={'Create and edit Cases and Tasks Forms'}
                    icon={<IconForms />}
                    to="/case-life-cycle/form"
                />
            </Grid>
            <Grid item xs={12} sm={6} md={4} lg={3}>
                <DashBoardCard
                    title="Process Engines"
                    subtitle={'Configure Process Engines'}
                    icon={<IconEngine />}
                    to="/case-life-cycle/process-engine"
                />
            </Grid>
            <Grid item xs={12} sm={6} md={4} lg={3}>
                <DashBoardCard
                    title="Export"
                    subtitle={'Export configurations to another environment'}
                    icon={<IconPackgeExport />}
                    to="/case-life-cycle/export"
                />
            </Grid>

            <Grid item md={8} sx={{ display: { sm: 'none', md: 'block', lg: 'none' } }} />

            {/* row 2 */}
            <Grid item xs={12} sx={{ mb: -2.25 }}>
                <Typography variant="h5">Settings</Typography>
            </Grid>
            <Grid item xs={12} sm={6} md={4} lg={3}>
                <DashBoardCard
                    title="Company Settings"
                    subtitle={'Global attributes describing the organization'}
                    icon={<IconBriefcase />}
                    to="/settings/company-settings"
                />
            </Grid>
            <Grid item xs={12} sm={6} md={4} lg={3}>
                <DashBoardCard
                    title="Identity"
                    subtitle={'Identity and Access Management'}
                    icon={<IconIdBadge2 />}
                    to="/settings/identity"
                />
            </Grid>
            <Grid item xs={12} sm={6} md={4} lg={3}>
                <DashBoardCard
                    title="Privacy Settings"
                    subtitle={'Comply with Data Governance'}
                    icon={<IconSpy />}
                    to="/settings/company-settings"
                />
            </Grid>
            <Grid item xs={12} sm={6} md={4} lg={3}>
                <DashBoardCard
                    title="User Engagement"
                    subtitle={'Track how the audience interacts with the platform'}
                    icon={<IconUsers />}
                    to="/settings/user-engagement"
                />
            </Grid>

            <Grid item md={8} sx={{ display: { sm: 'none', md: 'block', lg: 'none' } }} />

            {/* row 3 */}
            <Grid item xs={12} sx={{ mb: -2.25 }}>
                <Typography variant="h5">System</Typography>
            </Grid>
            <Grid item xs={12} sm={6} md={4} lg={3}>
                <DashBoardCard
                    title="Look and Fell"
                    subtitle={'Customize the Platform Appearance'}
                    icon={<IconPalette />}
                    to="/system/look-and-feel"
                />
            </Grid>
            <Grid item xs={12} sm={6} md={4} lg={3}>
                <DashBoardCard title="E-mail" subtitle={'Change e-mail settings'} icon={<IconMail />} to="/system/email" />
            </Grid>
            <Grid item xs={12} sm={6} md={4} lg={3}>
                <DashBoardCard
                    title="Monitoring"
                    subtitle={'Measure Performance'}
                    icon={<IconHeartRateMonitor />}
                    to="/system/monitoring"
                />
            </Grid>
            <Grid item xs={12} sm={6} md={4} lg={3}>
                <DashBoardCard title="Web Hooks" subtitle={'HTTP callbacks to external URLs'} icon={<IconWebhook />} to="/system/webhook" />
            </Grid>

            <Grid item md={8} sx={{ display: { sm: 'none', md: 'block', lg: 'none' } }} />
        </Grid>
    );
};

export default DashboardDefault;
