package game;

import java.io.*;
import java.net.*;

public class Server {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        System.out.println("Welcome to Server side");
        Packet p = null;
        boolean b = false;
        ServerSocket servers = null;

        /*Socket player1 = null;
        Socket player2 = null;*/
        int CurrentPlayer = 1;
        boolean GameEnd = false;
        /*ObjectOutputStream ToPlayer1 = null;
        ObjectInputStream FromPlayer1 = null;
        ObjectOutputStream ToPlayer2 = null;
        ObjectInputStream FromPlayer2 = null;*/

        servers = new ServerSocket(4444);

       }
}
