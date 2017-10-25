package main.settings;

import org.semanticweb.owlapi.model.IRI;

public enum SKOS
{
    ALTLABEL		("altLabel"),
    BROAD_MATCH		("broadMatch"),
    BROADER			("broader"),
    BROADER_TRANS	("broaderTransitive"),
    CHANGE_NOTE		("changeNote"),
    CLOSE_MATCH		("closeMatch"),
    COLLECT_PROP	("CollectableProperty"),
    COLLECTION		("Collection"),
    COMMENT			("comment"),
    CONCEPT			("Concept"),
    CONCEPT_SCHEME	("ConceptScheme"),
    DEFINITION		("definition"),
    DOCUMENT		("Document"),
    EDITORIAL_NOTE	("editorialNote"),
    EXACT_MATCH		("exactMatch"),
    EXAMPLE			("example"),
    HAS_TOP_CONCEPT	("hasTopConcept"),
    HIDDEN_LABEL	("hiddenLabel"),
    HISTORY_NOTE	("historyNote"),
    IMAGE			("Image"),
    IN_SCHEME		("inScheme"),
    LABEL_REL		("LabelRelation"),
    LABEL_RELATED	("labelRelated"),
    MAPPING_REL		("mappingRelation"),
    MEMBER			("member"),
    MEMBER_LIST		("memberList"),
    NARROW_MATCH	("narrowMatch"),
    NARROWER		("narrower"),
    NARROWER_TRANS	("narrowerTransitive"),
    NOTATION		("notation"),
    NOTE			("note"),
    ORDERED_COLECT	("OrderedCollection"),
    PREFLABEL		("prefLabel"),
    RELATED			("related"),
    RELATED_MATCH	("relatedMatch"),
    RESOURCE		("Resource"),
    SCOPE_NOTE		("scopeNote"),
    SEE_LABEL_REL	("seeLabelRelation"),
    SEMANTIC_REL	("semanticRelation"),
    TOP_CONCEPT		("topConceptOf");
	
    private String uri;
    
    private final String NAMESPACE = "http://www.w3.org/2004/02/skos/core#";
    
    SKOS(String s)
    {
    	uri = s;
    }
    
    /**
     * @return the SKOS term
     */
    public IRI toIRI()
    {
    	return IRI.create(NAMESPACE + uri);
    }
    
    public String toString()
    {
    	return uri;
    }
}
