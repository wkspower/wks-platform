import React from 'react';
import { Menu } from 'react-admin';

export const CustomMenu = () => (
    <React.Fragment>

        <Menu.DashboardItem />

        {/* Admin Menu */}
        <Menu>
            <Menu.Item to="/lookAndFeel" primaryText="Look and Feel" />

            <Menu.Item to="/email" primaryText="Email" />
            <Menu.Item to="/notification" primaryText="Notification" />
            <Menu.Item to="/integrations" primaryText="Integrations" />
            <Menu.Item to="/customCode" primaryText="Custom Code" />
            <Menu.Item to="/environments" primaryText="Environments" />
            <Menu.Item to="/jobs" primaryText="Jobs" />
            <Menu.Item to="/logs" primaryText="Logs" />
            <Menu.Item to="/monitoring" primaryText="Monitoring" />
            <Menu.Item to="/userEngagement" primaryText="User Engagement" />
        </Menu>

        {/* Settings */}
        <Menu>
            <Menu.Item to="/companySettings" primaryText="Company Settings" />
            <Menu.Item to="/multiTenancy" primaryText="Multi-tenancy" />
            <Menu.Item to="/privacyCenter" primaryText="Privacy Center" />
            <Menu.Item to="/identity" primaryText="Identity" />
            <Menu.Item to="/security" primaryText="Security" />
        </Menu>

        {/* Cases and Processes Management */}
        <Menu.Item to="/eventType" primaryText="Event Type" />
        <Menu.Item to="/listenerType" primaryText="Listener Type" />
        <Menu.Item to="/caseDefList" primaryText="Cases Definitions" />
        <Menu.Item to="/processEngines" primaryText="Processes Engines" />
        <Menu.Item to="/processDefList" primaryText="Processes Definitions" />
        <Menu.Item to="/taskDefList" primaryText="Tasks Definitions" />
        <Menu.Item to="/export" primaryText="Export" />

        {/* User Menu */}
        <Menu>
            <Menu.Item to="/caseList" primaryText="Cases" />
            <Menu.Item to="/tasklist" primaryText="Tasks" />
        </Menu>
    </React.Fragment>
);