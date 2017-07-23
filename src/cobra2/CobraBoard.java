package cobra2;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.Timer;

/**
 *
 * @author Nexor
 */
public class CobraBoard implements ActionListener, KeyListener {

    public static int SCALE = 10;
    public static int DEFAULT_GAME_SPEED = 10;//the less the faster
    public static int SLOW_GAME_SPEED = 100;
    private Random r;
    private Timer timer;
    public List<Cobra> cobras;
    public Cobra myCobra;
    public Point cherry;
    public int ticks = 0;
    public boolean paused;
    public boolean showHeadPos;
    public int gameSpeed;
    public int x;
    public int y;

    public CobraBoard(int x, int y) {
        this.x = x;
        this.y = y;
        r = new Random();
        gameSpeed = DEFAULT_GAME_SPEED;
        timer = new Timer(gameSpeed, this);
    }

    public void startGame() {
        paused = false;
        showHeadPos = true;
        ticks = 0;
        cobras = new CopyOnWriteArrayList<>();
        myCobra = new Cobra(r.nextInt(x / SCALE / 2) + SCALE / 2, r.nextInt(y / SCALE / 2) + SCALE / 2);
        myCobra.direction = 3;
        cobras.add(myCobra);
        CobraNetwork.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof Cobra) {
                    Cobra c = (Cobra) e.getSource();
                    if (e.getActionCommand().equals("client")) {
                        if (c.id == -1) {//new cobra
                            c.id = cobras.size() + 1;
                            cobras.add(c);
                            CobraNetwork.dispatchObjectToClients(c);
                        } else if (cobras.contains(c)) {
                            cobras.remove(c);
                            cobras.add(c);
                        } else {
                            System.out.println("IMPOSTOR COBRA");
                        }
                        CobraNetwork.dispatchObjectToClients(c);
                    } else if (e.getActionCommand().equals("server")) {
                        if (c.id == myCobra.id) {
                            myCobra = c;
                            cobras.remove(c);
                        }
                        cobras.add(c);
                    } else {
                        System.out.println("UNRECOGNIZED SOURCE");
                    }
                } else if (e.getSource() instanceof Point) {
                    if (e.getActionCommand().equals("server")) {
                        cherry = (Point) e.getSource();
                    }
                } else if (e.getSource().equals("c")) {
                    if (CobraNetwork.isServer()) {
                        System.out.println("Sending cobras to newly connected client");
                        for (Cobra cobra : cobras) {
                            CobraNetwork.dispatchObjectToClients(cobra);
                        }
                    } else {
                        System.out.println("UNRECOGNIZED WHAT");
                    }
                }

            }
        });
        CobraNetwork.init();
        if (!CobraNetwork.isServer()) {
            CobraNetwork.sendObjectToServer(myCobra);
        }
        cherry = new Point(x / SCALE - SCALE, y / SCALE - SCALE);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        boolean cherryUpdate = false;
        if (!paused && ticks++ % (showHeadPos ? SLOW_GAME_SPEED : DEFAULT_GAME_SPEED) == 0) {
            List<Cobra> toRemove = new ArrayList<>();
            for (Cobra c : cobras) {
                c.snakeParts.add(new Point(c.head.x, c.head.y));
                if (c.direction == 0 && checkInsideOfBound(c) && noTailAt(c.head.x, c.head.y - c.step)) {
                    c.head = new Point(c.head.x, c.head.y - c.step);
                } else if (c.direction == 1 && checkInsideOfBound(c) && noTailAt(c.head.x, c.head.y + c.step)) {
                    c.head = new Point(c.head.x, c.head.y + c.step);
                } else if (c.direction == 2 && checkInsideOfBound(c) && noTailAt(c.head.x - c.step, c.head.y)) {
                    c.head = new Point(c.head.x - c.step, c.head.y);
                } else if (c.direction == 3 && checkInsideOfBound(c) && noTailAt(c.head.x + c.step, c.head.y)) {
                    c.head = new Point(c.head.x + c.step, c.head.y);
                } else {
                    c.alive = false;
                    toRemove.add(c);
                    continue;
                }

                if (c.snakeParts.size() > c.tailLength) {
                    c.snakeParts.remove(0);
                }

                if (c.head.equals(cherry)) { //generates cherry after one is eaten
                    c.score += 10;
                    c.tailLength++;
                    cherry.setLocation(r.nextInt(x), r.nextInt(y));
                    cherryUpdate = true;
                }
            }
            cobras.removeAll(toRemove);
        }
        if (CobraNetwork.isServer() && cherryUpdate) {
            CobraNetwork.dispatchObjectToClients(cherry);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {//direction 0 = UP, DOWN=1, LEFT = 2, RIGHT = 3
        int i = e.getKeyCode();
        if (!cobras.isEmpty()) {
            myCobra = cobras.get(0);
            cobras.remove(0);
        }
        switch (i) {
            case KeyEvent.VK_LEFT:
                if (myCobra.direction != 3) {
                    myCobra.direction = 2;
                }
                break;
            case KeyEvent.VK_UP:
                if (myCobra.direction != 1) {
                    myCobra.direction = 0;
                }
                break;
            case KeyEvent.VK_RIGHT:
                if (myCobra.direction != 2) {
                    myCobra.direction = 3;
                }
                break;
            case KeyEvent.VK_DOWN:
                if (myCobra.direction != 0) {
                    myCobra.direction = 1;
                }
                break;
            case KeyEvent.VK_SPACE:
                if (!myCobra.alive) {
                    startGame();
                } else {
                    paused = !paused;
                }
                break;
            case KeyEvent.VK_0:
                showHeadPos = !showHeadPos;
                break;
            case KeyEvent.VK_SHIFT:
                myCobra.step = 2;
                break;
            case KeyEvent.VK_ESCAPE://exits the game
                System.exit(0);
        }
        cobras.add(0, myCobra);
        if (CobraNetwork.isServer()) {
            CobraNetwork.dispatchObjectToClients(myCobra);
        } else {
            CobraNetwork.sendObjectToServer(myCobra);
        }
    }

    private boolean checkInsideOfBound(Cobra c) {
//        if (c.head.x * SCALE > x - SCALE * SCALE || c.head.x < 0 || c.head.y < 0 || c.head.y * SCALE > y - SCALE * SCALE * 2) {
        if (c.head.x + 3 > x / SCALE || c.head.x < 0 || c.head.y < 0 || c.head.y + SCALE / 2 > y / SCALE) {
            return false;
        }
        return true;
    }

    private boolean noTailAt(int x, int y) {
        for (Cobra cobra : cobras) {
            for (Point point : cobra.snakeParts) {
                if (point.equals(new Point(x, y))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int i = e.getKeyCode();
        if (!cobras.isEmpty()) {
            myCobra = cobras.get(0);
            cobras.remove(0);
            if (i == KeyEvent.VK_SHIFT) {
                myCobra.step = 1;
            }
            cobras.add(0, myCobra);
            if (CobraNetwork.isServer()) {
                CobraNetwork.dispatchObjectToClients(myCobra);
            } else if(CobraNetwork.isConnected()) {
                CobraNetwork.sendObjectToServer(myCobra);
            }
        }
    }

}
