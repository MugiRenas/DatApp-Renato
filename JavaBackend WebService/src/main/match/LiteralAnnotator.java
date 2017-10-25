package main.match;

import java.util.Set;

import main.ontology.Lexicon;
import main.ontology.Ontology;

public class LiteralAnnotator implements Annotator {
	
	private Ontology o;
	
	public LiteralAnnotator(Ontology source) {
		o = source;
	}

	
	/**
	 * Searches for literal matches of the target and adds them to the AnnotationSet
	 * @param thresh: the thresh measure to see if the AnnotationCandidate has a weight worth of being added to the AnnotationSet
	 * @param target: the target term to look for
	 * @param source: the source ontology where we look in
	 * @return the AnnotationSet with the literal matches that match the target term above that threshold
	 */
	@Override
	public AnnotationSet annotate(double thresh, String target) {
		AnnotationSet a = new AnnotationSet();
		Lexicon sLex = o.getLexicon();
		Set<Integer> sourceIndexes = sLex.getClasses(target);
		if(sourceIndexes != null) {
			for(Integer j : sourceIndexes) {
				double weight = sLex.getCorrectedWeight(target, j);
				if(weight >= thresh){
					a.add(new AnnotationCandidate(o.getUri(j), sLex.getBestName(j), weight));
				}
			}
		}
		return a;
	}
}
