// assets
import { HomeOutlined } from '@ant-design/icons';

// constant
const icons = { HomeOutlined };

// ==============================|| DASHBOARD MENU ITEMS ||============================== //

const dashboard = {
    id: 'dashboard',
    title: '',
    type: 'group',
    children: [
        {
            id: 'default',
            title: 'Home',
            type: 'item',
            url: '/home',
            icon: icons.HomeOutlined,
            breadcrumbs: false
        }
    ]
};

export default dashboard;
