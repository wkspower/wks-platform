import { FolderOutlined } from '@ant-design/icons';
import { IconArchive, IconFileCheck, IconFileInvoice, IconList, IconSquareAsterisk, IconDatabase } from '@tabler/icons';

const icons = { FolderOutlined, IconFileInvoice, IconFileCheck, IconArchive, IconSquareAsterisk, IconList, IconDatabase };

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
            children: []
        },
        {
            id: 'task-list',
            title: 'Tasks',
            type: 'item',
            url: '/task-list',
            icon: icons.IconList,
            breadcrumbs: true
        },
        {
            id: 'record-list',
            title: 'Records',
            type: 'collapse',
            icon: icons.IconDatabase,
            children: []
        }
    ]
};

fetch(process.env.REACT_APP_API_URL + '/case-definition/')
    .then((response) => response.json())
    .then((data) => {
        data.forEach((element) => {
            utilities.children
                .filter((menu) => menu.id === 'case-list')[0]
                .children.push({
                    id: element.id,
                    title: element.name,
                    type: 'item',
                    url: '/case-list/' + element.id,
                    breadcrumbs: true
                });
        });
    })
    .catch((err) => {
        console.log(err.message);
    });

fetch(process.env.REACT_APP_API_URL + '/record-type/')
    .then((response) => response.json())
    .then((data) => {
        data.forEach((element) => {
            utilities.children
                .filter((menu) => menu.id === 'record-list')[0]
                .children.push({
                    id: element.id,
                    title: element.id,
                    type: 'item',
                    url: '/record-list/' + element.id,
                    breadcrumbs: true
                });
        });
    })
    .catch((err) => {
        console.log(err.message);
    });

export default utilities;
