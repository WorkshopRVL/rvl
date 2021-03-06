package org.purl.rvl.tooling.codegen.rdfreactor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.Reasoning;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Syntax;
import org.ontoware.rdfreactor.generator.CodeGenerator;


/**
 * @author Jan Polowinski
 *
 */
public class RVLapiGenerator {

	final public static String PATH_FOR_GEN_CODE = "src/main/java/";
	
	public static final String PACKAGE = "org.purl.rvl.java.gen.rvl";
//	public static final String PACKAGE = "org.purl.rvl.test.pizza";
//	public static final String PACKAGE = "org.purl.rvl.test.peopelpets";

	
	public static void main(String[] args) throws Exception {
		
		// create the RDF2GO Model
		Model model = RDF2Go.getModelFactory().createModel(Reasoning.rdfs);
		model.open();
		File file = new File(OntologyFile.RVL);
		if (file.exists()) {
			try {
				model.readFrom(new FileReader(file),
						Syntax.RdfXml);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// add extra triples only for code generation which are wrong with respect to RDFS or OWL semantics
		File fileExtra = new File(OntologyFile.RVL_EXTRA);
		if (fileExtra.exists()) {
			try {
				model.readFrom(new FileReader(fileExtra),
						Syntax.Turtle);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		File pathToGenCode = new File(PATH_FOR_GEN_CODE);
		CodeGenerator.generate(model, pathToGenCode, PACKAGE, Reasoning.rdfs, true,"");
		
//		CodeGenerator.generate(
//				ONTOLOGY_URL,
//				PATH_FOR_GEN_CODE,
//				PACKAGE,
//				Reasoning.rdfs,
//				true // deprecated, but better documented:,true
//				);
		
	}


}
