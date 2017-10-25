package main.settings;


public enum WordMatchStrategy
{
	BY_CLASS ("By_Class"),
	BY_NAME ("By_Name"),
	AVERAGE ("Average"),
	MAXIMUM ("Maximum"),
	MINIMUM ("Minimum");
	
	String label;
	
	WordMatchStrategy(String s)
    {
    	label = s;
    }
	
	public static WordMatchStrategy parseStrategy(String strat)
	{
		for(WordMatchStrategy s : WordMatchStrategy.values())
			if(strat.equalsIgnoreCase(s.toString()))
				return s;
		return null;
	}
	
    public String toString()
    {
    	return label;
	}
}