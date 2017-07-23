
import cobra2.CobraBoard;
import cobra2.CobraNetwork;
import cobra2.RenderPanel;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Nexor
 */
public class Main {

    public static void main(String[] args) {
        JFrame f = new JFrame();
        int x = 800, y = x;
        f.setSize(new Dimension(x, y));
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLocationRelativeTo(null);
        CobraBoard cb = new CobraBoard(x, y);
        JPanel p = new RenderPanel(cb);
        f.add(p);
        f.addKeyListener(cb);
        f.setVisible(true);
        cb.startGame();
    }
}
