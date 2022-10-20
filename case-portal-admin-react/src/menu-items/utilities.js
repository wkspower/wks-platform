// assets
import {
    IconPackgeExport,
    IconForms,
    IconChecklist,
    IconEngine,
    IconDatabase,
    IconEar,
    IconMessageCode,
    IconPencil,
    IconUsers,
    IconSpy,
    IconLock,
    IconIdBadge2,
    IconBoxMultiple,
    IconBriefcase,
    IconWorld,
    IconHeartRateMonitor,
    IconFileSearch,
    IconClockHour9,
    IconCloudDataConnection,
    IconMail,
    IconWebhook,
    IconBroadcast,
    IconNotification,
    IconPalette,
    IconAdjustments,
    IconBuilding,
    IconSettingsAutomation
} from '@tabler/icons';

// icons
const icons = {
    IconPackgeExport,
    IconForms,
    IconChecklist,
    IconEngine,
    IconDatabase,
    IconEar,
    IconMessageCode,
    IconPencil,
    IconUsers,
    IconSpy,
    IconLock,
    IconIdBadge2,
    IconBoxMultiple,
    IconBriefcase,
    IconWorld,
    IconHeartRateMonitor,
    IconFileSearch,
    IconClockHour9,
    IconCloudDataConnection,
    IconMail,
    IconWebhook,
    IconBroadcast,
    IconNotification,
    IconPalette,
    IconAdjustments,
    IconBuilding,
    IconSettingsAutomation
};

// ==============================|| MENU ITEMS - UTILITIES ||============================== //

const utilities = {
    id: 'utilities',
    title: '',
    type: 'group',
    children: [
        {
            id: 'system',
            title: 'System',
            type: 'collapse',
            icon: icons.IconBuilding,
            children: [
                {
                    id: 'look-and-feel',
                    title: 'Look And Feel',
                    type: 'item',
                    url: '/system/look-and-feel',
                    icon: icons.IconPalette,
                    breadcrumbs: true
                },
                {
                    id: 'email',
                    title: 'E-mail',
                    type: 'item',
                    url: '/system/email',
                    icon: icons.IconMail,
                    breadcrumbs: true
                },
                {
                    id: 'notifications',
                    title: 'Notifications',
                    type: 'item',
                    url: '/system/notification',
                    icon: icons.IconNotification,
                    breadcrumbs: true
                },
                {
                    id: 'integration',
                    title: 'Integrations',
                    type: 'item',
                    url: '/system/integration',
                    icon: icons.IconBroadcast,
                    breadcrumbs: true
                },
                {
                    id: 'webhook',
                    title: 'Web Hooks',
                    type: 'item',
                    url: '/system/webhook',
                    icon: icons.IconWebhook,
                    breadcrumbs: true
                },
                {
                    id: 'environments',
                    title: 'Environments',
                    type: 'item',
                    url: '/system/environment',
                    icon: icons.IconCloudDataConnection,
                    breadcrumbs: true
                },
                {
                    id: 'jobs',
                    title: 'Jobs',
                    type: 'item',
                    url: '/system/job',
                    icon: icons.IconClockHour9,
                    breadcrumbs: true
                },
                {
                    id: 'logs',
                    title: 'Logs',
                    type: 'item',
                    url: '/system/log',
                    icon: icons.IconFileSearch,
                    breadcrumbs: true
                },
                {
                    id: 'monitoring',
                    title: 'Monitoring',
                    type: 'item',
                    url: '/system/monitoring',
                    icon: icons.IconHeartRateMonitor,
                    breadcrumbs: true
                },
                {
                    id: 'languages',
                    title: 'Languages',
                    type: 'item',
                    url: '/system/languages',
                    icon: icons.IconWorld,
                    breadcrumbs: true
                }
            ]
        },
        {
            id: 'settings',
            title: 'Settings',
            type: 'collapse',
            icon: icons.IconAdjustments,
            children: [
                {
                    id: 'companySettings',
                    title: 'Company Settings',
                    type: 'item',
                    url: '/settings/company-settings',
                    icon: icons.IconBriefcase,
                    breadcrumbs: true
                },
                {
                    id: 'multiTenancy',
                    title: 'Multi Tenancy',
                    type: 'item',
                    url: '/settings/multi-tenancy',
                    icon: icons.IconBoxMultiple,
                    breadcrumbs: true
                },
                {
                    id: 'identity',
                    title: 'Identity',
                    type: 'item',
                    url: '/settings/identity',
                    breadcrumbs: true,
                    icon: icons.IconIdBadge2
                },
                {
                    id: 'security',
                    title: 'Security',
                    type: 'item',
                    url: '/settings/security',
                    breadcrumbs: true,
                    icon: icons.IconLock
                },
                {
                    id: 'privacySettings',
                    title: 'Privacy Settings',
                    type: 'item',
                    url: '/settings/privacy-settings',
                    breadcrumbs: true,
                    icon: icons.IconSpy
                },
                {
                    id: 'userEngagement',
                    title: 'User Engagement',
                    type: 'item',
                    url: '/settings/user-engagement',
                    breadcrumbs: true,
                    icon: icons.IconUsers
                }
            ]
        },
        {
            id: 'casesAndProcessesManagement',
            title: 'Case Life Cycle',
            type: 'collapse',
            icon: icons.IconSettingsAutomation,
            children: [
                {
                    id: 'case-definition',
                    title: 'Cases Definitions',
                    type: 'item',
                    url: '/case-life-cycle/case-definition',
                    breadcrumbs: true,
                    icon: icons.IconPencil
                },
                {
                    id: 'record-type',
                    title: 'Records Types',
                    type: 'item',
                    url: '/case-life-cycle/record-type',
                    breadcrumbs: true,
                    icon: icons.IconDatabase
                },
                {
                    id: 'process-engine',
                    title: 'Process Engines',
                    type: 'item',
                    url: '/case-life-cycle/process-engine',
                    breadcrumbs: true,
                    icon: icons.IconEngine
                },
                {
                    id: 'task-definition',
                    title: 'Task Definition',
                    type: 'item',
                    url: '/case-life-cycle/task-definition',
                    breadcrumbs: true,
                    icon: icons.IconChecklist
                },
                {
                    id: 'form',
                    title: 'Forms',
                    type: 'item',
                    url: '/case-life-cycle/form',
                    breadcrumbs: true,
                    icon: icons.IconForms
                },
                {
                    id: 'export',
                    title: 'Export',
                    type: 'item',
                    url: '/case-life-cycle/export',
                    breadcrumbs: true,
                    icon: icons.IconPackgeExport
                }
            ]
        }
    ]
};

export default utilities;
