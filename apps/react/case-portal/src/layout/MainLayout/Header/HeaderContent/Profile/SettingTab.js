// import { useState } from 'react'
// import { useTheme } from '@mui/material/styles'
// import { List, ListItemButton, ListItemIcon, ListItemText } from '@mui/material'
// import UserOutlined from '@ant-design/icons/UserOutlined'

// const SettingTab = () => {
//   const theme = useTheme()

//   const [selectedIndex, setSelectedIndex] = useState(0)
//   const handleListItemClick = (event, index) => {
//     setSelectedIndex(index)
//   }

//   return (
//     <List
//       component='nav'
//       sx={{
//         p: 0,
//         '& .MuiListItemIcon-root': {
//           minWidth: 32,
//           color: theme.palette.grey[500],
//         },
//       }}
//     >
//       <ListItemButton
//         selected={selectedIndex === 1}
//         onClick={(event) => handleListItemClick(event, 1)}
//       >
//         <ListItemIcon>
//           <UserOutlined />
//         </ListItemIcon>
//         <ListItemText primary='Account Settings' />
//       </ListItemButton>
//     </List>
//   )
// }

// export default SettingTab
