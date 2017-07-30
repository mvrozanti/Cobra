package cobra2;

import java.awt.Point;
import java.io.Serializable;

/**
 *
 * @author Nexor
 */
public class Harm implements Serializable {

    public Point pos;
    public int state;

    public Harm(Point pos) {
        this.pos = pos;
        state = 0;
    }

    @Override
    public boolean equals(Object obj) {
        return ((Harm) obj).pos.equals(pos);
    }

}
