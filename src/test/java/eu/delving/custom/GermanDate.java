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
package eu.delving.custom;

import static gr.forth.ics.isl.x3ml.X3MLGeneratorPolicy.CustomGenerator;
import static gr.forth.ics.isl.x3ml.X3MLGeneratorPolicy.CustomGeneratorException;

/**
 * a date interpreter
 */

public class GermanDate implements CustomGenerator {

    private String text;
    private Bounds bounds;

    enum Bounds {
        Upper, Lower
    }

    @Override
    public void setArg(String name, String value) throws CustomGeneratorException {
        if ("text".equals(name)) {
            text = value;
        }
        else if ("bound".equals(name)) {
            bounds = Bounds.valueOf(value);
        }
        else {
            throw new CustomGeneratorException("Unrecognized argument name: " + name);
        }
    }


    @Override
    public String getValue() throws CustomGeneratorException {
        if (text == null) {
            throw new CustomGeneratorException("Missing text argument");
        }
        if (bounds == null) {
            throw new CustomGeneratorException("Missing bounds argument");
        }
        return lookupCheat(bounds.toString(), text);
    }

    @Override
    public String getValueType() throws CustomGeneratorException {
        if(text == null) {
            throw new CustomGeneratorException("Missing text argument");
        }
        return text.startsWith("http") ? "URI" : "Literal";
    }
    
    /** Returns a boolean flag (with value set to false) indicating that this 
     * generator DOES NOT support merging values from similar elements
     * (elements having the same name). 
     * 
     * @return false*/
    @Override
    public boolean mergeMultipleValues(){
        return false;
    }
    
    @Override
    public void usesNamespacePrefix() {
        ;
    }

    private static String lookupCheat(String bounds, String value) {
        for (String[] entry : CHEAT) {
            if (entry[0].equals(value) && entry[1].equals(bounds)) {
                return entry[2];
            }
        }
        return String.format("Cheat needed for %s:%s", bounds, value);
    }

    private static String[][] CHEAT = {
            {"-116", "Lower", "-0116-12-31T00:00:00"},
            {"-116", "Upper", "-0116-01-01T00:00:00"},
            {"-115", "Lower", "-0115-12-31T00:00:00"},
            {"-115", "Upper", "-0115-01-01T00:00:00"}
    };
}
