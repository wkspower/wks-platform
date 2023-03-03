// assets
import { HomeOutlined } from '@ant-design/icons';
import i18n from '../i18n';

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
            title: i18n.t('menu.home'),
            type: 'item',
            url: '/home',
            icon: icons.HomeOutlined,
            breadcrumbs: false
        }
    ]
};

export default dashboard;
