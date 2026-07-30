import { Fragment } from 'react'
import { useTranslation } from 'react-i18next'
import {
  ListItemButton,
  ListItemIcon,
  ListItemText,
  ListSubheader,
} from '@mui/material'
import CheckOutlined from '@ant-design/icons/CheckOutlined'
import GlobalOutlined from '@ant-design/icons/GlobalOutlined'

import { setLocale } from '../../../../../i18n'
import { SUPPORTED_LOCALES } from '../../../../../i18n/locales'

/**
 * Language picker for the profile popper.
 *
 * Renders list items directly rather than wrapping them in its own <List>: it is
 * mounted inside ProfileTab's list, so a nested list would emit a <ul> inside a
 * <ul>. Sharing the parent list also means it inherits the popper's icon styling.
 *
 * The language names are autonyms and stay untranslated on purpose — someone
 * looking for their own language should find it written the way they write it,
 * whatever the UI is currently showing.
 */
const LanguageTab = () => {
  const { t, i18n } = useTranslation()

  return (
    <Fragment>
      <ListSubheader
        disableSticky
        sx={{ bgcolor: 'transparent', lineHeight: '32px' }}
      >
        {t('menu.language')}
      </ListSubheader>
      {SUPPORTED_LOCALES.map((locale) => {
        const selected = i18n.language === locale.code

        return (
          <ListItemButton
            key={locale.code}
            selected={selected}
            onClick={() => setLocale(locale.code)}
          >
            <ListItemIcon>
              {selected ? <CheckOutlined /> : <GlobalOutlined />}
            </ListItemIcon>
            <ListItemText primary={locale.label} />
          </ListItemButton>
        )
      })}
    </Fragment>
  )
}

export default LanguageTab
