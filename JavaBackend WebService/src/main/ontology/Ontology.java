package main.ontology;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import main.settings.LexicalType;
import main.settings.SKOS;
import main.util.StringParser;
import main.util.Table2Set;

public class Ontology
{

//Attributes
	
	//The OWL Ontology Manager and Data Factory
	protected OWLOntologyManager manager;
	protected OWLDataFactory factory;
	//The entity expansion limit property
    protected final String LIMIT = "entityExpansionLimit"; 
	//The data structures
    protected Lexicon lex;
	private WordLexicon wLex;
	private HashSet<Integer> obsolete;
	private HashMap<Integer,String> uris;
	private HashMap<String,Integer> indexes;
	private RelationshipMap rm;
	private boolean isSKOS;
	
//Constructors

	/**
	 * Constructs an empty ontology
	 */
	public Ontology()
	{
        //Increase the entity expansion limit to allow large ontologies
        System.setProperty(LIMIT, "1000000");
        //Get an Ontology Manager and Data Factory
        manager = OWLManager.createOWLOntologyManager();
        factory = manager.getOWLDataFactory();
        //Initialize the data structures
        lex = new Lexicon();
		wLex = null;
		obsolete = new HashSet<Integer>();
		uris = new HashMap<Integer,String>();
		indexes = new HashMap<String,Integer>();
		rm = new RelationshipMap();
	}
	
	
	/**
	 * Constructs an Ontology from an URI  
	 * @param uri: the URI of the input Ontology
	 * @throws OWLOntologyCreationException 
	 */
	public Ontology(URI uri) throws OWLOntologyCreationException
	{
		this();
        OWLOntology o;
        //Check if the URI is local
        if(uri.toString().startsWith("file:"))
		{
			File f = new File(uri);
			o = manager.loadOntologyFromOntologyDocument(f);
		}
		else
		{
			IRI i = IRI.create(uri);
			o = manager.loadOntology(i);
		}
		init(o);
		//Close the OntModel
        manager.removeOntology(o);
        //Reset the entity expansion limit
        System.clearProperty(LIMIT);
	}
	
	/**
	 * Constructs an Ontology from an OWLOntology
	 * @param o: the OWLOntology to use
	 */
	public Ontology(OWLOntology o)
	{
		this();
		init(o);
        //Reset the entity expansion limit
        System.clearProperty(LIMIT);
	}

//Public Methods

	public void close()
	{
		manager = null;
		factory = null;
		lex = null;
		wLex = null;
		obsolete = null;
		uris = null;
		rm = null;
	}
	
	/**
	 * @param index: the index of the entity to search in the Ontology
	 * @return whether the entity is contained in the Ontology
	 */
	public boolean contains(int index)
	{
		return uris.containsKey(index);
	}
	
	/**
	 * @return the Lexicon of the Ontology
	 */
	public Lexicon getLexicon()
	{
		return lex;
	}
	
	/**
	 * Gets the WordLexicon of this Ontology.
	 * Builds the WordLexicon if not previously built, or
	 * built for a specific language
	 * @return the WordLexicon of this Ontology
	 */
	public WordLexicon getWordLexicon()
	{
		if(wLex == null)
			wLex = new WordLexicon(lex);
		return wLex;
	}
	
	
	
	/**
	 * @param index: the index of the URI in the ontology
	 * @return whether the index corresponds to an obsolete class
	 */
	public boolean isObsoleteClass(int index)
	{
		return obsolete.contains(index);
	}
	
	/**
	 * @return whether this ontology is SKOS or OWL/OBO
	 */
	public boolean isSKOS()
	{
		return isSKOS;
	}
	


//Private Methods	

	//Builds the ontology data structures
	private void init(OWLOntology o)
	{
		//Check if the ontology is in SKOS format
		if(o.containsClassInSignature(SKOS.CONCEPT_SCHEME.toIRI()) &&
				o.containsClassInSignature(SKOS.CONCEPT.toIRI()))
		{
			isSKOS = true;
			getSKOSConcepts(o);
			//Extend the Lexicon
			lex.generateStopWordSynonyms();
			lex.generateParenthesisSynonyms();
			//Build the relationship map
			getSKOSRelationships(o);
		}
		else
		{
			isSKOS = false;
			//Update the URI of the ontology (if it lists one)
			//Get the classes and their names and synonyms
			getOWLClasses(o);
			//Extend the Lexicon
			lex.generateStopWordSynonyms();
			lex.generateParenthesisSynonyms();
			//Build the relationship map
			getOWLRelationships(o);
		}
	}
	
	//SKOS Thesauri
	
