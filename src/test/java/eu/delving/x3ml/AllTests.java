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
import gr.forth.ics.isl.x3ml.X3MLGeneratorPolicy;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import gr.forth.ics.isl.x3ml.engine.Generator;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static gr.forth.ics.isl.x3ml.X3MLEngine.exception;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.riot.Lang;
import static org.junit.Assert.assertTrue;

/**
 * @author Gerald de Jong <gerald@delving.eu>
 * @author Nikos Minadakis <minadakn@ics.forth.gr>
 * @author Yannis Marketakis <marketak@ics.forth.gr>
 */

@RunWith(Suite.class)
@org.junit.runners.Suite.SuiteClasses({
        TestConditions.class,
        TestBase.class,
        TestCoinA.class,
        TestCoinB.class,
        TestLido07.class,
        TestBM.class,
        TestRijks.class,
        TestGML.class,
        TestDoubleJoin.class
})
public class AllTests {
    public static final String MISSING = "!expect :     ";
    public static final String CORRECT = "        ";
    public static final String ERROR = "!error  ";
    private static XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
    private static XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private static List<String> indentStrings = new ArrayList<String>();

    public static X3MLEngine engine(String path) {
        List<String> errors = X3MLEngine.validate(resource(path));
        assertTrue("Invalid: " + errors, errors.isEmpty());
        return X3MLEngine.load(resource(path));
    }
    
    public static X3MLEngine engine(String mappingsPath, Pair<String,Lang> terminology) {
        List<String> errors = X3MLEngine.validate(resource(mappingsPath));
        assertTrue("Invalid: " + errors, errors.isEmpty());
        return X3MLEngine.load(resource(mappingsPath), resource(terminology.getLeft()), terminology.getRight());
    }

    //    public static X3MLContext context(String contextPath, String policyPath) throws X3MLException {
//        return X3MLContext.create(document(contextPath), policy(policyPath));
//    }
//
//    public static X3MLContext context(String contextPath, X3ML.ValuePolicy policy) throws X3MLException {
//        return X3MLContext.create(document(contextPath), policy);
//    }
//
    public static Generator policy(String path) {
        return X3MLGeneratorPolicy.load(resource(path), X3MLGeneratorPolicy.createUUIDSource(1));
    }
    
    public static Generator policy(String path, int uuidSize) {
        return X3MLGeneratorPolicy.load(resource(path), X3MLGeneratorPolicy.createUUIDSource(uuidSize));
    }

    public static Element document(String path) {
        try {
            return documentBuilderFactory().newDocumentBuilder().parse(resource(path)).getDocumentElement();
        }
        catch (Exception e) {
            throw exception("Unable to parse " + path);
        }
    }

    public static InputStream resource(String path) {
        return AllTests.class.getResourceAsStream(path);
    }

