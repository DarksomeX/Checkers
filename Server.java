package game;

import java.io.*;
import java.net.*;

public class Server {

    static Packet p;
    static ServerSocket servers;

    static Socket player1;
    static Socket player2;
    static int CurrentPlayer = 1;
    static boolean GameEnd = false;
    static ObjectOutputStream ToPlayer1;
    static ObjectInputStream FromPlayer1;
    static ObjectOutputStream ToPlayer2;
    static ObjectInputStream FromPlayer2;

    public static void main(String[] args) throws IOException, ClassNotFoundException, EOFException {
        System.out.println("Welcome to Server side");


        servers = new ServerSocket(4444);

        while (true) {
            try{
            player1 = servers.accept();
            ToPlayer1 = new ObjectOutputStream(player1.getOutputStream());
            FromPlayer1 = new ObjectInputStream(player1.getInputStream());
            p = new Packet((byte) 1);
            ToPlayer1.writeObject(p);
            player2 = servers.accept();
            ToPlayer2 = new ObjectOutputStream(player2.getOutputStream());
            FromPlayer2 = new ObjectInputStream(player2.getInputStream());
            p = new Packet((byte) 2);
            ToPlayer2.writeObject(p);

            p = new Packet(true);
            ToPlayer1.writeObject(p);
            p.setYourTurn(false);
            ToPlayer2.writeObject(p);
            }catch(IOException ex){
                continue;
            }

            while (true) {
                try {

                    if (CurrentPlayer == 1) {
                        while (true) {
                            p = (Packet) FromPlayer1.readObject();
                            if (p.isGameEnd()) {
                                GameEnd = true;
                                p.PerspectiveSwap();
                                ToPlayer2.writeObject(p);
                                break;
                            }
                            if (p.isDraw()) {
                                p.PerspectiveSwap();
                                ToPlayer2.writeObject(p);
                                p = (Packet) FromPlayer2.readObject();
                                p.PerspectiveSwap();
                                if (p.isDrawAccepted()) {
                                    GameEnd = true;
                                    ToPlayer1.writeObject(p);
                                    break;
                                } else {
                                    ToPlayer1.writeObject(p);
                                    continue;
                                }
                            }
                            if (p.isSurrender()) {

                                GameEnd = true;
                                p.PerspectiveSwap();
                                ToPlayer2.writeObject(p);
                                break;
                            }

                            if (p.isYourTurn()) {
                                CurrentPlayer = 2;
                                p.PerspectiveSwap();
                                ToPlayer2.writeObject(p);
                                break;

                            }
                            p.PerspectiveSwap();
                            ToPlayer2.writeObject(p);
                        }
                    } else {
                        while (true) {
                            p = (Packet) FromPlayer2.readObject();
                            if (p.isGameEnd()) {
                                GameEnd = true;
                                p.PerspectiveSwap();
                                ToPlayer1.writeObject(p);
                                CurrentPlayer = 1;
                                break;
                            }
                            if (p.isDraw()) {
                                p.PerspectiveSwap();
                                ToPlayer1.writeObject(p);
                                p = (Packet) FromPlayer1.readObject();
                                if (p.isDrawAccepted()) {
                                    GameEnd = true;
                                    ToPlayer2.writeObject(p);
                                    CurrentPlayer = 1;
                                    break;
                                } else {
                                    ToPlayer2.writeObject(p);
                                    continue;
                                }
                            }
                            if (p.isSurrender()) {
                                GameEnd = true;
                                p.PerspectiveSwap();
                                ToPlayer1.writeObject(p);
                                CurrentPlayer = 1;
                                break;
                            }

                            if (p.isYourTurn()) {
                                CurrentPlayer = 1;
                                p.PerspectiveSwap();
                                ToPlayer1.writeObject(p);
                                break;
                            }
                            p.PerspectiveSwap();
                            ToPlayer1.writeObject(p);
                        }
                    }
                    if (GameEnd) {
                        GameEnd = false;
                        p = (Packet) FromPlayer1.readObject();
                        if (p.isWantPlayMore() == false) {
                            CurrentPlayer = 1;
                            ToPlayer1.writeObject(p);
                            player1.close();
                            player2.close();
                            break;
                        }
                        p = (Packet) FromPlayer2.readObject();
                        if (p.isWantPlayMore() == false) {
                            CurrentPlayer = 1;
                            ToPlayer2.writeObject(p);
                            player1.close();
                            player2.close();
                            break;
                        }

                        p = new Packet((byte) 1);
                        ToPlayer1.writeObject(p);
                        p = new Packet((byte) 2);
                        ToPlayer2.writeObject(p);

                        p = new Packet(true);
                        ToPlayer1.writeObject(p);
                        p.setYourTurn(false);
                        ToPlayer2.writeObject(p);
                    }
                } catch (IOException | ClassNotFoundException  ex) {
                    Packet pck = new Packet();
                    pck.setConnectionLost(true);
                    try {
                        ToPlayer1.writeObject(pck);
                    } catch (IOException e) {
                        try {
                            ToPlayer2.writeObject(pck);
                        } catch (IOException e1) {

                            break;
                        }
                        break;
                    }

                    try {
                        ToPlayer2.writeObject(pck);
                    } catch (IOException e) {
                        try {
                            ToPlayer1.writeObject(pck);
                        } catch (IOException e1) {

                            break;
                        }
                        break;
                    }
                    player1.close();
                    player2.close();
                    break;
                }
            }
        }
    }
}
