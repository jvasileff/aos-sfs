package org.anodyneos.sfs.impl.translater;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.sfs.impl.util.CodeWriter;
import org.anodyneos.sfs.impl.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *  Attribute element must have name and either expr attribute or text contents.
 */
class ProcessorAttribute extends TranslaterProcessor {

    public static final String A_NAME = "name";
    public static final String A_VALUE = "value";


    public ProcessorAttribute(TranslaterContext ctx) {
        super(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        return super.getProcessorFor(uri, localName, qName);
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();
        String name = attributes.getValue(A_NAME);
        String value = attributes.getValue(A_VALUE);
        String codeValue;
        if(value.startsWith("{") && value.endsWith("}")) {
            // this is an expression
            codeValue = "org.anodyneos.sfs.util.SfsCoerce.coerceToString(" +
                value.substring(1, value.length() - 1) + ")";
        } else {
            codeValue = Util.escapeStringQuoted(value);
        }
        out.printIndent().println(
              "sfsContentHandler.addAttribute("
            +        "\"\"" // URI
            + ", " + Util.escapeStringQuoted(name) // localName
            + ", " + Util.escapeStringQuoted(name) // qName
            + ", " + "\"CDATA\"" // type
            + ", " + codeValue
            + ");"
        );
    }

    //public void characters(char[] ch, int start, int length) {
        // collect contents
        //sb.append(ch, start, length);
    //}

    //public void endElement(String uri, String localName, String qName) {
    //}
}
