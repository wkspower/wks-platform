import FolderOutlined from '@ant-design/icons/FolderOutlined';

import {
  IconArchive,
  IconFileCheck,
  IconFileInvoice,
  IconList,
  IconSquareAsterisk,
  IconDatabase,
} from '@tabler/icons-react';

import i18n from '../i18n';

const icons = {
  FolderOutlined,
  IconFileInvoice,
  IconFileCheck,
  IconArchive,
  IconSquareAsterisk,
  IconList,
  IconDatabase,
};

const workspace = {
  id: 'utilities',
  title: '',
  type: 'group',
  children: [
    {
      id: 'case-list',
      title: i18n.t('menu.case'),
      type: 'collapse',
      icon: icons.FolderOutlined,
      children: [],
    },
    {
      id: 'task-list',
      title: i18n.t('menu.task'),
      type: 'item',
      url: '/task-list',
      icon: icons.IconList,
      breadcrumbs: true,
    },
    {
      id: 'record-list',
      title: i18n.t('menu.record'),
      type: 'collapse',
      icon: icons.IconDatabase,
      children: [],
    },
  ],
};

export default workspace;
