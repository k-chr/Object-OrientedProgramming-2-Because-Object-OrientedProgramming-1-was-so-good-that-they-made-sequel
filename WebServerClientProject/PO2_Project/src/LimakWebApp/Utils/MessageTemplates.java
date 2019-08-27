package LimakWebApp.Utils;

import LimakWebApp.DataPackets.CredentialPacket;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * <h1>MessageTemplates</h1>
 * This class contains all necessary templates to prepare email to client
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   30.07.2019
 */
public final class MessageTemplates {

    private String contents;
    private String subject;

    /**
     * Constructor of MessageTemplates.
     * @param packet destination user
     * @param type type of message. If true the email will contain greetings message otherwise email will be reminder of new files
     * @param files Optional parameter - an array of file names to place them in content of reminder
     */
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

    /**
     * This method returns contents of email if object is valid, otherwise <code>null</code>
     * @param o Accessor of this method:
     * {@link LimakWebApp.ServerSide.Server}
     * or
     * {@link LimakWebApp.ServerSide.EmailUtil}
     * @return String
     */
    public final String getContents(Object o) {
        return o instanceof LimakWebApp.ServerSide.Server || o instanceof LimakWebApp.ServerSide.EmailUtil ? contents : null;
    }

    /**
     * This method returns subject of email if object is valid, otherwise <code>null</code>
     * @param o Accessor of this method:
     * {@link LimakWebApp.ServerSide.Server}
     * or
     * {@link LimakWebApp.ServerSide.EmailUtil}
     * @return String
     */
    public final String getSubject(Object o){
        return o instanceof LimakWebApp.ServerSide.Server || o instanceof LimakWebApp.ServerSide.EmailUtil ? subject : null;
    }
}
