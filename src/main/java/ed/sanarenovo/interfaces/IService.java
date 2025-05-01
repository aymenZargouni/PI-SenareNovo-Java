package ed.sanarenovo.interfaces;

import java.sql.SQLException;
import java.util.List;

public interface IService<E> {

    void addEntity(E e) ;
    void deleteEntity (int id);
    void updateEntity(E e, int id);
    List<E> getAll();
}
