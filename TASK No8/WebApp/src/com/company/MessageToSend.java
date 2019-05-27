package com.company;
import java.io.Serializable;
public class MessageToSend  implements Serializable{
    private String text = "";
    public MessageToSend(String notification){
        text = notification;
    }
    public String getMessage(){
        return text;
    }
}