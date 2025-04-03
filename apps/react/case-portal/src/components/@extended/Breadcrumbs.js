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

const Breadcrumbs = ({ navigation, title, ...others }) => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange } = dataGridStore
  const vertName = verticalChange?.selectedVertical

  const [notification, setNotification] = useState({
    open: false,
    message: '',
    severity: 'info',
  })

  // http://localhost:8085/storage/files1/newFile/downloads/sample.pdf?content-type=application/pdf

  // const handleOpenPdf = async (title) => {
  //   const fileName = `${title}.pdf`
  //   const fileUrl = `/files/${fileName}`

  //   try {
  //     const response = await fetch(fileUrl, { method: 'HEAD' })
  //     if (response.ok) {
  //       window.open(fileUrl, '_blank')
  //     } else {
  //       setNotification({
  //         open: true,
  //         message: 'Basis not found!',
  //         severity: 'error',
  //       })
  //     }
  //   } catch (error) {
  //     setNotification({
  //       open: true,
  //       message: 'Error checking file. Please try again.',
  //       severity: 'error',
  //     })
  //   }
  // }

  async function handleOpenPdf(title) {
    const url = `${Config.StorageUrl}/storage/files1/newFile/downloads/${title}_${vertName}.pdf?content-type=application/pdf`
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
          severity: 'error',
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
    itemContent = (
      <Typography
        variant='subtitle1'
        color='textPrimary'
        display='flex'
        alignItems='center'
      >
        {itemTitle}
        <Tooltip title={`Basis for ${itemTitle}`}>
          <IconButton
            size='medium'
            sx={{
              ml: 1,
              backgroundColor: 'transparent', // Transparent background
              '&:hover': {
                backgroundColor: 'rgba(0, 0, 0, 0.1)', // Light hover effect
              },
              padding: '6px', // Slightly increase padding for better spacing
            }}
            onClick={() => handleOpenPdf(item.title)}
          >
            <InfoIcon fontSize='medium' sx={{ opacity: 0.7 }} />{' '}
            {/* Slightly faded icon */}
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
          >
            <Grid item sx={{ ml: 1.5 }}>
              <MuiBreadcrumbs aria-label='breadcrumb'>
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
                {mainContent}
                {itemContent}
              </MuiBreadcrumbs>
            </Grid>
            {title && (
              <Grid item sx={{ mt: 2 }}>
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
