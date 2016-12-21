package game;

import java.io.Serializable;

public class Packet implements Serializable {
    private byte ID;
    private byte Desk[][];
    private Checker[] MyCheckers;
    private Checker[] EnemyCheckers;
    private int DragIndex;
    private boolean WantPlayMore;
    private boolean GameStarted = false;
    private boolean GameEnd = false;
    private boolean YourTurn = false;
    private boolean surrender = false;
    private boolean draw = false;
    private boolean drawAccepted = false;
    private boolean ConnectionLost = false;

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
            MyCheckers[i].x = Game.getCellSize()*8 - MyCheckers[i].x;
            MyCheckers[i].y = Game.getCellSize()*8 - MyCheckers[i].y;    

            EnemyCheckers[i].i = (byte) (7 - EnemyCheckers[i].i);
            EnemyCheckers[i].j = (byte) (7 - EnemyCheckers[i].j);
            EnemyCheckers[i].x = Game.getCellSize()*8 - EnemyCheckers[i].x;
            EnemyCheckers[i].y = Game.getCellSize()*8 - EnemyCheckers[i].y;
        }

        for (int i = 0; i <= 7; i++) {
            for (int j = 0; j <= 7; j++) {
                tmp2[7 - i][7 - j] = Desk[i][j];
            }
        }

        Desk = tmp2;

    }

    public byte[][] getDesk() {
        return Desk;
    }

    public void setDesk(byte[][] Desk) {
        this.Desk = Desk;
    }

    public Checker[] getMyCheckers() {
        return MyCheckers;
    }

    public void setMyCheckers(Checker[] MyCheckers) {
        this.MyCheckers = MyCheckers;
    }

    public Checker[] getEnemyCheckers() {
        return EnemyCheckers;
    }

    public void setEnemyCheckers(Checker[] EnemyCheckers) {
        this.EnemyCheckers = EnemyCheckers;
    }

    public int getDragIndex() {
        return DragIndex;
    }

    public void setDragIndex(int DragIndex) {
        this.DragIndex = DragIndex;
    }

    public boolean isWantPlayMore() {
        return WantPlayMore;
    }

    public void setWantPlayMore(boolean WantPlayMore) {
        this.WantPlayMore = WantPlayMore;
    }

    public boolean isGameStarted() {
        return GameStarted;
    }

    public void setGameStarted(boolean GameStarted) {
        this.GameStarted = GameStarted;
    }

    public boolean isGameEnd() {
        return GameEnd;
    }

    public void setGameEnd(boolean GameEnd) {
        this.GameEnd = GameEnd;
    }

    public boolean isYourTurn() {
        return YourTurn;
    }

    public void setYourTurn(boolean YourTurn) {
        this.YourTurn = YourTurn;
    }

    public boolean isSurrender() {
        return surrender;
    }

    public void setSurrender(boolean surrender) {
        this.surrender = surrender;
    }

    public boolean isDraw() {
        return draw;
    }

    public void setDraw(boolean draw) {
        this.draw = draw;
    }

    public boolean isDrawAccepted() {
        return drawAccepted;
    }


    public void setDrawAccepted(boolean drawAccepted) {
        this.drawAccepted = drawAccepted;
    }

    public boolean isConnectionLost() {
        return ConnectionLost;
    }

    public void setConnectionLost(boolean ConnectionLost) {
        this.ConnectionLost = ConnectionLost;
    }
}
