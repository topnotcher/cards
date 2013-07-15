import com.coldsteelstudios.irc.*;

public class NickServ implements Plugin {
	private Connection irc;

	private final static String prompts[] = new String[]{
		".*This nickname is registered and protected.*",
		".*This nick is owned by someone else.*"
	};

	private final static String ghosted[] = {
		".*Ghost with your nick has been killed.*",
		".*hold on your nick has been released.*"
	};

	private String ghostNick = null;

	private final static String NICKSERV = "NickServ";
	private final static String IDENTIFY = "identify";
	private final static String GHOST = "ghost";
	private final static String RELEASE = "release";

	private String pass = null;

	public NickServ() {}

	public void setPassword(String pass) {
		this.pass = pass;
	}

	public void init(Client client) {
		irc = client.getConnection();
		
		registerIdentifier();
		
		//register ghoster (or releaser?).
		irc.addMessageHandler(ghoster)
			.addCode(MessageCode.ERR_NICKNAMEINUSE)
			.addCode(MessageCode.ERR_NICKCOLLISION)
			.addCode(MessageCode.ERR_ERRONEUSNICKNAME);
		
		registerReclaimer();
	}

	private void registerIdentifier() {
		Connection.IrcMessageSubscription sub = irc.addMessageHandler(identifier);
		sub.addType( MessageType.NOTICE );

		for ( String ptn : prompts )
			sub.addPattern( java.util.regex.Pattern.compile(ptn) );

	}

	private void registerReclaimer() {
		Connection.IrcMessageSubscription sub = irc.addMessageHandler(reclaimer);
		sub.addType( MessageType.NOTICE );

		for ( String ptn : ghosted )
			sub.addPattern( java.util.regex.Pattern.compile(ptn) );

	}

	private static boolean fromNickServ(Message m) {
		MessageTarget src = m.getSource();
		return src.scope(MessageTarget.Scope.NICK) && src.getNick().equals(NICKSERV);
	}

	private MessageHandler identifier = new MessageHandler() {
		public void handle(MessageEvent e) {
			if ( !fromNickServ(e.getMessage()) || pass == null ) return;

			irc.msg(NICKSERV, IDENTIFY +" "+ pass);
		}
	};

	//sphinx.jaundies.com 433 notmario m :Nickname is already in use.
	private MessageHandler ghoster = new MessageHandler() {
		public void handle(MessageEvent e) {
			String nick = e.getMessage().getArg(2);	

			irc.msg(NICKSERV, GHOST +" "+ nick +" "+ pass);
			ghostNick = nick;
		}
	};

	private MessageHandler reclaimer = new MessageHandler() {
		public void handle(MessageEvent e) {
			if ( !fromNickServ(e.getMessage()) ) return;

			if ( ghostNick != null ) 
				irc.nick(ghostNick);

			ghostNick = null;
		}
	};
}
