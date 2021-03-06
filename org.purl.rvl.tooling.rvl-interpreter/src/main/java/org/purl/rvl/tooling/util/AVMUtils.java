package org.purl.rvl.tooling.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.impl.StatementImpl;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.util.RDFTool;
import org.purl.rvl.java.gen.viso.graphic.Containment;
import org.purl.rvl.java.gen.viso.graphic.DirectedLinking;
import org.purl.rvl.java.gen.viso.graphic.GraphicObject;
import org.purl.rvl.java.gen.viso.graphic.Labeling;
import org.purl.rvl.java.viso.graphic.GraphicObjectX;

/**
 * @author Jan Polowinski
 *
 */
public class AVMUtils {
	
	private final static Logger LOGGER = Logger.getLogger(AVMUtils.class.getName()); 
	
	/**
	 * Get all the GraphicObjects including connectors, labels ...
	 * 
	 * @return a set of GraphicObjectX
	 */
	public Set<GraphicObjectX> getAllGraphicObjects(Model modelAVM){
		
		Set<GraphicObjectX> gos = new HashSet<GraphicObjectX>();
		
		GraphicObject[] goArray = 
				GraphicObject.getAllInstances_as(modelAVM).asArray();
		
		for (int i = 0; i < goArray.length; i++) {
			GraphicObjectX startNode = (GraphicObjectX) goArray[i].castTo(GraphicObjectX.class);
			gos.add(startNode);
		}
		
		return gos;
	}
	
	/**
	 * For a given n-ary graphic relation rel and role (URI) roleURI return all graphic 
	 * objects.
	 * 
	 * @param modelAVM
	 * @param rel
	 * @param roleURI
	 * @return list of triples as statements (rel, role, graphicObject)
	 */
	public static List<Statement> getRolesAndGOsFor(
			Model modelAVM, Node rel, URI roleURI) {
		
			List<Statement> stmtList = new ArrayList<Statement>();
		
		try {
	
			String query = "" + 
					" SELECT DISTINCT ?s ?p ?o " + 
					" WHERE { " +
					" ?s ?p ?o . " + 
					" " + rel.toSPARQL() + " " + roleURI.toSPARQL() + " ?o " +
					" } ";
			
	
			QueryResultTable explMapResults = modelAVM.sparqlSelect(query);
			
			for (QueryRow row : explMapResults) {
				//LOGGER.finest("fetched SPARQL result row: " + row);
				try {
					Statement stmt = new StatementImpl(null, row.getValue("s").asURI(), row.getValue("p").asURI(), row.getValue("o"));
					RVLUtils.LOGGER.finest("build Statement: " + stmt.toString());
					stmtList.add(stmt);
				} catch (ClassCastException e){
					RVLUtils.LOGGER.finer("Skipped statement (blank node casting to URI?): " + e.getMessage());
				}
			}
		
		} catch (UnsupportedOperationException e){
			RVLUtils.LOGGER.warning("Problem with query to get statements: " + e.getMessage());
		} 
		
		return stmtList;
	}

	public static GraphicObjectX getGOForRole(
			Model modelAVM, Node rel, URI roleURI) {
		
			List<Statement> stmtList = AVMUtils.getRolesAndGOsFor(modelAVM,rel,roleURI);
			
			Node goNode = stmtList.get(0).getObject();
			
			org.purl.rvl.java.gen.viso.graphic.GraphicObject goGen = GraphicObjectX.getInstance(modelAVM, goNode.asResource());
			
			GraphicObjectX go = (GraphicObjectX) goGen.castTo(GraphicObjectX.class);
			
			return go;
	
	}

	/**
	 * Get only the GraphicObjects that need to be displayed. Remove objects
	 * playing the role of connectors for example.
	 * 
	 * @return
	 */
	public static Set<GraphicObjectX> getRelevantGraphicObjects(Model model) {

		Set<GraphicObjectX> gos = new HashSet<GraphicObjectX>();

		// get all subjects and the sv/tv table via SPARQL
		String query = "" + 
				"SELECT DISTINCT ?go " + 
				"WHERE { " +
				"	?go a " + GraphicObjectX.RDFS_CLASS.toSPARQL() + " ." +
//				"	?someRelation " + DirectedLinking.STARTNODE.toSPARQL() + " ?go ." +
				"	FILTER NOT EXISTS { ?someRelation " + DirectedLinking.LINKINGCONNECTOR.toSPARQL() + " ?go . }" +
				"	FILTER NOT EXISTS { ?someRelation " + Labeling.LABELINGLABEL.toSPARQL() + " ?go . }" +
				"} ";
		LOGGER.finest("query for relevant GOs: " + query);

		QueryResultTable explMapResults = model.sparqlSelect(query);
		for (QueryRow row : explMapResults) {
			GraphicObjectX go = (GraphicObjectX) GraphicObjectX.getInstance(model, row
					.getValue("go").asURI()).castTo(GraphicObjectX.class);
			gos.add(go);
			LOGGER.finest("Found relevant GO: " + row.getValue("go").toString() + " (" + go.getLabel() + ")");
		}

		return gos;
	}

	/**
	 * Returns a root node, i.e without incoming link, but with at least one outgoing link
	 * 
	 * @param model
	 * @return
	 */
	public static GraphicObjectX getRootNodeGraphicObject(Model model) {
				
		Set<GraphicObjectX> rootNodes = getRootNodesGraphicObject(model);
		GraphicObjectX pickedRootNode = null;
		
		if (rootNodes.size()>1) {
			LOGGER.info("multiple root node found. will return one of them");
		}

		for (Iterator<GraphicObjectX> iterator = rootNodes.iterator(); iterator.hasNext();) {
			pickedRootNode = (GraphicObjectX) iterator.next();
			break; // TODO: select best - not first
		}
		
		return pickedRootNode;
	}
	
