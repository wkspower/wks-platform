import { Grid, Typography } from '@mui/material'
import DashboardCard from 'components/cards/DashboardCard'
import { IconArchive, IconList, IconSquareAsterisk } from '@tabler/icons-react'
import { useTranslation } from 'react-i18next'

const DashboardDefault = () => {
  const { t } = useTranslation()

  return (
    <Grid container rowSpacing={4.5} columnSpacing={2.75}>
      {/* row 1 */}
      <Grid sx={{ mb: -2.25 }} size={12}>
        <Typography variant='h5'>{t('pages.dashboard.title')}</Typography>
      </Grid>
      <Grid
        size={{
          xs: 12,
          sm: 6,
          md: 4,
          lg: 3,
        }}
      >
        <DashboardCard
          title={t('pages.dashboard.cards.wipcases.label')}
          icon={<IconArchive />}
          to='/case-list/wip-cases'
        />
      </Grid>
      <Grid
        size={{
          xs: 12,
          sm: 6,
          md: 4,
          lg: 3,
        }}
      >
        <DashboardCard
          title={t('pages.dashboard.cards.caselist.label')}
          icon={<IconSquareAsterisk />}
          to='/case-list/cases'
        />
      </Grid>
      <Grid
        size={{
          xs: 12,
          sm: 6,
          md: 4,
          lg: 3,
        }}
      >
        <DashboardCard
          title={t('pages.dashboard.cards.tasklist.label')}
          icon={<IconList />}
          to='/task-list'
        />
      </Grid>
      <Grid
        sx={{ display: { sm: 'none', md: 'block', lg: 'none' } }}
        size={{
          md: 8,
        }}
      />
    </Grid>
  )
}

export default DashboardDefault
