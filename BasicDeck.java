import java.util.Iterator;

import java.util.Vector;
import java.util.Collections;

class BasicDeck implements Deck {

	protected Vector<Card> cards = new Vector<Card>();

	public BasicDeck() {}

	//shuffle the active cards
	public void shuffle() {
		Collections.shuffle(cards);
	}

	public Card pick() {
		return pickTop();
	}

	public void add(Card c) {
		addTop(c);		
	}

	//put on top...
	public void addTop(Card c) {
		cards.add(0,c);
	}

	//pub on bottom
	public void addBottom(Card c) {
		cards.add(c);
	}


	public Card pickTop() {
		return cards.remove(0);
	}
	
	public Card pickBottom() {
		return cards.remove(cards.size()-1);
	}

	public Card pick(int n) {
		return cards.get(n);
	}

	public Card[] pick(int picks[]) {
		Card ret[] = new Card[picks.length];
		
		int cnt = 0;
		for ( int n : picks ) 
			ret[cnt++] = cards.get(n);

		for ( Card c : ret )
			cards.remove(c);

		return ret;
	}

	public int count() {
		return cards.size();
	}

	//merge deck D into this deck
	public void merge(Deck d) {

		while ( d.count() > 0 ) 
			cards.add(d.pick());
	}

	public Iterator<Card> iterator() {
		return cards.iterator();
	}

	protected static class CardImpl implements Card {
		String text;
		public CardImpl(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}

}
