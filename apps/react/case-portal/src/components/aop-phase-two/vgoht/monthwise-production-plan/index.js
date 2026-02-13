import { Box } from '@mui/material'
import ProductGrid from './ProductGrid'
import BiproductGrid from './BiproductGrid'
import { Stack } from '../../../../../node_modules/@mui/material/index'

const MonthwiseProductionPlan = () => {
  return (
    <Box>
      <Stack sx={{ mt: 2, mb: 2 }}>
        <ProductGrid />
      </Stack>
      <Stack sx={{ mt: 3, mb: 2 }}>
        <BiproductGrid />
      </Stack>
    </Box>
  )
}

export default MonthwiseProductionPlan
