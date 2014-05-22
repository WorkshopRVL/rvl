package org.purl.rvl.example;

import java.io.FileNotFoundException;

import org.junit.Test;
import org.ontoware.rdf2go.Reasoning;
import org.purl.rvl.interpreter.test.TestOGVICProcess;
import org.purl.rvl.tooling.avm2d3.D3GeneratorTreeJSON;
import org.purl.rvl.tooling.process.ExampleData;
import org.purl.rvl.tooling.process.ExampleMapping;

public class UseCasePO_9 extends TestOGVICProcess {
	
	@Test
	public void testOGVICProcess() throws FileNotFoundException {
		
		//process.setUriStart("http://purl.org/obo/owl/");
		
		project.setReasoningDataModel(Reasoning.rdfs); // setting to rdfs leads to strange results three additional statements that seems to be unrelated

		project.registerMappingFile(ExampleMapping.PO_9);
		project.registerDataFile(ExampleData.PO);
		project.registerDataFile(ExampleData.PO_EXTRA_DATA);
		
		//project.setRvlInterpreter(new SimpleRVLInterpreter());
		project.setD3Generator(new D3GeneratorTreeJSON());
		//process.setD3Generator(new D3GeneratorSimpleJSON());
		
		loadProjectAndRunProcess();
	}


}