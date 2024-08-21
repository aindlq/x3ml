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
package gr.forth.ics.isl.x3ml.engine;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.shared.uuid.JenaUUID;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphSimpleMem;
import com.hp.hpl.jena.sparql.core.Quad;
import javax.xml.namespace.NamespaceContext;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static gr.forth.ics.isl.x3ml.X3MLEngine.exception;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import java.util.Iterator;
import java.util.List;

import gr.forth.Labels;
import gr.forth.UriValidator;
import gr.forth.ics.isl.x3ml.X3MLEngine.Output;
import gr.forth.ics.isl.x3ml.engine.X3ML.TypeElement;

import java.io.OutputStream;
import lombok.extern.log4j.Log4j2;


/**
 * The class is responsible for exporting the transformation results. More specifically 
 * it exports the contents of the Jena graph model.
 * It supports exporting triples and quads.
 *
 * @author Gerald de Jong &lt;gerald@delving.eu&gt;
 * @author Nikos Minadakis &lt;minadakn@ics.forth.gr&gt;
 * @author Yannis Marketakis &lt;marketak@ics.forth.gr&gt;
 */
@Log4j2
public class ModelOutput implements Output {

    public static DatasetGraph quadGraph=new DatasetGraphSimpleMem();
    private final Model model;
    private final NamespaceContext namespaceContext;

    //provenance related
    private final Model provenanceModel;
    private final String label_str = "http://www.w3.org/2000/01/rdf-schema#label";
    private final String p9_str = "http://www.cidoc-crm.org/cidoc-crm/P9_consists_of";
    private final String p140_str = "http://www.cidoc-crm.org/cidoc-crm/P140_assigned_attribute_to";
    private final String p141_str = "http://www.cidoc-crm.org/cidoc-crm/P141_assigned";
    private final String p177_str = "http://www.cidoc-crm.org/cidoc-crm/P177_assigned_property_of_type";

    private final Property label;
    private final Property p9;
    private final Property p140;
    private final Property p141;
    private final Property p177;
    //

