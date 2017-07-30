package cobra2;

import java.awt.Color;
import java.io.Serializable;

/**
 *
 * @author Nexor
 */
public class Portal extends Projectile implements Serializable {

    private static int DEFAULT_PORTAL_STEP = 10;
    private static int COUNT = 0;
    public static Portal BLUE_PORTAL;
    public static Portal ORANGE_PORTAL;
    public Color c;
    public Portal link;

    private Portal(Cobra myCobra) {
        super(myCobra);
        step = DEFAULT_PORTAL_STEP;
        c = COUNT++ % 2 == 0 ? Color.BLUE : Color.ORANGE;
    }

    public static Portal create(Cobra myCobra) {
        Portal p = new Portal(myCobra);
        if (COUNT % 2 == 0) {
            p.c = Color.BLUE;
            BLUE_PORTAL = p;
            if (ORANGE_PORTAL != null) {
                p.link(ORANGE_PORTAL);
            }
        } else {
            p.c = Color.ORANGE;
            ORANGE_PORTAL = p;
            if (BLUE_PORTAL != null) {
                p.link(BLUE_PORTAL);
            }
        }
        return p;
    }

    public void link(Portal p) {
        p.link = this;
        link = p;
    }

}
