package gr.forth.ics.isl.x3ml.engine.xpath;

import java.util.UUID;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class UUIDFunction_v3 extends ExtensionFunctionDefinition {
    private static String FUNCTION_NAME = "uuid";

    @Override
    public StructuredQName getFunctionQName() {
        return new StructuredQName(Namespace.CUSTOM_FUNCTIONS_PREFIX, Namespace.CUSTOM_FUNCTIONS_NAMESPACE, FUNCTION_NAME);
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[]{SequenceType.SINGLE_STRING};
    }

    @Override
    public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
        return SequenceType.SINGLE_STRING;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression() {
        return new ExtensionFunctionCall() {
            @Override
            public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
                String input = arguments[0].head().getStringValue();
                String uuid = UUID.nameUUIDFromBytes(input.getBytes()).toString().toUpperCase();
                return StringValue.makeStringValue(uuid);
            }
        };
    }
}
