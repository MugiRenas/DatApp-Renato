package main.settings;

public enum EntityType
{
	CLASS ("Class"),
	INDIVIDUAL ("Individual"),
    DATA ("Data Property"),
    OBJECT ("Object Property"),
    ANNOTATION ("Annotation Property");
    
    String label;
    
    EntityType(String s)
    {
    	label = s;
    }
    
    public String toString()
    {
    	return label;
    }
}