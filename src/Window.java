import javax.swing.JFrame;
import java.awt.event.*;

public class Window extends JFrame implements KeyListener{

	private static final long serialVersionUID = 1L;

	private short[][] keys = {
		{KeyEvent.VK_1, 0}, {KeyEvent.VK_2, 0}, {KeyEvent.VK_3, 0}, {KeyEvent.VK_4, 0}, // 1, 2, 3, C
		{KeyEvent.VK_Q, 0}, {KeyEvent.VK_W, 0}, {KeyEvent.VK_E, 0}, {KeyEvent.VK_R, 0},    // 4, 5, 6, D
		{KeyEvent.VK_A, 0}, {KeyEvent.VK_S, 0}, {KeyEvent.VK_D, 0}, {KeyEvent.VK_F, 0},    // 7, 8, 9, E
		{KeyEvent.VK_Z, 0}, {KeyEvent.VK_X, 0}, {KeyEvent.VK_C, 0}, {KeyEvent.VK_V, 0},   // A, 0, B, F
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
