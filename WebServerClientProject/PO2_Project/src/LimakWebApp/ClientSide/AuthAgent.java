package LimakWebApp.ClientSide;

import LimakWebApp.DataPackets.CredentialPacket;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;

import java.util.concurrent.Callable;

/**
 * <h1>AuthAgent</h1>
 * This class is used by {@link LoginPageController} to perform authorization process with server
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   01.08.2019
 */
public class AuthAgent implements Callable<Integer> {

    private volatile CredentialPacket credentialPacket;
    private volatile Socket socket;
    private volatile ObjectOutputStream inputSender;
    private volatile ObjectInputStream inputReceiver;

    /**
     * This method sets a socket necessary to perform initial communication with server.
     * @param socket an authorization socket to set
     */
    public void setSocket(Socket socket){
        this.socket = socket;
    }

    /**
     * This method sets credentials to perform initial communication with server.
     * @param packet credentials received from {@link LoginPageController}
     */
    public void setCredentialPacket(CredentialPacket packet){
        this.credentialPacket = packet;
    }

    /**
     * This method returns a reference to credentials of user who is trying to log in.
     * @return CredentialPacket
     */
    public CredentialPacket getCredentialPacket() {
        return credentialPacket;
    }

    /**
     * This method closes streams and sockets created or assigned during process of authentication
     */
    public void closeInitConnection(){
        try{
            if(inputReceiver != null) {
                inputReceiver.close();
            }
            if(inputSender != null) {
                    inputSender.close();
            }
            if(socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
        catch(IOException ignored){

        }
    }

    /**
     * Default constructor
     */
    public AuthAgent(){

    }

    /**
     * This method returns session's ID generated for current new user.
     * @return {@link String}
     */
    public String getSessionID(){
        try {
            return (String) inputReceiver.readObject();
        }
        catch(ClassNotFoundException | IOException e){
            e.printStackTrace();
            return "";
        }
    }

    private void initStreams(){
        try {
            inputSender = new ObjectOutputStream(this.socket.getOutputStream());
            inputSender.flush();
            InputStream input = this.socket.getInputStream();
            inputReceiver = new ObjectInputStream(input);
        }
        catch(IOException ignore){
        }
    }

    private synchronized Integer authorize(){
        try {
            inputSender.writeObject(this.credentialPacket);
            inputSender.flush();
            return (Integer)inputReceiver.readObject();
        }
        catch(ClassNotFoundException|IOException e){
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * This method initializes streams, returns a result code of authentication process.
     * @return Integer
     */
    @Override
    public Integer call() {
        if(socket == null || socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown()){
            return -1;
        }
        initStreams();
        return authorize();
    }
}
