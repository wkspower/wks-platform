import React, { useState } from 'react'
import PackagingAndConsumables from './PackagingAndConsumables'
import OtherCost from './OtherCost'

const PackagingConsumables = () => {
  const [refreshTrigger, setRefreshTrigger] = useState(0)
  const triggerRefresh = () => setRefreshTrigger((prev) => prev + 1)

  return (
    <div>
      <div>
        <PackagingAndConsumables
          refreshTrigger={refreshTrigger}
          triggerRefresh={triggerRefresh}
        />
      </div>
      <div style={{ marginTop: '24px' }}>
        <OtherCost refreshTrigger={refreshTrigger} />
      </div>
    </div>
  )
}

export default PackagingConsumables
