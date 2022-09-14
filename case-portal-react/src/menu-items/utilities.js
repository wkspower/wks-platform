// assets

// constant
// const icons = {
//     IconTypography,
//     IconPalette,
//     IconShadow,
//     IconWindmill
// };

// ==============================|| UTILITIES MENU ITEMS ||============================== //

const utilities = {
    id: 'utilities',
    title: 'Workspace',
    type: 'group',
    children: [
        {
            id: 'case-list',
            title: 'Cases',
            type: 'collapse',
            // icon: icons.IconWindmill,
            children: [
                {
                    id: 'wip-cases',
                    title: 'Work In Progress',
                    type: 'item',
                    url: '/case-list/wip-cases',
                    breadcrumbs: false
                },
                {
                    id: 'cases',
                    title: 'All',
                    type: 'item',
                    url: '/case-list/cases',
                    breadcrumbs: false
                },
                {
                    id: 'close-cases',
                    title: 'Closed',
                    type: 'item',
                    url: '/case-list/closed-cases',
                    breadcrumbs: false
                },
                {
                    id: '   archived-cases',
                    title: 'Archived',
                    type: 'item',
                    url: '/case-list/archived-cases',
                    breadcrumbs: false
                }
            ]
        },
        {
            id: 'task-list',
            title: 'Tasks',
            type: 'item',
            url: '/task-list',
            // icon: icons.IconTypography,
            breadcrumbs: false
        }

        // {
        //     id: 'util-typography',
        //     title: 'Typography',
        //     type: 'item',
        //     url: '/utils/util-typography',
        //     icon: icons.IconTypography,
        //     breadcrumbs: false
        // },
        // {
        //     id: 'util-color',
        //     title: 'Color',
        //     type: 'item',
        //     url: '/utils/util-color',
        //     icon: icons.IconPalette,
        //     breadcrumbs: false
        // },
        // {
        //     id: 'util-shadow',
        //     title: 'Shadow',
        //     type: 'item',
        //     url: '/utils/util-shadow',
        //     icon: icons.IconShadow,
        //     breadcrumbs: false
        // },
        // {
        //     id: 'icons',
        //     title: 'Icons',
        //     type: 'collapse',
        //     icon: icons.IconWindmill,
        //     children: [
        //         {
        //             id: 'tabler-icons',
        //             title: 'Tabler Icons',
        //             type: 'item',
        //             url: '/icons/tabler-icons',
        //             breadcrumbs: false
        //         },
        //         {
        //             id: 'material-icons',
        //             title: 'Material Icons',
        //             type: 'item',
        //             url: '/icons/material-icons',
        //             breadcrumbs: false
        //         }
        //     ]
        // }
    ]
};

export default utilities;
