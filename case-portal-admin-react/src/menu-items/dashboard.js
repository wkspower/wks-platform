// assets
import { HomeOutlined } from '@ant-design/icons';

// icons
const icons = {
    HomeOutlined
};

// ==============================|| MENU ITEMS - DASHBOARD ||============================== //

const dashboard = {
    id: 'group-dashboard',
    title: '',
    type: 'group',
    children: [
        {
            id: 'dashboard',
            title: 'Home',
            type: 'item',
            url: '/home',
            icon: icons.HomeOutlined,
            breadcrumbs: false
        }
    ]
};

export default dashboard;
