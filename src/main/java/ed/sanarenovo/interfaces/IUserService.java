package ed.sanarenovo.interfaces;

import ed.sanarenovo.entities.User;

import java.util.List;

public interface IUserService {
    void addUser(User user);
    void updateUser(User user);
    void deleteUser(int id);
    User getUserById(int id);
    List<User> getAllUsers();
}
