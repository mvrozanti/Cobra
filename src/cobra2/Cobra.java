package cobra2;

import java.awt.Color;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Nexor
 */
public class Cobra implements Serializable {

    public static int DEFAULT_TAIL_LENGTH = 7;
    public static Color USER_COLOR;
    public ArrayList<Point> snakeParts = new ArrayList<>();
    public int tailLength, direction, score;//direction 0 = UP, DOWN=1, LEFT = 2, RIGHT = 3
    public Point head;
    public int step;
    public Color color;
    public boolean alive;
    public int id = -1;

    public Cobra(int x, int y) {
        this(new Point(x, y));
    }

    public Cobra(Point head) {
        Random r = new Random();
        alive = true;
        this.head = head;
        step = 1;
        color = new Color(r.nextInt(128) + 128, r.nextInt(128) + 128, r.nextInt(128) + 128);
        if (USER_COLOR == null) {
            USER_COLOR = color;
        }
        tailLength = DEFAULT_TAIL_LENGTH;
        direction = r.nextInt(4);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + this.id;
        return hash;
    }
}
