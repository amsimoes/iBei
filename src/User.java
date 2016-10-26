import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User implements Serializable{
    public String username;
    public String password;
    public boolean banned=false;
    public List<String> notifications;
    public int leiloes=0;
    public int vitorias=0;

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

    public Boolean getBanned() {
        return banned;
    }

    public Boolean getLeiloes() {
        return leiloes;
    }

    public Boolean getvitorias() {
        return vitorias;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setBanned(Boolean banned) {
        this.banned = banned;
    }

    public Boolean setLeiloes() {
        this.leiloes = leiloes;
    }

    public Boolean getvitorias() {
        this.vitorias = vitorias;
    }

    public void addNotification(String text){
        this.notifications.add(text);
    }

    public List<String> getNotifications() {
        return notifications;
    }
}