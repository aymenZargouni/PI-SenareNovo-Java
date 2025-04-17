package ed.sanarenovo.interfaces;

import ed.sanarenovo.entities.User;

import java.util.List;

public interface IUserService<T> {
    void add(T t);
    void update(T t,int id);
    void delete(int id);
    T getById(int id);
    List<T> getAll();
}
