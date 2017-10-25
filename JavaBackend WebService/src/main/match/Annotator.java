package main.match;


public interface Annotator
{
	/**
	 * Finds AnnotationCandidates from the source Ontology for the target term
	 * @param source: the source Ontology
	 * @param target: the target term
	 * @param thresh: the similarity threshold for the alignment
	 * @return the AnnotationSet containing the AnnotationCandidates from the source Ontology
	 */
	public AnnotationSet annotate(double thresh, String target);
}