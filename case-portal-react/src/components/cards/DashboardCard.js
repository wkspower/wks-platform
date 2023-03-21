import { Link as RouterLink } from 'react-router-dom';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Link from '@mui/material/Link';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
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
            <Box sx={{ pt: 2.25 }}>
                <Typography variant="caption" color="textSecondary">
                    {subtitle}
                </Typography>
            </Box>
        </Link>
    </MainCard>
);

export default DashboardCard;
