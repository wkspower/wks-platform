import PropTypes from 'prop-types'
import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { IconButton, Tooltip } from '@mui/material'
import InfoIcon from '@mui/icons-material/Info'
import Notification from 'components/Utilities/Notification'

import MuiBreadcrumbs from '@mui/material/Breadcrumbs'
import { Grid, Typography } from '@mui/material'
import MainCard from '../MainCard'
import { useSession } from 'SessionStoreContext'
import Config from 'consts/index'
import { useSelector } from 'react-redux'
import { useDispatch } from 'react-redux'
import { setScreenTitle } from 'store/reducers/dataGridStore'
import Chip from '@mui/material/Chip'
import Stack from '@mui/material/Stack'

const Breadcrumbs = ({ navigation, title, ...others }) => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical
  const plantName = JSON.parse(localStorage.getItem('selectedPlant'))?.name
  const siteName = JSON.parse(localStorage.getItem('selectedSite'))?.name
  const verticalId = localStorage.getItem('verticalId')
  const verticalName = JSON.parse(
    localStorage.getItem('selectedVertical'),
  )?.name
  const dispatch = useDispatch()

  // const siteName = JSON.parse(localStorage.getItem('selectedSite'))?.name;

  const [notification, setNotification] = useState({
    open: false,
    message: '',
    severity: 'info',
  })

  function getRoleName(verticalId, screenId) {
    const roleMapping = {
      '5CC84A47-9717-4142-8E66-B60EBE0CF703': {
        'product-demand': 'CTS Engineer',
        'product-mcu-val': 'Plant Manager',

        'shutdown-plan': 'Maintenance Engineer',
        'slowdown-plan': 'Maintenance Engineer',
        'maintenance-details': 'Maintenance Engineer',

        'production-norms': 'Plant Manager',
        'catalyst-selectivity': 'CTS Engineer',
        'normal-op-norms': 'Plant Manager',
        'shutdown-norms': 'Maintenance Engineer',
        'slowdown-norms': 'Maintenance Engineer',
        'consumption-norms': 'Plant Manager',
        'feed-stock': 'Plant Manager',
        workflow: 'Plant Manager',
        'aop-annual-cost-report': 'Plant Manager',
      },
      'BF5D7508-96EB-496E-BEB0-4828CB1A1B11': {
        'product-demand': 'CTS Engineer',
        'product-mcu-val': 'Plant Manager',

        'shutdown-plan': 'Maintenance Engineer',
        'slowdown-plan': 'Maintenance Engineer',
        'maintenance-details': 'Maintenance Engineer',

        'production-norms': 'Plant Manager',
        'catalyst-selectivity': 'CTS Engineer',
        'normal-op-norms': 'Plant Manager',
        'shutdown-norms': 'Maintenance Engineer',
        'slowdown-norms': 'Maintenance Engineer',
        'consumption-norms': 'Plant Manager',

        workflow: 'Plant Manager',
        'aop-annual-cost-report': 'Plant Manager',
      },
    }

    const verticalRoleMap = roleMapping[verticalId]
    return verticalRoleMap?.[screenId]
      ? `Role : ${verticalRoleMap[screenId]}`
      : ' '
  }

  async function handleOpenPdf(title) {
    const url = `${Config.StorageUrl}/storage/files/${vertName}/${siteName}/${plantName}/downloads/${title}.pdf?content-type=application/pdf`
    const headers = {
      Authorization: `Bearer ${keycloak.token}`,
    }

    try {
      const resp = await fetch(url, {
        method: 'GET',
        headers,
      })
      if (!resp.ok) {
        setNotification({
          open: true,
          message: 'Basis not found! Please try again.',
          severity: 'info',
        })
        return
      }
      const blob = await resp.blob()
      const fileURL = window.URL.createObjectURL(blob)
      window.open(fileURL, '_blank')
      return true
    } catch (e) {
      console.error('Error fetching PDF:', e)
      return Promise.reject(e)
    }
  }

  async function handleOpenPdfTemp(title) {
    const url = `/files/${title}.pdf`

    try {
      const resp = await fetch(url, {
        method: 'GET',
      })

      if (!resp.ok) {
        setNotification({
          open: true,
          message: 'Basis not found! Please try again later.',
          severity: 'info',
        })
        return
      }

      const blob = await resp.blob()
      const fileURL = window.URL.createObjectURL(blob)
      window.open(fileURL, '_blank')
      return true
    } catch (e) {
      console.error('Error fetching PDF:', e)
      return Promise.reject(e)
    }
  }

  const location = useLocation()
  const [main, setMain] = useState()
  const [item, setItem] = useState()

  useEffect(() => {
    let title = item?.title

    const verticalName = JSON.parse(
      localStorage.getItem('selectedVertical'),
    )?.name?.toLowerCase()

    // if (title === 'Business Demand') {
    //   if (verticalName === 'meg') {
    //     title = 'Business Demand (Percentage)'
    //   } else if (verticalName === 'pe') {
    //     title = 'Business Demand (Absolute)'
    //   }
    // }

    dispatch(
      setScreenTitle({
        title,
      }),
    )
  }, [item, vertName])

  // set active item state
  const getCollapse = (menu) => {
    if (menu.children) {
      menu.children.filter((collapse) => {
        if (collapse.type && collapse.type === 'collapse') {
          getCollapse(collapse)
        } else if (collapse.type && collapse.type === 'item') {
          if (location.pathname === collapse.url) {
            setMain(menu)
            setItem(collapse)
            // console.log('collapse', collapse)
            var title = collapse?.title
          }
        }
        return false
      })
    }
  }

  useEffect(() => {
    navigation?.items?.map((menu) => {
      if (menu.type && menu.type === 'group') {
        getCollapse(menu)
      }
      return false
    })
  })

  // only used for component demo breadcrumbs
  if (location.pathname === '/breadcrumbs') {
    location.pathname = '/dashboard/analytics'
  }

  let mainContent
  let itemContent
  let breadcrumbContent = <Typography />
  let itemTitle = ''

  // collapse item
  if (main && main.type === 'collapse') {
    mainContent = (
      // <Typography component={Link} to={document.location.pathname} variant="h6" sx={{ textDecoration: 'none' }} color="textSecondary">
      <Typography
        variant='h6'
        sx={{ textDecoration: 'none' }}
        color='textSecondary'
      >
        {main.title}
      </Typography>
    )
  }

  // items
  if (item && item.type === 'item') {
    itemTitle = item.title
    var title1 = itemTitle
    if (
      title1 === 'Business Demand' &&
      verticalChange?.selectedVertical?.toLowerCase() === 'meg'
    ) {
      title1 = 'Business Demand (Percentage)'
    }

    if (
      title1 === 'Business Demand' &&
      verticalChange?.selectedVertical?.toLowerCase() === 'pe'
    ) {
      title1 = 'Business Demand (Absolute)'
    }

    itemContent = (
      <Typography
        variant='subtitle1'
        color='textPrimary'
        display='flex'
        alignItems='center'
      >
        {/* HIDE THE TITLE NAME  */}
        {/* {title1} */}
        <Tooltip title={`Basis for ${itemTitle}`}>
          <IconButton
            size='medium'
            sx={{
              ml: 1,
              backgroundColor: 'transparent',
              '&:hover': {
                backgroundColor: 'rgba(0, 0, 0, 0.1)',
              },
              padding: '6px',
            }}
            onClick={() => handleOpenPdf(item?.id)}
          >
            <InfoIcon fontSize='medium' sx={{ color: '#0100cb' }} />
          </IconButton>
        </Tooltip>
      </Typography>
    )

    // main
    if (item.breadcrumbs !== false) {
      breadcrumbContent = (
        <MainCard
          border={false}
          sx={{ mb: 3, bgcolor: 'transparent' }}
          {...others}
          content={false}
        >
          <Grid
            container
            direction='column'
            justifyContent='flex-start'
            alignItems='flex-start'
            spacing={1}
            sx={{ marginTop: '-18px' }}
          >
            {/* <Grid item sx={{ ml: 1.5, display: none }}> */}
            {/* <MuiBreadcrumbs aria-label='breadcrumb'> */}
            {/* HIDE HOME OPTION FROM Navigators MENU */}
            {/* <Typography
                  component={Link}
                  to='/home'
                  color='textSecondary'
                  variant='h6'
                  sx={{ textDecoration: 'none' }}
                >
                  Home
                </Typography> */}

            {/* {mainContent} */}

            {/* <Typography
                  component='div'
                  sx={{
                    textDecoration: 'none',
                    fontWeight: 800,
                    color: 'black',
                    // fontStyle: 'italic',
                    fontSize: '1rem',
                  }}
                >
                  {verticalName} / {siteName} / {plantName}
                </Typography>
                {itemContent}
              </MuiBreadcrumbs>
            </Grid> */}

            <Grid
              container
              sx={{ ml: 1.5 }}
              justifyContent='space-between'
              alignItems='center'
            >
              <Grid item>
                <Typography
                  component='div'
                  sx={{
                    textDecoration: 'none',
                    fontWeight: 800,
                    color: 'black',
                    fontSize: '0.8rem',
                    display: 'flex',
                    alignItems: 'center',
                  }}
                >
                  {verticalName} / {siteName} / {plantName} |{' '}
                  {getRoleName(verticalId, item?.id)}
                  {itemContent}
                </Typography>
              </Grid>

              <Stack spacing={0.5} sx={{ alignItems: 'center' }}>
                <Grid item>
                  <Chip
                    color='primary'
                    variant='outlined'
                    // label={getRoleName(verticalId, item?.id)}
                    className='role-name'
                    sx={{ border: 'none' }} // Remove the border
                  />
                </Grid>
              </Stack>
            </Grid>

            {/* HIDE THE TITLE NAME */}
            {title && (
              <Grid item sx={{ mt: 0.5 }}>
                <Typography variant='h5'>{item.title}</Typography>
              </Grid>
            )}
          </Grid>
          {/* Notification Component */}
          <Notification
            open={notification.open}
            message={notification.message}
            severity={notification.severity}
            onClose={() => setNotification({ ...notification, open: false })}
          />
        </MainCard>
      )
    }
  }

  return breadcrumbContent
}

Breadcrumbs.propTypes = {
  navigation: PropTypes.object,
  title: PropTypes.bool,
}

export default Breadcrumbs
