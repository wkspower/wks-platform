// assets
import { HomeOutlined } from '@ant-design/icons';
import i18n from '../i18n';

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
            title: i18n.t('menu.home'),
            type: 'item',
            url: '/home',
            icon: icons.HomeOutlined,
            breadcrumbs: false
        }
    ]
};

export default dashboard;
