import React from 'react'
import { Box, Typography, Stack } from '@mui/material'
import honLogo from '../../../assets/images/hon.svg'

const Footer = () => {
  return (
    <Box
      component='footer'
      sx={{
        py: 0.625,
        textAlign: 'center',
        backgroundColor: '#f5f5f5',
        mt: 'auto',
      }}
    >
      <Stack
        direction='row'
        justifyContent='center'
        alignItems='center'
        spacing={1}
      >
        <Typography variant='body2' color='textSecondary'>
          Powered by Honeywell © 2025
        </Typography>

        <Box
          component='img'
          src={honLogo}
          alt='Honeywell'
          sx={{ height: 10 }}
        />
      </Stack>
    </Box>
  )
}

export default Footer
