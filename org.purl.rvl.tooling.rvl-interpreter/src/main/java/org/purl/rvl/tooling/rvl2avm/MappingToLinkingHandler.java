/**
 * 
 */
package org.purl.rvl.tooling.rvl2avm;

import java.util.logging.Logger;

import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.ModelSet;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.Resource;
import org.purl.rvl.exception.InsufficientMappingSpecificationException;
import org.purl.rvl.java.gen.viso.graphic.DirectedLinking;
import org.purl.rvl.java.gen.viso.graphic.UndirectedLinking;
import org.purl.rvl.java.viso.graphic.GraphicObjectX;
import org.purl.rvl.java.viso.graphic.ShapeX;
import org.purl.rvl.tooling.util.AVMUtils;

/**
 * @author Jan Polowinski
 * 
 */
public class MappingToLinkingHandler extends MappingToP2GOTORHandler {

	public MappingToLinkingHandler(ModelSet modelSet,
			RVLInterpreter rvlInterpreter, Model modelAVM) {
		super(modelSet, rvlInterpreter, modelAVM);
	}

	private final static Logger LOGGER = Logger
			.getLogger(MappingToLinkingHandler.class.getName());

	@Override
	public void encodeStatement(Statement statement)
			throws InsufficientMappingSpecificationException {

		Resource subject = statement.getSubject();
		Resource object = statement.getObject().asResource();

		LOGGER.finest("Subject label "
				+ AVMUtils.getGoodLabel(subject, modelAVM));
		LOGGER.finest("Object label " + AVMUtils.getGoodLabel(object, modelAVM));

		LOGGER.fine("Statement to be mapped : " + statement);

		// For each statement, create a startNode GO representing the subject
		// (if not exists)
		GraphicObjectX subjectNode = rvlInterpreter
				.createOrGetGraphicObject(subject);
		LOGGER.finest("Created GO for subject: " + subject.toString());

		// For each statement, create an endNode GO representing the object (if
		// not exists)
		// Node object = statement.getObject();

		GraphicObjectX objectNode = rvlInterpreter
				.createOrGetGraphicObject(object);
		LOGGER.finest("Created GO for object: " + object.toString());

		// create a connector and add default color
		// GraphicObjectX connector = new GraphicObjectX(modelAVM, true);
		GraphicObjectX connector = new GraphicObjectX(modelAVM,
				"http://purl.org/rvl/example-avm/GO_Connector_"
						+ rvlInterpreter.createNewInternalID(), true);
		// connector.setLabel(statement.getPredicate());
		connector.setLabel(AVMUtils.getGoodLabel(statement.getPredicate(),
				modelAVM)); // TODO: actually the label of the connector
							// representing this ; statement contains evtl. used
							// subproperty

		// generic graphic relation needed for submappings
		// (could also be some super class of directed linking, undirected
		// linking, containment ,...)
		Resource rel = null;

		// directed linking
		if (mapping.getTargetGraphicRelation().equals(
				DirectedLinking.RDFS_CLASS)) {

			// create the directed linking relation
			// DirectedLinking dlRel = new DirectedLinking(modelAVM, true);
			DirectedLinking dlRel = new DirectedLinking(modelAVM,
					"http://purl.org/rvl/example-avm/GR_"
							+ rvlInterpreter.createNewInternalID(), true);
			dlRel.setLabel(AVMUtils.getGoodLabel(
					mapping.getTargetGraphicRelation(), modelAVM));

			// configure the relation
			if (mapping.isInvertSourceProperty()) {
				dlRel.setEndnode(subjectNode);
				dlRel.setStartnode(objectNode);
				subjectNode.addLinkedfrom(dlRel);
				objectNode.addLinkedto(dlRel);
			} else {
				dlRel.setStartnode(subjectNode);
				dlRel.setEndnode(objectNode);
				subjectNode.addLinkedto(dlRel);
				objectNode.addLinkedfrom(dlRel);
			}

			// set default shape of directed connectors
			connector
					.setShapenamed(new ShapeX(
							modelAVM,
							"http://purl.org/viso/shape/commons/UMLAssociation",
							false));

			dlRel.setLinkingconnector(connector);
			rel = dlRel;

		} else { // undirected linking

			// create the undirected linking relation
			// UndirectedLinking udlRel = new UndirectedLinking(modelAVM, true);
			UndirectedLinking udlRel = new UndirectedLinking(modelAVM,
					"http://purl.org/rvl/example-avm/GR_"
							+ rvlInterpreter.createNewInternalID(), true);
			udlRel.setLabel(AVMUtils.getGoodLabel(
					mapping.getTargetGraphicRelation(), modelAVM));

			// configure the relation
			udlRel.addLinkingnode(subjectNode);
			udlRel.addLinkingnode(objectNode);
			subjectNode.addLinkedwith(udlRel);
			objectNode.addLinkedwith(udlRel);

			// set default shape of undirected connectors
			connector.setShapenamed(new ShapeX(modelAVM,
					"http://purl.org/viso/shape/commons/Line", false));

			udlRel.setLinkingconnector(connector);
			rel = udlRel;
		}

		// submappings
		if (mapping.hasSub_mapping()) {

			if (null != rel) {
				// DirectedLinking etc need to be subclasses of (n-ary)
				// GraphicRelation
				rvlInterpreter.applySubmappings(mapping, statement, rel);
			} else {
				LOGGER.warning("Submapping existed, but could not be applied, since no parent graphic relation was provided.");
			}
		}

	}

}
