import { Link as RouterLink } from 'react-router-dom';

// material-ui
import { Box, Grid, Link, Stack, Typography } from '@mui/material';

// project import
import MainCard from 'components/MainCard';

// assets

const DashboardCard = ({ title, subtitle, to, icon }) => (
    <MainCard contentSX={{ p: 2.25 }}>
        <Link component={RouterLink} to={to}>
            <Stack spacing={0.5}>
                <Typography variant="h6" color="textSecondary">
                    {title}
                </Typography>
                <Grid container alignItems="center">
                    <Grid item>{icon}</Grid>
                </Grid>
            </Stack>
        </Link>
        <Box sx={{ pt: 2.25 }}>
            <Typography variant="caption" color="textSecondary">
                {subtitle}
            </Typography>
        </Box>
    </MainCard>
);

export default DashboardCard;
