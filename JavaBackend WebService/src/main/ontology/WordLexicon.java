package main.ontology;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import main.settings.LexicalType;
import main.util.StopList;
import main.util.Table2Map;
import main.util.Table2Set;
import main.util.Table3Set;

public class WordLexicon
{

//Attributes

	//The maximum size of class blocks
	private final int MAX_BLOCK_SIZE = 10000;
	//A link to the original Lexicon
	private Lexicon lex;
	//The list of stop words to ignore when building this WordLexicon
	private Set<String> stopSet;
	//The map of blocks (Integer) to words (String) to classes (Integer)
	private Table3Set<Integer,String,Integer> wordClasses;
	//The map of classes (Integer) to words (String) with weights (Double)
	private Table2Map<Integer,String,Double> classWords;
	//The map of names (String) to words (String)
	private Table2Set<String,String> nameWords;
	//The map of word (String) evidence contents (Double)
	private HashMap<String,Double> wordECs;
	//The map of class (Integer) total evidence contents (Double), which is the
	//sum of evidence contents of all its words (multiplied by frequency)
	private HashMap<Integer,Double> classECs;
	//The map of name (String) evidence contents (Double), which is the sum
	//of evidence contents of all its words (multiplied by frequency)
	private HashMap<String,Double> nameECs;
	//Auxiliary count of words entered into the WordLexicon
	private int total;
	
//Constructors

	/**
	 * Constructs a new WordLexicon from the given Lexicon
	 * @param l: the Lexicon from which the WordLexicon is derived
	 */
	public WordLexicon(Lexicon l)
	{
		lex = l;
		init();
	}
	
	
	//Builds the WordLexicon from the original Lexicon
		private void init()
		{
			//Initialize the data structures
			stopSet = StopList.read();
			wordClasses = new Table3Set<Integer,String,Integer>();
			classWords = new Table2Map<Integer,String,Double>();
			nameWords = new Table2Set<String,String>();
			wordECs = new HashMap<String,Double>();
			classECs = new HashMap<Integer,Double>();
			nameECs = new HashMap<String,Double>();
			total = 0;
			//Get the classes from the Lexicon
			Set<Integer> classes = lex.getClasses();
			//For each class
			for(Integer c: classes)
			{
				//Get all names 
				Set<String> names;
				names = lex.getNames(c);
				if(names == null)
					continue;
				//And add the words for each name 
				for(String n: names)
					if(!lex.getTypes(n,c).contains(LexicalType.FORMULA))
						addWords(n, c);
			}
			//Compute the maximum EC
			double max = Math.log(total);
			//Compute and store the normalized EC for
			//each word in the WordLexicon
			for(String w : wordECs.keySet())
			{
				double ec = 1 - (Math.log(wordECs.get(w)) / max);
				wordECs.put(w, ec);
			}
			//The total EC for each class
			for(Integer i : classWords.keySet())
			{
				double ec = 0.0;
				for(String w : classWords.keySet(i))
					ec += wordECs.get(w) * getWordWeight(w, i);
				classECs.put(i, ec);
			}
			//And the total EC for each name
			for(String n : nameWords.keySet())
			{
				double ec = 0.0;
				for(String w : nameWords.get(n))
					ec += wordECs.get(w);
				nameECs.put(n, ec);
			}
		}
		
		
		//Adds all words for a given name and classId
		private void addWords(String name, int classId)
		{
			String[] words = name.split(" ");
			for(String w : words)
			{
				String word = w.replaceAll("[()]", "");
				if(stopSet.contains(word) || word.length() < 2 || !word.matches(".*[a-zA-Z].*"))
					continue;
				//Get the current block number (as determined by the number of classes)
				int block = classWords.keySet().size()/MAX_BLOCK_SIZE;
				//Add the block-word-class triple
				wordClasses.add(block,word,classId);
				//Update the current weight of the word for the classId
				Double weight = classWords.get(classId,word);
				if(weight == null)
					weight = lex.getCorrectedWeight(name, classId);
				else
					weight += lex.getCorrectedWeight(name, classId);
				//Add the class-word-weight triple
				classWords.add(classId, word, weight);
				//Add the name-word pair
				nameWords.add(name, word);
				//Update the word frequency
				Double freq = wordECs.get(word);
				if(freq == null)
					freq = 1.0;
				else
					freq++;
				wordECs.put(word,freq);
				//Update the total;
				total++;
			}
		}
		
		
		/**
		 * @param word: the word to search in the WordLexicon
		 * @param classId: the class to search in the WordLexicon
		 * @return the weight of the word for the class in the WordLexicon
		 */
		public double getWordWeight(String word, int classId)
		{
			if(!classWords.contains(classId, word))
				return -1.0;
			return classWords.get(classId, word);
		}
		
		
		/**
		 * @return the number of blocks in the WordLexicon
		 */
		public int blockCount()
		{
			return wordClasses.keyCount();
		}
		
		
		/**
		 * @return the table of words for a given block of classes
		 */
		public Table2Set<String,Integer> getWordTable(int block)
		{
			return wordClasses.get(block);
		}
		
		
		/**
		 * @param w: the word to search in the WordLexicon
		 * @return the EC of the given word
		 */
		public double getWordEC(String w)
		{
			if(wordECs.containsKey(w))
				return wordECs.get(w);
			return -1.0;
		}
		
		
		/**
		 * @param classId: the class to search in the WordLexicon
		 * @return the EC of the given class
		 */
		public double getClassEC(int classId)
		{
			if(classECs.containsKey(classId))
				return classECs.get(classId);
			return -1.0;
		}
		
		
		/**
		 * @return the set of names for the given classId
		 */
		public Set<String> getNames(int classId)
		{
			HashSet<String> names = new HashSet<String>();
			for(String n : lex.getNames(classId))
				if(nameWords.contains(n))
					names.add(n);
			return names;
		}
		
		
		/**
		 * @param name: the name to search in the WordLexicon
		 * @param classId: the class to search in the WordLexicon
		 * @return the weight of the name for the class in the WordLexicon
		 */
		public double getNameWeight(String name, int classId)
		{
			return lex.getCorrectedWeight(name,classId);
		}
		
		
		/**
		 * @return the set of words for the given classId
		 */
		public Set<String> getWords(String name)
		{
			if(!nameWords.contains(name))
				return new HashSet<String>();
			return new HashSet<String>(nameWords.get(name));
		}
		
		
		/**
		 * @param n: the name to search in the WordLexicon
		 * @return the EC of the given name
		 */
		public double getNameEC(String n)
		{
			if(nameECs.containsKey(n))
				return nameECs.get(n);
			return -1.0;
		}
		
		
		/**
		 * @return the set of words for the given classId
		 */
		public Set<String> getWords(int classId)
		{
			if(!classWords.contains(classId))
				return new HashSet<String>();
			return classWords.keySet(classId);
		}

}
