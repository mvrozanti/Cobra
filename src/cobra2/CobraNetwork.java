package cobra2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nexor
 */
public class CobraNetwork {

    private static ServerSocket socketFromServer;
    private static String ip = "127.0.0.1";
    private static int port = 448;
    private static List<Socket> connections = new ArrayList<>();
    private static HashMap<Socket, DataOutputStream> connectionMap = new HashMap<>();
    private static boolean isServer;
    private static boolean isConnected = false;
    private static List<ActionListener> actionListeners = new ArrayList<>();

    public static void init() {
        if (!connect()) {
            initializeServer();
        }
    }

    private static void initializeServer() {
        try {
            System.out.println("Server initialized...");
            socketFromServer = new ServerSocket(port, 8, InetAddress.getByName(ip));
            startAcceptingNewConnections();
            isConnected = true;
            isServer = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isConnected() {
        return isConnected;
    }

    /**
     * who calls this? i mean watafaq m8 wt u thinkn
     *
     * @param al
     */
    public static void addActionListener(ActionListener al) {
        actionListeners.add(al);
    }

    public static void main(String[] args) {
        CobraNetwork.init();
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getSource());
            }
        });
        while (!CobraNetwork.isConnected) {
            continue;
        }
        while (true) {
            if (CobraNetwork.isServer) {
                dispatchObjectToClients("cat");
            } else {
                sendObjectToServer("mouse");
            }
        }
    }

    public static void sendObjectToServer(Object o) {
        try {
            DataOutputStream dos;
            if (connectionMap.get(connections.get(0)) == null) {
                dos = new DataOutputStream(connections.get(0).getOutputStream());
                connectionMap.put(connections.get(0), dos);
            } else {
                dos = connectionMap.get(connections.get(0));
            }
//            oos.reset();
            dos.write(serialize(o));
            dos.flush();
        } catch (Exception ex) {
            Logger.getLogger(CobraNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void dispatchObjectToClient(Object o, Socket s) {
        DataOutputStream dos = null;
        if (connectionMap.get(s) == null) {
            try {
                dos = new DataOutputStream(s.getOutputStream());
                connectionMap.put(s, dos);
            } catch (IOException ex) {
                Logger.getLogger(CobraNetwork.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            dos = connectionMap.get(s);
        }
        try {
            dos.write(serialize(o));
            dos.flush();
        } catch (Exception ex) {
            Logger.getLogger(CobraNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void dispatchObjectToClients(Object o) {
        for (Socket s : connections) {
            dispatchObjectToClient(o, s);
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
                    dispatchObjectToClient(new Integer(connections.size() + 1), client);
                    startClientHandler(client);
                } catch (IOException ex) {
                    Logger.getLogger(CobraNetwork.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    private static void startClientHandler(Socket s) {
        try {
            DataInputStream dis = new DataInputStream(s.getInputStream());
            for (ActionListener al : actionListeners) {
                al.actionPerformed(new ActionEvent("c", 0, null));
            }
            new Thread(() -> {
                System.out.println("Client handler initialized for " + s.getInetAddress().getHostAddress() + "...");
                while (true) {
                    try {
                        int dataLength = dis.available();
                        if (dataLength > 0) {
                            System.out.println("Listening for incoming object from client...");
                            byte[] bytes = new byte[dataLength];
                            dis.readFully(bytes);
                            for (ActionListener al : actionListeners) {
                                al.actionPerformed(new ActionEvent(deserialize(bytes), 0, "client"));
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(CobraNetwork.class.getName()).log(Level.SEVERE, null, ex);
//                    } catch (ClassNotFoundException ex) {
//                        Logger.getLogger(CobraNetwork.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (Exception ex) {
                        Logger.getLogger(CobraNetwork.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }).start();
        } catch (Exception e) {
            Logger.getLogger(CobraNetwork.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private static boolean connect() {
        DataInputStream dis;
        try {
            Socket socketToServer = new Socket(ip, port);
            isServer = false;
            connections.add(socketToServer);
            dis = new DataInputStream(socketToServer.getInputStream());
            System.out.println("This client is connected.");
            connectionMap.put(socketToServer, null);
            isConnected = true;
        } catch (IOException ex) {
//            Logger.getLogger(CobraNetwork.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        new Thread(() -> {
            while (true) {
                try {
                    int dataLength = dis.available();
                    if (dataLength > 0) {
                        byte[] bytes = new byte[dataLength];
                        dis.readFully(bytes);
                        for (ActionListener al : actionListeners) {
                            al.actionPerformed(new ActionEvent(deserialize(bytes), 0, "server"));
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(CobraNetwork.class.getName()).log(Level.SEVERE, null, ex);
                    continue;
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(CobraNetwork.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(CobraNetwork.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
        return true;
    }

    public static boolean isServer() {
        return isServer;
    }

    private static byte[] serialize(Object o) throws Exception {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(o);
            return bos.toByteArray();
        }
    }

    private static Object deserialize(byte[] b) throws Exception {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(b);
                ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        }
    }

    public static void close() {
        for (Socket connection : connections) {
            try {
                connection.close();
            } catch (IOException ex) {
                Logger.getLogger(CobraNetwork.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