    public ModelOutput(Model model, Model provenanceModel, NamespaceContext namespaceContext) {
        this.model = model;
        this.namespaceContext = namespaceContext;

        this.provenanceModel = provenanceModel;
        this.label = this.provenanceModel.createProperty(label_str);
        this.p9 = this.provenanceModel.createProperty(p9_str);
        this.p140 = this.provenanceModel.createProperty(p140_str);
        this.p141 = this.provenanceModel.createProperty(p141_str);
        this.p177 = this.provenanceModel.createProperty(p177_str);
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public void close() {
        model.close();        
    }
    
    public String getNamespace(TypeElement typeElement){
        if (typeElement == null) {
            throw exception("Missing qualified name");
        }
        if (typeElement.getLocalName().startsWith("http:")){
            return typeElement.getLocalName();
        }else{
            String typeElementNamespace = namespaceContext.getNamespaceURI(typeElement.getPrefix());
            return typeElementNamespace+typeElement.getLocalName();
        }
    }

    public Resource createTypedResource(String uriString, TypeElement typeElement) {
        if (typeElement == null) {
            throw exception("Missing qualified name");
        }
        if (typeElement.getLocalName().startsWith("http:")){
            String typeElementNamespace = "";
            return model.createResource(uriString, model.createResource(typeElementNamespace + typeElement.getLocalName()));
        }else{
            String typeElementNamespace = namespaceContext.getNamespaceURI(typeElement.getPrefix());
            if(typeElementNamespace==null){
                throw exception("The namespace with prefix \""+typeElement.getPrefix()+"\" has not been declared");
            }
            return model.createResource(uriString, model.createResource(typeElementNamespace + typeElement.getLocalName()));
        }
    }
    
    /* Used for creating labels (rdfs:label or skos:label) */
    public Property createProperty(TypeElement typeElement) {
        if (typeElement == null) {
            throw exception("Missing qualified name");
        }
        
        if (typeElement.getLocalName().startsWith("http:")){
            String typeElementNamespace = "";
            return model.createProperty(typeElementNamespace, typeElement.getLocalName());
        }else{ 
            String typeElementNamespace = namespaceContext.getNamespaceURI(typeElement.getPrefix());
            if(typeElementNamespace==null){
                throw exception("The namespace with prefix \""+typeElement.getPrefix()+"\" has not been declared");
            }
            return model.createProperty(typeElementNamespace, typeElement.getLocalName());
        }
        
    }

    public Property createProperty(X3ML.Relationship relationship) {
        if (relationship == null) {
            throw exception("Missing qualified name");
        }
        if (relationship.getLocalName().startsWith("http:")){
            String propertyNamespace = "";
            return model.createProperty(propertyNamespace, relationship.getLocalName());
        }else if (relationship.getLocalName().equals("MERGE")){
            return null;
        }
        else{ 
            String propertyNamespace = namespaceContext.getNamespaceURI(relationship.getPrefix());
            if(propertyNamespace==null){
                throw exception("The namespace with prefix \""+relationship.getPrefix()+"\" has not been declared");
            }
            return model.createProperty(propertyNamespace, relationship.getLocalName());
        }
    }
    

    public Literal createLiteral(String value, String language) {
        return model.createLiteral(value, language);
    }

    public Literal createTypedLiteral(String value, TypeElement typeElement) {
        String literalNamespace = namespaceContext.getNamespaceURI(typeElement.getPrefix());
        String typeUri = literalNamespace + typeElement.getLocalName();
        if(literalNamespace == null) {  //we have a fully qualified namespace (e.g. http://www.w3.org/2001/XMLSchema#dateTime)
            typeUri=typeElement.getLocalName();
        }
        return model.createTypedLiteral(value, typeUri);
    }

    /** Exports the transformed contents of graph in XML abbreviated RDF format using the given output stream.
     * 
     * @param out the output stream that will be used for exporting the transformed contents */
    @Override
    public void writeXML(OutputStream out) {
        if(X3ML.RootElement.hasNamedGraphs){
            this.updateNamedgraphRefs(XPathInput.entireInputExportedRefUri);
            this.writeQuads(out);
        }else{
            model.write(out, Labels.OUTPUT_FORMAT_RDF_XML_ABBREV);
        }
    }
    
    private void updateNamedgraphRefs(String uri){
        Iterator<Quad> qIter=quadGraph.find(Node.ANY, Node.ANY, Node.ANY, Node.ANY);
        while(qIter.hasNext()){
            quadGraph.add(new ResourceImpl("http://default").asNode(), 
                          new ResourceImpl(uri).asNode(), 
                          new ResourceImpl("http://PX_is_refered_by").asNode(), 
                          new ResourceImpl(qIter.next().getGraph().getURI()).asNode());
        }
    }
    
    /** Exports the transformed contents of graph in RDF/XML format using the given output stream.
     * 
     * @param out the output stream that will be used for exporting the transformed contents */
    public void writeXMLPlain(OutputStream out) {
        model.write(out, Labels.OUTPUT_FORMAT_RDF_XML);
    }
    
    /** Exports the transformed contents of graph in NTRIPLES format using the given output stream.
     * 
     * @param out the output stream that will be used for exporting the transformed contents */
    public void writeNTRIPLE(OutputStream out) {
        model.write(out, Labels.OUTPUT_FORMAT_NTRIPLE);
    }

    /** Exports the transformed contents of graph in TURTLE format using the given output stream.
     * 
     * @param out the output stream that will be used for exporting the transformed contents */
    public void writeTURTLE(OutputStream out) {
        model.write(out, Labels.OUTPUT_FORMAT_TURTLE);
    }
    
    /** Exports the transformed contents of graph in TURTLE format using the given output stream.
     * 
     * @param out the output stream that will be used for exporting the transformed contents */
    public void writeJsonLD(OutputStream out) {
        model.write(out, Labels.OUTPUT_FORMAT_JSON_LD);
    }

    /** Exports the transformed contents of the Jena model in the given output stream with respect to 
     * the given format. Depending on the selected format the contents can be exported as triples or 
     * as quads. More specifically, if namedgraphs have been used within the mappings, then the transformed 
     * contents will be exported in TRIG format (even if the given format is different). 
     * 
     * @param out the output stream that will be used for exporting the transformed contents
     * @param format the export format. It can be any of the following: [application/rdf+xml,
     *                                                                   application/rdf+xml_plain, 
     *                                                                   application/n-triples, 
     *                                                                   application/trig, 
     *                                                                   text/turtle]
     */
    @Override
    public void write(OutputStream out, String format) {
        if(X3ML.RootElement.hasNamedGraphs){    //export quads
            if(!Labels.OUTPUT_MIME_TYPE_TRIG.equalsIgnoreCase(format)){
                throw exception("Invalid mime type used for exporting quads. Please specify "+Labels.TRIG+" format");
//                log.warn("Invalid mime type used for exporting quads.");
//                File outputFileTrig=new File("output-"+System.currentTimeMillis()+"."+Labels.TRIG);
//                log.warn("Exporting contents in TRIG format in file "+outputFileTrig);
//                try{
//                    writeQuads(new PrintStream(outputFileTrig));
//                }catch(FileNotFoundException ex){
//                    throw exception("An error occurred while exporting Quads",ex);
//                }
            }else{
                writeQuads(out);
            }
        }else{  //export triples
            if (Labels.OUTPUT_MIME_TYPE_NTRIPLES.equalsIgnoreCase(format)) {
                writeNTRIPLE(out);
            } else if (Labels.OUTPUT_MIME_TYPE_TURTLE.equalsIgnoreCase(format)) {
                writeTURTLE(out);
            } else if (Labels.OUTPUT_MIME_TYPE_RDF_XML.equalsIgnoreCase(format)) {
                writeXML(out);
            } else if (Labels.OUTPUT_MIME_TYPE_RDF_XML_ABBREV.equalsIgnoreCase(format)){
                writeXMLPlain(out);
            } else if (Labels.OUTPUT_MIME_TYPE_TRIG.equalsIgnoreCase(format)){
                writeQuads(out);
            } else if (Labels.OUTPUT_MIME_TYPE_JSON_LD.equalsIgnoreCase(format)){
                writeJsonLD(out);
            }else {
                writeXML(out);
            }
        }
    }
    
    /** Exports the transformed contents of graph as Quads using the given output stream.
     * The contents are exported in TRIG format.
     * This method is used when: (a) the mappings contain namedgraphs, (b) the user defined trig as the export format.
     * 
     * @param out the output stream that will be used for exporting the transformed contents */
    public void writeQuads(OutputStream out){
        StmtIterator stIter=model.listStatements();
        String defaultGraphSpace="http://default";
        if(quadGraph.isEmpty()){    // No namedgraphs were used, so output everything from the triples model in the quadGraph to export it in TRIG format
            Node defgraph=new ResourceImpl(defaultGraphSpace).asNode();
            while(stIter.hasNext()){
                Statement st=stIter.next();
                quadGraph.add(defgraph, st.getSubject().asNode(), st.getPredicate().asNode(), st.getObject().asNode());
            } 
        }else{  // There are namedgraphs, So export in the default graph only those triples that are not assigned any namedgraph
            Node defgraph=new ResourceImpl(defaultGraphSpace).asNode();
            while(stIter.hasNext()){
                Statement st=stIter.next();
                if(!quadGraph.contains(null,st.getSubject().asNode(), st.getPredicate().asNode(), st.getObject().asNode())){
                    quadGraph.add(defgraph, st.getSubject().asNode(), st.getPredicate().asNode(), st.getObject().asNode());
                }
            } 
        }
        RDFDataMgr.write(out, quadGraph, Lang.TRIG); // or NQUADS
        
    }

    @Override
    public String[] toStringArray() {
        return toString().split("\n");
    }

    @Override
    public String toString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeNTRIPLE(new PrintStream(baos));
        return new String(baos.toByteArray());
    }

