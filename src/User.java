import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User implements Serializable{
    public String username;
    public String password;
    public boolean admin=false;
    public boolean banned=false;
    public List<String> notifications;

    public User(String username, String password){
        this.username = username;
        this.password = password;
        this.notifications = Collections.synchronizedList(new ArrayList<String>());;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public Boolean getBanned() {
        return banned;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public void setBanned(Boolean banned) {
        this.banned = banned;
    }

    public void addNotification(String text){
        this.notifications.add(text);
    }

    public List<String> getNotifications() {
        return notifications;
    }
}