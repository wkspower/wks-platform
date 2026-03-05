import React from 'react'
import { Stack } from '../../../../../node_modules/@mui/material/index'
import ConfigurationAccordian from 'components/aop-phase-two/common/components/ConfigurationAccordian'
import { useSession } from 'SessionStoreContext'
import { useSelector } from 'react-redux'
import { getRoleName } from 'services/role-service'

const AopDesignBasis = () => {
  const keycloak = useSession()
  const dataGridStore = useSelector((state) => state.dataGridStore)
  const { oldYear, plantObject, siteObject, verticalObject, year } =
    dataGridStore

  const PLANT_ID = plantObject?.id
  const SITE_ID = siteObject?.id
  const VERTICAL_ID = verticalObject?.id
  const AOP_YEAR = year?.selectedYear
  const IS_OLD_YEAR = oldYear?.oldYear
  const READ_ONLY = getRoleName(keycloak, IS_OLD_YEAR)
  const isOldYear = false
  return (
    <div>
      <Stack sx={{ mt: 1, mb: 1 }}>
        <ConfigurationAccordian
          PLANT_ID={PLANT_ID}
          AOP_YEAR={AOP_YEAR}
          READ_ONLY={READ_ONLY}
          isOldYear={isOldYear}
          isSummaryRequired={true}
        />
      </Stack>
    </div>
  )
}

export default AopDesignBasis
