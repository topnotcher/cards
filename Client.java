import com.coldsteelstudios.irc.*;
import com.coldsteelstudios.irc.client.*;

public class Client {
	private Connection irc;
	private SyncManager sync;

	private String host;
	private String nick;
	private int port;

	public Client() {
	}

	public Client(String host, int port, String nick) throws ConnectionException {
		setHost(host);
		setPort(port);
		setNick(nick);
	}

	public void connect() throws ConnectionException {
		irc = new Connection(host, port, nick);
		sync = new SyncManager(irc);

	
		irc.connect();
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public Connection getConnection() {
		return irc;
	}

	public SyncManager getSyncManager() {
		return sync;
	}

	public void registerPlugin(Plugin p) {
		p.init(this);
	}
}
