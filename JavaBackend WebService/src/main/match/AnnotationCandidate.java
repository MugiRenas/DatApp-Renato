package main.match;


public class AnnotationCandidate implements Comparable<AnnotationCandidate> {
	private String name;
	private double weight;
	private String uri;
	
	public AnnotationCandidate(){
		uri = null;
		name = null;
		weight = 0;
	}
	
	public AnnotationCandidate(String uri2, String name2, double weight2){
		uri = uri2;
		name = name2;
		weight = weight2;
	}

	public String getName() {
		return name;
	}


	public double getWeight() {
		return weight;
	}


	public String getUri() {
		return uri;
	}
	
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	@Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(!(obj instanceof AnnotationCandidate)) return false;
        return this.uri.equals(((AnnotationCandidate) obj).uri);
    }
    
    
    @Override
    public int hashCode() {
        return this.uri.hashCode();
    }

	@Override
	public int compareTo(AnnotationCandidate o) {
		Double w = new Double(this.weight);
		Double x = new Double(o.weight);
		return (w.compareTo(x));
	}
	
	
}
