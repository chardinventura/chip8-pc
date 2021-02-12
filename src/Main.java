import java.io.IOException;

public class Main {

	public static void main(String[] args){

		Emulator emulator = new Emulator();
		emulator.init();

		try{
			emulator.load_memory(args[0]);
		}catch(IOException e) {
			System.err.println("Rom[".concat(args[0]).concat("] not found."));
			System.exit(1);
		}

		while(true)
			emulator.run();
	}
}