	//Processes the classes and their lexical information
	private void getSKOSConcepts(OWLOntology o)
	{
		//The Lexical type and weight
		LexicalType type;
		double weight;
		//SKOS concepts are instances of class "concept"
		//Thus, we start by retrieving this class
		OWLClass concept = getClass(o,SKOS.CONCEPT.toIRI());
		if(concept == null)
			return;
		//Then retrieve its instances, as well as those of its subclasses
		Set<OWLIndividual> indivs = concept.getIndividuals(o);
		for(OWLClassExpression c : concept.getSubClasses(o))
			if(c instanceof OWLClass)
				indivs.addAll(c.asOWLClass().getIndividuals(o));
		//And process them as if they were OWL classes
		int id = 0;
		for(OWLIndividual i : indivs)
		{
			if(!i.isNamed())
				continue;
			OWLNamedIndividual ind = i.asOWLNamedIndividual();
			String indivUri = ind.getIRI().toString();
			//Add it to the global list of URIs (as a class)
			uris.put(++id,indivUri);
			indexes.put(indivUri, id);
			//Get the local name from the URI
			String name = getLocalName(indivUri);
			//If the local name is not an alphanumeric code, add it to the lexicon
			if(!StringParser.isNumericId(name))
			{
				type = LexicalType.LOCAL_NAME;
				weight = type.getDefaultWeight();
				lex.addClass(id, name, type, "", weight);
			}

			//Now get the class's annotations (including imports)
			Set<OWLAnnotation> annots = ind.getAnnotations(o);
            for(OWLAnnotation annotation : annots)
            {
            	//Labels and synonyms go to the Lexicon
            	String propUri = annotation.getProperty().getIRI().toString();
            	type = LexicalType.getLexicalType(propUri);
            	if(type != null)
            	{
	            	weight = type.getDefaultWeight();
	            	if(annotation.getValue() instanceof OWLLiteral)
	            	{
	            		OWLLiteral val = (OWLLiteral) annotation.getValue();
	            		name = val.getLiteral();
	            		lex.addClass(id, name, type, "", weight);
		            }
	            	else if(annotation.getValue() instanceof IRI)
	            	{
	            		OWLNamedIndividual ni = factory.getOWLNamedIndividual((IRI) annotation.getValue());
	                    for(OWLAnnotation a : ni.getAnnotations(o))
	                    {
	                       	if(a.getValue() instanceof OWLLiteral)
	                       	{
	                       		OWLLiteral val = (OWLLiteral) a.getValue();
	                       		name = val.getLiteral();
    		            		lex.addClass(id, name, type, "", weight);
	                       	}
	            		}
	            	}
            	}
	        }
		}
	}
	
	//Reads all class relationships
	private void getSKOSRelationships(OWLOntology o)
	{
		//For simplicity, we convert "broader", "broader_transitive", "narrower"
		//and "narrower_transitive" to subclass relationships
		//Create a temporary map of disjoints from "related" concepts
		Table2Set<Integer,Integer> disj = new Table2Set<Integer,Integer>();
		//Check that the thesaurus explicitly defines the SKOS object properties
		//(for convenience, we test only the "skos:broader") 
		boolean hasObject = o.containsObjectPropertyInSignature(SKOS.BROADER.toIRI());
		//Retrieving the "concept" class
		OWLClass concept = getClass(o,SKOS.CONCEPT.toIRI());
		if(concept == null)
			return;
		//Then retrieve its instances, as well as those of its subclasses
		Set<OWLIndividual> indivs = concept.getIndividuals(o);
		for(OWLClassExpression c : concept.getSubClasses(o))
			if(c instanceof OWLClass)
				indivs.addAll(c.asOWLClass().getIndividuals(o));
		//And process them as if they were OWL classes
		for(OWLIndividual i : indivs)
		{
			if(!i.isNamed())
				continue;
			OWLNamedIndividual ind = i.asOWLNamedIndividual();
			Integer child = indexes.get(ind.getIRI().toString());
			if(child == null)
				continue;
			//If the thesaurus has the SKOS Object Properties properly defined
			if(hasObject)
			{
				//We can retrieve the Object Properties of each concept
				Map<OWLObjectPropertyExpression, Set<OWLIndividual>> iProps = i.getObjectPropertyValues(o);
				for(OWLObjectPropertyExpression prop : iProps.keySet())
				{
					if(prop.isAnonymous())
						continue;
					//Get each property's IRI
					IRI rel = prop.asOWLObjectProperty().getIRI();
					//Check that the Object Property is one of the SKOS relations
					if(!rel.equals(SKOS.BROADER.toIRI()) && !rel.equals(SKOS.BROADER_TRANS.toIRI()) &&
							!rel.equals(SKOS.NARROWER.toIRI()) && !rel.equals(SKOS.BROADER_TRANS.toIRI()))
						continue;
					//And if so, get the related concepts
					for(OWLIndividual p : iProps.get(prop))
					{
						if(!p.isNamed())
							continue;
						int parent = indexes.get(p.asOWLNamedIndividual().getIRI().toString());
						if(parent == -1)
							continue;
						//And add the relation according to the Object Property
						if(rel.equals(SKOS.BROADER.toIRI()) || rel.equals(SKOS.BROADER_TRANS.toIRI()))
							rm.addClassRelationship(child, parent, -1, false);
						else if(rel.equals(SKOS.NARROWER.toIRI()) || rel.equals(SKOS.BROADER_TRANS.toIRI()))
							rm.addClassRelationship(parent, child, -1, false);
					}
				}
			}
			//Otherwise, they will likely register as Annotation Properties
			else
			{
				//So we have to get them from the annotations
				for(OWLAnnotation p : ind.getAnnotations(o))
				{
					IRI rel = p.getProperty().getIRI();
					if(!rel.equals(SKOS.BROADER.toIRI()) && !rel.equals(SKOS.BROADER_TRANS.toIRI()) &&
							!rel.equals(SKOS.NARROWER.toIRI()) && !rel.equals(SKOS.BROADER_TRANS.toIRI()) &&
							!rel.equals(SKOS.RELATED.toIRI()))
						continue;
					OWLAnnotationValue v = p.getValue();
					if(!(v instanceof IRI))
						continue;
					int parent = indexes.get(v.toString());
					if(parent == -1)
						continue;
					//And add the relation according to the Object Property
					if(rel.equals(SKOS.BROADER.toIRI()) || rel.equals(SKOS.BROADER_TRANS.toIRI()))
						rm.addClassRelationship(child, parent, -1, false);
					else if(rel.equals(SKOS.NARROWER.toIRI()) || rel.equals(SKOS.BROADER_TRANS.toIRI()))
						rm.addClassRelationship(parent, child, -1, false);
				}
			}
		}
		for(Integer i : disj.keySet())
		{
			Set<Integer> top1 = getTopParents(i);
			for(Integer j : disj.get(i))
			{
				Set<Integer> top2 = getTopParents(j);
				for(Integer k : top1)
					for(Integer l : top2)
						rm.addDisjoint(k, l);
			}
		}
	}
	
