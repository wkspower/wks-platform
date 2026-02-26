# case-portal-admin-react

Describe global configs about this project.

## Configure i18n

Add new entry on <b>i18n/pt_br.js</b> and <b>i18n/en_us.js</b>

## Using i18n

```js
...
import { useTranslation } from 'react-i18next';

const ExamplePage = () => {
    const { t } = useTranslation();

    return <div>{t('pages.dashboard.title')}</div>;
};
...
```

## Customize menu items with external links

```bash
#edit

$ code src/consts/customMenuItems.js
```

```js
...
  const menuItems = [
    {
        title: 'site 1',
        url: 'https://www.demo1.com',
    },
    {
        title: 'site 2',
        url: 'https://www.demo2.com',
    }
];
...
```

## References

[React internationalization (i18n)](https://www.i18next.com)
