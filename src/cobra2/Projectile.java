package cobra2;

import com.sun.javafx.scene.traversal.Direction;
import static com.sun.javafx.scene.traversal.Direction.DOWN;
import static com.sun.javafx.scene.traversal.Direction.LEFT;
import static com.sun.javafx.scene.traversal.Direction.RIGHT;
import static com.sun.javafx.scene.traversal.Direction.UP;
import java.awt.Point;
import java.io.Serializable;

/**
 *
 * @author Nexor
 */
public class Projectile implements Serializable {

    public static int DEFAULT_PROJECTILE_STEP = 3;
    public Direction dir;
    public int step;
    public Point pos;

    public static void main(String[] args) throws InterruptedException {
        double val = 0.9;
        for (int i = 1; i < 100000000; i++) {
            val = Math.sin(val * i * Math.PI);
            System.out.println(val);
            Thread.sleep(10);
        }
    }

    public Projectile(Cobra myCobra) {
        pos = new Point(myCobra.getDirection() == LEFT
                ? myCobra.head.x - DEFAULT_PROJECTILE_STEP * 2
                : myCobra.getDirection() == RIGHT
                        ? myCobra.head.x + DEFAULT_PROJECTILE_STEP * 2 : myCobra.head.x,
                myCobra.getDirection() == UP
                        ? myCobra.head.y - DEFAULT_PROJECTILE_STEP * 2
                        : myCobra.getDirection() == DOWN
                                ? myCobra.head.y + DEFAULT_PROJECTILE_STEP * 2 : myCobra.head.y);
        this.dir = myCobra.getDirection();
        step = DEFAULT_PROJECTILE_STEP;
    }

    public Direction getDirection() {
        return dir;
    }

    public void setDirection(Direction dir) {
        this.dir = dir;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

}
