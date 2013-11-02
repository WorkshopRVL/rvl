package org.purl.rvl.interpreter.rvl.manual;

import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.node.BlankNode;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.URI;
import org.purl.rvl.interpreter.rvl.Object_to_ObjectRelation;
import org.purl.rvl.interpreter.rvl.Property_to_Graphic_Object_to_Object_RelationMapping;
import org.purl.rvl.interpreter.rvl.Sub_mappingrelation;

public class PropertyToGO2ORMapping extends
		Property_to_Graphic_Object_to_Object_RelationMapping {
	
	static final String NL =  System.getProperty("line.separator");
	
	public PropertyToGO2ORMapping(Model model, URI classURI,
			Resource instanceIdentifier, boolean write) {
		super(model, classURI, instanceIdentifier, write);
		// TODO Auto-generated constructor stub
	}

	public PropertyToGO2ORMapping(Model model, Resource instanceIdentifier,
			boolean write) {
		super(model, instanceIdentifier, write);
		// TODO Auto-generated constructor stub
	}

	public PropertyToGO2ORMapping(Model model, String uriString, boolean write)
			throws ModelRuntimeException {
		super(model, uriString, write);
		// TODO Auto-generated constructor stub
	}

	public PropertyToGO2ORMapping(Model model, BlankNode bnode, boolean write) {
		super(model, bnode, write);
		// TODO Auto-generated constructor stub
	}

	public PropertyToGO2ORMapping(Model model, boolean write) {
		super(model, write);
		// TODO Auto-generated constructor stub
	}
	
	public String toString(){
		
		String s = "";
		
		// try to get the string description from the (manual) PropertyMapping class, which is not in the super-class hierarchy
		PropertyMapping pm = (PropertyMapping) this.castTo(PropertyMapping.class);
		s += pm.toString();
		
		// targetAttribute is specific to P2GAM
		Object_to_ObjectRelation tgo2or = this.getAllTargetobject_to_objectrelation_as().firstValue();
		String tgaString = tgo2or.getAllLabel_as().count()>0 ? tgo2or.getAllLabel_as().firstValue() : tgo2or.toString();
		s += "from P2GO2ORM(m): target GOTOR: " + tgaString + NL ;
		
		// list sub-mappings
		Sub_mappingrelation smr = this.getAllSub_mapping_as().firstValue();
		s += smr + NL;
		s += "from P2GO2ORM(m): Sub-mapping to mapping: " + smr.getAllSub_mapping_as().firstValue() + NL ;
		s += "from P2GO2ORM(m): Sub-mapping on role: " + smr.getAllOnrole_as().firstValue() + NL ;
		s += "from P2GO2ORM(m): Sub-mapping on triple part: " + smr.getAllOntriplepart_as().firstValue() + NL ;

		return s;
	}

}
