package LimakWebApp.ClientSide;

import LimakWebApp.DataPackets.CredentialPacket;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;

import java.util.concurrent.Callable;

public class AuthAgent implements Callable<Integer> {

    private volatile CredentialPacket credentialPacket;
    private volatile Socket socket;
    private volatile ObjectOutputStream inputSender;
    private volatile ObjectInputStream inputReceiver;

    public void setSocket(Socket socket){
        this.socket = socket;
    }

    public void setCredentialPacket(CredentialPacket packet){
        this.credentialPacket = packet;
    }

    public CredentialPacket getCredentialPacket() {
        return credentialPacket;
    }

    public void closeInitConnection(){
        try{
            inputReceiver.close();
            inputSender.close();
            if(!socket.isClosed())
                socket.close();
        }
        catch(IOException ignored){

        }
    }

    AuthAgent(){

    }
    String getSessionID(){
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
            System.out.println("got output stream");
            InputStream input = this.socket.getInputStream();
            inputReceiver = new ObjectInputStream(input);
            System.out.println("got input stream");
        }
        catch(IOException ignore){
            ignore.printStackTrace();
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
            return null;
        }
    }

    @Override
    public Integer call() {
        initStreams();
        return authorize();
    }
}
