package main.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;
import main.ontology.Lexicon;
import main.ontology.Ontology;
import main.settings.LexicalType;
import main.settings.StringSimMeasure;
import main.util.ISub;

public class StringAnnotator implements Annotator
{

//Attributes

	private Ontology o;
	private Lexicon lex;
	//Similarity measure
	private StringSimMeasure measure = StringSimMeasure.ISUB;
	//Correction factor (to make string similarity values comparable to word similarity values
	//and thus enable their combination and proper selection; 0.8 is optimized for the ISub measure)
	private final double CORRECTION = 0.80;
	//The available CPU threads
	private int threads;

//Constructors

	/**
	 * Constructs a new ParametricStringMatcher with default
	 * String similarity measure (ISub)
	 */
	public StringAnnotator(Ontology source)
	{
		threads = Runtime.getRuntime().availableProcessors();
		o = source;
		lex = o.getLexicon();
	}


//Public Methods

	/**
	 * Searches for string matches of the target and adds them to the AnnotationSet
	 * @param thresh: the thresh measure to see if the AnnotationCandidate has a weight worth of being added to the AnnotationSet
	 * @param target: the target term to look for
	 * @param source: the source ontology where we look in
	 * @return the AnnotationSet with the string matches that match the target term above that threshold
	 */
	@Override
	public AnnotationSet annotate(double thresh, String target)
	{
		System.out.println("Running String Matcher");
		System.out.println("hello renato");
		long time = System.currentTimeMillis()/1000;
		AnnotationSet a1 = new AnnotationSet();
		Set<Integer> sources = lex.getClasses();
		ArrayList<AnnotatorTask> tasks = new ArrayList<AnnotatorTask>();
		for(Integer i : sources)
		{
			tasks.add(new AnnotatorTask(i, target));
		}
		List<Future<AnnotationCandidate>> results;
		ExecutorService exec = Executors.newFixedThreadPool(threads);
		try
		{
			results = exec.invokeAll(tasks);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
	        results = new ArrayList<Future<AnnotationCandidate>>();
		}
		exec.shutdown();
		for(Future<AnnotationCandidate> fm : results)
		{
			try
			{
				AnnotationCandidate ac = fm.get();
				if(ac.getWeight() >= thresh)
					a1.add(ac);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		time = System.currentTimeMillis()/1000 - time;
		System.out.println("Finished in " + time + " seconds");
		return a1;
	}


//Private Methods

	//Computes the maximum String similarity between two Classes by doing a
	//pairwise comparison of all their names
	private double mapTwoEntities(int sId, String target)
	{
		double maxSim = 0.0;
		double sim;

		Set<String> sourceNames = lex.getNames(sId);
		if(sourceNames == null)
			return maxSim;
		for(String s : sourceNames)
		{
			if(lex.getTypes(s,sId).contains(LexicalType.FORMULA))
				continue;
			if (s.equals(target)) {
				sim = lex.getCorrectedWeight(s, sId);
				return sim;
			}
			sim = lex.getCorrectedWeight(s, sId);
			sim *= stringSimilarity(s,target);
			if(sim > maxSim)
				maxSim = sim;
		}
		return maxSim;
	}

	// Computes the string the similarity between two Strings
	private double stringSimilarity(String s, String t)
	{
		double sim = 0.0;
		if(measure.equals(StringSimMeasure.ISUB))
			sim = ISub.stringSimilarity(s,t);
		else if(measure.equals(StringSimMeasure.EDIT))
		{
			Levenshtein lv = new Levenshtein();
			sim = lv.getSimilarity(s, t);
		}
		else if(measure.equals(StringSimMeasure.JW))
		{
			JaroWinkler jv = new JaroWinkler();
			sim = jv.getSimilarity(s, t);
		}
		else if(measure.equals(StringSimMeasure.QGRAM))
		{
			QGramsDistance q = new QGramsDistance();
			sim = q.getSimilarity(s, t);
		}
		sim *= CORRECTION;
		return sim;
	}

	//Callable class for matching two classes
	private class AnnotatorTask implements Callable<AnnotationCandidate>
	{
		private int source;
		private String target;

		AnnotatorTask(int s, String t)
	    {
			source = s;
	        target = t;
	    }

	    @Override
	    public AnnotationCandidate call()
	    {
       		return new AnnotationCandidate(o.getUri(source), lex.getBestName(source), mapTwoEntities(source,target));
        }
	}
}