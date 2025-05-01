package ed.sanarenovo.interfaces;

import java.util.List;

public interface UiService <T>{
    void addService(T Service);
    void deleteService(T Service);
    void updateService(T Service, int id);
    List<T> getService();
    T getServiceById(int id);
}
