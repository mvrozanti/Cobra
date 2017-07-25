package cobra2;

import java.awt.Point;

/**
 *
 * @author Nexor
 */
public class Harm {
    public Point pos;
    public int state;

    public Harm(Point pos) {
        this.pos = pos;
        state = 0;
    }

    @Override
    public boolean equals(Object obj) {
        return ((Harm)obj).pos.equals(pos);
    }
    
    
}
