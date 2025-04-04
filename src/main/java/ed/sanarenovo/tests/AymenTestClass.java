package ed.sanarenovo.tests;

import ed.sanarenovo.entities.User;
import ed.sanarenovo.services.UserService;
import ed.sanarenovo.utils.MyConnection;

import java.util.List;

public class AymenTestClass {
    public static void main(String[] args) {


        UserService userService = new UserService();

        MyConnection.getInstance();

        List<User> users = userService.getAllUsers();
        for (User u : users) {
            System.out.println("ID: " + u.getId());
            System.out.println("Email: " + u.getEmail());
            System.out.println("Roles: " + u.getRoles());
            System.out.println("Blocked: " + u.isBlocked());
            System.out.println("Reset Token: " + u.getResetToken());
            System.out.println("Expires At: " + u.getResetTokenExpiresAt());
            System.out.println("--------------------------");
        }



        // ✅ Add new user
        User newUser = new User();
        newUser.setEmail("test@example.com");
        newUser.setPassword("hashedpassword123");
        newUser.setRoles("[\"ROLE_ADMIN\"]");
        newUser.setBlocked(false);
        newUser.setResetToken(null);
        newUser.setResetTokenExpiresAt(null);

        userService.addUser(newUser);

    }
}
