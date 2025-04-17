package ed.sanarenovo.tests;

import ed.sanarenovo.entities.Blog;
import ed.sanarenovo.entities.Category;
import ed.sanarenovo.entities.Comment;
import ed.sanarenovo.services.BlogServices;
import ed.sanarenovo.services.CategoryServices;
import ed.sanarenovo.services.CommentServices;

import java.util.Calendar;

public class HichemMain {
    public static void main(String[] args) {
        BlogServices blogServices = new BlogServices();
        Blog blog1 = new Blog("Titre 1", "Contenu du blog", "image.jpg");
        Blog blog2 = new Blog("Titre 2", "Contenu du blog 2", "image2.jpg");
        //blogServices.addBlog(blog1);
        //blogServices.addBlog(blog2);
        //System.out.println(blogServices.getBlogs());
        //blogServices.updateBlog(new Blog("title nv", "content nv", "image_nv.jpg"), 1);
        //System.out.println(blogServices.getBlogById(1));
        //blogServices.deleteBlog(blog2);
        //System.out.println(blogServices.getBlogs());

        System.out.println("-----------------------------");

        CategoryServices categoryServices = new CategoryServices();
        Category cat1 = new Category("Santé");
        Category cat2 = new Category("Medecine");
        //categoryServices.addCategory(cat1);
        //categoryServices.addCategory(cat2);
        //System.out.println("Liste des catégories : " + categoryServices.getCategorys());
        //categoryServices.updateCategory(new Category("Médecine Générale"), 1);
        //System.out.println("Liste des catégories : " + categoryServices.getCategorys());
        //categoryServices.deleteCategory(new Category(2));

        System.out.println("-----------------------------");

        CommentServices commentServices = new CommentServices();
        Comment comment1 = new Comment("Bien Article");
        //commentServices.addComment(comment1);
        //System.out.println("Liste des Comments : " + commentServices.getComments());
        //commentServices.updateComment(new Comment("eeeeeeeeeee"), 1);
        //System.out.println("Liste des Comments : " + commentServices.getComments());
        //commentServices.deleteComment(1);

    }

}
