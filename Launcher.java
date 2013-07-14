import com.coldsteelstudios.irc.*;
import com.coldsteelstudios.irc.client.*;

public class Launcher {

	Connection irc;
	SyncManager sync;

	public static void main(String[] args) {
		new Launcher();
	}

	private Launcher() {
		irc = new Connection("sphinx.jaundies.com", 6667, "cards");
		sync = new SyncManager(irc);

		try {
			irc.connect();
		} catch (ConnectionException e) {
			printException(e);
			System.exit(1);
		}

		new Cards(irc,sync,"#cards", "blacks.txt", "whites.txt");
	}

	/**
	 * prints an exception to the debug window.
	 */
	private static void printException(Throwable e) {
		System.err.println( e.toString() );

		for (StackTraceElement st : e.getStackTrace())
			System.err.println( st.toString() );

	}
}
