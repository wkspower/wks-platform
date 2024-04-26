import { Grid, Typography } from '@mui/material'
import DashboardCard from 'components/cards/DashboardCard'
import { IconForms, IconPalette, IconPencil } from '@tabler/icons-react'

const DashboardDefault = () => {
  return (
    <Grid container rowSpacing={4.5} columnSpacing={2.75}>
      <Grid item xs={12} sx={{ mb: -2.25 }}>
        <Typography variant='h5'>Case Life Cycle</Typography>
      </Grid>

      <Grid item xs={12} sm={6} md={4} lg={3}>
        <DashboardCard
          title='Case Definitions'
          subtitle={'Create and edit Case Definitions'}
          icon={<IconPencil />}
          to='/case-life-cycle/case-definition'
        />
      </Grid>
      <Grid item xs={12} sm={6} md={4} lg={3}>
        <DashboardCard
          title='Record Types'
          subtitle={'Create and edit Record types'}
          icon={<IconForms />}
          to='/case-life-cycle/record-type'
        />
      </Grid>

      <Grid item xs={12} sm={6} md={4} lg={3}>
        <DashboardCard
          title='Forms'
          subtitle={'Create and edit Cases and Tasks Forms'}
          icon={<IconForms />}
          to='/case-life-cycle/form'
        />
      </Grid>

      <Grid
        item
        md={8}
        sx={{ display: { sm: 'none', md: 'block', lg: 'none' } }}
      />

      <Grid item xs={12} sx={{ mb: -2.25 }}>
        <Typography variant='h5'>System</Typography>
      </Grid>
      <Grid item xs={12} sm={6} md={4} lg={3}>
        <DashboardCard
          title='Look and Feel'
          subtitle={'Customize the Platform Appearance'}
          icon={<IconPalette />}
          to='/system/look-and-feel'
        />
      </Grid>

      <Grid
        item
        md={8}
        sx={{ display: { sm: 'none', md: 'block', lg: 'none' } }}
      />
    </Grid>
  )
}

export default DashboardDefault
