package game;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.Math.sqrt;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Game extends JPanel implements MouseListener, MouseMotionListener {

    private final BufferedImage Button1;
    private final BufferedImage Button2;
    private final BufferedImage Checker1;
    private final BufferedImage Checker2;
    private final BufferedImage King1;
    private final BufferedImage King2;
    private final BufferedImage DeskImg;
    private final BufferedImage Side;
    
    private final Client client;
    private final byte[][] Desk;
    private boolean MyTurn = false;

    private static final short CELL_SIZE = 75;

    private byte MyPlayerID = -1;
    private byte EnemyPlayerID = -1;
    private boolean AllowToDrag = false;
    private int DragIndex = -1;
    private final Checker[] MyCheckers;
    private final Checker[] EnemyCheckers;
    private int Move0Attack1 = -1;
    private final Checker[] Dying;
    private int sizeDying = 0;
    private boolean surrender = false;
    private boolean EnemySurrendered = false;
    private boolean draw = false;
    private boolean drawAccepted = false;
    private boolean FullGameEnd = false;
    private boolean GameEnd = false;
    private boolean IWin = false;
    private int playerWin = 0;
    private boolean WantToPlayMore;
    private boolean RefusedDraw = false;
    private boolean ConnectionLost = false;

    private final ObjectInputStream FromServer;
    private final ObjectOutputStream ToServer;
    private Packet p;

    private void SetDefaultCheckersPosition(byte player) {
        byte shift = 0;
        if (player == MyPlayerID) {
            shift = 5;
        }
        for (int i = 0; i <= 7; i++) {
            for (int j = 0 + shift; j <= 2 + shift; j++) {
                if ((i + j) % 2 == 1) {
                    Desk[i][j] = player;
                }
            }
        }

    }

    private void SetDefaultCheckersCoordinates() {
        int mysize = 0;
        int enemysize = 0;
        for (int i = 0; i <= 7; i++) {
            for (int j = 0; j <= 2; j++) {
                if ((i + j) % 2 == 1) {
                    EnemyCheckers[enemysize] = new Checker((i * CELL_SIZE) + CELL_SIZE / 2, (j * CELL_SIZE) + CELL_SIZE / 2, (byte) i, (byte) j);
                    enemysize++;
                }
            }
        }

        for (int i = 0; i <= 7; i++) {
            for (int j = 5; j <= 7; j++) {
                if ((i + j) % 2 == 1) {
                    MyCheckers[mysize] = new Checker((i * CELL_SIZE) + CELL_SIZE / 2, (j * CELL_SIZE) + CELL_SIZE / 2, (byte) i, (byte) j);
                    mysize++;
                }
            }
        }

    }

    public Game(byte player, ObjectInputStream FromServer, ObjectOutputStream ToServer, Client client) throws IOException {

        Button1 = ImageIO.read(getClass().getResource("/resources/button1.jpg"));
        Button2 = ImageIO.read(getClass().getResource("/resources/button2.jpg"));
        Checker1 = ImageIO.read(getClass().getResource("/resources/Checker1.png"));
        Checker2 = ImageIO.read(getClass().getResource("/resources/Checker2.png"));
        King1 = ImageIO.read(getClass().getResource("/resources/King1.png"));
        King2 = ImageIO.read(getClass().getResource("/resources/King2.png"));
        DeskImg = ImageIO.read(getClass().getResource("/resources/Desk.jpg"));
        Side = ImageIO.read(getClass().getResource("/resources/Side.jpg"));

        this.client = client;
        JPanel panel = this;
        JButton surrenderButton = new JButton();
        JButton drawButton = new JButton();

        surrenderButton.setBounds((int) (CELL_SIZE * 8.1), CELL_SIZE * 3, CELL_SIZE, CELL_SIZE);
        drawButton.setBounds((int) (CELL_SIZE * 8.1), CELL_SIZE * 4, CELL_SIZE, CELL_SIZE);

        drawButton.setIcon(new ImageIcon(Button2));

        surrenderButton.setIcon(new ImageIcon(Button1));

        surrenderButton.addActionListener((ActionEvent e) -> {
            if (MyTurn) {
                surrender = true;
                GameEnd = true;
                playerWin = EnemyPlayerID;
                repaint();
                SendPacket();
            }
        });

        drawButton.addActionListener((ActionEvent e) -> {
            if (MyTurn) {
                draw = true;
                
                SendPacket();
                Packet packet = null;
                JOptionPane.showMessageDialog(panel, "Waiting answer...");
                try {
                    packet = (Packet) FromServer.readObject();
                    
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                }
                if ((packet != null) && packet.isDrawAccepted()) {
                    GameEnd = true;
                    drawAccepted = true;
                } else {
                    JOptionPane.showMessageDialog(panel, "Opponent refused...");
                    
                }
                repaint();
            }
        });
        this.FromServer = FromServer;
        this.ToServer = ToServer;
        MyCheckers = new Checker[12];
        EnemyCheckers = new Checker[12];
        Dying = new Checker[12];
        Desk = new byte[8][8];
        MyPlayerID = player;
        if (MyPlayerID == 1) {
            EnemyPlayerID = 2;
            MyTurn = true;
        } else {
            EnemyPlayerID = 1;
        }

        SetDefaultCheckersPosition(MyPlayerID);
        SetDefaultCheckersPosition(EnemyPlayerID);
        SetDefaultCheckersCoordinates();
        InitPanel(surrenderButton,drawButton);
    }
    
    private void InitPanel(JButton a, JButton b){
        this.setLayout(null);
        this.add(a);
        this.add(b);
        addMouseMotionListener(this);
        addMouseListener(this);
    }

    private void paintCheckers(Graphics g) {
        BufferedImage MyChecker;
        BufferedImage EnemyChecker;
        BufferedImage MyKing;
        BufferedImage EnemyKing;

        if (MyPlayerID == 1) {
            MyChecker = Checker1;
            EnemyChecker = Checker2;
            MyKing = King1;
            EnemyKing = King2;

        } else {
            MyChecker = Checker2;
            EnemyChecker = Checker1;
            MyKing = King2;
            EnemyKing = King1;

        }

        for (int i = 0; i <= 11; i++) {

            if (EnemyCheckers[i].exist) {
                if (EnemyCheckers[i].isKing) {

                    g.drawImage(EnemyKing, EnemyCheckers[i].x - CELL_SIZE / 2, EnemyCheckers[i].y - CELL_SIZE / 2, this);
                } else {
                    g.drawImage(EnemyChecker, EnemyCheckers[i].x - CELL_SIZE / 2, EnemyCheckers[i].y - CELL_SIZE / 2, this);
                }

            }
        }

        for (int i = 0; i <= 11; i++) {

            if (MyCheckers[i].exist) {
                if (MyCheckers[i].isKing) {

                    g.drawImage(MyKing, MyCheckers[i].x - CELL_SIZE / 2, MyCheckers[i].y - CELL_SIZE / 2, this);
                } else {
                    g.drawImage(MyChecker, MyCheckers[i].x - CELL_SIZE / 2, MyCheckers[i].y - CELL_SIZE / 2, this);
                }

            }
        }

        if (DragIndex != -1) {
            if (MyTurn == false) {

                if (EnemyCheckers[DragIndex].isKing) {

                    g.drawImage(EnemyKing, EnemyCheckers[DragIndex].x - CELL_SIZE / 2, EnemyCheckers[DragIndex].y - CELL_SIZE / 2, this);
                } else {
                    g.drawImage(EnemyChecker, EnemyCheckers[DragIndex].x - CELL_SIZE / 2, EnemyCheckers[DragIndex].y - CELL_SIZE / 2, this);
                }

            } else {

                if (MyCheckers[DragIndex].isKing) {

                    g.drawImage(MyKing, MyCheckers[DragIndex].x - CELL_SIZE / 2, MyCheckers[DragIndex].y - CELL_SIZE / 2, this);
                } else {
                    g.drawImage(MyChecker, MyCheckers[DragIndex].x - CELL_SIZE / 2, MyCheckers[DragIndex].y - CELL_SIZE / 2, this);
                }
            }
        }

    }

    private void paintDesk(Graphics g) {
        g.drawImage(DeskImg, 0, 0, this);
        g.drawImage(Side, CELL_SIZE * 8, 0, this);
    }

    private void EndGameWindow() {

        String str;
        if (playerWin == MyPlayerID) {
            str = "You Win";
        } else {
            str = "Enemy Win";
        }
        if (drawAccepted) {
            str = "Draw";
        }
        if (EnemySurrendered) {
            str = "Enemy Surrendered";
        }
        if (surrender) {
            str = "You Surrendered";
        }

        int input = JOptionPane.showConfirmDialog(this, "Do you want to play more?", str, JOptionPane.YES_NO_OPTION);
        if (input == JOptionPane.YES_OPTION) {
            WantToPlayMore = true;
            SendPacket();
            client.Awake();

        }
        if ((input == JOptionPane.NO_OPTION) || (input == JOptionPane.CLOSED_OPTION)) {
            WantToPlayMore = false;

            SendPacket();
            client.Awake();

        }
    }

    @Override
    public void paintComponent(Graphics g) {

        paintDesk(g);
        paintCheckers(g);

        if (GameEnd) {
            if (FullGameEnd == false) {
                EndGameWindow();
            } else {
                FullGameEnd = false;
                repaint();
            }
        } else {
            if (MyTurn) {
                p = new Packet(MyCheckers, EnemyCheckers, Desk, DragIndex);
                try {
                    ToServer.writeObject(p);
                } catch (IOException ex) {
                    Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            if (MyTurn == false) {
                try {
                    p = (Packet) FromServer.readObject();
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (p.isConnectionLost()) {
                    JOptionPane.showMessageDialog(this, "Enemy disconnected");
                    ConnectionLost = true;
                    client.Awake();
                }

                DragIndex = p.getDragIndex();

                for (int i = 0; i <= 11; i++) {
                    this.MyCheckers[i].exist = p.getMyCheckers()[i].exist;
                    this.MyCheckers[i].isKing = p.getMyCheckers()[i].isKing;
                    this.MyCheckers[i].Dying = p.getMyCheckers()[i].Dying;
                    this.MyCheckers[i].i = p.getMyCheckers()[i].i;
                    this.MyCheckers[i].j = p.getMyCheckers()[i].j;
                    this.MyCheckers[i].x = p.getMyCheckers()[i].x;
                    this.MyCheckers[i].y = p.getMyCheckers()[i].y;
                    this.EnemyCheckers[i].exist = p.getEnemyCheckers()[i].exist;
                    this.EnemyCheckers[i].isKing = p.getEnemyCheckers()[i].isKing;
                    this.EnemyCheckers[i].i = p.getEnemyCheckers()[i].i;
                    this.EnemyCheckers[i].j = p.getEnemyCheckers()[i].j;
                    this.EnemyCheckers[i].x = p.getEnemyCheckers()[i].x;
                    this.EnemyCheckers[i].y = p.getEnemyCheckers()[i].y;
                }

                for (int i = 0; i <= 7; i++) {
                    for (int j = 0; j <= 7; j++) {
                        Desk[i][j] = p.getDesk()[i][j];
                    }
                }

                if (p.isGameEnd()) {
                    FullGameEnd = true;
                    GameEnd = true;
                    playerWin = EnemyPlayerID;
                    repaint();
                    return;
                }
                if (p.isSurrender()) {
                    GameEnd = true;
                    playerWin = MyPlayerID;
                    EnemySurrendered = true;
                    repaint();

                    return;
                }

                if (p.isDraw()) {
                    int input = JOptionPane.showConfirmDialog(this, "Your opponent offers a draw. Do you agree?", "Offer a draw", JOptionPane.YES_NO_OPTION);
                    if ((input == JOptionPane.NO_OPTION) || (input == JOptionPane.YES_NO_CANCEL_OPTION)) {
                        JOptionPane.getRootFrame().dispose();
                        RefusedDraw = true;
                        paintDesk(g);
                        paintCheckers(g);
                        SendPacket();

                    }
                    if (input == JOptionPane.YES_OPTION) {
                        JOptionPane.getRootFrame().dispose();
                        GameEnd = true;
                        drawAccepted = true;
                        paintDesk(g);
                        paintCheckers(g);
                        SendPacket();
                        EndGameWindow();
                        return;
                    }
                }
                if (MyLose()) {
                    GameEnd = true;
                    playerWin = EnemyPlayerID;
                    repaint();
                    return;
                } else {
                    if (p.isYourTurn()) {
                        MyTurn = true;
                    }
                }
                repaint();
            }
        }
    
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (GameEnd != true) {
            if (MyTurn) {
                int x = e.getX();
                int y = e.getY();
                if (getCellfromCoordinates(x, y)) {
                    for (int i = 0; i <= 11; i++) {
                        if (MyCheckers[i].exist) {
                            int x1 = MyCheckers[i].x;
                            int y1 = MyCheckers[i].y;
                            double res = sqrt(Math.pow(x1 - x, 2) + Math.pow(y1 - y, 2));
                            if ((res < (CELL_SIZE - 20) / 2)) {
                                AllowToDrag = true;
                                DragIndex = i;
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    byte DeskX;
    byte DeskY;

    private boolean getCellfromCoordinates(int x, int y) {
        for (int i = 0; i <= 7; i++) {
            for (int j = 0; j <= 7; j++) {
                if ((x > i * CELL_SIZE) && (x < i * CELL_SIZE + CELL_SIZE) && (y > j * CELL_SIZE) && (y < j * CELL_SIZE + CELL_SIZE)) {
                    DeskX = (byte) i;
                    DeskY = (byte) j;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean CellIsDark() {
        return ((DeskX + DeskY) % 2) == 1;
    }

    private boolean CellIsFree() {
        return Desk[DeskX][DeskY] == 0;
    }

    private void Dying(int DeskX, int DeskY) {
        for (int i = 0; i <= 11; i++) {
            if (EnemyCheckers[i].exist) {
                if ((EnemyCheckers[i].i == DeskX) && (EnemyCheckers[i].j == DeskY)) {
                    Desk[DeskX][DeskY] = (byte) -EnemyPlayerID;
                    EnemyCheckers[i].Dying = true;
                    Dying[sizeDying] = EnemyCheckers[i];
                    sizeDying++;
                    break;
                }
            }
        }
    }

    private void Die() {

        for (int i = 0; i <= sizeDying - 1; i++) {
            Desk[Dying[i].i][Dying[i].j] = 0;
            Dying[i].exist = false;
            Dying[i].Dying = false;
            Dying[i].x = 1000;
            Dying[i].y = 1000;
            Dying[i].i = -1;
            Dying[i].j = -1;

        }
    }

    int kx = 0;
    int ky = 0;

    private boolean isRightDirection() {
        if ((DeskX < MyCheckers[DragIndex].i) && (DeskY < MyCheckers[DragIndex].j)
                && (DeskX - DeskY == MyCheckers[DragIndex].i - MyCheckers[DragIndex].j)) {
            kx = 1;
            ky = 1;
            return true;
        }
        if ((DeskX > MyCheckers[DragIndex].i) && (DeskY > MyCheckers[DragIndex].j)
                && (DeskX - DeskY == MyCheckers[DragIndex].i - MyCheckers[DragIndex].j)) {
            kx = -1;
            ky = -1;
            return true;
        }
        if ((DeskX > MyCheckers[DragIndex].i) && (DeskY < MyCheckers[DragIndex].j)
                && (DeskX + DeskY == MyCheckers[DragIndex].i + MyCheckers[DragIndex].j)) {
            kx = -1;
            ky = 1;
            return true;
        }
        if ((DeskX < MyCheckers[DragIndex].i) && (DeskY > MyCheckers[DragIndex].j)
                && (DeskX + DeskY == MyCheckers[DragIndex].i + MyCheckers[DragIndex].j)) {
            kx = 1;
            ky = -1;
            return true;

        }
        return false;
    }

    private boolean AttackExist(int i) {
        if (MyCheckers[i].exist) {
            if (MyCheckers[i].isKing) {
                int dist = 1;
                while (true) {
                    if ((MyCheckers[i].i + (dist + 1) < 0) || (MyCheckers[i].i + (dist + 1) > 7) || (MyCheckers[i].j + (dist + 1) < 0) || (MyCheckers[i].j + (dist + 1) > 7)) {
                        break;
                    }
                    if ((Desk[MyCheckers[i].i + dist][MyCheckers[i].j + dist] == MyPlayerID) || (Desk[MyCheckers[i].i + dist][MyCheckers[i].j + dist] == -EnemyPlayerID)) {
                        break;
                    }

                    if (Desk[MyCheckers[i].i + dist][MyCheckers[i].j + dist] == EnemyPlayerID) {
                        if ((Desk[MyCheckers[i].i + (dist + 1)][MyCheckers[i].j + (dist + 1)] != 0)) {
                            break;
                        }
                        return true;
                    }
                    dist++;
                }

                dist = 1;
                while (true) {
                    if ((MyCheckers[i].i - (dist + 1) < 0) || (MyCheckers[i].i - (dist + 1) > 7) || (MyCheckers[i].j - (dist + 1) < 0) || (MyCheckers[i].j - (dist + 1) > 7)) {

                        break;
                    }
                    if ((Desk[MyCheckers[i].i - dist][MyCheckers[i].j - dist] == MyPlayerID) || (Desk[MyCheckers[i].i - dist][MyCheckers[i].j - dist] == -EnemyPlayerID)) {
                        break;
                    }

                    if (Desk[MyCheckers[i].i - dist][MyCheckers[i].j - dist] == EnemyPlayerID) {
                        if ((Desk[MyCheckers[i].i - (dist + 1)][MyCheckers[i].j - (dist + 1)] != 0)) {
                            break;
                        }
                        return true;
                    }
                    dist++;
                }

                dist = 1;
                while (true) {
                    if ((MyCheckers[i].i + (dist + 1) < 0) || (MyCheckers[i].i + (dist + 1) > 7) || (MyCheckers[i].j - (dist + 1) < 0) || (MyCheckers[i].j - (dist + 1) > 7)) {

                        break;
                    }
                    if ((Desk[MyCheckers[i].i + dist][MyCheckers[i].j - dist] == MyPlayerID) || (Desk[MyCheckers[i].i + dist][MyCheckers[i].j - dist] == -EnemyPlayerID)) {
                        break;
                    }

                    if (Desk[MyCheckers[i].i + dist][MyCheckers[i].j - dist] == EnemyPlayerID) {
                        if ((Desk[MyCheckers[i].i + (dist + 1)][MyCheckers[i].j - (dist + 1)] != 0)) {
                            break;
                        }
                        return true;
                    }
                    dist++;
                }

                dist = 1;
                while (true) {
                    if ((MyCheckers[i].i - (dist + 1) < 0) || (MyCheckers[i].i - (dist + 1) > 7) || (MyCheckers[i].j + (dist + 1) < 0) || (MyCheckers[i].j + (dist + 1) > 7)) {

                        break;
                    }
                    if ((Desk[MyCheckers[i].i - dist][MyCheckers[i].j + dist] == MyPlayerID) || (Desk[MyCheckers[i].i - dist][MyCheckers[i].j + dist] == -EnemyPlayerID)) {
                        break;
                    }

                    if (Desk[MyCheckers[i].i - dist][MyCheckers[i].j + dist] == EnemyPlayerID) {
                        if ((Desk[MyCheckers[i].i - (dist + 1)][MyCheckers[i].j + (dist + 1)] == MyPlayerID) || (Desk[MyCheckers[i].i - (dist + 1)][MyCheckers[i].j + (dist + 1)] == -EnemyPlayerID) || (Desk[MyCheckers[i].i - (dist + 1)][MyCheckers[i].j + (dist + 1)] == EnemyPlayerID)) {
                            break;
                        }

                        return true;
                    }
                    dist++;
                }

            } else {
                if ((MyCheckers[i].i - 1 >= 0) && (MyCheckers[i].j - 1 >= 0) && (MyCheckers[i].i - 1 <= 7) && (MyCheckers[i].j - 1 <= 7)) {
                    if (Desk[MyCheckers[i].i - 1][MyCheckers[i].j - 1] == EnemyPlayerID) {
                        if ((MyCheckers[i].i - 2 >= 0) && (MyCheckers[i].j - 2 >= 0) && (MyCheckers[i].i - 2 <= 7) && (MyCheckers[i].j - 2 <= 7) && (Desk[MyCheckers[i].i - 2][MyCheckers[i].j - 2] == 0)) {
                            if (Desk[MyCheckers[i].i - 2][MyCheckers[i].j - 2] == 0) {
                                return true;
                            }
                        }
                    }
                }

                if ((MyCheckers[i].i + 1 >= 0) && (MyCheckers[i].j + 1 >= 0) && (MyCheckers[i].i + 1 <= 7) && (MyCheckers[i].j + 1 <= 7)) {
                    if (Desk[MyCheckers[i].i + 1][MyCheckers[i].j + 1] == EnemyPlayerID) {
                        if ((MyCheckers[i].i + 2 >= 0) && (MyCheckers[i].j + 2 >= 0) && (MyCheckers[i].i + 2 <= 7) && (MyCheckers[i].j + 2 <= 7) && (Desk[MyCheckers[i].i + 2][MyCheckers[i].j + 2] == 0)) {
                            if (Desk[MyCheckers[i].i + 2][MyCheckers[i].j + 2] == 0) {
                                return true;
                            }
                        }
                    }
                }

                if ((MyCheckers[i].i - 1 >= 0) && (MyCheckers[i].j + 1 >= 0) && (MyCheckers[i].i - 1 <= 7) && (MyCheckers[i].j + 1 <= 7)) {
                    if (Desk[MyCheckers[i].i - 1][MyCheckers[i].j + 1] == EnemyPlayerID) {
                        if ((MyCheckers[i].i - 2 >= 0) && (MyCheckers[i].j + 2 >= 0) && (MyCheckers[i].i - 2 <= 7) && (MyCheckers[i].j + 2 <= 7) && (Desk[MyCheckers[i].i - 2][MyCheckers[i].j + 2] == 0)) {
                            if (Desk[MyCheckers[i].i - 2][MyCheckers[i].j + 2] == 0) {
                                return true;
                            }
                        }
                    }
                }

                if ((MyCheckers[i].i + 1 >= 0) && (MyCheckers[i].j - 1 >= 0) && (MyCheckers[i].i + 1 <= 7) && (MyCheckers[i].j - 1 <= 7)) {
                    if (Desk[MyCheckers[i].i + 1][MyCheckers[i].j - 1] == EnemyPlayerID) {
                        if ((MyCheckers[i].i + 2 >= 0) && (MyCheckers[i].j - 2 >= 0) && (MyCheckers[i].i + 2 <= 7) && (MyCheckers[i].j - 2 <= 7) && (Desk[MyCheckers[i].i + 2][MyCheckers[i].j - 2] == 0)) {
                            if (Desk[MyCheckers[i].i + 2][MyCheckers[i].j - 2] == 0) {
                                return true;
                            }
                        }
                    }
                }

            }
        }
        return false;
    }

    private boolean AttackExistAllCheckers() {
        for (int i = 0; i <= 11; i++) {
            if (AttackExist(i)) {
                return true;
            }
        }

        return false;
    }

    private boolean AttackExistCurrentChecker() {
        return AttackExist(DragIndex);
    }

    private boolean KingAttack() {

        if (isRightDirection()) {
            int i = 1;
            int EnemyCheckersCount = 0;
            int KillX = -1;
            int KillY = -1;
            while (true) {
                if ((DeskX + kx * i == MyCheckers[DragIndex].i) || (DeskY + ky * i == MyCheckers[DragIndex].j)) {
                    break;
                }
                if (Desk[DeskX + kx * i][DeskY + ky * i] == MyPlayerID) {
                    return false;
                }
                if (Desk[DeskX + kx * i][DeskY + ky * i] == EnemyPlayerID) {
                    EnemyCheckersCount++;
                    KillX = DeskX + kx * i;
                    KillY = DeskY + ky * i;
                }
                i++;
            }
            if (EnemyCheckersCount == 1) {

                Dying(KillX, KillY);
                Move0Attack1 = 1;
                return true;
            }
        }
        return false;
    }

    private boolean Attack() {
        if ((DeskX < MyCheckers[DragIndex].i) && (DeskY < MyCheckers[DragIndex].j)
                && (DeskX - DeskY == MyCheckers[DragIndex].i - MyCheckers[DragIndex].j)
                && Desk[DeskX + 1][DeskY + 1] == EnemyPlayerID) {

            Dying(DeskX + 1, DeskY + 1);
            Move0Attack1 = 1;
            return true;
        }
        if ((DeskX > MyCheckers[DragIndex].i) && (DeskY > MyCheckers[DragIndex].j)
                && (DeskX - DeskY == MyCheckers[DragIndex].i - MyCheckers[DragIndex].j)
                && Desk[DeskX - 1][DeskY - 1] == EnemyPlayerID) {
            Desk[DeskX - 1][DeskY - 1] = 0;
            Dying(DeskX - 1, DeskY - 1);
            Move0Attack1 = 1;
            return true;
        }
        if ((DeskX > MyCheckers[DragIndex].i) && (DeskY < MyCheckers[DragIndex].j)
                && (DeskX + DeskY == MyCheckers[DragIndex].i + MyCheckers[DragIndex].j)
                && Desk[DeskX - 1][DeskY + 1] == EnemyPlayerID) {

            Dying(DeskX - 1, DeskY + 1);
            Move0Attack1 = 1;
            return true;
        }
        if ((DeskX < MyCheckers[DragIndex].i) && (DeskY > MyCheckers[DragIndex].j)
                && (DeskX + DeskY == MyCheckers[DragIndex].i + MyCheckers[DragIndex].j)
                && Desk[DeskX + 1][DeskY - 1] == EnemyPlayerID) {

            Dying(DeskX + 1, DeskY - 1);
            Move0Attack1 = 1;
            return true;
        }

        return false;
    }

    private boolean Move() {
        if (AttackExistAllCheckers()) {
            return false;
        } else {
            if (MyCheckers[DragIndex].isKing) {
                if (isRightDirection()) {
                    int i = 1;
                    while (true) {
                        if ((DeskX + kx * i == MyCheckers[DragIndex].i) || (DeskY + ky * i == MyCheckers[DragIndex].j)) {
                            Move0Attack1 = 0;
                            return true;
                        }
                        if ((Desk[DeskX + kx * i][DeskY + ky * i]) != 0) {
                            return false;
                        }

                        i++;
                    }
                }
            } else {
                if (((DeskX == MyCheckers[DragIndex].i - 1) && (DeskY == MyCheckers[DragIndex].j - 1))
                        || ((DeskX == MyCheckers[DragIndex].i + 1) && (DeskY == MyCheckers[DragIndex].j - 1))) {
                    Move0Attack1 = 0;
                    return true;
                }

            }
        }
        return false;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (GameEnd != true) {
            if (MyTurn) {
                int x = e.getX();
                int y = e.getY();
                if (DragIndex >= 0) {

                    if (getCellfromCoordinates(x, y)
                            && CellIsDark()
                            && CellIsFree()
                            && (KingAttack() || Attack() || Move())) {

                        MyCheckers[DragIndex].x = DeskX * CELL_SIZE + CELL_SIZE / 2;
                        MyCheckers[DragIndex].y = DeskY * CELL_SIZE + CELL_SIZE / 2;
                        Desk[MyCheckers[DragIndex].i][MyCheckers[DragIndex].j] = 0;
                        MyCheckers[DragIndex].i = DeskX;
                        MyCheckers[DragIndex].j = DeskY;
                        Desk[DeskX][DeskY] = MyPlayerID;
                        if (DeskY == 0) {
                            MyCheckers[DragIndex].isKing = true;
                        }
                        if (Move0Attack1 == 1) {
                            if (AttackExistCurrentChecker() == false) {
                                MyTurn = false;
                                Move0Attack1 = -1;
                                Die();
                                sizeDying = 0;
                                if (EnemyLose()) {
                                    FullGameEnd = true;
                                    GameEnd = true;
                                    playerWin = MyPlayerID;
                                    IWin = true;
                                    SendPacket();
                                    repaint();
                                    return;
                                } else {
                                    SendPacket();
                                }

                            }
                        } else {
                            MyTurn = false;
                            Move0Attack1 = -1;
                            Die();
                            sizeDying = 0;
                            SendPacket();
                        }

                    } else {

                        MyCheckers[DragIndex].x = MyCheckers[DragIndex].i * CELL_SIZE + CELL_SIZE / 2;
                        MyCheckers[DragIndex].y = MyCheckers[DragIndex].j * CELL_SIZE + CELL_SIZE / 2;

                    }
                    AllowToDrag = false;
                    DragIndex = -1;
                }
                if (MyTurn) {
                    SendPacket();
                }
                repaint();
            }
        }
    }

    private void SendPacket() {

        p = new Packet(MyCheckers, EnemyCheckers, Desk, DragIndex);
        if (IWin) {
            p.setGameEnd(true);
        }
        if (GameEnd) {
            p.setWantPlayMore(WantToPlayMore);
        }
        if (drawAccepted) {
            p.setDrawAccepted(true);
        }
        if (draw) {
            p.setDraw(true);
            draw = false;
        }
        if (surrender) {
            p.setSurrender(true);
        }
        if (MyTurn == false) {
            if (RefusedDraw) {
                RefusedDraw = false;
            } else {
                p.setYourTurn(true);
            }
        }
        try {
            ToServer.writeObject(p);
        } catch (IOException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private boolean MyLose() {
        for (int i = 0; i <= 11; i++) {
            if (MyCheckers[i].exist) {
                return false;
            }
        }
        return true;
    }

    private boolean EnemyLose() {
        for (int i = 0; i <= 11; i++) {
            if (EnemyCheckers[i].exist) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (GameEnd != true) {
            if (AllowToDrag) {

                int dx = e.getX() - MyCheckers[DragIndex].x;
                int dy = e.getY() - MyCheckers[DragIndex].y;
                MyCheckers[DragIndex].x += dx;
                MyCheckers[DragIndex].y += dy;
                if (MyTurn) {
                    SendPacket();
                }
                repaint();
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    public static short getCellSize() {
        return CELL_SIZE;
    }

    public boolean isConnectionLost() {
        return ConnectionLost;
    }

    public boolean isWantToPlayMore() {
        return WantToPlayMore;
    }



}
