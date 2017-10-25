package main.settings;


public enum Problem
{
   	OBSOLETION	("Obsolete Classes"),
   	CARDINALITY ("Cardinality Conflicts"),
   	COHERENCE	("Coherence Conflicts"),
   	QUALITY		("Low Quality Mappings");
   	
   	final String value;
    	
   	Problem(String s)
   	{
   		value = s;
   	}
	    	
   	public String toString()
   	{
   		return value;
   	}
   	
	public static Problem parseStep(String step)
	{
		for(Problem s : Problem.values())
			if(step.equalsIgnoreCase(s.toString()))
				return s;
		return null;
	}
}
