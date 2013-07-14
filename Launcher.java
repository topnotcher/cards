import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class Launcher {

	private static final String CONFIG_FILE = "config.xml";

	public static void main(String[] args) {
		XMLDecoder d;
		try {
			d = new XMLDecoder(new BufferedInputStream(new FileInputStream(CONFIG_FILE)));
		} catch( IOException e ) {
			System.err.println("Error reading config file: "+e.getMessage());
			return;
		}

		while (true) try {
			//just read in a bunch of objects. 
			//The method calls are all via the config
			d.readObject();
		} catch (ArrayIndexOutOfBoundsException e) {
			//normal behavior
			break;
		} catch (Exception e) {
			System.err.println("Error reading beans: "+ e.getMessage());
			break;
		}
		try { d.close(); } catch (Exception e) {}
	}

}
