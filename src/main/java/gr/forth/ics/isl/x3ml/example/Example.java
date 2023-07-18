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
package gr.forth.ics.isl.x3ml.example;

import gr.forth.ics.isl.x3ml.X3MLEngine;
import static gr.forth.ics.isl.x3ml.X3MLEngine.exception;
import gr.forth.ics.isl.x3ml.X3MLGeneratorPolicy;
import gr.forth.ics.isl.x3ml.engine.Generator;
import gr.forth.ics.isl.x3ml.engine.GeneratorContext;
import gr.forth.Labels;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.riot.Lang;
import org.w3c.dom.Element;

/**
 * @author Yannis Marketakis (marketak 'at' ics 'dot' forth 'dot' gr)
 * @author Nikos Minadakis (minadakn 'at' ics 'dot' forth 'dot' gr)
 */
public class Example {
    
    public static void main(String[] args) throws FileNotFoundException,  IOException{       
        
        final String MAPPINGS_PATH="example/mappings.x3ml";
        final String GENERATOR_POLICY_PATH="example/generator-policy.xml";  //if empty, the generator will not be used
        final String INPUT_PATH="example/input.xml";
        final String ASSOCIATION_TABLE_PATH=""; ////if empty, the generator will not be used
        final int UUID_SIZE=0;
        final Pair<String,Lang> terminology=Pair.of("example/terms.nt", Lang.NT);   //if empty it will not be used
        final outputFormat OUT_FORMAT=outputFormat.TRIG;
        final outputStream OUT_STREAM=outputStream.SYSTEM_OUT;
        X3MLEngine.REPORT_PROGRESS=false;
        
        X3MLEngine engine;
        if(terminology.getLeft()!=null && !terminology.getLeft().isEmpty()){    //we use a terminology
            engine = engine(MAPPINGS_PATH, terminology);
        }else{
            engine = engine(MAPPINGS_PATH);
        }

        Generator policy;
        if(GENERATOR_POLICY_PATH.isEmpty()){
            policy = X3MLGeneratorPolicy.load(null, X3MLGeneratorPolicy.createUUIDSource(UUID_SIZE));
        }else{
            policy = X3MLGeneratorPolicy.load(new FileInputStream(new File(GENERATOR_POLICY_PATH)), X3MLGeneratorPolicy.createUUIDSource(UUID_SIZE));
        }
        X3MLEngine.Output output = engine.execute(document(INPUT_PATH), policy);
        switch(OUT_FORMAT){
            case RDF_XML:
                switch(OUT_STREAM){
                        case SYSTEM_OUT:
                            output.writeXML(System.out);
                            break;
                        case FILE:
                            output.write(new PrintStream(new File("output.rdf")), Labels.OUTPUT_MIME_TYPE_RDF_XML);
                            break;
                        default:    //don't output
                }break;
            case NTRIPLES:
                switch(OUT_STREAM){
                        case SYSTEM_OUT:
                            output.write(System.out,Labels.OUTPUT_MIME_TYPE_NTRIPLES);
                            break;
                        case FILE:
                            output.write(new PrintStream(new File("output.nt")), Labels.OUTPUT_MIME_TYPE_NTRIPLES);
                            break;
                        default:    //don't output
                }break;
            case TURTLE:
                switch(OUT_STREAM){
                        case SYSTEM_OUT:
                            output.write(System.out,Labels.OUTPUT_MIME_TYPE_TURTLE);
                            break;
                        case FILE:
                            output.write(new PrintStream(new File("output.ttl")), Labels.OUTPUT_MIME_TYPE_TURTLE);
                            break;
                        default:    //don't output
                }break;
            case TRIG:
                switch(OUT_STREAM){
                        case SYSTEM_OUT:
                            output.write(System.out,Labels.OUTPUT_MIME_TYPE_TRIG);
                            break;
                        case FILE:
                            output.write(new PrintStream(new File("output.trig")), Labels.OUTPUT_MIME_TYPE_TRIG);
                            break;
                        default:    //don't output
                }break;
        }
        if(!ASSOCIATION_TABLE_PATH.isEmpty()){
            GeneratorContext.exportAssociationTable(ASSOCIATION_TABLE_PATH);
        }
    }
    
    private enum outputFormat{
        RDF_XML,
        NTRIPLES,
        TURTLE,
        TRIG
    }
    
    private enum outputStream{
        SYSTEM_OUT,
        FILE,
        DISABLED
    }
    
    private static X3MLEngine engine(String path) throws FileNotFoundException {
        return X3MLEngine.load(new FileInputStream(new File(path)));
    }
    
    private static X3MLEngine engine(String mappinsPath, Pair<String,Lang> terminology) throws FileNotFoundException {
        return X3MLEngine.load(new FileInputStream(new File(mappinsPath)),
                               new FileInputStream(new File(terminology.getLeft())),
                               terminology.getRight());
    }
    
    private static Element document(String path) {
        try {
            return documentBuilderFactory().newDocumentBuilder().parse(new File(path)).getDocumentElement();
        }
        catch (Exception e) {
            throw exception("Unable to parse " + path+"\n"+e.toString());
        }
    }
    
    private static DocumentBuilderFactory documentBuilderFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory;
    }
}