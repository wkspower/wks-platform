import { Menu } from 'react-admin';

export const CustomMenu = () => (
    <Menu>
        <Menu.DashboardItem />
        <Menu.Item to="/caseDefList" primaryText="Cases Definitions" />
        <Menu.Item to="/caseList" primaryText="Cases" />
        <Menu.Item to="/tasklist" primaryText="Tasks" />
    </Menu>
);