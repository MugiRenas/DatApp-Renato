/******************************************************************************
* Copyright 2013-2016 LASIGE                                                  *
*                                                                             *
* Licensed under the Apache License, Version 2.0 (the "License"); you may     *
* not use this file except in compliance with the License. You may obtain a   *
* copy of the License at http://www.apache.org/licenses/LICENSE-2.0           *
*                                                                             *
* Unless required by applicable law or agreed to in writing, software         *
* distributed under the License is distributed on an "AS IS" BASIS,           *
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    *
* See the License for the specific language governing permissions and         *
* limitations under the License.                                              *
*                                                                             *
*******************************************************************************
* Matches Ontologies by measuring the word similarity between their classes,  *
* using a weighted Jaccard index.                                             *
*                                                                             *
* @author Daniel Faria                                                        *
******************************************************************************/
package main.match;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import main.ontology.Lexicon;
import main.ontology.Ontology;
import main.ontology.WordLexicon;

public class WordAnnotator implements Annotator
{

//Attributes
	
	private Ontology o;
	private WordLexicon sourceWLex;
	private Lexicon sourceLex;
	private int threads;

//Constructors
	
	/**
	 * Constructs a new WordMatcher with default options
	 */
	public WordAnnotator(Ontology source)
	{
		sourceWLex = source.getWordLexicon();
		sourceLex = source.getLexicon();
		threads = Runtime.getRuntime().availableProcessors();
		o = source;
	}
	
	
	
//Public Methods
	/**
	 * Searches for word matches of the target and adds them to the AnnotationSet
	 * @param thresh: the thresh measure to see if the AnnotationCandidate has a weight worth of being added to the AnnotationSet
	 * @param target: the target term to look for
	 * @param source: the source ontology where we look in
	 * @return the AnnotationSet with the word matches that match the words of the target term above that threshold
	 */
	@Override
	public AnnotationSet annotate(double thresh, String target)
	{
		System.out.println("Running Word Matcher");
		long time = System.currentTimeMillis()/1000;
		AnnotationSet a1 = new AnnotationSet();
		Set<Integer> sources = sourceLex.getClasses();
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
	

	//Computes the maximum word-based (bag-of-words) similarity between
	//two classes' names, for use by both match() and rematch()
	private double nameSimilarity(int sourceId, String target)
	{
		double nameSim = 0;
		double sim, weight;
		Set<String> sourceNames = sourceLex.getNames(sourceId);
		for(String s : sourceNames)
		{
			if (s.equals(target)) {
				weight = sourceWLex.getNameWeight(s,sourceId);
				return weight;
			}
			weight = sourceWLex.getNameWeight(s,sourceId);
			sim = weight;
			sim *= nameSimilarity(s,target);
			if(sim > nameSim)
				nameSim = sim;
		}
		return nameSim;
	}
	
	//Computes the word-based (bag-of-words) similarity between two names
	private double nameSimilarity(String s, String t)
	{
		Set<String> sourceWords = sourceWLex.getWords(s);
		Set<String> targetWords = new HashSet<String>(Arrays.asList(t.split(" ")));
		double intersection = 0.0; // use the copy constructor
		double union = sourceWords.size() + targetWords.size();
		for(String w : sourceWords)
			if(targetWords.contains(w))
				intersection++;
		union -= intersection;
		return intersection/union;
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
	       		return new AnnotationCandidate(o.getUri(source), sourceLex.getBestName(source), nameSimilarity(source,target));
	        }
		}
}