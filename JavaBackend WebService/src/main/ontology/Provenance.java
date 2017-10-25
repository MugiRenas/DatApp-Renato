package main.ontology;

import main.settings.LexicalType;

public class Provenance implements Comparable<Provenance>
{

//Attributes

	//The lexical type of the name for the class (see aml.settings.LexicalType)
	private LexicalType type;
	//The source of the name for the class ("" if it is the ontology that lists
	//the class, or an ontology URI/name if it is an external resource - e.g. other ontology, wordnet)
	private String source;
	//The weight of the name for the class (according to aml.settings.LexicalType, or in case of
	//lexical extension, based on the match similarity)
	private double weight;
	
//Constructors
	
	/**
	 * Constructs a new Provenance object with the given values
	 * @param t: the type of the lexical entry (localName, label, etc)
	 * @param s: the source of the lexical entry (ontology uri, etc)
 	 * @param l: the language of the lexical entry ("en", "de", "pt", etc)
	 * @param w: the weight of the lexical entry
	 */
	public Provenance(LexicalType t, String s, double w)
	{
		type = t;
		source = s;
		weight = w;
	}
	
	
	@Override
	/**
	 * Provenances are compared first with regard to whether
	 * they are internal or external, and then by weight
	 */
	public int compareTo(Provenance o)
	{
		if(this.isExternal() && !o.isExternal())
			return -1;
		if(!this.isExternal() && o.isExternal())
			return 1;
		if(this.weight > o.weight)
			return 1;
		if(this.weight < o.weight)
			return -1;
		return 0;
	}
	
	
	/**
	 * @return whether this Provenance is external
	 */
	public boolean isExternal()
	{
		return !source.equals("");
	}
	
	
	/**
	 * @return the type of this Provenance
	 */
	public LexicalType getType()
	{
		return type;
	}
	
	
	/**
	 * @return the weight of this Provenance
	 */
	public double getWeight()
	{
		return weight;
	}
	
	
	/**
	 * @return the source of this Provenance object
	 */
	public String getSource()
	{
		return source;
	}
}
