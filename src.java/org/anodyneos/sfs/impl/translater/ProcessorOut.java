package org.anodyneos.sfs.impl.translater;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.sfs.impl.util.CodeWriter;
import org.anodyneos.sfs.impl.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class ProcessorOut extends HelperProcessorNS {

    private ProcessorContent processorContent;
    private StringBuffer sb;

    public static final String A_EXPR = "expr";

    public ProcessorOut(TranslaterContext ctx, ProcessorContent p) {
        super(ctx);
        this.processorContent = p;
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        // looks like a new element is coming, so flush characters.
        flushCharacters();
        return processorContent.getProcessorFor(uri, localName, qName);
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // handle "expr" attribute if exists
        String expr = attributes.getValue(A_EXPR);
        if (null != expr && expr.length() > 0) {
            CodeWriter out = getTranslaterContext().getCodeWriter();
            out.printIndent().println("sfsContentHandler.characters(" + expr.trim() + ");");
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
        flushCharacters();
    }

}
