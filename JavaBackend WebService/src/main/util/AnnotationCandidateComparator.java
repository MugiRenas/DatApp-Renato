package main.util;

import java.util.Comparator;

import main.match.AnnotationCandidate;

public class AnnotationCandidateComparator implements Comparator<AnnotationCandidate> {

	public int compare(AnnotationCandidate x, AnnotationCandidate y) {
	    // TODO: Handle null x or y values
	    int startComparison = compare(x.getWeight(), y.getWeight());
	    return startComparison;
	  }

	  // I don't know why this isn't in Long...
	  private static int compare(double a, double b) {
	    return a > b ? -1
	         : a < b ? 1
	         : 0;
	  }
}
