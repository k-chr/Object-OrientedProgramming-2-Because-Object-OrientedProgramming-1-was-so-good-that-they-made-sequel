package LimakWebApp.DataPackets;

import java.io.Serializable;

/**
 * <h1>CredentialPacket</h1>
 * This data packet provides encapsulation for user credentials
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   09.06.2019
 */
public class CredentialPacket implements Comparable, Serializable {

    private String userEmail;
    private String userName;
    private String userFolderPath;

    /**
     * Basic constructor of CredentialPacket.
     * @param email provided user email
     * @param name  provided user name
     * @param path provided path to user's directory
     */
    public CredentialPacket(String email, String name, String path){
        userEmail = email;
        userName = name;
        userFolderPath = path;
    }

    /**
     * This method returns user's email
     * @return String
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * This method returns user's name
     * @return String
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Method indicates whether {@link CredentialPacket} is valid or not.
     * @return boolean
     */
    public boolean isEmpty(){
        return (userName.isEmpty() || userFolderPath.isEmpty() || userEmail.isEmpty());
    }

    /**
     * This method returns user's path to directory
     * @return String
     */
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
