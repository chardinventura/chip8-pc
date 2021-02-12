import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class Panel extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private boolean[] pixels;
	private byte pixelSize;

	@Override
	public void paint(Graphics arg0) {
		super.paint(arg0);

		for (short i = 0; i < pixels.length; i++) {

			if(pixels[i])
				arg0.setColor(Color.WHITE);
			else
				arg0.setColor(Color.BLACK);

			short x = (short) ((i & 0x3F) * pixelSize);
			short y = (short) ((i >> 0x6) * pixelSize);
			arg0.fillRect(x, y, pixelSize, pixelSize);
		}
	}

	public byte getPixelSize() {
		return pixelSize;
	}

	public void setPixelSize(byte pixelSize) {
		this.pixelSize = pixelSize;
	}

	public boolean[] getPixels() {
		return pixels;
	}

	public void setPixels(boolean[] pixels) {
		this.pixels = pixels;
	}
}
