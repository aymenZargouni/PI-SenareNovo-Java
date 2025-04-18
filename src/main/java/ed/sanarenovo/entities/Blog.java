package ed.sanarenovo.entities;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Blog {
    private int id;
    private String title;
    private String content;
    private String image;
    private List<Category> categories = new ArrayList<>();

    public Blog() {

    }
    public Blog(int id, String title, String content, String image, List<Category> categories) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.image = image;
        this.categories = categories;
    }
    public Blog(int id, String title, String content, String image) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.image = image;
    }


    public Blog(String title, String content, String image) {
        this.title = title;
        this.content = content;
        this.image = image;
    }

    public Blog (String title, String content){
        this.title = title;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<Category> getCategories() {
        return categories;
    }
    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }
    @Override
    public String toString() {
        return "Blog{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}

