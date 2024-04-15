import { Grid, Typography } from '@mui/material';
import DashboardCard from 'components/cards/DashboardCard';
import { IconArchive, IconList, IconSquareAsterisk } from '@tabler/icons-react';
import { useTranslation } from 'react-i18next';

const DashboardDefault = () => {
  const { t } = useTranslation();

  return (
    <Grid container rowSpacing={4.5} columnSpacing={2.75}>
      {/* row 1 */}
      <Grid item xs={12} sx={{ mb: -2.25 }}>
        <Typography variant='h5'>{t('pages.dashboard.title')}</Typography>
      </Grid>
      <Grid item xs={12} sm={6} md={4} lg={3}>
        <DashboardCard
          title={t('pages.dashboard.cards.wipcases.label')}
          icon={<IconArchive />}
          to='/case-list/wip-cases'
        />
      </Grid>
      <Grid item xs={12} sm={6} md={4} lg={3}>
        <DashboardCard
          title={t('pages.dashboard.cards.caselist.label')}
          icon={<IconSquareAsterisk />}
          to='/case-list/cases'
        />
      </Grid>
      <Grid item xs={12} sm={6} md={4} lg={3}>
        <DashboardCard
          title={t('pages.dashboard.cards.tasklist.label')}
          icon={<IconList />}
          to='/task-list'
        />
      </Grid>

      <Grid
        item
        md={8}
        sx={{ display: { sm: 'none', md: 'block', lg: 'none' } }}
      />
    </Grid>
  );
};

export default DashboardDefault;
