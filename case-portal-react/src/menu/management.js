import i18n from '../i18n';

// assets
import {
    IconPackgeExport,
    IconForms,
    IconChecklist,
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

const management = {
    id: 'management',
    title: 'Management',
    caption: 'Management',
    type: 'group',
    children: [
        {
            id: 'system',
            title: i18n.t('menu.system'),
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
                }
            ]
        },
        {
            id: 'casesAndProcessesManagement',
            title: i18n.t('menu.caselifecicle'),
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
                    id: 'form',
                    title: 'Forms',
                    type: 'item',
                    url: '/case-life-cycle/form',
                    breadcrumbs: true,
                    icon: icons.IconForms
                }
            ]
        }
    ]
};

export default management;
