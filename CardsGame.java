import java.util.Vector;
import java.util.List;
import java.util.Collections;

class CardsGame {

	public enum GameState {START,PLAYING,END;}
	public enum RoundState {START,PICK_WAIT,END,NONE;}

	private GameState state;

	/**
	 * ROUND SHIT
	 */
	private RoundState roundState;
	private int czar = -1;
	private Card blackCard;
	private int pickNum = 0;
	private Vector<CardsPlayer> choices;

	public static final int MAX_PLAYERS = 10;
	public static final int MIN_PLAYERS = 2;
	private static final int HAND_SIZE = 10;

	private Deck blacks;
	private Deck whites;

	//white discard pile
	private Deck discard = new BasicDeck();

	//used if a player leaves the game!
	private Deck forfeit = new BasicDeck();

	private List<CardsPlayer> players = new Vector<CardsPlayer>();

	public CardsGame(Deck blacks, Deck whites) {
		this.blacks = blacks;
		this.whites = whites;
		end();
	}

	public void end() {
		state = GameState.END;
		roundState = RoundState.NONE;
		czar = -1;
	}

	public Iterable<CardsPlayer> iterChoices() {
		return (Iterable<CardsPlayer>)choices;
	}
	
	public CardsPlayer chooseWinner(int n) {
		if ( n >= choices.size() ) throw new RuntimeException("fuck you");

		CardsPlayer player =  choices.get(n);

		player.getBlack().add(blackCard);
		blackCard = null;

		return player;
	}

	public void reset() {
		whites.merge(discard);
		blacks.merge(forfeit);

		for (CardsPlayer p : players) {
			blacks.merge(p.getBlack());
			whites.merge(p.getWhite());
			whites.merge(p.getPicks());
		}

		if ( blackCard != null )
			blacks.add(blackCard);

		whites.shuffle();
		blacks.shuffle();
		players.clear();
		state = GameState.START;
		roundState = RoundState.NONE;
		czar = -1;
	}

	public void start() {
		state = GameState.PLAYING;
		roundState = RoundState.START;
	}
	
	public CardsPlayer getPlayer(String name) {
		for ( CardsPlayer player  : players ) 
			if (player.getName().equals(name))
				return player;

		return null;
	}

	public void pickWhite(String nick, int picks[]) {
		CardsPlayer player = getPlayer(nick);
		if ( player == null ) return;
		
		if ( player.getPicks().count() == 0 && !player.getName().equals(getCzar().getName())) {

			for ( Card c : player.getWhite().pick(picks) )
				player.getPicks().addBottom(c);
		}

		//now check to see if we should transition into the END state
		int count = 0;
		for (CardsPlayer p : players )
			if (p.getPicks().count() != 0) count++;

		if ( count == players.size()-1) {
			roundState = RoundState.END;
			choices = new Vector<CardsPlayer>(players.size()-1);

			for ( int i = 0; i < players.size(); i++ ) {
				if ( i == czar ) continue;
				choices.add(players.get(i));
			}

			Collections.shuffle(choices);
		}
	

	}

	public void startRound() {

		if ( czar == -1 ) 
			czar = 0;
		//@TODO completely ignored case where czar leaves the game 
		//this will work, but cause a skip...
		else 
			czar = ( czar >= (players.size()-1) ) ? 0 : czar+1;

		//JUST in case!
		if ( blackCard != null )
			blacks.add(blackCard);

		blackCard = blacks.pick();
		pickNum = countBlanks(blackCard.getText());
		roundState = RoundState.PICK_WAIT;
		discardPicks();
		topHands();
	}

	//current czar
	public CardsPlayer getCzar() {
		return players.get(czar);
	}

	//current black
	public Card getBlackCard() {
		return blackCard;
	}
	
	//number of blanks in blackCard
	public int getBlanks() {
		return pickNum;
	}

	//count blanks in black (count %s)
	//ODD that this is in here kind of? yeah, it is!
	private int countBlanks(String str) {
		int count = 0;
		int from = 0;
		while ( (from = str.indexOf("%s", from)) >= 0 ) {
			count++;
			from += 2;
		}
		return count;
	}

	public GameState getState() {
		return state;
	}

	public RoundState getRoundState() {
		return roundState;
	}

	public void join(String nick) {
		CardsPlayer player;

		for (CardsPlayer p : players) 
			if (p.getName().equals(nick)) {
				player = p;
				return;
			}

		player = new CardsPlayer(nick);
		topPlayerHand(player);
		players.add(player);
	}

	public int numPlayers() {
		return players.size();
	}

	private void topPlayerHand(CardsPlayer player) {
		/** @TODO out of cards? */
		while (player.getWhite().count() < HAND_SIZE)
			player.getWhite().add( whites.pick() );
	}

	private void discardPicks() {
		for (CardsPlayer p : players)
			discard.merge(p.getPicks());
	}

	private void topHands() {

		for (CardsPlayer p : players)
			topPlayerHand(p);
	}

	public void leave(String nick) {

		CardsPlayer player = null;
		
		for ( int i = 0; i < players.size(); i++ ) {
			player = players.get(i);
			
			if ( nick.equals(player.getName()) ) {
				players.remove(i);
				break;
			} else { 
				player = null;
			}
		}

		if ( player == null ) return;

		discard.merge(player.getWhite());
		forfeit.merge(player.getBlack());

		if ( players.size() < MIN_PLAYERS )
			state = GameState.END;
	}

	public void nick(String oldnick, String newnick) {
		for ( CardsPlayer player : players ) {
			if (player.getName().equals(oldnick))
				player.setName(newnick);
		}
	}	
}
