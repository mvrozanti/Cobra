package cobra2;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Nexor
 */
public class CobraNetwork {

    private static ServerSocket socketFromServer;
    private static String ip = "127.0.0.1";
    private static int port = 448;
    private static List<Socket> connections = new ArrayList<>();
    private static boolean isServer;
    private static boolean isConnected;
    private static List<ActionListener> actionListeners = new ArrayList<>();

    public static void init() {
        if (!connect()) {
            initializeServer();
            isConnected = true;
        }
        isConnected = false;
    }

    private static void initializeServer() {
        try {
            System.out.println("Server initialized...");
            socketFromServer = new ServerSocket(port, 8, InetAddress.getByName(ip));
            isServer = true;
            startAcceptingNewConnections();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isConnected() {
        return !isConnected;
    }

    public void waitUntilConnected() {
        while(!isConnected()){
            continue;
        }
    }

    /**
     * who calls this? i mean watafaq m8 wt u thinkn
     *
     * @param al
     */
    public static void addActionListener(ActionListener al) {
        actionListeners.add(al);
    }

    public static void sendObjectToServer(Object o) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(connections.get(0).getOutputStream());
            oos.writeObject(o);
            oos.flush();
        } catch (Exception ex) {
            Logger.getLogger(CobraNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void dispatchObjectToClients(Object o) {
        for (Socket connection : connections) {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(connection.getOutputStream());
//                oos.reset();
                oos.writeObject(o);
                oos.flush();
            } catch (IOException ex) {
                Logger.getLogger(CobraNetwork.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void startAcceptingNewConnections() {
        new Thread(() -> {
            System.out.println("Started accepting new connections...");
            while (true) {
                Socket client = null;
                try {
                    client = socketFromServer.accept();
                    connections.add(client);
                    startClientHandler(client);
                } catch (IOException ex) {
                    Logger.getLogger(CobraNetwork.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    private static void startClientHandler(Socket s) {
        try {
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            for (ActionListener al : actionListeners) {
                al.actionPerformed(new ActionEvent("c", 0, null));
            }
            new Thread(() -> {
                System.out.println("Client handler initialized for " + s.getInetAddress().getHostAddress() + "...");
                while (true) {
                    try {
//                        System.out.println("Listening for incoming object from client...");
                        Object itemToUpdate = ois.readObject();
                        for (ActionListener al : actionListeners) {
                            al.actionPerformed(new ActionEvent(itemToUpdate, 0, "client"));
                        }
                    } catch (IOException ex) {
//                        Logger.getLogger(CobraNetwork.class.getName()).log(Level.SEVERE, null, ex);
                        continue;
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(CobraNetwork.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }).start();
        } catch (Exception e) {
            Logger.getLogger(CobraNetwork.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private static boolean connect() {
        try {
            Socket s = new Socket(ip, port);
            isServer = false;
            connections.add(s);
            System.out.println("This client is connected.");
            ObjectInputStream ois = new ObjectInputStream(connections.get(0).getInputStream());
            new Thread(() -> {
                while (true) {
                    try {
//                        if (ois.available() > 0) {
//                        System.out.println("Listening for incoming objects from server...");
                        Object incomingMessage = ois.readObject();
                        for (ActionListener al : actionListeners) {
                            al.actionPerformed(new ActionEvent(incomingMessage, 0, "server"));
                        }
                    } catch (IOException ex) {
//                        Logger.getLogger(CobraNetwork.class.getName()).log(Level.SEVERE, null, ex);
                        continue;
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(CobraNetwork.class.getName()).log(Level.SEVERE, null, ex);
                    }
//                        }
                }
            }).start();
            return true;
        } catch (IOException ex) {
//            Logger.getLogger(CobraNetwork.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public static boolean isServer() {
        return isServer;
    }
}
