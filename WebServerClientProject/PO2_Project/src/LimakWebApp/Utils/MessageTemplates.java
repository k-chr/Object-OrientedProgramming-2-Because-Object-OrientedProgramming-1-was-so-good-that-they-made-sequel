package LimakWebApp.Utils;

import LimakWebApp.DataPackets.CredentialPacket;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class MessageTemplates {

    private String contents;
    private String subject;

    public MessageTemplates(CredentialPacket packet, Boolean type, String... files){
        contents = type ?
                new StringBuilder()
                        .append("Hello ").append(packet.getUserName()).append("!").append('\n')
                        .append("Thank you for joining to our community!").append('\n')
                        .append("Enjoy our File Transfer Service!")
                        .append('\n').append("Greetings!")
                .toString()
                :   new StringBuilder()
                        .append("Dear ").append(packet.getUserName()).append("!").append('\n')
                        .append("Several new files are waiting for you on server! It's time to download them!")
                        .append('\n').append("List of files: ").append("\n\t")
                        .append((files.length > 0) ? Arrays.stream(files).collect(Collectors.joining("\n\t")): "No files")
                .toString();
        subject = type ? "Hello new user!" : "New files are waiting for you!";
    }

    public final String getContents(Object o) {
        return o instanceof LimakWebApp.ServerSide.Server || o instanceof LimakWebApp.ServerSide.EmailUtil ? contents : null;
    }

    public final String getSubject(Object o){
        return o instanceof LimakWebApp.ServerSide.Server || o instanceof LimakWebApp.ServerSide.EmailUtil ? subject : null;
    }
}