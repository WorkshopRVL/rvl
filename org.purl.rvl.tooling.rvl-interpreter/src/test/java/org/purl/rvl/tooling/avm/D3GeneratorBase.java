package org.purl.rvl.tooling.avm;

import java.io.FileWriter;
import java.io.IOException;

import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Syntax;


public class D3GeneratorBase {

	protected static final String NL = System.getProperty("line.separator");

	protected Model model;
	protected Model modelVISO;
	
	/**
	 * @param model
	 * @param modelVISO
	 */
	public D3GeneratorBase(Model model, Model modelVISO) {
		super();
		this.model = model;
		this.modelVISO = modelVISO;
	}

	public D3GeneratorBase() {
		super();
	}

	/**
	 * Saves the whole Model to a tmp file 
	 * TODO: does not currently filter out non-avm triples!
	 */
	public void writeAVMToFile(){

	    try {
	     FileWriter writer = new FileWriter(OGVICProcess.tmpAvmModelFileName);
	     model.writeTo(writer, Syntax.Turtle);
	    } catch (IOException e) {
	     e.printStackTrace();
	    }
	    // close the model
	    // model.close();
	    // -NO!!! since there is more than one Thread, close would be performed before the data is added to the model, resulting in a NullPointerException of the RDF2GO model
	}
	
}