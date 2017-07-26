/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cobra2;

import static com.sun.javafx.scene.traversal.Direction.LEFT;
import static com.sun.javafx.scene.traversal.Direction.RIGHT;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class RenderPanel extends JPanel {

    public static Color BG_COLOR = Color.DARK_GRAY.darker().darker().darker();

    private CobraBoard cb;

    public RenderPanel(CobraBoard cb) {
        this.cb = cb;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(BG_COLOR);
        g.fillRect(0, 0, cb.x, cb.y);
        g.setColor(Color.RED);
        String hud;
        if (cb.showHeadPos) {
            hud = "isConnected: " + CobraNetwork.isConnected()
                    + " isServer:" + CobraNetwork.isServer()
                    + " Score: " + cb.myCobra.score
                    + ", Tail Length: " + cb.myCobra.tailLength
                    + ", Ticks: " + cb.ticks
                    + ", Direction: " + cb.myCobra.getDirection()
                    + ", Head Pos: (" + cb.myCobra.head.x + "," + cb.myCobra.head.y + ")";
        } else {
            hud = "Score: " + cb.myCobra.score + ", Tail Length: " + cb.myCobra.tailLength + ", Ticks: " + cb.ticks;
        }
        g.drawString(hud, 15, 15);

        if (cb.cherry != null) {
            drawSquareAt(g, cb.cherry);
        }

        g.setColor(cb.myCobra.color);
        g.fillRect((int) (cb.x * .96), 0, CobraBoard.SCALE, CobraBoard.SCALE);

        if (cb.paused) {
            int originalFontSize = g.getFont().getSize();
            g.setColor(Color.WHITE);
            g.setFont(g.getFont().deriveFont(0, 100));
            g.drawString("PAUSED", cb.x / 3, cb.y / 3);
            g.setFont(g.getFont().deriveFont(0, originalFontSize));
        }
        for (Portal p : cb.portals) {
            g.setColor(((Portal) p).c);
            drawProjectile(g, p);
        }
        for (Projectile p : cb.projectiles) {
            g.setColor(Color.RED);
            drawProjectile(g, p);
        }
        for (Harm h : cb.harms) {
            g.setColor(Color.getHSBColor((float) Math.random(), 1, 1));//WHAT COLOR???
            drawSquareAt(g, h.pos);
        }

        for (Cobra c : cb.cobras) {
            if (c.isInvisible() && !c.equals(cb.myCobra) || !c.alive) {
                g.setColor(BG_COLOR);
            } else if (c.isInvisible() && c.equals(cb.myCobra)) {
                g.setColor(
                        new Color(
                                cb.myCobra.color.getRed(),
                                cb.myCobra.color.getGreen(),
                                cb.myCobra.color.getBlue(),
                                60));
            } else {
                g.setColor(c.color);
            }
            for (Point p : c.snakeParts) {
                drawSquareAt(g, p);
            }
            drawSquareAt(g, c.head);

        }
        if (!cb.myCobra.alive) {
            int r = (int) (256 * Math.random());
            int gr = (int) (256 * Math.random());
            int b = (int) (256 * Math.random());
            Color randomColor = Color.getHSBColor(r, gr, b);
            g.setColor(randomColor);
            g.drawString("LOST", new Random().nextInt(650), new Random().nextInt(500) + 90);
        }
        repaint();
    }

    private void drawSquareAt(Graphics g, Point p) {
        g.fillRect(p.x * CobraBoard.SCALE, p.y * CobraBoard.SCALE, CobraBoard.SCALE, CobraBoard.SCALE);
    }

    private void drawProjectile(Graphics g, Projectile p) {
        if (p.dir == RIGHT || p.dir == LEFT) {
            g.fillRect(p.pos.x * CobraBoard.SCALE, p.pos.y * CobraBoard.SCALE + CobraBoard.SCALE, CobraBoard.SCALE, 1);
            g.fillRect(p.pos.x * CobraBoard.SCALE, p.pos.y * CobraBoard.SCALE, CobraBoard.SCALE, 1);
        } else {
            g.fillRect(p.pos.x * CobraBoard.SCALE + CobraBoard.SCALE, p.pos.y * CobraBoard.SCALE, 1, CobraBoard.SCALE);
            g.fillRect(p.pos.x * CobraBoard.SCALE, p.pos.y * CobraBoard.SCALE, 1, CobraBoard.SCALE);
        }
    }
}