	//OWL Ontologies

	//Processes the classes and their lexical information
	private void getOWLClasses(OWLOntology o)
	{
		//The Lexical type and weight
		LexicalType type;
		double weight;
		//The label property
		OWLAnnotationProperty label = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		//Get an iterator over the ontology classes
		Set<OWLClass> owlClasses = o.getClassesInSignature(true);
		int id = 0;
		//Then get the URI for each class
		for(OWLClass c : owlClasses)
		{
			String classUri = c.getIRI().toString();
			if(classUri == null || classUri.endsWith("owl#Thing") || classUri.endsWith("owl:Thing"))
				continue;
			//Add it to the global list of URIs
			uris.put(++id,classUri);
			indexes.put(classUri,id);
			
			//Get the local name from the URI
			String name = getLocalName(classUri);
			//If the local name is not an alphanumeric code, add it to the lexicon
			if(!StringParser.isNumericId(name))
			{
				type = LexicalType.LOCAL_NAME;
				weight = type.getDefaultWeight();
				lex.addClass(id, name, type, "", weight);
			}

			//Now get the class's annotations (including imports)
			Set<OWLAnnotation> annots = c.getAnnotations(o);
			for(OWLOntology ont : o.getImports())
				annots.addAll(c.getAnnotations(ont));
            for(OWLAnnotation annotation : annots)
            {
            	//Labels and synonyms go to the Lexicon
            	String propUri = annotation.getProperty().getIRI().toString();
            	type = LexicalType.getLexicalType(propUri);
            	if(type != null)
            	{
	            	weight = type.getDefaultWeight();
	            	if(annotation.getValue() instanceof OWLLiteral)
	            	{
	            		OWLLiteral val = (OWLLiteral) annotation.getValue();
	            		name = val.getLiteral();
	            		String lang = val.getLang();
	            		if(lang.equals(""))
	            			lang = "en";
	            		lex.addClass(id, name, type, "", weight);
		            }
	            	else if(annotation.getValue() instanceof IRI)
	            	{
	            		OWLNamedIndividual ni = factory.getOWLNamedIndividual((IRI) annotation.getValue());
	                    for(OWLAnnotation a : ni.getAnnotations(o,label))
	                    {
	                       	if(a.getValue() instanceof OWLLiteral)
	                       	{
	                       		OWLLiteral val = (OWLLiteral) a.getValue();
	                       		name = val.getLiteral();
	                       		String lang = val.getLang();
	    	            		if(lang.equals(""))
	    	            			lang = "en";
    		            		lex.addClass(id, name, type, "", weight);
	                       	}
	            		}
	            	}
            	}
            	//Deprecated classes are flagged as obsolete
            	else if(propUri.endsWith("deprecated") &&
            			annotation.getValue() instanceof OWLLiteral)
            	{
            		OWLLiteral val = (OWLLiteral) annotation.getValue();
            		if(val.isBoolean())
            		{
            			boolean deprecated = val.parseBoolean();
            			if(deprecated)
            				obsolete.add(id);
            		}
            	}
	        }
		}
	}
	
	
	//Reads all class relationships
	private void getOWLRelationships(OWLOntology o)
	{
		//Get an iterator over the ontology classes
		Set<OWLClass> classes = o.getClassesInSignature(true);
		//For each class index
		for(OWLClass c : classes)
		{
			//Get its identifier
			Integer child = indexes.get(c.getIRI().toString());
			if(child == null)
				continue;
			
			//Get the subclass expressions to capture and add relationships
			Set<OWLClassExpression> superClasses = c.getSuperClasses(o);
			for(OWLOntology ont : o.getDirectImports())
				superClasses.addAll(c.getSuperClasses(ont));
			for(OWLClassExpression e : superClasses)
				addRelationship(o,c,e,true,false);
			
			//Get the equivalence expressions to capture and add relationships
			Set<OWLClassExpression> equivClasses = c.getEquivalentClasses(o);
			for(OWLOntology ont : o.getDirectImports())
				equivClasses.addAll(c.getEquivalentClasses(ont));
			for(OWLClassExpression e : equivClasses)
				addRelationship(o,c,e,false,false);
		}
	}
	
