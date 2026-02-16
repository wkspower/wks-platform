import { Box } from '@mui/material'
import UnitCapacityGrid from './UnitCapacityComponents/UnitCapacityGrid'

const UnitCapacity = ({
  PLANT_ID,
  SITE_ID,
  VERTICAL_ID,
  AOP_YEAR,
  currentTab,
  snackbarData,
  setSnackbarData,
  snackbarOpen,
  setSnackbarOpen,
}) => {
  const capacityTypes = [
    { key: 'design', title: 'Design Capacity' },
    { key: 'maxAchieved', title: 'Max Achieved Capacity' },
    { key: 'currentOperating', title: 'Current Operating Capacity' },
  ]

  return (
    <Box>
      {capacityTypes.map((type) => (
        <UnitCapacityGrid
          key={type.key}
          capacityType={type.key}
          title={type.title}
          PLANT_ID={PLANT_ID}
          SITE_ID={SITE_ID}
          VERTICAL_ID={VERTICAL_ID}
          AOP_YEAR={AOP_YEAR}
          snackbarData={snackbarData}
          setSnackbarData={setSnackbarData}
          snackbarOpen={snackbarOpen}
          setSnackbarOpen={setSnackbarOpen}
        />
      ))}
    </Box>
  )
}

export default UnitCapacity
