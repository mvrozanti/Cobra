package cobra2;

import com.sun.javafx.scene.traversal.Direction;
import static com.sun.javafx.scene.traversal.Direction.DOWN;
import static com.sun.javafx.scene.traversal.Direction.LEFT;
import static com.sun.javafx.scene.traversal.Direction.RIGHT;
import static com.sun.javafx.scene.traversal.Direction.UP;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.swing.Timer;

/**
 *
 * evt.getActionCommand doesnt have to exist ya kno general collision method?
 *
 * @author Nexor
 */
public class CobraBoard implements ActionListener, KeyListener {

    public static int SCALE = 10;
    public static int DEFAULT_GAME_SPEED = 10;//the less the faster
    public static int SLOW_GAME_SPEED = 100;
    private final Point bounds;
    private Random r;
    private Timer timer;
    public Set<Cobra> cobras;
    public List<Harm> harms;
    public List<Projectile> projectiles;
    public List<Portal> portals;
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
        harms = new CopyOnWriteArrayList<>();
        projectiles = new CopyOnWriteArrayList<>();
        portals = new CopyOnWriteArrayList<>();
        timer = new Timer(gameSpeed, this);
        bounds = new Point(x / SCALE - SCALE / 3, y / SCALE - SCALE / 2);
        myCobra = new Cobra(r.nextInt(bounds.x / 2 - 1) + bounds.x / 3, r.nextInt(bounds.y / 2 - 1) + bounds.y / 3);
    }

    public void startGame() {
        paused = false;
        showHeadPos = false;
        ticks = 0;
        cobras = new CopyOnWriteArraySet<>();
        cobras.add(myCobra);
        CobraNetwork.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof String && e.getSource().equals("c")) {
                    if (CobraNetwork.isServer()) {
                        CobraNetwork.send(cherry);
                        for (Projectile p : portals) {
                            CobraNetwork.send(p);
                        }
                        for (Portal p : portals) {
                            CobraNetwork.send(p);
                        }
                        System.out.println("Sending cobras to newly connected client");
                        for (Cobra cobra : cobras) {
                            CobraNetwork.send(cobra);
                        }
                    } else {
                        System.out.println("UNRECOGNIZED WHAT");
                    }
                } else if (e.getSource() instanceof Cobra) {
                    System.out.println(e.getSource().toString());
                    Cobra c = (Cobra) e.getSource();
                    if (e.getActionCommand().equals("client")) {// if it comes from client
                        boolean newlyDiscovered = false;
                        if (cobras.contains(c)) {
                            newlyDiscovered = true;
                            System.out.println("His cobra was removed: " + cobras.remove(c));
                        }
                        if (c.id == -1) {//new cobra
                            System.out.println("WE SHOULD NOT RECEIVE COBRAS WITH id -1");
                        } else {
                            if (!newlyDiscovered) {
                                CobraNetwork.send(c);
                            }
                            System.out.println("Cobra added: " + cobras.add(c));
                        }
                    } else {//if it comes from server
                        Cobra cToRemove = null;
                        if (c.id == myCobra.id) {
                            cToRemove = myCobra;
                            myCobra = c;
                        } else {
                            for (Cobra cobra : cobras) {
                                if (cobra.id == c.id) {
                                    cToRemove = cobra;
                                    break;
                                }
                            }
                        }
                        System.out.println("Removed cobra: " + cobras.remove(cToRemove));
                        System.out.println("Added cobra: " + cobras.add(c));
                    }
                } else if (e.getSource() instanceof Portal) {
                    System.out.println("Receiving portal!");
                    Portal p = (Portal) e.getSource();
                    portals.add(p);
                    if (CobraNetwork.isServer()) {
                        CobraNetwork.send(p);
                    }
                } else if (e.getSource() instanceof Projectile) {
                    System.out.println("Receiving projectile!");
                    Projectile p = (Projectile) e.getSource();
                    projectiles.add(p);
                    if (CobraNetwork.isServer()) {
                        CobraNetwork.send(p);
                    }
                } else if (e.getSource() instanceof Harm) {
                    System.out.println("Receiving harm!");
                    Harm h = (Harm) e.getSource();
                    harms.add(h);
                    if (CobraNetwork.isServer()) {
                        CobraNetwork.send(h);
                    }
                } else if (e.getSource() instanceof Integer && e.getActionCommand().equals("server")) {
                    myCobra.id = (Integer) e.getSource();
                } else if (e.getSource() instanceof Point) {
                    System.out.println("Receiving cherry!");
                    cherry = (Point) e.getSource();
                }
            }
        });
        if (!CobraNetwork.isConnected()) {
            CobraNetwork.init();
        }
        while (!CobraNetwork.isConnected()) {
            continue;
        }
        if (!CobraNetwork.isServer()) {
            myCobra.setStep(0);
            CobraNetwork.send(myCobra);
        } else {
            cherry = new Point(new Random().nextInt(bounds.x - 1), new Random().nextInt(bounds.y - 1));
            myCobra.id = 1;
        }
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        boolean cherryUpdate = false;
        if (!paused && ticks++ % gameSpeed == 0) {
            List<Cobra> cobrasToRemove = new ArrayList<>();
            List<Harm> harmsToRemove = new ArrayList<>();
            List<Projectile> projectilesToRemove = new ArrayList<>();
            List<Portal> portalsToRemove = new ArrayList<>();
            for (Cobra c : cobras) {
                if (ticks - c.getTicksInvisible() > 300) {
                    c.setVisible(true, Integer.MAX_VALUE);
                }
                portalLoop:
                for (Portal p : portals) {
                    // EXPERIMENTAL FOR SICK MECHANICS
//                    for (Portal possiblePassThrough : portals) {
//                        if(possiblePassThrough.pos.equals(p.pos) && !p.c.equals(possiblePassThrough) && p != possiblePassThrough){
//                            p.pos = (Point) possiblePassThrough.pos.clone();
//                            p.dir = Cobra.reverse(possiblePassThrough.dir);
//                            portalsToRemove.add(possiblePassThrough);
//                        }
//                    }
                    if (p.link != null) {
                        for (Projectile proj : projectiles) {
                            if (p.pos.equals(proj.pos)) {
                                Portal other = p.link;
                                proj.pos = (Point) other.pos.clone();
                                proj.setDirection(Cobra.reverse(other.dir));
                                break portalLoop;
                            }
                        }
                        if (p.pos.equals(c.head)) {
                            c.head = (Point) p.link.pos.clone();
                            c.setDirection(Cobra.reverse(p.link.dir), Integer.MAX_VALUE);
                            portalsToRemove.add(p);
                            portalsToRemove.add(p.link);
                            break;
                        }
                    }
                }
                portals.removeAll(portalsToRemove);

                c.snakeParts.add(new Point(c.head.x, c.head.y));
                if (c.step > 0) {
                    if (c.getDirection() == UP && checkInsideOfBound(c.head) && noTailAt(c.head.x, c.head.y - c.getStep())) {
                        c.head.y = c.head.y - c.getStep();
                    } else if (c.getDirection() == DOWN && checkInsideOfBound(c.head) && noTailAt(c.head.x, c.head.y + c.getStep())) {
                        c.head.y = c.head.y + c.getStep();
                    } else if (c.getDirection() == LEFT && checkInsideOfBound(c.head) && noTailAt(c.head.x - c.getStep(), c.head.y)) {
                        c.head.x = c.head.x - c.getStep();
                    } else if (c.getDirection() == RIGHT && checkInsideOfBound(c.head) && noTailAt(c.head.x + c.getStep(), c.head.y)) {
                        c.head.x = c.head.x + c.getStep();
                    } else {
                        c.alive = false;
                    }
                }

                if (c.snakeParts.size() > c.tailLength) {
                    c.snakeParts.remove(0);
                }

                if (c.head.equals(cherry)) { //generates cherry after one is eaten
                    c.score += 10;
                    c.tailLength++;
                    cherry.setLocation(r.nextInt(bounds.x - 1), r.nextInt(bounds.y - 1));
                    cherryUpdate = true;
                }

                for (Portal p : portals) {
                    if (p.getDirection() == UP) {
                        p.pos.y = p.pos.y - p.getStep();
                    } else if (p.getDirection() == DOWN) {
                        p.pos.y = p.pos.y + p.getStep();
                    } else if (p.getDirection() == LEFT) {
                        p.pos.x = p.pos.x - p.getStep();
                    } else if (p.getDirection() == RIGHT) {
                        p.pos.x = p.pos.x + p.getStep();
                    }
                    if (!checkInsideOfBound(p.pos)) {
                        System.out.println("portal hit bound");
                        if (p.pos.x > bounds.x) {
                            p.pos.x = bounds.x;
                        }
                        if (p.pos.y > bounds.y) {
                            p.pos.y = bounds.y;
                        }
                        if (p.pos.x < 0) {
                            p.pos.x = 0;
                        }
                        if (p.pos.y < 0) {
                            p.pos.y = 0;
                        }
                        ((Portal) p).step = 0;
                    }
                }

                for (Projectile p : projectiles) {
                    if (p.getDirection() == UP) {
                        p.pos.y = p.pos.y - p.getStep();
                    } else if (p.getDirection() == DOWN) {
                        p.pos.y = p.pos.y + p.getStep();
                    } else if (p.getDirection() == LEFT) {
                        p.pos.x = p.pos.x - p.getStep();
                    } else if (p.getDirection() == RIGHT) {
                        p.pos.x = p.pos.x + p.getStep();
                    }
                    for (Point part : c.snakeParts) {
                        if (p.pos.equals(part)) {
                            projectilesToRemove.add(p);
                            c.alive = false;
                            CobraNetwork.send(c);
                            cobrasToRemove.add(c);
                            break;
                        }
                    }
                }
                projectiles.removeAll(projectilesToRemove);
                for (Harm h : harms) {
                    if (c.head.equals(h.pos)) {
                        c.snakeParts.remove(0);
                        c.tailLength--;
                        harmsToRemove.add(h);
                    }
                }
                harms.removeAll(harmsToRemove);
            }
            cobras.removeAll(cobrasToRemove);
        }
        if (CobraNetwork.isServer() && cherryUpdate) {
            CobraNetwork.send(cherry);
        }
    }

    private boolean checkInsideOfBound(Point p) {
        if (p.x > bounds.x || p.x < 0 || p.y < 0 || p.y > bounds.y) {
            return false;
        }
        return true;
    }

    private boolean noTailAt(int x, int y) {
        for (Cobra cobra : cobras) {
            for (Point part : cobra.snakeParts) {
                if (new Point(x, y).equals(part)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        //unnecessary for now
    }

    @Override
    public void keyPressed(KeyEvent e) {//direction 0 = UP, DOWN=1, LEFT = 2, RIGHT = 3
        int i = e.getKeyCode();
        Object relevantObject = null;
        if (i == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        } else if (i == KeyEvent.VK_SPACE) {
            if (!myCobra.alive) {
                myCobra.reset();
                relevantObject = myCobra;
            }
        }
        if (myCobra.alive) {
            switch (i) {
                case KeyEvent.VK_SHIFT:
                    if (myCobra.getStep() != 2) {
                        myCobra.setStep(2);
                    }
                    relevantObject = myCobra;
                    break;
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    if (myCobra.getDirection() != RIGHT && myCobra.getDirection() != LEFT && noTailAt(myCobra.head.x - myCobra.step, myCobra.head.y)) {
                        myCobra.setDirection(LEFT, ticks);
                    }
                    relevantObject = myCobra;
                    break;
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W: {
                    if (myCobra.getDirection() != DOWN && myCobra.getDirection() != UP && noTailAt(myCobra.head.x, myCobra.head.y - myCobra.step)) {
                        myCobra.setDirection(UP, ticks);
                    }
                    relevantObject = myCobra;
                }
                break;
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    if (myCobra.getDirection() != LEFT && myCobra.getDirection() != RIGHT && noTailAt(myCobra.head.x + myCobra.step, myCobra.head.y)) {
                        myCobra.setDirection(RIGHT, ticks);
                    }
                    relevantObject = myCobra;
                    break;
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:
                    if (myCobra.getDirection() != UP && myCobra.getDirection() != DOWN && noTailAt(myCobra.head.x, myCobra.head.y + myCobra.step)) {
                        myCobra.setDirection(DOWN, ticks);
                    }
                    relevantObject = myCobra;
                    break;
                case KeyEvent.VK_1:
                    //deploy harm
                    if (myCobra.harmCount > 0) {
                        harms.add(myCobra.deployHarm());
                    }
                    relevantObject = harms.get(harms.size() - 1);
                    break;
                case KeyEvent.VK_2:
                    //invisibility
                    myCobra.setVisible(myCobra.isInvisible(), ticks);
                    relevantObject = myCobra;
                    break;
                case KeyEvent.VK_3:
                    //projectile launching
                    if (myCobra.projectileCount > 0) {
                        projectiles.add(new Projectile(myCobra));
                    }
                    relevantObject = projectiles.get(projectiles.size() - 1);
                    break;
                case KeyEvent.VK_4:
                    //portal launching
                    if (myCobra.portalCount > 0) {
                        Portal newPortal = Portal.create(myCobra);
                        if (portals.size() % 2 != 0) {
                            portals.get(portals.size() - 1).link = newPortal;
                            newPortal.link(portals.get(portals.size() - 1));
                        }
                        portals.add(newPortal);
                    }
                    relevantObject = portals.get(portals.size() - 1);
                    break;
                case KeyEvent.VK_0:
                    showHeadPos = !showHeadPos;
                    gameSpeed = gameSpeed == SLOW_GAME_SPEED ? DEFAULT_GAME_SPEED : SLOW_GAME_SPEED;
                    break;
            }
        }
        cobras.add(myCobra);
        if (CobraNetwork.isConnected() && relevantObject != null) {
            CobraNetwork.send(relevantObject);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int i = e.getKeyCode();
        if (i == KeyEvent.VK_SHIFT) {
            myCobra.setStep(Cobra.DEFAULT_STEP);
            cobras.add(myCobra);
            CobraNetwork.send(myCobra);
        }
    }
}
