import HomeOutlined from '@ant-design/icons/HomeOutlined'
import i18n from '../i18n'

const icons = { HomeOutlined }

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
      breadcrumbs: false,
    },
  ],
}

export default dashboard
