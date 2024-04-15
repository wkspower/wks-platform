import Avatar from '@mui/material/Avatar';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import User1 from 'assets/images/users/avatar-3.png';
import { useTranslation } from 'react-i18next';
import CommentForm from './CommentForm';

const Comment = ({
  comment,
  replies,
  setActiveComment,
  activeComment,
  updateComment,
  deleteComment,
  addComment,
  parentId = null,
  currentUserId,
}) => {
  const isEditing =
    activeComment &&
    activeComment.id === comment.id &&
    activeComment.type === 'editing';
  const isReplying =
    activeComment &&
    activeComment.id === comment.id &&
    activeComment.type === 'replying';
  // const fiveMinutes = 300000;
  // const timePassed = new Date() - new Date(comment.createdAt) > fiveMinutes;
  const canDelete = replies.length === 0; // && currentUserId === comment.userId && !timePassed;
  const canReply = true; //Boolean(currentUserId);
  const canEdit = true; //currentUserId === comment.userId && !timePassed;
  const replyId = parentId ? parentId : comment.id;
  const createdAt = new Date(comment.createdAt).toLocaleDateString();

  const { t } = useTranslation();

  return (
    <div key={comment.id} className='comment'>
      <div className='comment-image-container'>
        <Avatar alt='John Doe' src={User1} />
      </div>
      <div className='comment-right-part'>
        <div className='comment-content'>
          <Box sx={{ display: 'flex', flexDirection: 'row', m: 1 }}>
            <Typography sx={{ p: 0.5 }} variant='h5'>
              {comment.userName}
            </Typography>
            <Typography sx={{ p: 0.5 }}>{createdAt}</Typography>
          </Box>
        </div>
        {!isEditing && <Typography variant='h6'>{comment.body}</Typography>}
        {isEditing && (
          <CommentForm
            submitLabel={t('pages.comments.actions.edit.update')}
            hasCancelButton
            initialText={comment.body}
            handleSubmit={(text) => updateComment(text, comment.id)}
            handleCancel={() => {
              setActiveComment(null);
            }}
          />
        )}
        <div className='comment-actions'>
          {canReply && (
            <Button
              className='comment-action'
              onClick={() =>
                setActiveComment({ id: comment.id, type: 'replying' })
              }
            >
              {t('pages.comments.actions.reply')}
            </Button>
          )}
          {canEdit && (
            <Button
              className='comment-action'
              onClick={() =>
                setActiveComment({ id: comment.id, type: 'editing' })
              }
            >
              {t('pages.comments.actions.edit.action')}
            </Button>
          )}
          {canDelete && (
            <Button
              className='comment-action'
              onClick={() => deleteComment(comment.id)}
            >
              {t('pages.comments.actions.delete')}
            </Button>
          )}
        </div>
        {isReplying && (
          <CommentForm
            submitLabel='Reply'
            handleSubmit={(text) => addComment(text, replyId)}
          />
        )}
        {replies.length > 0 && (
          <div className='replies'>
            {replies.map((reply) => (
              <Comment
                comment={reply}
                key={reply.id}
                setActiveComment={setActiveComment}
                activeComment={activeComment}
                updateComment={updateComment}
                deleteComment={deleteComment}
                addComment={addComment}
                parentId={comment.id}
                replies={[]}
                currentUserId={currentUserId}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default Comment;
