import { Box, useMediaQuery } from '@mui/material';
import MobileSection from './MobileSection';
import Notification from './Notification';
import Profile from './Profile';
import Search from './Search';

// ==============================|| HEADER - CONTENT ||============================== //

const HeaderContent = ({ keycloak }) => {
    const matchesXs = useMediaQuery((theme) => theme.breakpoints.down('md'));

    return (
        <>
            {!matchesXs && <Search />}
            {matchesXs && <Box sx={{ width: '100%', ml: 1 }} />}
            <Notification />
            {!matchesXs && <Profile keycloak={keycloak} />}
            {matchesXs && <MobileSection />}
        </>
    );
};

export default HeaderContent;
