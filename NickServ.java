import com.coldsteelstudios.irc.*;

public class NickServ implements Plugin {
	private Connection irc;

	private final String prompts[] = new String[]{
		".*This nickname is registered and protected.*",
		".*This nick is owned by someone else.*"
	};

	private final String NICKSERV = "NickServ";
	private final String IDENTIFY = "identify";

	private String pass = null;

	public NickServ() {}

	public void setPassword(String pass) {
		this.pass = pass;
	}

	public void init(Client client) {
		irc = client.getConnection();

		Connection.IrcMessageSubscription sub = irc.addMessageHandler(identifier);
		sub.addType( MessageType.NOTICE );

		for ( String ptn : prompts )
			sub.addPattern( java.util.regex.Pattern.compile(ptn) );
	}

	private MessageHandler identifier = new MessageHandler() {
		public void handle(MessageEvent e) {
			MessageTarget src = e.getMessage().getSource();
			if ( !src.scope(MessageTarget.Scope.NICK) || !src.getNick().equals(NICKSERV) || pass == null ) return;

			irc.msg(NICKSERV, IDENTIFY +" "+ pass);
		}
	};
}
