import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Emulator {

	private final short MEMORY_SIZE = 0x1000;
	private final short STACK_SIZE = 0x10;
	private final short V_SIZE = 0x10;

	private short[] memory = new short[MEMORY_SIZE];
	private char[] stack = new char[STACK_SIZE];
	private short[] v = new short[V_SIZE];
	private char i;
	private byte delayTimer;
	private byte soundTimer;
	private char pc;
	private byte sp;
	private boolean[] pixels = new boolean[2048];
	private Window window;
	private Panel panel;

	private short[] sprites = {
		0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
		0x20, 0x60, 0x20, 0x20, 0x70, // 1
		0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
		0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
		0x90, 0x90, 0xF0, 0x10, 0x10, // 4
		0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
		0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
		0xF0, 0x10, 0x20, 0x40, 0x40, // 7
		0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
		0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
		0xF0, 0x90, 0xF0, 0x90, 0x90, // A
		0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
		0xF0, 0x80, 0x80, 0x80, 0xF0, // C
		0xE0, 0x90, 0x90, 0x90, 0xE0, // D
		0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
		0xF0, 0x80, 0xF0, 0x80, 0x80, // F
	};

	public void init() {

		memory = Arrays.copyOf(sprites, MEMORY_SIZE);
		Arrays.fill(stack, (char) 0x0);
		Arrays.fill(v, (short) 0x0);
		i = 0x0;
		delayTimer = 0x0;
		soundTimer = 0x0;
		pc = 0x200;
		sp = 0x0;
		Arrays.fill(pixels, false);
		window = new Window();
		panel = new Panel();
		window.add(panel);
		panel.setPixels(pixels);
		panel.setPixelSize((byte) 10);
	}

	public void load_memory(String rom) throws IOException {

		try(InputStream inputStream = new FileInputStream(rom)) {

			short b;

			for(int i = 0; (b = (short) inputStream.read()) != -1; i++)
				memory[pc + i] = b;

		}catch(IOException e) {
			throw e;
		}
	}

	public void run() {

		char opcode = (char) ((memory[pc] << 8) | memory[pc + 1]);
		short nnn = (short) (opcode & 0xFFF);
		short kk = (short) (opcode & 0xFF);
		byte n = (byte) (opcode & 0xF);
		byte x = (byte) ((opcode >> 8) & 0xF);
		byte y = (byte) ((opcode >> 4) & 0xF);

		pc = (char) ((pc + 2) & 0xFFF);

		switch(opcode & 0xF000) {
			case 0x0000:
				switch(opcode & 0xFF) {
					case 0xE0:
						System.out.println("CLS\n");
						Arrays.fill(pixels, false);
						panel.repaint();
						System.exit(4);
						break;
					case 0xEE:
						System.out.println("RET\n");
						if(sp > 0)
							pc = stack[--sp];
						break;
				}
				break;
			case 0x1000:
				System.out.printf("JP addr(%x)\n", (int) nnn);
				pc = (char) nnn;
				break;
			case 0x2000:
				System.out.printf("CALL addr(%x)\n", (int) nnn);
				if(sp < (STACK_SIZE - 1))
					stack[sp++] = pc;
				pc = (char) nnn;
				break;
			case 0x3000:
				System.out.printf("SE Vx[%x](%x), byte(%x)\n", x, v[x], kk);
				if(v[x] == kk)
					pc += 2;
				break;
			case 0x4000:
				System.out.printf("SNE Vx[%x](%x), byte(%x)\n", x, v[x], kk);
				if(v[x] != kk)
					pc += 2;
				break;
			case 0x5000:
				System.out.printf("SE Vx[%x](%x), Vy(%x)\n", x, v[x], v[y]);
				if(v[x] == v[y])
					pc += 2;
				break;
			case 0x6000:
				System.out.printf("LD Vx[%x](%x), kk(%x)\n", x, v[x], kk);
				v[x] = kk;
				break;
			case 0x7000:
				System.out.printf("ADD Vx[%x](%x), byte(%x)\n", x, v[x], kk);
				v[x] = (short) ((v[x] + kk) & 0xFF);
				break;
			case 0x8000:
				switch(opcode & 0xF) {
					case 0x0:
						System.out.printf("LD Vx[%x](%x), Vy(%x)\n", x, v[x], v[y]);
						v[x] = v[y];
						break;
					case 0x1:
						System.out.printf("OR Vx[%x](%x), Vy(%x)\n", x, v[x], v[y]);
						v[x] |= v[y];
						break;
					case 0x2:
						System.out.printf("AND Vx[%x](%x), Vy(%x)\n", x, v[x], v[y]);
						v[x] &= v[y];
						break;
					case 0x3:
						System.out.printf("XOR Vx[%x](%x), Vy(%x)\n", x, v[x], v[y]);
						v[x] ^= v[y];
						break;
					case 0x4:
						v[0xF] = (short) ((v[x] + v[y]) > 0xFF ? 1 : 0);
						System.out.printf("ADD Vx[%x](%x), Vy(%x). Carry(%x)\n", x, v[x], v[y], v[0xF]);
						v[x] = (short) ((v[x] + v[y]) & 0xFF);
						break;
					case 0x5:
						v[0xF] = (short) (v[x] > v[y] ? 1 : 0);
						System.out.printf("SUB Vx[%x](%x), Vy(%x). Carry(%x)\n", x, v[x], v[y], v[0xF]);
						v[x] = (short) ((v[x] - v[y]) & 0xFF);
						break;
					case 0x6:
						v[0xF] = (short) ((v[x] & 0x1) == 1 ? 1 : 0);
						System.out.printf("SHR Vx[%x](%x) {, Vy(%x)}. Carry(%x)\n", x, v[x], v[y], v[0xF]);
						v[x] = (short) ((v[x] >> 0x1) & 0xFF);
						break;
					case 0x7:
						v[0xF] = (short) (v[y] > v[x] ? 1 : 0);
						System.out.printf("SUBN Vx[%x](%x), Vy(%x). Carry(%x)\n", x, v[x], v[y], v[0xF]);
						v[x] = (short) ((v[y] - v[x]) & 0xFF);
					case 0xE:
						v[0xF] = (short) ((v[x] >> 0x7) == 1 ? 1 : 0);
						System.out.printf("SHL Vx[%x](%x) {, Vy(%x)}. Carry(%x)\n", x, v[x], v[y], v[0xF]);
						v[x] = (short) ((v[x] << 0x1) & 0xFF);
						break;
					default:
						System.err.printf("Opcode[%x] not implemented.\n", (int) opcode);
						System.exit(1);
						break;
				}
				break;
			case 0x9000:
				System.out.printf("SNE Vx[%x](%x), Vy(%x)\n", x, v[x], v[y]);
				if(v[x] != v[y])
					pc += 2;
				break;
			case 0xA000:
				System.out.printf("LD I(%x), addr(%x)\n", (int) i, (int) nnn);
				i = (char) nnn;
				break;
			case 0xB000:
				System.out.printf("JP V0(%x), addr(%x)\n", v[0], (int) nnn);
				pc = (char) ((v[0] + nnn) & 0xFFF);
				break;
			case 0xC000:
				System.out.printf("RND Vx[%x](%x), byte(%x)\n", x, v[x], kk);
				v[x] = (short) ((int)(Math.random() * 10) & kk);
				break;
			case 0xD000:
				System.out.printf("DRW Vx[%x](%x), Vy[%x](%x), nibble(%x)\n", x, v[x], y, v[y], n);

				for (byte i = 0; i < n; i++) {
					short sprite = memory[this.i + i];
					for (byte j = 0; j < 8; j++) {
						byte px = (byte) ((v[x] + j) & 0x3F);
						byte py = (byte) ((v[y] + i) & 0x1F);
						pixels[0x40 * py + px] ^= (sprite & (1 << (7-j))) != 0;
					}
				}
				try{
					Thread.sleep(1000/60);
				}catch(InterruptedException e){

				}
				panel.repaint();
				break;
			case 0xE000:
				switch(opcode & 0xFF) {
					case 0x9E:
						System.out.printf("SKP Vx[%x](%x)\n", x, v[x]);
						if(window.getKeys()[v[x]][1] == 1)
							pc += 2;
						break;
					case 0xA1:
						System.out.printf("SKNP Vx[%x](%x)\n", x, v[x]);
						if(window.getKeys()[v[x]][1] != 1)
							pc += 2;
						break;
				}
				break;
			case 0xF000:
				switch(opcode & 0xFF) {
					case 0x07:
						System.out.printf("LD Vx[%x](%x), DT(%x)\n", x, v[x], delayTimer);
						v[x] = delayTimer;
						break;
					case 0x0A:
						System.out.printf("LD Vx[%x](%x), k(%x)\n", x, v[x], 0x000000000000000);
						System.exit(2);
						break;
					case 0x15:
						System.out.printf("LD DT(%x), Vx[%x](%x)\n", delayTimer, x, v[x]);
						delayTimer = (byte) v[x];
						break;
					case 0x18:
						System.out.printf("LD ST(%x), Vx[%x](%x)\n", soundTimer, x, v[x]);
						soundTimer = (byte) v[x];
						break;
					case 0x1E:
						System.out.printf("ADD I(%x), Vx[%x](%x)\n", i, (int)x, v[x]);
						i = (char) ((i + v[x]) & 0xFFF);
						break;
					case 0x29:
						System.out.printf("LD F(%x), Vx[%x](%x)\n", 0x000000000000000, x, v[x]);
						i = (char) ((v[x] & 0xF) * 5);
						break;
					case 0x33:
						System.out.printf("LD B(%x), Vx[%x](%x)\n", 0x000000000000000, x, v[x]);
						memory[i] = (short) (v[x] / 100);
						memory[i + 1] = (short) ((v[x] / 10) % 0xA);
						memory[i + 2] = (short) (v[x] % 0xA);
						break;
					case 0x55:
						System.out.printf("LD [I], Vx[%x](%x)\n", x, v[x]);
						for (byte i = 0; i <= x; i++)
							memory[this.i + i] = v[i];
						break;
					case 0x65:
						System.out.printf("LD Vx[%x](%x), [I]\n", x, v[x]);
						for (byte i = 0; i <= x; i++)
							 v[i] = memory[this.i + i];
						break;
				}
				break;
			default:
				System.err.printf("Opcode[%x] not implemented.\n", (int) opcode);
				System.exit(1);
				break;
		}

		if(delayTimer != 0)
			delayTimer--;
		if(soundTimer != 0)
			soundTimer--;
	}
}
