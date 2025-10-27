import InfoIcon from '@mui/icons-material/Info'
import { IconButton, Tooltip } from '@mui/material'
import Notification from 'components/Utilities/Notification'
import PropTypes from 'prop-types'
import { useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'

import { Grid, Typography } from '@mui/material'
import { useSession } from 'SessionStoreContext'
import StepperNav from 'components/Utilities/StepperNav'
import Config from 'consts/index'
import { useDispatch, useSelector } from 'react-redux'
import { setScreenTitle } from 'store/reducers/dataGridStore'
import { Box } from '../../../node_modules/@mui/material/index'
import MainCard from '../MainCard'

const Breadcrumbs = ({ navigation, title, ...others }) => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { verticalChange, plantObject, verticalObject, siteObject, year } =
    dataGridStore

  const dispatch = useDispatch()

  const PLANT_ID = plantObject?.id
  const VERTICAL_ID = verticalObject?.id
  const SITE_ID = siteObject?.id
  const AOP_YEAR = year?.selectedYear

  const PLANT_NAME = plantObject?.name
  const VERTICAL_NAME = verticalObject?.name
  const SITE_NAME = siteObject?.name

  const [notification, setNotification] = useState({
    open: false,
    message: '',
    severity: 'info',
  })

  async function handleOpenPdf(title) {
    const url = `${Config.StorageUrl}/storage/files/${VERTICAL_NAME}/${SITE_NAME}/${PLANT_NAME}/downloads/${title}.pdf?content-type=application/pdf`
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
    // console.log('titletitle', title)
    var url = ''
    if (title != 'production-aop')
      url = `${window.location.origin}/files/DTC.xlsx`
    else {
      url = `${window.location.origin}/files/Blue Print.docx`
    }

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
      console.error('Error fetching file:', e)
      return Promise.reject(e)
    }
  }

  async function handleOpenPdfTempSSRS(title) {
    try {
      let baseurl = ''
      baseurl =
        'http://sjmnpb174/ReportServer/Pages/ReportViewer.aspx?%2fAOP&rs:Command=Render'
      const params = new URLSearchParams({
        verticalId: VERTICAL_ID,
        siteId: SITE_ID,
        plantId: PLANT_ID,
        finYear: AOP_YEAR,
      })
      const url = `${baseurl}?${params.toString()}`

      window.open(url, '_blank')
      return true
    } catch (e) {
      console.error('Error opening link:', e)
      return Promise.reject(e)
    }
  }

  const location = useLocation()
  const [main, setMain] = useState()
  const [item, setItem] = useState()

  useEffect(() => {
    let title = item?.title

    dispatch(
      setScreenTitle({
        title,
      }),
    )
  }, [item, VERTICAL_NAME, PLANT_ID, AOP_YEAR])

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
    const normalizedTitle = itemTitle?.toLowerCase().replace(/\s/g, '')

    if (
      [
        'monthwiseproductionplan',
        'overallaopconsumption(norm/quantity)',
      ].includes(normalizedTitle) &&
      VERTICAL_NAME?.toLowerCase() == 'meg'
    ) {
      itemContent = (
        <Typography
          variant='subtitle1'
          color='textPrimary'
          display='flex'
          alignItems='center'
        >
          {/* HIDE THE TITLE NAME  */}

          <Tooltip title={`Basis for ${itemTitle}`}>
            <IconButton
              size='small'
              sx={{
                backgroundColor: 'transparent',
                '&:hover': {
                  backgroundColor: 'rgba(0, 0, 0, 0.1)',
                },
              }}
              onClick={() => handleOpenPdfTemp(item?.id)}
            >
              <InfoIcon fontSize='small' sx={{ color: '#0100cb' }} />
            </IconButton>
          </Tooltip>
        </Typography>
      )
    } else if (
      (['aopapprovalflow'].includes(normalizedTitle) &&
        VERTICAL_NAME?.toLowerCase() === 'pe') ||
      VERTICAL_NAME?.toLowerCase() === 'pp'
    ) {
      itemContent = (
        <Typography
          variant='subtitle1'
          color='textPrimary'
          display='flex'
          alignItems='center'
        >
          {/* HIDE THE TITLE NAME  */}
          {/* {title1} */}
          <Tooltip title={`Report`}>
            <IconButton
              size='small'
              sx={{
                backgroundColor: 'transparent',
                '&:hover': {
                  backgroundColor: 'rgba(0, 0, 0, 0.1)',
                },
              }}
              onClick={() => handleOpenPdfTempSSRS(item?.id)}
            >
              <InfoIcon fontSize='small' sx={{ color: '#0100cb' }} />
            </IconButton>
          </Tooltip>
        </Typography>
      )
    } else {
      itemContent = null
    }

    // console.log('keycloak?.realmAccess?.roles', keycloak?.idTokenParsed)

    // main
    if (
      item.breadcrumbs !== false &&
      location?.pathname !== '/user-management' &&
      location?.pathname !== '/user-form'
    ) {
      breadcrumbContent = (
        <MainCard
          border={false}
          sx={{ bgcolor: 'transparent' }}
          {...others}
          content={false}
        >
          {location?.pathname.startsWith('/production-norms-plan') && (
            <Box>
              <StepperNav />
            </Box>
          )}
          <Grid
            container
            direction='column'
            justifyContent='flex-start'
            alignItems='flex-start'
          >
            <Grid
              container
              sx={{ mt: 0, mb: 1 }}
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
                    fontSize: '0.7rem',
                    display: 'flex',
                    alignItems: 'center',
                  }}
                >
                  {VERTICAL_NAME} / {SITE_NAME} / {PLANT_NAME}{' '}
                  {/* {getRoleName(verticalId, item?.id)} */}
                  {/* {keycloak?.realmAccess?.roles[0]} */}
                  {/* {itemContent} */}
                </Typography>
              </Grid>
            </Grid>
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
