import com.coldsteelstudios.irc.*;
import com.coldsteelstudios.irc.client.*;

import java.util.StringTokenizer;
import java.util.Vector;

public class Cards implements Plugin {

	private Connection irc;
	private SyncManager sync;

	private String channel;

	private final int MAX_SYNC_TIME = 30;

	private final String BLANK = "_____";

	private CardsGame game;

	public Cards() {}

	public Cards(String channel) {
		this.channel = channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public void createGame(String blackFile, String whiteFile) throws java.io.IOException {
		game = new CardsGame(new FileDeck(blackFile), new FileDeck(whiteFile));
	}

	public void init(Client client) {
		irc = client.getConnection();
		sync = client.getSyncManager();

		irc.addMessageHandler(cmdHandler).addType(MessageType.QUERY);
		irc.addMessageHandler(initHandler).addCode( MessageCode.RPL_WELCOME );
	}
	
	private void setup() {
		irc.join(channel);
		
		//set bot mode (unrealircd only?)
		//@TODO umode in irc lib and set this via the client.
		irc.send("MODE",irc.nick(), "+B");

		Channel chan;
		int tries = 0;
		while ( (chan = sync.getChannel(channel) ) == null && tries < MAX_SYNC_TIME ) 
			try { Thread.sleep(1); } catch (Exception e) {}
		
		if ( chan == null )
			throw new RuntimeException("Failed to sync...");

		chan.addChannelListener( channelListener );
	}

	/**
	 * Join nick to game if game is not full.
	 */
	private void join(String nick) {

		if ( game.getState() == CardsGame.GameState.END )
			return;

		if ( game.numPlayers() < game.MAX_PLAYERS ) {
			game.join(nick);
			pubmsg(nick + " has joined the game!");

			if ( game.getState() == CardsGame.GameState.START && game.numPlayers() >= game.MIN_PLAYERS ) 
				pubmsg("Send !start to begin the game.");

		} else {
			privmsg(nick, "Game full.");
		}
	}

	/**
	 * Show nick his white cards.
	 */
	private void showHand(String nick) {
		CardsPlayer player = game.getPlayer(nick);
		if ( player == null ) return;

		privmsg(nick, formatQuestion());
		int i = 0;
		for ( Card card : player.getWhite() )
			privmsg(nick, "["+ (i++) +"] " + card.getText());
	}

	private void stop() {
		game.reset();
	}

	private void start() {
		if ( game.getState() == CardsGame.GameState.START && game.numPlayers() >= game.MIN_PLAYERS ) {
			game.start();
			pubmsg("New game begins!");
			startRound();
		} else {
			game.reset();
			pubmsg("New game started. Send !join to join.");
		}
	}

	private void privmsg(String nick, String msg) {
		irc.msg(nick,msg);
	}

	private void privmsg(User u, String msg) {
		privmsg(u.getNick(),msg);
	}

	private void privnotice(User u, String msg) {
		privnotice(u.getNick(),msg);
	}

	private void privnotice(String nick, String msg) {
		irc.notice(nick,msg);
	}

	private void pubmsg(String msg) {
		irc.msg(channel, msg);
	}

	private void leaveGame(User u, String reason) {
		if ( game.leave(u.getNick()) )
			pubmsg(u.getNick() + " has left the game (" + reason+")");
	}

	private void startRound() {
		game.startRound();
	
		pubmsg(String.format(">>> %s ------------ [%s] -------------", bold("New round!"), game.getCzar().getName()));

		pubmsg(formatQuestion());

		pubmsg( String.format( "[%s] Pick %d cards!", game.getCzar().getName(), game.getBlanks()) );
	}

	private static String fillBlanks(String text, String ... blanks) {
		return String.format(text, (Object[])blanks);
	}

	private static String[] arrayFill(String fill, int n) {
		String ret[] = new String[n];
		for ( int i = 0; i < n; ++i )
			ret[i] = fill;

		return ret;

	}
	private String formatQuestion() {
		Card black = game.getBlackCard();
		String msg = fillBlanks(black.getText(), arrayFill(BLANK, game.getBlanks()));
		return String.format( "[%s] %s", game.getCzar().getName(), msg) ;
	}

	private void pick(String nick, int picks[]) {
		if ( game.getState() != CardsGame.GameState.PLAYING ) {
			privnotice(nick, "No game in progress!");
			return;
		}

		if ( game.getRoundState() == CardsGame.RoundState.PICK_WAIT && game.getPlayer(nick) != null ) {
			doPlayerPicks(nick, picks);
		} else if ( game.getRoundState() == CardsGame.RoundState.END && nick.equals(game.getCzar().getName()) ) {
			int pick = picks[0];
			
			String answer = game.getBlackCard().getText();

			CardsPlayer winner = game.chooseWinner(pick);
			
			answer = subPicks(answer, winner.getPicks());

			pubmsg(">>> " + answer);
			pubmsg(String.format("[%s] Round over! %s wins the round!", game.getCzar().getName(), winner.getName()));
			
			startRound();
		}
	}

	private void doPlayerPicks(String nick, int picks[]) {
		if ( game.isCzar(nick) ) {
			privnotice(nick, "The Card Czar cannot respond.");
		} else if ( game.getBlanks() != picks.length ) {
			privnotice(nick, "You must pick " + game.getBlanks() + " cards (e.g. pick 1 2)");
		} else {

			if ( game.pickWhite(nick, picks) ) 
				privnotice(nick, "Picks recorded!");
		
			if ( game.getRoundState() == CardsGame.RoundState.END ) 
					showResponses();
		}
	}

	public String subPicks(String text, Deck picks) {
			String blanks[] = new String[picks.count()];
			int n = 0;
			for ( Card card : picks )
				blanks[n++] = bold(card.getText());

			return fillBlanks(text,blanks);
	}



	private void showResponses() {
		pubmsg("---------- Responses: ---------- ");

		String black = game.getBlackCard().getText();

		int i = 0;
		for ( CardsPlayer player : game.iterChoices() ) {
			String answer = String.format("[%s] [%d] %s", game.getCzar().getName(),i++,black);

			answer = subPicks(answer, player.getPicks());
			pubmsg(answer);
		}

		privnotice(game.getCzar().getName(), "Choose your favorite response by sending !pick N");

	}

	private void handleCmd(String nick, String msg, boolean priv ) {
		if ( msg.equals("start") ) {
			start();
		} else if (msg.equals("join") && !priv)  { 
			join(nick);
		} else if ( msg.equals("show") ) {
			showHand(nick);
		} else if ( msg.startsWith("pick") ) {
			//format: pick 1,2,3...
			try {
				pick(nick, parsePicks(msg.substring(5)));
			} catch (Exception e ) {
				//DGAF
			}
		} else if ( msg.startsWith("help") ) {
			help(nick);
		} else if ( msg.startsWith("stop") ) {
			stop();
		}
	}
	
	private int[] parsePicks(String msg) {
		int picks[] = new int[game.getBlanks()];
		int count = 0;

		StringTokenizer st = new StringTokenizer(msg);
		while (st.hasMoreTokens() && count < picks.length) 
			picks[count++] = Integer.parseInt(st.nextToken().trim());

		int picks_sized[] = new int[count];
		System.arraycopy(picks,0,picks_sized,0,count);
		return picks_sized;
	}

	private boolean userOnChannel(String nick) {
		for ( User u : sync.getChannel(channel) ) {
			if ( u.getNick().equals(nick) )
				return true;
		}
		return false;
	}

	private static String bold(String txt) {
		return '\002'+txt+'\002';
	}

	private static String cmdhelp(String cmd, String help) {
		return bold("!"+cmd) + " - " + help;
	}

	private void help(String nick) {
		privmsg(nick, "--- Cards Against Jaundies Bot v0.1 ---");
		privmsg(nick, cmdhelp("join", "Joins the game (if one is running"));
		privmsg(nick, cmdhelp("start", "Starts a new game. After at least " + game.MIN_PLAYERS + " join, sending !start again begins the game."));
		privmsg(nick, cmdhelp("show","Shows your hand and the current black card."));
		privmsg(nick, cmdhelp("pick","Pick card(s) or an option. Examples: !pick 0, !pick 0 1"));
		privmsg(nick, "----------------------------------------------------------------");
		privmsg(nick, "Legal bullshit: The content of this game and parts of the content are based on Cards Against Humanity (http://cardsagainsthumanity.com)");

	}

	private MessageHandler cmdHandler = new MessageHandler() {
		public void handle(MessageEvent e) {
			Message m = e.getMessage();
			MessageTarget dst = m.getTarget();
			String srcNick = m.getSource().getNick();
			String msg = m.getMessage().trim();

			if ( dst.scope(MessageTarget.Scope.CHANNEL) ) {
				if ( dst.getChannel().equals(channel) && msg.startsWith("!") )
					handleCmd(srcNick, msg.substring(1), false);
			} else if ( userOnChannel(srcNick) ) {
				handleCmd(srcNick, msg.startsWith("!") ? msg.substring(1) : msg, true);
			}
		}
	};

	private MessageHandler initHandler = new MessageHandler() {
		public void handle(MessageEvent e) {
			setup();
		}
	};

	private ChannelListener channelListener = new ChannelAdapter() {
		public void kick(Channel c, User u) {
			leaveGame(u, "kicked from the channel");
		}

		public void join(Channel c, User u) {
			//@todo game in progress?
			privnotice(u, "["+channel+"] Welcome to Cards Against Jaundies on "+channel+".");
			privnotice(u, "["+ channel+"] By remaining on this channel you foreit your right to be offended.");
			privnotice(u, "[" +channel+"] Send !help for help.");
		}

		public void nick(Channel c, User u, String oldnick) {
			game.nick(oldnick, u.getNick());
		}

		public void part(Channel c, User u) {
			leaveGame(u, "left the channel");
		}
	};

}
