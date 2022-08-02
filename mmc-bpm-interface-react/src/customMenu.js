import { Menu } from 'react-admin';

export const CustomMenu = () => (
    <Menu>
        <Menu.DashboardItem />
        <Menu.Item to="/case" primaryText="Cases" />
        <Menu.Item to="/tasklist" primaryText="Task List" />
    </Menu>
);