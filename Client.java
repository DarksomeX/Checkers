package game;

import java.io.*;
import java.net.*;
import javax.swing.JFrame;

public class Client {

    private static final String IP = "109.87.44.189";
    private final ObjectInputStream FromServer;
    private final ObjectOutputStream ToServer;

    public Client(ObjectInputStream FromServer, ObjectOutputStream ToServer) {
        this.FromServer = FromServer;
        this.ToServer = ToServer;
    }

    public synchronized void Awake() {
        this.notify();
    }

    public synchronized void GameThread() throws InterruptedException, IOException, ClassNotFoundException {
        while (true) {
            Packet p;
            
            p = (Packet) FromServer.readObject();
            byte PlayerID = p.getID();

            p = (Packet) FromServer.readObject();
            if (p.isGameStarted()) {
                JFrame jf = new JFrame();
                jf.setTitle("Checkers");
                Game g = new Game(PlayerID, FromServer, ToServer, this);
                jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                jf.setResizable(false);
                jf.setSize((int)(Game.getCellSize()*9.1)+6,Game.getCellSize()*8+29);
                jf.setLocationRelativeTo(null);

                jf.add(g);
                jf.setVisible(true);
                this.wait();
                jf.dispose();
                if (g.isConnectionLost()) {
                    break;
                }
                if (g.isWantToPlayMore() == false) {
                    break;
                }

            }
        }

    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

        try (Socket server = new Socket(IP, 4444)) {
            System.out.println("Welcome to Client side");
            System.out.println("Connecting to... 109.87.44.189");
            
            try (ObjectOutputStream ToServer = new ObjectOutputStream(server.getOutputStream()); 
                    ObjectInputStream FromServer = new ObjectInputStream(server.getInputStream())) {
                
                Client c = new Client(FromServer, ToServer);
                c.GameThread();
                
            }
        }

    }
}