	public static Set<GraphicObjectX> getRootNodesGraphicObject(Model model) {
		
		if (null==model) {
			LOGGER.warning("Model was null. Couldn't get root nodes.");
			return null;
		}
		
		Set<GraphicObjectX> rootNodes = new HashSet<GraphicObjectX>();

		String query = "" + 
				"SELECT DISTINCT ?go " + 
				"WHERE { " +
				"	?go a " + GraphicObjectX.RDFS_CLASS.toSPARQL()  +
				" { " + 
				"	?someRelation " + DirectedLinking.STARTNODE .toSPARQL() + " ?go " + 
				" } UNION { " +
				"	?someRelation " + Containment.CONTAINMENTCONTAINER .toSPARQL() + " ?go " + 
				" }" + 
				"	FILTER NOT EXISTS { ?someOtherRelation " + Containment.CONTAINMENTCONTAINEE .toSPARQL() + " ?go }" + 
				"	FILTER NOT EXISTS { ?someOtherRelation " + DirectedLinking.ENDNODE .toSPARQL() + " ?go }" + 
						// (no relation points to the go as an endNode)
				"} ";
		LOGGER.finest("query for root nodes: " + query);

		QueryResultTable rootNodeResults = model.sparqlSelect(query);
		for (QueryRow row : rootNodeResults) {
			
			GraphicObjectX possibleRootNode = (GraphicObjectX) GraphicObjectX.getInstance(model, row
					.getValue("go").asURI()).castTo(GraphicObjectX.class);
			
			LOGGER.finer("Found possible root node " + possibleRootNode.asURI() + " (" + possibleRootNode.getLabel() +")");
			rootNodes.add(possibleRootNode);
		}

		return rootNodes;
	}


	public static Set<DirectedLinking> getDirectedLinkingRelationsFrom(
			Model model, GraphicObjectX parentGO) {
		
		Set<DirectedLinking> dlFromHere = new HashSet<DirectedLinking>();

		String query = "" + 
				"SELECT DISTINCT ?dl " + 
				"WHERE { " +
				"	?dl a " + DirectedLinking.RDFS_CLASS.toSPARQL() + " ." +
				"	?dl " + DirectedLinking.STARTNODE .toSPARQL() + parentGO.toSPARQL() + " ." + 
						// (some relation points to the go as a startNode)
				"} ";
		
		String parentGOLabel = "";
		try{parentGOLabel = parentGO.getLabel() ; } catch (Exception e) {}
		LOGGER.finer("Query for directed linking relations with start node " + parentGOLabel + " " + parentGO.asURI());
		LOGGER.finest("Query: " + query);

		QueryResultTable results = model.sparqlSelect(query);
		for (QueryRow row : results) {
			dlFromHere.add(DirectedLinking.getInstance(model, row
					.getValue("dl").asURI()));
		}
		
		return dlFromHere;
	}
	
	// cloned from linking, much redundant, similar code
	public static Set<Containment> getContainmentRelationsFrom(Model model,
			GraphicObjectX parentGO) {
		
		Set<Containment> relFromHere = new HashSet<Containment>();

		String query = "" + 
				"SELECT DISTINCT ?rel " + 
				"WHERE { " +
				"	?rel a " + Containment.RDFS_CLASS.toSPARQL() + " ." +
				"	?rel " + Containment.CONTAINMENTCONTAINER .toSPARQL() + parentGO.toSPARQL() + " ." + 
						// (some relation points to the go as a container)
				"} ";
		
		String parentGOLabel = ""; // TODO label and exception handling here ...
		try{parentGOLabel = parentGO.getLabel() ; } catch (Exception e) {}
		LOGGER.finer("Query for containment relations with container " + parentGOLabel + " " + parentGO.asURI());
		LOGGER.finest("Query: " + query);

		QueryResultTable results = model.sparqlSelect(query);
		for (QueryRow row : results) {
			relFromHere.add(Containment.getInstance(model, row
					.getValue("rel").asURI()));
		}
		
		return relFromHere;

	}

	public static String getOrGenerateDefaultLabelString(Model model, org.ontoware.rdf2go.model.node.Resource resource){
		
		String genLabel = getGoodLabel(resource,model);

		return genLabel;
	}

	public static String getLocalName(Node node, Model model){
		
		String localName = "local-name-could-not-be-fetched";
		
		try {	 
			localName =  RDFTool.getShortName(node.asURI().toString());
		}
		catch (Exception e) {
			LOGGER.finest("Local name could not be fetched. Blank node?");
			localName = node.toString();
		}

		return localName;
	}
	
	public static String getGoodLabel(Node node, Model model){
		
		
		String label = "no-good-label-could-be-calculated";
		
		try {	 
			// somehow causes runtime exception with jena when not casted to resource as below
			label =  RDFTool.getGoodLabel(node.asResource(), model);
		}
		catch (Exception e) {
			
			LOGGER.finest("No good label could be calculated.");
			
			try {

				label = getLocalName(node, model);
				
			} catch (Exception e1) {
				
				LOGGER.finest("Local name could not be calculated.");
				label = node.toString();
			}
		}

		return label;
	}




}
