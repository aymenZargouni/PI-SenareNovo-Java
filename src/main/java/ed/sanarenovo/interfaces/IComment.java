package ed.sanarenovo.interfaces;

import java.util.List;

public interface IComment<T> {
    void addComment(T comment);
    void deleteComment(int id);
    void updateComment(T comment, int id);
    List<T> getComments();
    T getCommentById(int id);
}
