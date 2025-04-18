package ed.sanarenovo.entities;

import java.util.Objects;

public class Comment {

    private int id;
    private String content;
    private Blog blog;
    public Comment() {}

    public Comment(int id, String content) {
        this.id = id;
        this.content = content;
    }

    public Comment(String content) {
        this.content = content;
    }

    public Comment(String content, Blog blog) {
        this.content = content;
        this.blog = blog;
    }
    public Comment(int id, String content, Blog blog) {
        this.id = id;
        this.content = content;
        this.blog = blog;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public Blog getBlog() {
        return blog;
    }
    public void setBlog(Blog blog) {
        this.blog = blog;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return id == comment.id && Objects.equals(content, comment.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, content);
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", content='" + content + '\'' +
                '}';
    }

}
