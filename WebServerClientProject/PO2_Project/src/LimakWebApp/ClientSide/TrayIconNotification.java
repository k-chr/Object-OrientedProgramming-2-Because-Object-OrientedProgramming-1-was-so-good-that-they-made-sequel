package LimakWebApp.ClientSide;

import LimakWebApp.DataPackets.CredentialPacket;

import javafx.application.Platform;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import java.awt.AWTException;
import java.awt.Font;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * <h1>TrayIconNotification</h1>
 * This class provides tray notification for client app.
 * @author  Kamil Chrustowski
 * @version 1.0
 * @since   10.08.2019
 */
public class TrayIconNotification extends JFrame {

    private Timer notificationTimer;
    private Stage stage;
    private DateFormat timeFormat;
    private Image image;
    private CredentialPacket user;
    private MediaTracker tracker;
    private static final String iconImageLoc =
            "../Resources/progIcoSmall.png";

    /**
     * This constructor initializes {@link MediaTracker}, checks if trays notifications are available on system, reads an image for tray icon, and adds tray to given stage
     * @param user credentials to display tray notification
     * @param stage stage to apply tray notification to
     */
    public TrayIconNotification(CredentialPacket user, Stage stage){
        tracker = new MediaTracker(this);
        if (!SystemTray.isSupported()) {
            return;
        }
        this.user = user;
        this.stage = stage;
        notificationTimer = new Timer();
        timeFormat = SimpleDateFormat.getTimeInstance();
        try {
            image = ImageIO.read(getClass().getResource(iconImageLoc));
        }
        catch( IOException io){
            io.printStackTrace();
        }
        tracker.addImage(image, 0);
        try {
            tracker.waitForAll();
        } catch (InterruptedException ignored) {
        }
        addAppToTray();
    }

    private void addAppToTray() {
        try {
            SystemTray tray = SystemTray.getSystemTray();
            TrayIcon trayIcon = new TrayIcon(image);
            trayIcon.addActionListener(e -> Platform.runLater(()->{
                showStage();
                notificationTimer.cancel();
                tray.remove(trayIcon);
            }));
            trayIcon.addMouseListener(
                new MouseInputAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        Platform.runLater(() -> {
                            showStage();
                            notificationTimer.cancel();
                            tray.remove(trayIcon);
                        });
                    }
                }
            );
            MenuItem openItem = new MenuItem("Show");
            openItem.addActionListener(e -> Platform.runLater(()->{
                showStage();
                notificationTimer.cancel();
                tray.remove(trayIcon);
            }));
            Font defaultFont = Font.decode(null);
            Font boldFont = defaultFont.deriveFont(java.awt.Font.BOLD);
            openItem.setFont(boldFont);
            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(event -> {
                notificationTimer.cancel();
                tray.remove(trayIcon);
            });

            final PopupMenu popup = new PopupMenu();
            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);

            notificationTimer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            SwingUtilities.invokeLater(() ->
                                    trayIcon.displayMessage(
                                            "Hello: " + user.getUserName() ,
                                            "The time is now " + timeFormat.format(new Date()) + "\nNew files have already\narrived to your disk\nCHECK THEM OUT!",
                                            TrayIcon.MessageType.INFO
                                    )
                            );
                        }
                    },
                    1_000,
                    10_000
            );
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("Encountered error with notification tray");
            e.printStackTrace();
        }
    }

    private void showStage(){
        if(stage != null) {
            stage.setIconified(false);
            stage.requestFocus();
            stage.toFront();
            stage.setAlwaysOnTop(true);
            stage.setAlwaysOnTop(false);
        }
    }
}
