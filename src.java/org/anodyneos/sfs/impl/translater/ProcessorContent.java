package org.anodyneos.sfs.impl.translater;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.sfs.impl.util.CodeWriter;
import org.anodyneos.sfs.impl.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/*
public class ProcessorContent extends NullProcessor {
    public ProcessorContent(TranslaterContext ctx) {
        super(ctx);
    }
}
*/

class ProcessorContent extends TranslaterProcessor {

    // this is used to break out of logic mode and begin outputing result text
    // or elements.
    public static final String SFS_OUT = "out";
    public static final String SFS_ATTRIBUTE = "attribute";
    public static final String SFS_EXPR = "expr";
    public static final String SFS_LOGIC = "logic";
    public static final String SFS_IF = "if";

    private StringBuffer sb;

    public ProcessorContent(TranslaterContext ctx) {
        super(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        // looks like a new element is comming, so flush characters.
        flushCharacters();
        if (URI_SFS.equals(uri)) {
            if (SFS_EXPR.equals(localName)) {
                return new ProcessorExpr(getTranslaterContext());
            } else if (SFS_LOGIC.equals(localName)) {
                return new ProcessorLogic(getTranslaterContext(), this);
            } else if (SFS_OUT.equals(localName)) {
                return new ProcessorOut(getTranslaterContext(), this);
            } else if (SFS_ATTRIBUTE.equals(localName)) {
                return new ProcessorAttribute(getTranslaterContext());
            } else if (SFS_IF.equals(localName)) {
                return new ProcessorIf(getTranslaterContext(), this);
            } else {
                return super.getProcessorFor(uri, localName, qName);
            }
        } else {
            return this;
        }
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();
     
        Map prefixBuffer = getTranslaterContext().getBufferedStartPrefixMappings();
        Iterator it = prefixBuffer.keySet().iterator();
        while (it.hasNext()) {
            String prefix = (String) it.next();
            String tmpUri = (String) prefixBuffer.get(prefix);

            out.printIndent().println(
                    "sfsContentHandler.startPrefixMapping("
                  + "\""   + prefix + "\""
                  + ",\""  + uri + "\""
                  + ");"
              );
        }
        getTranslaterContext().clearBufferedStartPrefixMappings();

        // start element
        out.printIndent().println(
              "sfsContentHandler.startElement("
            +        Util.escapeStringQuoted(uri)
            + ", " + Util.escapeStringQuoted(localName)
            + ", " + Util.escapeStringQuoted(qName)
            + ", null);"
        );
        // set attributes
        for (int i = 0; i < attributes.getLength(); i++) {
            String value = attributes.getValue(i);
            String codeValue;
            if(value.startsWith("{") && value.endsWith("}")) {
                // this is an expression
                codeValue = "org.anodyneos.sfs.util.SfsCoerce.coerceToString(" +
                    value.substring(1, value.length() - 1) + ")";
            } else {
                codeValue = Util.escapeStringQuoted(attributes.getValue(i));
            }
            out.printIndent().println(
                  "sfsContentHandler.addAttribute("
                +        Util.escapeStringQuoted(attributes.getURI(i))
                + ", " + Util.escapeStringQuoted(attributes.getLocalName(i))
                + ", " + Util.escapeStringQuoted(attributes.getQName(i))
                + ", " + Util.escapeStringQuoted(attributes.getType(i))
                + ", " + codeValue
                + ");"
            );
        }
    }

    public void characters(char[] ch, int start, int length) {
        if (null == sb) {
            sb = new StringBuffer();
        }
        sb.append(ch, start, length);
    }

    private void flushCharacters() {
        // what about strip-space?  Is this what we want?  Configurable?
        CodeWriter out = getTranslaterContext().getCodeWriter();
        if (sb != null) {
            String s = sb.toString();
            String t = s.trim();
            // don't output if only whitespace
            if (! "".equals(t)) {
                out.printIndent().println("sfsContentHandler.characters(" + Util.escapeStringQuoted(s) + ");");
            }
            sb = null;
        }
    }

    public void endElement(String uri, String localName, String qName) {
        // end element
        flushCharacters();
        CodeWriter out = getTranslaterContext().getCodeWriter();
        out.printIndent().println(
              "sfsContentHandler.endElement("
            +        Util.escapeStringQuoted(uri)
            + ", " + Util.escapeStringQuoted(localName)
            + ", " + Util.escapeStringQuoted(qName)
            + ");"
        );

        Set prefixBuffer = getTranslaterContext().getBufferedEndPrefixes();
        Iterator it = prefixBuffer.iterator();
        while (it.hasNext()) {
            String prefix = (String) it.next();

            out.printIndent().println(
                  "sfsContentHandler.endPrefixMapping("
                + "\""   + prefix + "\""
                + ");"
            );
            getTranslaterContext().clearBufferedEndPrefixes();
        }
    }
}
