
import java.io.BufferedReader;
import java.io.FileReader;

class FileDeck extends BasicDeck {

	public FileDeck(String file) throws java.io.IOException, java.io.FileNotFoundException {
		populateDeck(file);
	}

	private void populateDeck(String file) throws java.io.IOException, java.io.FileNotFoundException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;

		while ((line = br.readLine()) != null) 
			add(new CardImpl(line));
		br.close();
	}
}
