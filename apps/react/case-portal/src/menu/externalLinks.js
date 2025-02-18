import i18n from '../i18n'
import { IconExternalLink } from '@tabler/icons-react'
import menuItens from 'consts/customMenuItems'

// icons
const icons = {
  IconExternalLink,
}

const links =
  menuItens
    .filter((item) => !!item.url)
    .filter((item) => !!item.title)
    .map((item) => {
      return {
        id: 'link',
        title: item.title,
        type: 'item',
        url: item.url,
        icon: icons.IconExternalLink,
        breadcrumbs: true,
        target: '_blank',
      }
    }) ?? []

const external = {
  id: 'externallinks',
  title: i18n.t('menu.externalLinks'),
  caption: i18n.t('menu.externalLinks'),
  type: 'group',
  external: true,
  children: [
    {
      id: 'externallinks',
      title: 'Links',
      type: 'collapse',
      icon: icons.IconBuilding,
      children: links,
      external: true,
    },
  ],
}

export default external
