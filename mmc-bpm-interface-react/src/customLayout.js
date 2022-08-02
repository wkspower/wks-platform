import { Layout } from 'react-admin';

import { CustomMenu } from './customMenu';

export const CustomLayout = props => <Layout {...props} menu={CustomMenu} />;