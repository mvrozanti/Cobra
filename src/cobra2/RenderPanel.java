/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cobra2;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class RenderPanel extends JPanel {

    public static int CUR_COLOR = 0;

    private CobraBoard cb;

    public RenderPanel(CobraBoard cb) {
        this.cb = cb;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(CUR_COLOR));
        g.fillRect(0, 0, cb.x, cb.y);
        for (Cobra c : cb.cobras) {
            g.setColor(c.color);
            for (Point p : c.snakeParts) {
                g.fillRect(p.x * CobraBoard.SCALE, p.y * CobraBoard.SCALE, CobraBoard.SCALE, CobraBoard.SCALE);
            }
            g.fillRect(c.head.x * CobraBoard.SCALE, c.head.y * CobraBoard.SCALE, CobraBoard.SCALE, CobraBoard.SCALE);
            g.setColor(Color.RED);
            if (cb.cherry != null) {
                g.fillRect(cb.cherry.x * CobraBoard.SCALE, cb.cherry.y * CobraBoard.SCALE, CobraBoard.SCALE, CobraBoard.SCALE);
            }
            String time = "" + cb.ticks;
            String hud = "";
            if (cb.showHeadPos) {
                hud = "isServer:" + CobraNetwork.isServer() + " Score: " + c.score + ", Tail Length: " + c.tailLength + ", Ticks: " + time + ", Head Pos: (" + c.head.x + "," + c.head.y + ")";
                g.drawString(hud, 15, 15);
            } else {
                hud = "Score: " + c.score + ", Tail Length: " + c.tailLength + ", Ticks: " + cb.ticks;
                g.drawString(hud, 15, 15);
            }
            if (cb.paused) {
                int originalFontSize = g.getFont().getSize();
                g.setColor(Color.WHITE);
                g.setFont(g.getFont().deriveFont(0, 100));
                g.drawString("PAUSED", cb.x / 3, cb.y / 3);
                g.setFont(g.getFont().deriveFont(0, originalFontSize));
            }
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
}
