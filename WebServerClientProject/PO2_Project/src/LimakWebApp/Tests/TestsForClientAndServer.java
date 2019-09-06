package LimakWebApp.Tests;

import LimakWebApp.ClientSide.ApacheWatchService;
import LimakWebApp.ClientSide.AuthAgent;
import LimakWebApp.ClientSide.Client;
import LimakWebApp.DataPackets.CredentialPacket;
import LimakWebApp.DataPackets.MessageToSend;
import LimakWebApp.DataPackets.SocketHandler;
import LimakWebApp.ServerSide.CommunicationServiceThreadHandler;
import LimakWebApp.ServerSide.Server;
import LimakWebApp.Utils.Constants;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.net.Socket;

import java.nio.file.Files;
import java.nio.file.Path;

import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * <h1>TestsForClientAndServer</h1>
 * This class performs tests prepared for client's and server's side of this project.
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   01.09.2019
 */
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class TestsForClientAndServer {

    private TestClientController clientController;
    private TestServerController serverController;
    private Server server;
    private final long size = 0xFF;
    private static Path sharedTempDir;

    /**
     * This method initialises temporary directory to perform I/O operations for tests.
     * @throws IOException If any problem with I/O occurred
     */
    @Before
    public void initTemp() throws IOException{
        sharedTempDir = Files.createTempDirectory("Temporary");
    }

    /**
     * This test checks reaction of {@link Server} if we provide existing user's credentials
     * @throws IOException If any problem with I/O occurred
     */
    @Test
    public void authorizeCorrectExistingUser() throws IOException {
        Runnable task = () -> {
            CredentialPacket serverPacket = new CredentialPacket(Constants.getServerEMail(this), "Server", sharedTempDir.resolve(Constants.getServerDirectory(this)).toString());
            serverController = new TestServerController(serverPacket);
            serverController.updateListOfClients(new CredentialPacket("215691@edu.p.lodz.pl", "Kamil", sharedTempDir + "\\Kamil"), false);
            server = new Server();
            server.setController(serverController);
            serverController.setServer(server);
            serverController.authorize();
        };
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Thread thread = new Thread(task);
        thread.start();
        CredentialPacket packet = new CredentialPacket("215691@edu.p.lodz.pl", "Kamil", sharedTempDir + "\\Kamil");
        AuthAgent authAgent = new AuthAgent();
        Socket socket = new Socket(Constants.serverIP, Constants.authPort);
        authAgent.setSocket(socket);
        authAgent.setCredentialPacket(packet);
        Future<Integer> rV = executorService.submit(authAgent);
        int result = -1;
        try {
            result = rV.get();
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        authAgent.closeInitConnection();
        Assertions.assertEquals(0, result);
    }

    /**
     * This test checks reaction of {@link Server} if we provide new user's credentials
     * @throws IOException If any problem with I/O occurred
     */
    @Test
    public void authorizeNewClient() throws IOException {
        Runnable task = () -> {
            CredentialPacket serverPacket = new CredentialPacket(Constants.getServerEMail(this), "Server", sharedTempDir.resolve(Constants.getServerDirectory(this)).toString());
            serverController = new TestServerController(serverPacket);
            server = new Server();
            server.setController(serverController);
            serverController.setServer(server);
            serverController.authorize();
        };
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Thread thread = new Thread(task);
        thread.start();
        CredentialPacket packet = new CredentialPacket("chrustek@interia.pl", "Limak", sharedTempDir + "\\Limak");
        AuthAgent authAgent = new AuthAgent();
        Socket socket = new Socket(Constants.serverIP, Constants.authPort);
        authAgent.setSocket(socket);
        authAgent.setCredentialPacket(packet);
        Future<Integer> rV = executorService.submit(authAgent);
        int result = -1;
        try {
            result = rV.get();
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        authAgent.closeInitConnection();
        Assertions.assertEquals(9, result);
    }

    /**
     * This test checks reaction of {@link Server} if we provide not valid user
     * @throws IOException If any problem with I/O occurred
     */
    @Test
    public void authorizeInvalidClient() throws IOException {
        Runnable task = () -> {
            CredentialPacket serverPacket = new CredentialPacket(Constants.getServerEMail(this), "Server", sharedTempDir.resolve(Constants.getServerDirectory(this)).toString());
            serverController = new TestServerController(serverPacket);
            serverController.updateListOfClients(new CredentialPacket("215691@edu.p.lodz.pl", "Kamil", sharedTempDir + "\\Kamil"), false);
            server = new Server();
            server.setController(serverController);
            serverController.setServer(server);
            serverController.authorize();
        };
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Thread thread = new Thread(task);
        thread.start();
        CredentialPacket packet = new CredentialPacket("215691@edu.p.lodz.pl", "Limak", sharedTempDir + "\\Limak");
        AuthAgent authAgent = new AuthAgent();
        Socket socket = new Socket(Constants.serverIP, Constants.authPort);
        authAgent.setSocket(socket);
        authAgent.setCredentialPacket(packet);
        Future<Integer> rV = executorService.submit(authAgent);
        int result = -1;
        try {
            result = rV.get();
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        authAgent.closeInitConnection();
        Assertions.assertNotEquals(9, result);
        Assertions.assertNotEquals(0, result);
    }

    /**
     * This test checks if communication between {@link Client} and {@link CommunicationServiceThreadHandler} is performed properly
     * @throws IOException If any trouble with I/O occurred
     */
    @Test
    public void rcvCommandAndInitCommunicationTest() throws IOException{
        Runnable task = () -> {
            CredentialPacket serverPacket = new CredentialPacket(Constants.getServerEMail(this), "Server", sharedTempDir.resolve(Constants.getServerDirectory(this)).toString());
            serverController = new TestServerController(serverPacket);
            server = new Server();
            server.setController(serverController);
            serverController.setServer(server);
            serverController.authorize();
        };
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Thread thread = new Thread(task);
        thread.start();
        CredentialPacket packet = new CredentialPacket("215691@edu.p.lodz.pl", "Limak", sharedTempDir + "\\Limak");
        AuthAgent authAgent = new AuthAgent();
        Socket socket = new Socket(Constants.serverIP, Constants.authPort);
        authAgent.setSocket(socket);
        authAgent.setCredentialPacket(packet);
        Future<Integer> rV = executorService.submit(authAgent);
        int result = -1;
        try {
            result = rV.get();
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        if(result == 0 || result == 9){
            String ID = authAgent.getSessionID();
            clientController = new TestClientController();
            clientController.setSessionID(ID);
            clientController.setStatusText("Session is active");
            ArrayList<Socket> socketList =  new ArrayList<>();
            socketList.add(new Socket(Constants.serverIP, Constants.filePort));
            socketList.add(new Socket(Constants.serverIP, Constants.commPort));
            SocketHandler socketHandler = new SocketHandler(socketList);
            Client client = new Client(socketHandler, packet, clientController);
            clientController.setClient(client);
            clientController.setCredentialPacket(packet);
            try {
                Thread.sleep(1000);
            }
            catch(InterruptedException ignored){}
            Assertions.assertEquals("Received command", clientController.getStatus());
            Assertions.assertTrue(clientController.getLog().contains("RECEIVE_LIST_OF_FILES"));
            executorService.shutdownNow();
            authAgent.closeInitConnection();
        }
    }

    /**
     * This test checks if {@link ApacheWatchService} properly detects new file in listened directory and if {@link Client} properly send detected file to {@link LimakWebApp.ServerSide.CommunicationServiceThreadHandler}
     * @throws IOException If any trouble with I/O occurred
     */
    @Test
    public void watcherServiceTestAndFileServiceTest()throws IOException{
        Runnable task = () -> {
            CredentialPacket serverPacket = new CredentialPacket(Constants.getServerEMail(this), "Server", sharedTempDir.resolve(Constants.getServerDirectory(this)).toString());
            serverController = new TestServerController(serverPacket);
            server = new Server();
            server.setController(serverController);
            serverController.setServer(server);
            serverController.authorize();
        };
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Thread thread = new Thread(task);
        thread.start();
        CredentialPacket packet = new CredentialPacket("215691@edu.p.lodz.pl", "Limak", sharedTempDir + "\\Limak");
        AuthAgent authAgent = new AuthAgent();
        Socket socket = new Socket(Constants.serverIP, Constants.authPort);
        authAgent.setSocket(socket);
        authAgent.setCredentialPacket(packet);
        Future<Integer> rV = executorService.submit(authAgent);
        int result = -1;
        try {
            result = rV.get();
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        if(result == 0 || result == 9){
            String ID = authAgent.getSessionID();
            authAgent.closeInitConnection();
            executorService.shutdownNow();
            clientController = new TestClientController();
            clientController.setSessionID(ID);
            clientController.setStatusText("Session is active");
            ArrayList<Socket> socketList =  new ArrayList<>();
            clientController.setCredentialPacket(packet);
            clientController.createDirectories();
            socketList.add(new Socket(Constants.serverIP, Constants.filePort));
            socketList.add(new Socket(Constants.serverIP, Constants.commPort));
            SocketHandler socketHandler = new SocketHandler(socketList);
            Client client = new Client(socketHandler, packet, clientController);
            clientController.setClient(client);
            try {
                ApacheWatchService apacheWatchService = new ApacheWatchService(clientController);
                clientController.setWatchService(apacheWatchService);
                clientController.runWatcher();
            }
            catch(IOException io){
                io.printStackTrace();
            }
            File fileToCopy = new File(sharedTempDir + "\\text_file.txt");
            BufferedWriter fileBuffer = new BufferedWriter(new FileWriter(fileToCopy));
            fileBuffer.write(randomContent());
            fileBuffer.close();
            Path path = Paths.get(packet.getUserFolderPath());
            Files.copy(sharedTempDir.resolve("text_file.txt"),path.resolve("text_file.txt"), StandardCopyOption.REPLACE_EXISTING);
            try {
                Thread.sleep(1500);
            }
            catch(InterruptedException ignored){}
            Assertions.assertEquals("All files were sent", clientController.getStatus());
            Assertions.assertEquals("File saved successfully", serverController.getStatus());
        }
    }

    /**
     * This test checks if client properly shares a file to provided user.
     * @throws IOException If any trouble with I/O occurred
     */
    @Test
    public void shareFileToUserTest() throws IOException{
        Runnable task = () -> {
            CredentialPacket serverPacket = new CredentialPacket(Constants.getServerEMail(this), "Server", sharedTempDir.resolve(Constants.getServerDirectory(this)).toString());
            serverController = new TestServerController(serverPacket);
            serverController.updateListOfClients(new CredentialPacket("chrustek@interia.pl", "Kamil", sharedTempDir + "\\Kamil"), false);
            server = new Server();
            server.setController(serverController);
            serverController.setServer(server);
            serverController.authorize();
        };
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Thread thread = new Thread(task);
        thread.start();
        CredentialPacket packet = new CredentialPacket("215691@edu.p.lodz.pl", "Limak", sharedTempDir + "\\Limak");
        AuthAgent authAgent = new AuthAgent();
        Socket socket = new Socket(Constants.serverIP, Constants.authPort);
        authAgent.setSocket(socket);
        authAgent.setCredentialPacket(packet);
        Future<Integer> rV = executorService.submit(authAgent);
        int result = -1;
        try {
            result = rV.get();
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        if(result == 0 || result == 9){
            String ID = authAgent.getSessionID();
            authAgent.closeInitConnection();
            executorService.shutdownNow();
            clientController = new TestClientController();
            clientController.setSessionID(ID);
            clientController.setStatusText("Session is active");
            ArrayList<Socket> socketList =  new ArrayList<>();
            clientController.setCredentialPacket(packet);
            clientController.createDirectories();
            socketList.add(new Socket(Constants.serverIP, Constants.filePort));
            socketList.add(new Socket(Constants.serverIP, Constants.commPort));
            SocketHandler socketHandler = new SocketHandler(socketList);
            Client client = new Client(socketHandler, packet, clientController);
            clientController.setClient(client);
            try {
                ApacheWatchService apacheWatchService = new ApacheWatchService(clientController);
                clientController.setWatchService(apacheWatchService);
                clientController.runWatcher();
            }
            catch(IOException io){
                io.printStackTrace();
            }
            File fileToCopy = new File(sharedTempDir + "\\text_file.txt");
            BufferedWriter fileBuffer = new BufferedWriter(new FileWriter(fileToCopy));
            fileBuffer.write(randomContent());
            fileBuffer.close();
            Path path = Paths.get(packet.getUserFolderPath());
            Files.copy(sharedTempDir.resolve("text_file.txt"),path.resolve("text_file.txt"), StandardCopyOption.REPLACE_EXISTING);
            clientController.setItemToShare(path.resolve("text_file.txt").toFile());
            MessageToSend command = new MessageToSend(packet, MessageToSend.COMMAND_TYPE.SHARE_FILE_TO_USER);
            ArrayList<Object> items = new ArrayList<>();
            items.add("Kamil");
            items.add(clientController.getItemToShare().getName());
            command.addContents(items);
            try {
                Thread.sleep(600);
            }
            catch(InterruptedException ignored){}
            clientController.getClient().submitCmd(command);
            try {
                Thread.sleep(100);
            }
            catch(InterruptedException ignored){}
            Assertions.assertEquals("Shared successfully", clientController.getStatus());
        }
    }

    /**
     * Cleans connections
     * @throws IOException If any problem with I/O occurred
     */
    @After
    public void clearClientAndServer() throws IOException{
        if(clientController != null) {
            clientController.cleanUp();
        }
        if(serverController != null) {
            serverController.cleanUp();
        }
        deleteDirectory(sharedTempDir);
    }

    private void deleteDirectory(Path root) throws IOException{
        File[] files = root.toFile().listFiles();
        if(files != null){
            for(File file : files){
                if(file.isDirectory()){
                    deleteDirectory(file.toPath());
                }
                else{
                    Files.deleteIfExists(file.toPath());
                }
            }
            Files.deleteIfExists(root);
        }
    }
    private String randomContent(){
        Random generator = new Random();
        StringBuilder content = new StringBuilder();
        for(long i = 0; i < size; ++i){

            content.append((char)generator.nextInt(Character.MAX_VALUE-20000));
        }
        return content.toString();
    }
}
