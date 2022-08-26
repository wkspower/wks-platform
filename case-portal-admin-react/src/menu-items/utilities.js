// assets
import { IconTypography, IconPalette, IconShadow, IconWindmill } from '@tabler/icons';

// constant
const icons = {
    IconTypography,
    IconPalette,
    IconShadow,
    IconWindmill
};

// ==============================|| UTILITIES MENU ITEMS ||============================== //

const utilities = {
    id: 'utilities',
    title: 'Utilities',
    type: 'group',
    children: [
        {
            id: 'system',
            title: 'System',
            type: 'collapse',
            icon: icons.IconWindmill,
            children: [
                {
                    id: 'look-and-feel',
                    title: 'Look And Feel',
                    type: 'item',
                    url: '/system/look-and-feel',
                    breadcrumbs: false
                },
                {
                    id: 'email',
                    title: 'E-mail',
                    type: 'item',
                    url: '/system/email',
                    breadcrumbs: false
                },
                {
                    id: 'notifications',
                    title: 'Notifications',
                    type: 'item',
                    url: '/system/notification',
                    breadcrumbs: false
                },
                {
                    id: 'integrations',
                    title: 'Integrations',
                    type: 'item',
                    url: '/system/integration',
                    breadcrumbs: false
                },
                {
                    id: 'webhook',
                    title: 'Web Hooks',
                    type: 'item',
                    url: '/system/webhook',
                    breadcrumbs: false
                },
                {
                    id: 'environments',
                    title: 'Environments',
                    type: 'item',
                    url: '/system/environment',
                    breadcrumbs: false
                },
                {
                    id: 'jobs',
                    title: 'Jobs',
                    type: 'item',
                    url: '/system/job',
                    breadcrumbs: false
                },
                {
                    id: 'logs',
                    title: 'Logs',
                    type: 'item',
                    url: '/system/log',
                    breadcrumbs: false
                },
                {
                    id: 'monitoring',
                    title: 'Monitoring',
                    type: 'item',
                    url: '/system/monitoring',
                    breadcrumbs: false
                }
            ]
        },
        {
            id: 'settings',
            title: 'Settings',
            type: 'collapse',
            icon: icons.IconShadow,
            children: [
                {
                    id: 'companySettings',
                    title: 'Company Settings',
                    type: 'item',
                    url: '/settings/company-settings',
                    breadcrumbs: false
                },
                {
                    id: 'multiTenancy',
                    title: 'Multi Tenancy',
                    type: 'item',
                    url: '/settings/multi-tenancy',
                    breadcrumbs: false
                },
                {
                    id: 'indentity',
                    title: 'Identity',
                    type: 'item',
                    url: '/settings/indentity',
                    breadcrumbs: false
                },
                {
                    id: 'security',
                    title: 'Security',
                    type: 'item',
                    url: '/settings/security',
                    breadcrumbs: false
                },
                {
                    id: 'privacySettings',
                    title: 'Privacy Settings',
                    type: 'item',
                    url: '/settings/privacy-settings',
                    breadcrumbs: false
                },
                {
                    id: 'userEngagement',
                    title: 'User Engagement',
                    type: 'item',
                    url: '/settings/user-engagement',
                    breadcrumbs: false
                }
            ]
        },
        {
            id: 'casesAndProcessesManagement',
            title: 'Cases and Processes Management',
            type: 'collapse',
            icon: icons.IconShadow,
            children: [
                {
                    id: 'case-definition',
                    title: 'Cases Definitions',
                    type: 'item',
                    url: '/case-definition',
                    breadcrumbs: false
                },
                {
                    id: 'event-type-definition',
                    title: 'Event Types',
                    type: 'item',
                    url: '/event-type-definition',
                    breadcrumbs: false
                },
                {
                    id: 'listener-type-definition',
                    title: 'Listener Type',
                    type: 'item',
                    url: '/listener-type-definition',
                    breadcrumbs: false
                },
                {
                    id: 'data-domain',
                    title: 'Data Domain',
                    type: 'item',
                    url: '/data-domain',
                    breadcrumbs: false
                },
                {
                    id: 'process-engine',
                    title: 'Processes Engines',
                    type: 'item',
                    url: '/process-engine',
                    breadcrumbs: false
                },
                {
                    id: 'task-definition',
                    title: 'Task Definition',
                    type: 'item',
                    url: '/task-definition',
                    breadcrumbs: false
                },
                {
                    id: 'export',
                    title: 'Export',
                    type: 'item',
                    url: '/event-type-definition',
                    breadcrumbs: false
                }
            ]
        }
    ]
};

export default utilities;
