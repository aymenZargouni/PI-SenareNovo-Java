package ed.sanarenovo.interfaces;

import java.util.List;

public interface ICategory <T>{
    void addCategory(T category);
    void deleteCategory(T category);
    void updateCategory(T category, int id);
    List<T> getCategorys();
    T getCategoryById(int id);
}
