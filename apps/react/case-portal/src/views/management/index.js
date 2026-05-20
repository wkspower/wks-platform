import { Grid, Typography } from '@mui/material'
import DashboardCard from 'components/cards/DashboardCard'
import { IconForms, IconPalette, IconPencil } from '@tabler/icons-react'

const DashboardDefault = () => {
  return (
    <Grid container rowSpacing={4.5} columnSpacing={2.75}>
      <Grid sx={{ mb: -2.25 }} size={12}>
        <Typography variant='h5'>Case Life Cycle</Typography>
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
          title='Case Definitions'
          subtitle={'Create and edit Case Definitions'}
          icon={<IconPencil />}
          to='/case-life-cycle/case-definition'
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
          title='Record Types'
          subtitle={'Create and edit Record types'}
          icon={<IconForms />}
          to='/case-life-cycle/record-type'
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
          title='Forms'
          subtitle={'Create and edit Cases and Tasks Forms'}
          icon={<IconForms />}
          to='/case-life-cycle/form'
        />
      </Grid>
      <Grid
        sx={{ display: { sm: 'none', md: 'block', lg: 'none' } }}
        size={{
          md: 8,
        }}
      />
      <Grid sx={{ mb: -2.25 }} size={12}>
        <Typography variant='h5'>System</Typography>
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
          title='Look and Feel'
          subtitle={'Customize the Platform Appearance'}
          icon={<IconPalette />}
          to='/system/look-and-feel'
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
