// assets
import { ChromeOutlined, QuestionOutlined } from '@ant-design/icons';
import i18n from '../i18n';

// icons
const icons = {
    ChromeOutlined,
    QuestionOutlined
};

const support = {
    id: 'support',
    title: '',
    type: 'group',
    children: [
        {
            id: 'documentation',
            title: i18n.t('menu.documentation'),
            type: 'item',
            url: '#',
            icon: icons.QuestionOutlined,
            external: true,
            target: true
        }
    ]
};

export default support;
