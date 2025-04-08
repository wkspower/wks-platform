// useSafeNavigate.js
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { setIsBlocked } from 'store/reducers/dataGridStore'
import { activeItem } from 'store/reducers/menu'

export const useSafeNavigate = () => {
  const navigate = useNavigate()
  const dispatch = useDispatch()
  const isBlocked = useSelector((state) => state.dataGridStore.isBlocked)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [nextPath, setNextPath] = useState(null)
  //   console.log(isBlocked)
  const safeNavigate = (path) => {
    if (isBlocked) {
      setNextPath(path)
      setDialogOpen(true)
    } else {
      navigate(path)
    }
  }
  const itemHandler = (id) => {
    if (!isBlocked) dispatch(activeItem({ openItem: [id] }))
  }

  const confirmLeave = (id) => {
    dispatch(setIsBlocked(false))
    navigate(nextPath)
    dispatch(activeItem({ openItem: [id] }))
    setDialogOpen(false)
  }

  return {
    safeNavigate,
    dialogOpen,
    setDialogOpen,
    confirmLeave,
    itemHandler,
    nextPath,
    setNextPath,
  }
}
