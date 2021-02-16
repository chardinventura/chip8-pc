import javax.swing.JFrame;
import java.awt.event.*;

public class Window extends JFrame implements KeyListener{

	private static final long serialVersionUID = 1L;

	private short[][] keys = {
		{150, 0}, {91, 0}, {161, 0}, {162, 0}, // 1, 2, 3, C
		{59, 0}, {44, 0}, {46, 0}, {80, 0},    // 4, 5, 6, D
		{65, 0}, {79, 0}, {69, 0}, {85, 0},    // 7, 8, 9, E
		{222, 0}, {81, 0}, {74, 0}, {75, 0},   // A, 0, B, F
	};

	public Window() {
		setSize(650, 350);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		addKeyListener(this);
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		for (byte i = 0; i < keys.length; i++) {
			if(keys[i][0] == arg0.getKeyCode()) {
				keys[i][1] = 1;
				break;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		for (byte i = 0; i < keys.length; i++) {
			if(keys[i][0] == arg0.getKeyCode()) {
				keys[i][1] = 0;
				break;
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}

	public short[][] getKeys() {
		return keys;
	}

	public void setKeys(short[][] keys) {
		this.keys = keys;
	}
}
