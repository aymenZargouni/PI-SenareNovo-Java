package ed.sanarenovo.interfaces;

import java.util.List;

public interface Uisalle <T>{
    void addSalle(T Service);
    void deleteSalle(T Service);
    void updateSalle(T Service);
    List<T> getSalles();
    T getSalleById(int id);
}
