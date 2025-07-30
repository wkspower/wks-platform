import { useMenuContext } from 'menu/menuProvider'
import { useMemo } from 'react'

export function usePermissions() {
  const { permissions } = useMenuContext()
  return useMemo(() => {
    const hasRead = permissions.includes('read')
    const hasWrite = permissions.includes('write')
    const hasApprove = permissions.includes('approve')

    return {
      isReadOnly: hasRead && !hasWrite && !hasApprove,
      isWriteOnly: !hasRead && hasWrite && !hasApprove,
      isReadWrite: hasRead && hasWrite && !hasApprove,
      isFullAccess: hasRead && hasWrite && hasApprove,
      isApproveOnly: !hasRead && !hasWrite && hasApprove,
    }
  }, [permissions])
}
