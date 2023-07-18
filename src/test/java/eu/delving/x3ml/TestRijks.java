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
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.List;

import static eu.delving.x3ml.AllTests.*;
import static org.junit.Assert.assertTrue;

/**
 * @author Gerald de Jong <gerald@delving.eu>
 * @author Yannis Marketakis <marketak@ics.forth.gr>
 * @author Nikos Minadakis <minadakn@ics.forth.gr>
 */
public class TestRijks {

    @Test
    public void testRough() {
        X3MLEngine engine = engine("/rijks/rijks.x3ml");
        X3MLEngine.Output output = engine.execute(document("/rijks/rijks.xml"), policy("/rijks/rijks-policy.xml"));
        //expected result ?
    }

    @Test
    public void testAttribute() {
        X3MLEngine engine = engine("/rijks/02-attribute.x3ml");
        X3MLEngine.Output output = engine.execute(document("/rijks/rijks.xml"), policy("/rijks/02-attribute-policy.xml"));
        //expected result ?
    }

    @Test
    public void testDimension() {
        X3MLEngine engine = engine("/rijks/01-dimension.x3ml");
        X3MLEngine.Output output = engine.execute(document("/rijks/rijks.xml"), policy("/rijks/01-dimension-policy.xml"));
        String[] mappingResult = output.toStringArray();
        String[] expectedResult = xmlToNTriples("/rijks/01-dimension-rdf.xml");
        List<String> diff = compareNTriples(expectedResult, mappingResult);
        assertTrue("\n" + StringUtils.join(diff, "\n") + "\n", errorFree(diff));
    }
}