    // provenance related methods

    // we assume here that 3M executes all mappings sequentially
    // so first domain, then links, then range for every link
    // so the provenance logic is implemented in a statefull manner
    
    private Resource pathE13;
    public void startE13Link(Path path, List<Resource> os) {
        this.pathE13 = this.provenanceModel.createResource(JenaUUID.generate().asURN());

        if (path.path.comments != null && path.path.comments.comments.size() > 0) {
            String linkLabel = path.path.comments.comments.get(0).content;
            String fieldUriStr = UriValidator.encodeURI(provenanceModel.getNsPrefixURI("prefix") + "field/" + linkLabel).toASCIIString();
            Property linkField = this.provenanceModel.createProperty(fieldUriStr);
            this.pathE13.addProperty(p177, linkField);
        }

        for (Resource o: os) {
          this.pathE13.addProperty(p140, o);
        }
    }

    public void addE13Path(Path path$, Resource s, Property p, RDFNode o) {
        String e13Uri = "http://artresearch.net/resource/E13/" + DigestUtils.sha256Hex(s.toString() + p.toString() + o.toString());
        Resource e13 = this.provenanceModel.createResource(e13Uri);

        e13.addProperty(this.p140, s)
           .addProperty(this.p177, p)
           .addProperty(this.p141, o);

        this.pathE13.addProperty(this.p9, e13);
    }

    public void endE13Link(Property p, RDFNode o) {
        this.pathE13.addProperty(p141, o);
    }

    @Override
    public void writeProvenance(OutputStream outputStream, String rdfFormat) {   
         this.provenanceModel.write(outputStream, rdfFormat);     
    }
}
