import com.coldsteelstudios.irc.*;
import com.coldsteelstudios.irc.client.*;

import java.util.Vector;

public class Client {
	private Connection irc;
	private SyncManager sync;

	private String host;
	private String nick;
	private int port;

	private Vector<Plugin> plugins = new Vector<Plugin>();

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
		
		for (Plugin p : plugins)
			p.init(this);
	
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
		plugins.add(p);
	}
}
