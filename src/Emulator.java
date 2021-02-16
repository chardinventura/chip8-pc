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
					// CLS
					case 0xE0:
						Arrays.fill(pixels, false);
						panel.repaint();
						System.exit(4);
						break;
					case 0xEE:
						// RET
						if(sp > 0)
							pc = stack[--sp];
						break;
				}
				break;
			// JP addr
			case 0x1000:
				pc = (char) nnn;
				break;
			// CALL addr
			case 0x2000:
				if(sp < (STACK_SIZE - 1))
					stack[sp++] = pc;
				pc = (char) nnn;
				break;
			// SE Vx, byte
			case 0x3000:
				if(v[x] == kk)
					pc += 2;
				break;
			// SNE Vx, byte
			case 0x4000:
				if(v[x] != kk)
					pc += 2;
				break;
			// SE Vx, Vy
			case 0x5000:
				if(v[x] == v[y])
					pc += 2;
				break;
			// LD Vx, kk
			case 0x6000:
				v[x] = kk;
				break;
			// ADD Vx, byte
			case 0x7000:
				v[x] = (short) ((v[x] + kk) & 0xFF);
				break;
			case 0x8000:
				switch(opcode & 0xF) {
					// LD Vx, Vy
					case 0x0:
						v[x] = v[y];
						break;
					// OR Vx, Vy
					case 0x1:
						v[x] |= v[y];
						break;
					// AND Vx, Vy
					case 0x2:
						v[x] &= v[y];
						break;
					// XOR Vx, Vy
					case 0x3:
						v[x] ^= v[y];
						break;
					// ADD Vx, Vy
					case 0x4:
						v[0xF] = (short) ((v[x] + v[y]) > 0xFF ? 1 : 0);
						v[x] = (short) ((v[x] + v[y]) & 0xFF);
						break;
					// SUB Vx, Vy
					case 0x5:
						v[0xF] = (short) (v[x] > v[y] ? 1 : 0);
						v[x] = (short) ((v[x] - v[y]) & 0xFF);
						break;
					// SHR Vx {, Vy}
					case 0x6:
						v[0xF] = (short) ((v[x] & 0x1) == 1 ? 1 : 0);
						v[x] = (short) ((v[x] >> 0x1) & 0xFF);
						break;
					// SUBN Vx, Vy
					case 0x7:
						v[0xF] = (short) (v[y] > v[x] ? 1 : 0);
						v[x] = (short) ((v[y] - v[x]) & 0xFF);
					// SHL Vx {, Vy}
					case 0xE:
						v[0xF] = (short) ((v[x] >> 0x7) == 1 ? 1 : 0);
						v[x] = (short) ((v[x] << 0x1) & 0xFF);
						break;
					default:
						System.err.printf("Opcode[%x] not implemented.\n", (int) opcode);
						System.exit(1);
						break;
				}
				break;
			// SNE Vx, Vy
			case 0x9000:
				if(v[x] != v[y])
					pc += 2;
				break;
			// LD I, addr
			case 0xA000:
				i = (char) nnn;
				break;
			// JP V0, addr
			case 0xB000:
				pc = (char) ((v[0] + nnn) & 0xFFF);
				break;
			// RND Vx, byte
			case 0xC000:
				v[x] = (short) ((int)(Math.random() * 10) & kk);
				break;
			// DRW Vx, Vy, nibble
			case 0xD000:
				for (byte i = 0; i < n; i++) {
					short sprite = memory[this.i + i];
					for (byte j = 0; j < 8; j++) {
						byte px = (byte) ((v[x] + j) & 0x3F);
						byte py = (byte) ((v[y] + i) & 0x1F);
						pixels[0x40 * py + px] ^= ((sprite >> (7-j)) & 0x1) != 0;
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
					// SKP Vx
					case 0x9E:
						if(window.getKeys()[v[x]][1] == 1)
							pc += 2;
						break;
					// SKNP Vx
					case 0xA1:
						if(window.getKeys()[v[x]][1] != 1)
							pc += 2;
						break;
				}
				break;
			case 0xF000:
				switch(opcode & 0xFF) {
					// LD Vx, DT
					case 0x07:
						v[x] = delayTimer;
						break;
					// LD Vx, k
					case 0x0A:
						// TODO
						System.exit(2);
						break;
					// LD DT, Vx
					case 0x15:
						delayTimer = (byte) v[x];
						break;
					// LD ST, Vx
					case 0x18:
						soundTimer = (byte) v[x];
						break;
					// ADD I, Vx
					case 0x1E:
						i = (char) ((i + v[x]) & 0xFFF);
						break;
					// LD F, Vx
					case 0x29:
						i = (char) ((v[x] & 0xF) * 5);
						break;
					// LD B, Vx
					case 0x33:
						memory[i] = (short) (v[x] / 100);
						memory[i + 1] = (short) ((v[x] / 10) % 0xA);
						memory[i + 2] = (short) (v[x] % 0xA);
						break;
					// LD [I], Vx
					case 0x55:
						for (byte i = 0; i <= x; i++)
							memory[this.i + i] = v[i];
						break;
					// LD Vx, [I]
					case 0x65:
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
