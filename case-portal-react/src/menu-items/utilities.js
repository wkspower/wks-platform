import { FolderOutlined } from '@ant-design/icons';
import { IconArchive, IconFileCheck, IconFileInvoice, IconList, IconSquareAsterisk } from '@tabler/icons';

const icons = { FolderOutlined, IconFileInvoice, IconFileCheck, IconArchive, IconSquareAsterisk, IconList };

// ==============================|| UTILITIES MENU ITEMS ||============================== //

const utilities = {
    id: 'utilities',
    title: '',
    type: 'group',
    children: [
        {
            id: 'case-list',
            title: 'Cases',
            type: 'collapse',
            icon: icons.FolderOutlined,
            children: [
                {
                    id: 'wip-cases',
                    title: 'Work In Progress',
                    type: 'item',
                    url: '/case-list/wip-cases',
                    breadcrumbs: true,
                    icon: icons.IconFileInvoice
                },
                {
                    id: 'cases',
                    title: 'All',
                    type: 'item',
                    url: '/case-list/cases',
                    breadcrumbs: true,
                    icon: icons.IconSquareAsterisk
                },
                {
                    id: 'close-cases',
                    title: 'Closed',
                    type: 'item',
                    url: '/case-list/closed-cases',
                    breadcrumbs: true,
                    icon: icons.IconFileCheck
                },
                {
                    id: '   archived-cases',
                    title: 'Archived',
                    type: 'item',
                    url: '/case-list/archived-cases',
                    breadcrumbs: true,
                    icon: icons.IconArchive
                }
            ]
        },
        {
            id: 'task-list',
            title: 'Tasks',
            type: 'item',
            url: '/task-list',
            icon: icons.IconList,
            breadcrumbs: true
        }
    ]
};

export default utilities;
