package game;

import java.io.Serializable;

public class Packet implements Serializable {

    byte Desk[][];
    public Checker[] MyCheckers;
    public Checker[] EnemyCheckers;
    private byte ID;
    int DragIndex;
    boolean WantPlayMore;
    boolean GameStarted = false;
    boolean GameEnd = false;
    boolean YourTurn = false;
    boolean surrender = false;
    boolean draw = false;
    boolean drawAccepted = false;
    boolean ConnectionLost = false;

    public Packet(byte ID) {
        this.ID = ID;
    }

    public Packet() {

    }

    public Packet(boolean start) {
        if (start) {
            GameStarted = true;
            YourTurn = true;
        }
    }

    public Packet(Checker[] MyCheckers, Checker[] EnemyCheckers, byte[][] Desk, int DragIndex) {
        this.MyCheckers = new Checker[12];
        this.EnemyCheckers = new Checker[12];
        this.Desk = new byte[8][8];
        this.DragIndex = DragIndex;
        for (int i = 0; i <= 7; i++) {
            for (int j = 0; j <= 7; j++) {
                this.Desk[i][j] = Desk[i][j];
            }
        }

        for (int i = 0; i <= 11; i++) {
            this.MyCheckers[i] = new Checker();
            this.MyCheckers[i].exist = MyCheckers[i].exist;
            this.MyCheckers[i].isKing = MyCheckers[i].isKing;
            this.MyCheckers[i].i = MyCheckers[i].i;
            this.MyCheckers[i].j = MyCheckers[i].j;
            this.MyCheckers[i].x = MyCheckers[i].x;
            this.MyCheckers[i].y = MyCheckers[i].y;
            this.EnemyCheckers[i] = new Checker();
            this.EnemyCheckers[i].exist = EnemyCheckers[i].exist;
            this.EnemyCheckers[i].isKing = EnemyCheckers[i].isKing;
            this.EnemyCheckers[i].i = EnemyCheckers[i].i;
            this.EnemyCheckers[i].j = EnemyCheckers[i].j;
            this.EnemyCheckers[i].x = EnemyCheckers[i].x;
            this.EnemyCheckers[i].y = EnemyCheckers[i].y;
        }
    }

    public byte getID() {
        return ID;
    }

    public void setID(byte ID) {
        this.ID = ID;
    }

    public void PerspectiveSwap() {
        Checker[] tmp;
        byte[][] tmp2 = new byte[8][8];
        tmp = MyCheckers;
        MyCheckers = EnemyCheckers;
        EnemyCheckers = tmp;
        for (int i = 0; i <= 11; i++) {
            MyCheckers[i].i = (byte) (7 - MyCheckers[i].i);
            MyCheckers[i].j = (byte) (7 - MyCheckers[i].j);
            MyCheckers[i].x = 800 - MyCheckers[i].x;
            MyCheckers[i].y = 800 - MyCheckers[i].y;

            EnemyCheckers[i].i = (byte) (7 - EnemyCheckers[i].i);
            EnemyCheckers[i].j = (byte) (7 - EnemyCheckers[i].j);
            EnemyCheckers[i].x = 800 - EnemyCheckers[i].x;
            EnemyCheckers[i].y = 800 - EnemyCheckers[i].y;
        }

        for (int i = 0; i <= 7; i++) {
            for (int j = 0; j <= 7; j++) {
                tmp2[7 - i][7 - j] = Desk[i][j];
            }
        }

        Desk = tmp2;

    }
}
