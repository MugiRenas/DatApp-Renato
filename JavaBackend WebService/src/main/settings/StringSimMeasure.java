package main.settings;


public enum StringSimMeasure
{
	ISUB ("ISub"),
	EDIT ("Levenstein"),
	JW ("Jaro-Winkler"),
	QGRAM ("Q-gram");
	
	String label;
	
	StringSimMeasure(String s)
    {
    	label = s;
    }
	
	public static StringSimMeasure parseMeasure(String m)
	{
		for(StringSimMeasure s : StringSimMeasure.values())
			if(m.equalsIgnoreCase(s.toString()))
				return s;
		return null;
	}
	
    public String toString()
    {
    	return label;
	}
}