	//Auxiliary Methods
	
	//Gets a named class from the given OWLOntology 
	private OWLClass getClass(OWLOntology o, IRI classIRI)
	{
		OWLClass cl = null;
		for(OWLClass c : o.getClassesInSignature())
		{
			if(c.getIRI().equals(classIRI))
			{
				cl = c;
				break;
			}
		}
		return cl;
	}
	
	//Add a relationship between two classes to the RelationshipMap
	private void addRelationship(OWLOntology o, OWLClass c, OWLClassExpression e, boolean sub, boolean inverse)
	{
		int child = indexes.get(c.getIRI().toString());
		Integer parent;
		ClassExpressionType type = e.getClassExpressionType();
		//If it is a class, and we didn't use the reasoner, process it here
		if(type.equals(ClassExpressionType.OWL_CLASS))
		{
			parent = indexes.get(e.asOWLClass().getIRI().toString());
			if(parent == null)
				return;
			if(sub)
			{
				if(inverse)
					rm.addDirectSubclass(parent, child);
				else
					rm.addDirectSubclass(child, parent);
				String name = lex.getBestName(parent);
				if(name.contains("Obsolete") || name.contains("obsolete") ||
						name.contains("Retired") || name.contains ("retired") ||
						name.contains("Deprecated") || name.contains("deprecated"))
					obsolete.add(child);
			}
			else
				rm.addEquivalentClass(child, parent);
		}
		//If it is an intersection of classes, capture the implied subclass relationships
		else if(type.equals(ClassExpressionType.OBJECT_INTERSECTION_OF))
		{
			Set<OWLClassExpression> inter = e.asConjunctSet();
			for(OWLClassExpression cls : inter)
				addRelationship(o,c,cls,true,false);
		}
		//If it is a union of classes, capture the implied subclass relationships
		else if(type.equals(ClassExpressionType.OBJECT_UNION_OF))
		{
			Set<OWLClassExpression> union = e.asDisjunctSet();
			for(OWLClassExpression cls : union)
				addRelationship(o,c,cls,true,true);
		}
	}
	
	//Gets the top level parents of a class (recursively)
	private Set<Integer> getTopParents(int classId)
	{
		return getTopParents(rm.getSuperClasses(classId, true));
	}
	
	//Gets the top level parents of a class (recursively)
	private Set<Integer> getTopParents(Set<Integer> classes)
	{
		Set<Integer> parents = new HashSet<Integer>();
		for(int i : classes)
		{
			boolean check = parents.addAll(rm.getSuperClasses(i, true));
			if(!check)
				parents.add(i);
		}
		if(parents.equals(classes))
			return classes;
		else
			return getTopParents(parents);
	}
	
	/**
	 * @param uri: the uri to get the local name
	 * @return the local name of the given uri
	 */
	public String getLocalName(String uri)
	{
		if(uri == null)
			return null;
		int i = uri.indexOf("#") + 1;
		if(i == 0)
			i = uri.lastIndexOf("/") + 1;
		return uri.substring(i);
	}
	
	/**
	 * @param i: the index of the class that we want to retrieve the URI
	 * @return the URI of the class determined by that index
	 */
	public String getUri(Integer i) {
		return uris.get(i);
	}

}