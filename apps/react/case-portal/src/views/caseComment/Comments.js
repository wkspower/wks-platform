import React from 'react'
import Typography from '@mui/material/Typography'
import { useSession } from 'SessionStoreContext'
import { useEffect, useState } from 'react'
import { CaseService } from '../../services'
import Comment from './Comment'
import CommentForm from './CommentForm'
import { deleteComment as deleteCommentApi } from './api'
import './comments.css'

export const Comments = ({ comments, aCase, getCaseInfo }) => {
  const [backendComments, setBackendComments] = useState(comments)

  const [activeComment, setActiveComment] = useState(null)

  const rootComments = backendComments.filter(
    (backendComment) =>
      backendComment.parentId === null ||
      !Object.prototype.hasOwnProperty.call(backendComment, 'parentId'),
  )

  const getReplies = (commentId) =>
    backendComments
      .filter((backendComment) => backendComment.parentId === commentId)
      .sort(
        (a, b) =>
          new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime(),
      )

  const keycloak = useSession()

  const addComment = async (text, parentId) => {
    CaseService.addComment(keycloak, text, parentId, aCase.businessKey)
      .then(() => {
        getCaseInfo(aCase)
      })
      .then(() => {
        getCaseInfo(aCase)
        setActiveComment(null)
      })
      .catch((err) => console.error(err))
  }

  const updateComment = (text, commentId) => {
    CaseService.updateComment(keycloak, text, commentId, aCase.businessKey)
      .then(() => {
        getCaseInfo(aCase)
        setActiveComment(null)
      })
      .catch((err) => {
        console.error(err)
        setActiveComment(null)
      })
  }

  const deleteComment = (commentId) => {
    // if (window.confirm('Are you sure you want to remove comment?')) {
    deleteCommentApi().then(() => {
      const updatedBackendComments = backendComments.filter(
        (backendComment) => backendComment.id !== commentId,
      )
      setBackendComments(updatedBackendComments)
    })
    CaseService.deleteComment(keycloak, commentId, aCase.businessKey)
      .then(() => {
        getCaseInfo(aCase, true)
        setActiveComment(null)
      })
      .catch((err) => {
        console.error(err)
        setActiveComment(null)
      })
    // }
  }

  useEffect(() => {
    setBackendComments(comments)
  })

  return (
    <React.Fragment>
      <Typography
        variant='h5'
        sx={{ textDecoration: 'none' }}
        color='textSecondary'
      >
        Comments
      </Typography>

      <CommentForm submitLabel='Send' handleSubmit={addComment} />
      {rootComments.map((rootComment) => (
        <Comment
          key={rootComment.id}
          comment={rootComment}
          replies={getReplies(rootComment.id)}
          activeComment={activeComment}
          setActiveComment={setActiveComment}
          addComment={addComment}
          deleteComment={deleteComment}
          updateComment={updateComment}
          currentUserId={keycloak.tokenParsed.preferred_username}
        />
      ))}
    </React.Fragment>
  )
}
