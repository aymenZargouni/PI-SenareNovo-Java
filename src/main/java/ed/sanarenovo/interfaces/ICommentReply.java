package ed.sanarenovo.interfaces;

import ed.sanarenovo.entities.CommentReply;
import java.util.List;

public interface ICommentReply {
    void addReply(CommentReply reply);
    void deleteReply(int id);
    void updateReply(CommentReply reply, int id);
    List<CommentReply> getAllReplies();
    List<CommentReply> getRepliesByCommentId(int commentId);
    List<CommentReply> getRepliesByUserId(int userId);
    CommentReply getReplyById(int id);
} 