    public static String toXml(Node node) {
        if (node.getNodeType() != Node.ELEMENT_NODE)
            throw new IllegalArgumentException("toXml should only be called on an element");
        try {
            Map<String, String> namespaces = new TreeMap<String, String>();
            gatherNamespaces(node, namespaces);
            List<Namespace> nslist = new ArrayList<Namespace>();
            for (Map.Entry<String, String> entry : namespaces.entrySet()) {
                if (entry.getValue().trim().isEmpty()) continue;
                nslist.add(eventFactory.createNamespace(entry.getKey(), entry.getValue()));
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            XMLEventWriter out = outputFactory.createXMLEventWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            out.add(eventFactory.createStartDocument());
            out.add(eventFactory.createCharacters("\n"));
            List<Attribute> attributes = getAttributes(node);
            String prefix = node.getPrefix();
            if (prefix == null) prefix = "";
            String uri = node.getNamespaceURI();
            if (uri == null) uri = "";
            String localName = node.getLocalName();
            if (localName == null) {
                localName = node.getNodeName();
            }
            out.add(eventFactory.createStartElement(prefix, uri, localName, attributes.iterator(), nslist.iterator()));
            out.add(eventFactory.createCharacters("\n"));
            NodeList kids = node.getChildNodes();
            for (int walk = 0; walk < kids.getLength(); walk++) {
                Node kid = kids.item(walk);
                switch (kid.getNodeType()) {
                    case Node.TEXT_NODE:
                    case Node.CDATA_SECTION_NODE:
                    case Node.COMMENT_NODE:
                        break;
                    case Node.ELEMENT_NODE:
                        nodeToXml(out, kid, 1);
                        break;
                    default:
                        throw new RuntimeException("Node type not implemented: " + kid.getNodeType());
                }
            }
            out.add(eventFactory.createCharacters("\n"));
            out.add(eventFactory.createEndElement(prefix, uri, localName));
            out.add(eventFactory.createEndDocument());
            out.flush();
            return new String(outputStream.toByteArray(), "UTF-8");
        }
        catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String[] xmlToNTriples(String xmlResource) {
        Model model = ModelFactory.createMemModelMaker().createModel("gumby");
        model.read(resource(xmlResource), null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        model.write(baos, "N-TRIPLE");
        return new String(baos.toByteArray()).split("\n");
    }

    public static List<String> compareNTriples(String[] expected, String[] actual) {
        Set<String> actualSet = new TreeSet<String>(Arrays.asList(actual));
        Set<String> expectedSet = new TreeSet<String>(Arrays.asList(expected));
        List<String> errors = new ArrayList<String>();
        for (String actualOne : actualSet) {
            if (expectedSet.contains(actualOne)) {
                errors.add(CORRECT + ":     " + filterTriple(actualOne));
            }
            else {
                errors.add(ERROR + ":     " + filterTriple(actualOne));
            }
        }
        for (String expectedOne : expectedSet) {
            if (!actualSet.contains(expectedOne)) {
                errors.add(MISSING + filterTriple(expectedOne));
            }
        }
        Collections.sort(errors);
//        Collections.sort(errors, new Comparator<String>() {
//            @Override
//            public int compare(String a, String b) {
//                return getPredicate(a).compareTo(getPredicate(b));
//            }
//        });
        return errors;
    }

    public static boolean errorFree(List<String> diff) {
        for (String line : diff) {
            if (line.startsWith("!")) {
                return false;
            }
        }
        return true;
    }

    // === private stuff

    private static Pattern TRIPLE = Pattern.compile("^.*<?_?([^> ]+)>?\\s+<([^>]+)>\\s+<?([^>]+)>? \\.$");

    private static String getPredicate(String s) {
        Matcher matcher = TRIPLE.matcher(s);
        if (!matcher.matches()) {
            throw new RuntimeException("Mismatch: [" + s + "]");
        }
        return matcher.group(2) + " " + matcher.group(1);
    }

    private static String filterTriple(String triple) {
        return triple;
//        Matcher matcher = TRIPLE.matcher(triple);
//        if (!matcher.matches()) {
//            throw new RuntimeException("Mismatch: [" + triple + "]");
//        }
//        String subject = matcher.group(1);
//        String predicate = matcher.group(2);
//        String object = matcher.group(3);
//        return String.format("[%s] -(%s)-> [%s]", lastSlash(subject), lastSlash(predicate), lastSlash(object));
    }

    private static String lastSlash(String part) {
        int delim = Math.max(part.lastIndexOf('/'), Math.max(part.lastIndexOf('#'), part.lastIndexOf(':')));
        if (delim < 0) {
            return part;
        }
        else {
            return part.substring(delim + 1);
        }
    }

    private static void nodeToXml(XMLEventWriter out, Node node, int level) throws XMLStreamException {
        if (node.getLocalName() == null) return;
        List<Attribute> attributes = getAttributes(node);
        String indentString = level > 0 ? indentString(level) : null;
        if (indentString != null) out.add(eventFactory.createCharacters(indentString));
        out.add(eventFactory.createStartElement(
                node.getPrefix() == null ? "" : node.getPrefix(),
                node.getNamespaceURI() == null ? "" : node.getNamespaceURI(),
                node.getLocalName(),
                level > 0 ? attributes.iterator() : null,
                null
        ));
        NodeList kids = node.getChildNodes();
        boolean nodeHasSubelement = false;
        for (int walk = 0; walk < kids.getLength(); walk++) {
            if (kids.item(walk).getNodeType() == Node.ELEMENT_NODE) {
                nodeHasSubelement = true;
                break;
            }
        }
//        if (nodeHasSubelement) out.add(eventFactory.createCharacters("\n"));
        for (int walk = 0; walk < kids.getLength(); walk++) {
            Node kid = kids.item(walk);
            switch (kid.getNodeType()) {
                case Node.TEXT_NODE:
                    out.add(eventFactory.createCharacters(kid.getTextContent()));
                    break;
                case Node.CDATA_SECTION_NODE:
                    out.add(eventFactory.createCData(kid.getTextContent()));
                    break;
                case Node.ATTRIBUTE_NODE:
                    break;
                case Node.ELEMENT_NODE:
                    nodeToXml(out, kid, level + 1);
                    break;
            }
        }
        if (nodeHasSubelement && indentString != null) out.add(eventFactory.createCharacters(indentString));
        out.add(eventFactory.createEndElement(node.getPrefix(), node.getNamespaceURI(), node.getLocalName()));
    }

    private static List<Attribute> getAttributes(Node node) {
        NamedNodeMap nodeAttributes = node.getAttributes();
        List<Attribute> attributes = new ArrayList<Attribute>();
        for (int walk = 0; walk < nodeAttributes.getLength(); walk++) {
            Node attrItem = nodeAttributes.item(walk);
            if (attrItem.getPrefix() == null || attrItem.getPrefix().isEmpty()) {
                attributes.add(eventFactory.createAttribute(attrItem.getNodeName(), attrItem.getNodeValue()));
            }
            else {
                attributes.add(eventFactory.createAttribute(
                        attrItem.getPrefix(), attrItem.getNamespaceURI(), attrItem.getLocalName(),
                        attrItem.getNodeValue()
                ));
            }
        }
        return attributes;
    }

    private static String indentString(int level) {
        if (level >= indentStrings.size()) {
            StringBuilder indentBuilder = new StringBuilder(level * 4);
            for (int walk = 0; walk < level + 1; walk++) {
                if (walk == indentStrings.size()) indentStrings.add(indentBuilder.toString());
                indentBuilder.append("    ");
            }
        }
        return indentStrings.get(level);
    }

    private static void gatherNamespaces(Node node, Map<String, String> namespaces) {
        if (node.getPrefix() != null && node.getNamespaceURI() != null) {
            namespaces.put(node.getPrefix(), node.getNamespaceURI());
        }
        if (node instanceof Element) {
            Element element = (Element) node;
            NamedNodeMap attrs = element.getAttributes();
            for (int walk = 0; walk < attrs.getLength(); walk++) {
                if (attrs.item(walk).getPrefix() == null) continue;
                namespaces.put(attrs.item(walk).getPrefix(), attrs.item(walk).getNamespaceURI());
            }
        }
        NodeList list = node.getChildNodes();
        for (int walk = 0; walk < list.getLength(); walk++) {
            Node sub = list.item(walk);
            gatherNamespaces(sub, namespaces);
        }
    }

    public static DocumentBuilderFactory documentBuilderFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory;
    }
}
