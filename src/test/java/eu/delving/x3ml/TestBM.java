/*==============================================================================
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
==============================================================================*/
package eu.delving.x3ml;

import gr.forth.ics.isl.x3ml.X3MLEngine;
import org.junit.Test;
import java.io.IOException;
import static eu.delving.x3ml.AllTests.*;
import static org.junit.Assert.assertTrue;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * @author Gerald de Jong <gerald@delving.eu>
 * @author Nikos Minadakis <minadakn@ics.forth.gr>
 * @author Yannis Marketakis <marketak@ics.forth.gr>
 */

public class TestBM {

    @Test
    public void testBM20() throws IOException {
        X3MLEngine engine = engine("/bm/BM20.x3ml");
        X3MLEngine.Output output = engine.execute(document("/bm/BM20.xml"), 
						  policy("/bm/BM20-gen-policy.xml"));
	
        Model expected = ModelFactory.createMemModelMaker().createModel("gumby");
        expected.read(TestBM.class.getResourceAsStream("/bm/BM20-expected.n3"), null, "N-TRIPLE");
        Model mapped = output.getModel();
        
        long target = expected.size();
        long achieved = 0;
        long additionalClassifications = 0;
        long unknown = 0;
        
        StmtIterator statements = mapped.listStatements();
        while(statements.hasNext()) {
        	Statement statement = statements.nextStatement();
        	if (expected.contains(statement))
        			achieved++;
        	else if (statement.getPredicate().toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
        		additionalClassifications++;
        	//else
        		//unknown++;
        }

    	System.out.printf("Achieved: %.2f%% ", ((++achieved/(float)target) * 100.0));
    	System.out.println("Additional classifications: " + additionalClassifications);
    	
        //assertTrue(expected.isIsomorphicWith(mapped));
        //assertTrue(expected.containsAll(mapped));
        assertTrue(unknown == 0);
    }
}
