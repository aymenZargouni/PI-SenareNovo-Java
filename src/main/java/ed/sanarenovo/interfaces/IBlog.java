package ed.sanarenovo.interfaces;

import java.util.List;

public interface IBlog<T> {

    void addBlog(T blog);
    void deleteBlog(T blog);
    void updateBlog(T blog, int id);
    List<T> getBlogs();
    T getBlogById(int id);
}

