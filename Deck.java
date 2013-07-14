
/**
 * a list of cards (player hand, actually deck, etc!)
 */
interface Deck extends Iterable<Card> {

	//shuffle the active cards
	public void shuffle();

	public void add(Card c);

	//put on top...
	public void addTop(Card c);

	//pub on bottom
	public void addBottom(Card c);

	public Card pick();

	public Card pickTop();
	
	public Card pickBottom();

	public Card pick(int n);

	public Card[] pick(int picks[]);

	public int count();

	//merge deck D into this deck
	public void merge(Deck d);

}
