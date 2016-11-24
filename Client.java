package game;

import java.io.*;
import java.net.*;
import javax.swing.JFrame;

public class Client {

    Socket server;
    ObjectInputStream FromServer;
    ObjectOutputStream ToServer;

    public Client(Socket server, ObjectInputStream FromServer, ObjectOutputStream ToServer) {
        this.server = server;
        this.FromServer = FromServer;
        this.ToServer = ToServer;
    }

    public synchronized void Awake() {
        this.notify();
    }

    public synchronized void GameThread() throws InterruptedException, IOException, ClassNotFoundException {
        boolean FirstTime = true;
        while (true) {
            Packet p = null;
            
            p = (Packet) FromServer.readObject();
            /*if((p.WantPlayMore == false)&&FirstTime == false){
                break;
            }
            FirstTime = false;*/
            byte PlayerID = p.getID();

            p = (Packet) FromServer.readObject();
            if (p.GameStarted) {
                JFrame jf = new JFrame();
                jf.setTitle("Checkers");
                Game g = new Game(PlayerID, server, FromServer, ToServer, this);
                jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                jf.setResizable(false);
                jf.setSize(806 + g.size + 10, 829);
                jf.setLocationRelativeTo(null);

                jf.add(g);
                jf.setVisible(true);
                this.wait();
                jf.dispose();
                if (g.ConnectionLost) {
                    break;
                }
                if (g.WantToPlayMore == false) {
                    break;
                }

            }
        }

    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

        Socket server = new Socket("109.87.44.189", 4444);

        System.out.println("Welcome to Client side");
        System.out.println("Connecting to... 109.87.44.189");

        ObjectOutputStream ToServer = new ObjectOutputStream(server.getOutputStream());
        ObjectInputStream FromServer = new ObjectInputStream(server.getInputStream());

        Client c = new Client(server, FromServer, ToServer);
        c.GameThread();

        FromServer.close();
        ToServer.close();
        server.close();

    }
}
