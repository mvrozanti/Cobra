package cobra2;

import com.sun.javafx.scene.traversal.Direction;
import static com.sun.javafx.scene.traversal.Direction.DOWN;
import static com.sun.javafx.scene.traversal.Direction.LEFT;
import static com.sun.javafx.scene.traversal.Direction.RIGHT;
import static com.sun.javafx.scene.traversal.Direction.UP;
import java.awt.Color;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 * harm
 * invisiblity
 * projectile (maybe bigify/paralyzer)
 * blink/teleport
 * 
 * blindness
 * bigify
 * mana machanics
 * invincible // hueDisplay mariolike?
 * LSD-mode
 * @author Nexor
 */
public final class Cobra implements Serializable {

    public static int DEFAULT_TAIL_LENGTH = 7;
    public static int DEFAULT_STEP = 1;
    public static int DEFAULT_HARM_COUNT = 3;
    public static Color USER_COLOR;
    public ArrayList<Point> snakeParts;
    public int ticksInvisible, tailLength, score, step, harmCount, projectileCount, id;
    private final Point startHead;
    private Direction dir;
    public Point head;
    public Color color;
    public boolean alive, isInvisible;
    public int ticksTurned;
    public int portalCount;

    public Cobra(int x, int y) {
        this(new Point(x, y));
    }
    
    public static Direction reverse(Direction d){
        return d == RIGHT ? LEFT : d == LEFT ? RIGHT : d == UP ? DOWN : UP;
    }

    public Cobra(Point head) {
        this.startHead = head;
        reset();
    }

    public void setVisible(boolean visibility, int ticks) {
        isInvisible = !visibility;
        ticksInvisible = ticks;
    }
    
    public int getTicksInvisible(){
        return ticksInvisible;
    }

    public void reset() {
        this.head = (Point) startHead.clone();
        snakeParts = new ArrayList<>();
        tailLength = DEFAULT_TAIL_LENGTH;
        alive = true;
        isInvisible = false;
        id = -1;//is this ok? should server just send a new cobra instead of generating one on client?
        step = 1;
        if (USER_COLOR == null) {
            USER_COLOR = color;
        }
        ticksTurned = 100;
        Random r = new Random();
        dir = Direction.values()[r.nextInt(Direction.values().length - 3)];
        color = new Color(r.nextInt(128) + 128, r.nextInt(128) + 128, r.nextInt(128) + 128);
        harmCount = System.getProperty("user.name").equalsIgnoreCase("NEXOR") ? Integer.MAX_VALUE : DEFAULT_HARM_COUNT;//this is cheating neegga
        projectileCount = System.getProperty("user.name").equalsIgnoreCase("NEXOR") ? Integer.MAX_VALUE : DEFAULT_HARM_COUNT;
        portalCount = System.getProperty("user.name").equalsIgnoreCase("NEXOR") ? Integer.MAX_VALUE : DEFAULT_HARM_COUNT;
    }
    
    public Harm deployHarm() {
        harmCount--;
        return new Harm(snakeParts.get(0));
    }
    
    public Direction getDirection(){
        return dir;
    }
    
    public void setDirection(Direction d, int ticks){
        dir = d;
        ticksTurned = ticks;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getStep() {
        return step;
    }

    @Override
    public boolean equals(Object obj) {
        return ((Cobra) obj).id == id;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + this.id;
        return hash;
    }

    @Override
    public String toString() {
        return id + " (" + head.x + ", " + head.y + ')' + (dir == UP ? "/\\" : dir == DOWN ? "\\/" : dir == LEFT ? "<" : ">");
    }

    public boolean isInvisible() {
        return isInvisible;
    }
}
