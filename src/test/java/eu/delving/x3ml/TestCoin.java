package eu.delving.x3ml;

import org.junit.Test;

import java.util.UUID;

import static eu.delving.x3ml.AllTests.context;
import static eu.delving.x3ml.AllTests.engine;

/**
 * @author Gerald de Jong <gerald@delving.eu>
 */

public class TestCoin {

    @Test
    public void testSimpleCoinExample() throws X3MLException {
        X3MLContext context = context("/coin/coin-input.xml", new X3ML.URIPolicy() {
            @Override
            public String generateUri(String name, X3ML.URIArguments arguments) {
                if (!"UUID".equals(name)) throw new X3MLException("Only handles UUID");
                return "uuid:" + UUID.randomUUID().toString();
            }
        });
        engine("/coin/coin1.x3ml").execute(context);
        context.write(System.out);
    }
}
