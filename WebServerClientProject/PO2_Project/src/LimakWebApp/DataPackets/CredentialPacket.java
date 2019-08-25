package LimakWebApp.DataPackets;

import java.io.Serializable;

public class CredentialPacket implements Comparable, Serializable {

    private String userEmail;
    private String userName;
    private String userFolderPath;

    public CredentialPacket(String email, String name, String path){
        userEmail = email;
        userName = name;
        userFolderPath = path;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isEmpty(){
        return (userName.isEmpty() || userFolderPath.isEmpty() || userEmail.isEmpty());
    }

    public String getUserFolderPath() {
        return userFolderPath;
    }

    @Override
    public int compareTo(Object o){
        int err=-1;
        if(o instanceof CredentialPacket){
            ++err;
            if(((CredentialPacket) o).getUserName().compareTo(this.userName) != 0) {
                ++err;
            }
            if(((CredentialPacket) o).getUserEmail().compareTo(this.userEmail) != 0) {
                ++err;++err;++err;
            }
            if(((CredentialPacket) o).getUserFolderPath().compareTo(this.userFolderPath) != 0){
                ++err;++err;++err;++err;++err;
            }
        }
        return err;
    }

    @Override
    public boolean equals(Object o){
        return this.compareTo(o) == 0;
    }

    @Override
    public int hashCode(){
        return userName.hashCode();
    }

    @Override
    public String toString(){
        return this.getUserName();
    }

}
