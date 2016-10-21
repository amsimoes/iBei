public class User{
    public String username;
    public String password;
    public Boolean admin=false;
    public Boolean banned=false;

    public User(String username, String password){
        this.username = username;
        this.password = password;

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
}