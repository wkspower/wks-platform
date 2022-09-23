import { useEffect, useState } from 'react';
import {
    createComment as createCommentApi,
    deleteComment as deleteCommentApi,
    getComments as getCommentsApi,
    updateComment as updateCommentApi
} from './api';
import Comment from './Comment';
import CommentForm from './CommentForm';

import { Typography } from '@mui/material';
import MainCard from 'components/MainCard';

import './comments.css';

export const Comments = ({ commentsUrl, currentUserId }) => {
    const [backendComments, setBackendComments] = useState([]);
    const [activeComment, setActiveComment] = useState(null);
    const rootComments = backendComments.filter((backendComment) => backendComment.parentId === null);
    const getReplies = (commentId) =>
        backendComments
            .filter((backendComment) => backendComment.parentId === commentId)
            .sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
    const addComment = (text, parentId) => {
        createCommentApi(text, parentId).then((comment) => {
            setBackendComments([comment, ...backendComments]);
            setActiveComment(null);
        });
    };

    const updateComment = (text, commentId) => {
        updateCommentApi(text).then(() => {
            const updatedBackendComments = backendComments.map((backendComment) => {
                if (backendComment.id === commentId) {
                    return { ...backendComment, body: text };
                }
                return backendComment;
            });
            setBackendComments(updatedBackendComments);
            setActiveComment(null);
        });
    };
    const deleteComment = (commentId) => {
        // if (window.confirm('Are you sure you want to remove comment?')) {
        deleteCommentApi().then(() => {
            const updatedBackendComments = backendComments.filter((backendComment) => backendComment.id !== commentId);
            setBackendComments(updatedBackendComments);
        });
        // }
    };

    useEffect(() => {
        getCommentsApi().then((data) => {
            setBackendComments(data);
        });
    }, []);

    return (
        <MainCard sx={{ p: 2 }} content={false}>
            <Typography variant="h5" sx={{ textDecoration: 'none' }} color="textSecondary">
                Comments
            </Typography>

            <CommentForm submitLabel="Send" handleSubmit={addComment} />
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
                    currentUserId={currentUserId}
                />
            ))}
        </MainCard>
    );
};
