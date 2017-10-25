package main;

import java.net.URI;

import main.util.StringParser;
import main.match.StringAnnotator;
import main.match.WordAnnotator;
import main.match.AnnotationSet;
import main.match.LiteralAnnotator;
import main.ontology.Ontology;
import main.ontology.RelationshipMap;

public class Main
{
	private static Ontology source;
	private static RelationshipMap rels;
	private static double thresh;
	private static final double BASE_THRESH = 0.5;
	private static AnnotationSet set;
	private static String json;
//Main Method
	public static void main(String[] args) throws Exception
	{
		//Path to input ontology files (edit manually)
		URI sourcePath;
		//Read the arguments
		String target = args[0];
		json = new String();
		//If it is a formula, parse it and label it as such
		if(StringParser.isFormula(target))
		{
			target = StringParser.normalizeFormula(target);
		}
		//Otherwise, parse it normally
		else
		{
			target = StringParser.normalizeName(target);
		}
		String links = args[1];
		String[] links2 = links.split(",");
		for(int i = 0; i < links2.length; i++){
			sourcePath = null;
			sourcePath = new URI(links2[i]);
			source = null;
			rels = null;
			set = null;
			set = new AnnotationSet();
	    	rels = new RelationshipMap();
			source = new Ontology(sourcePath);
			rels.transitiveClosure();
			thresh = BASE_THRESH;
			LiteralAnnotator ln = new LiteralAnnotator(source);
			set.addAll(ln.annotate(thresh, target));
			StringAnnotator psm = new StringAnnotator(source);
			set.addAll(psm.annotate(thresh, target));
			WordAnnotator wm = new WordAnnotator(source);
			set.addAll(wm.annotate(thresh, target));
		}
		set.sort();
		json = set.createJSON(set.getList());
		System.out.println(json);
		set.clear();
	}
	
	public static String Json() {
		return json;
	}
}