class CardsPlayer {

	private String nick;

	//black card winnings
	private Deck blacks = new BasicDeck();

	//white cards = hand
	private Deck whites = new BasicDeck();

	private Deck picks = new BasicDeck();

	public CardsPlayer(String nick) {
		this.nick = nick;
	}

	public void pick(int n) {
		picks.add(whites.pick(n));
	}

	public Deck getWhite() {
		return whites;
	}

	public Deck getBlack() {
		return blacks;
	}

	public Deck getPicks() {
		return picks;
	}

	public String getName() {
		return nick;
	}

	public void setName(String name) {
		nick = name;	
	}
}
