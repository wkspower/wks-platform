import PropTypes from 'prop-types';

// material-ui
import { AppBar, IconButton, Toolbar, useMediaQuery } from '@mui/material';
import { useTheme } from '@mui/material/styles';

// project import
import AppBarStyled from './AppBarStyled';
import HeaderContent from './HeaderContent';

// assets
import { MenuFoldOutlined, MenuUnfoldOutlined } from '@ant-design/icons';

// ==============================|| MAIN LAYOUT - HEADER ||============================== //

const Header = ({ open, handleDrawerToggle, keycloak }) => {
    const theme = useTheme();
    const matchDownMD = useMediaQuery(theme.breakpoints.down('lg'));

    const iconBackColor = 'grey.100';
    const iconBackColorOpen = 'grey.200';

    // common header
    const mainHeader = (
        <Toolbar>
            <IconButton
                disableRipple
                aria-label="open drawer"
                onClick={handleDrawerToggle}
                edge="start"
                color="secondary"
                sx={{ color: 'text.primary', bgcolor: open ? iconBackColorOpen : iconBackColor, ml: { xs: 0, lg: -2 } }}
            >
                {!open ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            </IconButton>
            <HeaderContent keycloak={keycloak} />
        </Toolbar>
    );

    // app-bar params
    const appBar = {
        position: 'fixed',
        color: 'inherit',
        elevation: 0,
        sx: {
            borderBottom: `1px solid ${theme.palette.divider}`
            // boxShadow: theme.customShadows.z1
        }
    };

    return (
        <>
            {!matchDownMD ? (
                <AppBarStyled open={open} {...appBar}>
                    {mainHeader}
                </AppBarStyled>
            ) : (
                <AppBar {...appBar}>{mainHeader}</AppBar>
            )}
        </>
    );
};

Header.propTypes = {
    open: PropTypes.bool,
    handleDrawerToggle: PropTypes.func
};

export default Header;
