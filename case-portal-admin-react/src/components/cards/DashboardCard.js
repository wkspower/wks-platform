import { Link as RouterLink } from 'react-router-dom';

// material-ui
import { Box, Grid, Link, Stack, Typography } from '@mui/material';

// project import
import MainCard from 'components/MainCard';

// assets

const DashboardCard = ({ title, subtitle, to, icon }) => (
    <MainCard contentSX={{ p: 2.25 }}>
        <Stack spacing={0.5}>
            <Typography variant="h6" color="textSecondary">
                {title}
            </Typography>
            <Grid container alignItems="center">
                <Grid item>
                    <Link component={RouterLink} to={to}>
                        {icon}
                    </Link>
                </Grid>
            </Grid>
        </Stack>
        <Box sx={{ pt: 2.25 }}>
            <Typography variant="caption" color="textSecondary">
                {subtitle}
            </Typography>
        </Box>
    </MainCard>
);

export default DashboardCard;
