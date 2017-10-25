package main.settings;


public enum LexicalType
{
    LOCAL_NAME ("localName", 1.0),
    LABEL ("label", 0.95),
    EXACT_SYNONYM ("exactSynonym", 0.9),
    OTHER_SYNONYM ("otherSynonym", 0.85),
    INTERNAL_SYNONYM ("internalSynonym", 0.9),
    EXTERNAL_MATCH ("externalSynonym", 0.85),
    FORMULA ("formula", 0.8);
    
    String label;
    double weight;
    
    LexicalType(String s, double w)
    {
    	label = s;
    	weight = w;
    }
    
	/**
	 * @param type: the lexical type to weight
	 * @return the default weight of that lexical type
	 */
	public double getDefaultWeight()
	{
		return weight;
	}
	
	/**
	 * Converts a property to a LexicalType
	 * @param prop: the URI of the property
	 * @return the default LexicalType for that property
	 */
	public static LexicalType getLexicalType(String prop)
	{
		if(prop.endsWith("label") || prop.endsWith("prefLabel"))
			return LABEL;
		if(prop.endsWith("hasExactSynonym") || prop.endsWith("FULL_SYN") ||
				prop.endsWith("alternative_term"))
			return EXACT_SYNONYM;
		if(prop.endsWith("hasBroadSynonym") || prop.endsWith("hasNarrowSynonym"))
			return null;
		if(prop.endsWith("altLabel") || prop.contains("synonym") ||
				prop.contains("Synonym") || prop.contains("SYN"))
			return OTHER_SYNONYM;
		return null;
	}
	
	/**
	 * Parses a LexicalType
	 * @param t: the LexicalType label
	 * @return the LexicalType corresponding to the label
	 */
	public static LexicalType parseLexicalType(String t)
	{
		for(LexicalType type : LexicalType.values())
			if(t.equalsIgnoreCase(type.label))
				return type;
		return null;
	}
	    
    public String toString()
    {
    	return label;
    }
}

