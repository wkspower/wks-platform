import React from 'react'
import Footer from 'layout/MainLayout/Footer/index'
import { Box } from '@mui/material'

const Layout = ({ children }) => {
  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        minHeight: '100vh',
        userSelect: 'none',
      }}
    >
      <Box sx={{ flex: 1 }}>{children}</Box>

      <Footer />
    </Box>
  )
}

export default Layout
