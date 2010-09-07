package org.anodyneos.sfs.impl.translater;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.sfs.impl.util.CodeWriter;
import org.anodyneos.sfs.impl.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class ProcessorIf extends HelperProcessorNS {

    private ProcessorContent processorContent;
    private StringBuffer sb;

    public static final String A_TEST = "test";
    public static final String A_EXPR = "expr";

    public ProcessorIf(TranslaterContext ctx, ProcessorContent p) {
        super(ctx);
        this.processorContent = p;
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        // looks like a new element is comming, so flush characters.
        flushCharacters();
        return processorContent.getProcessorFor(uri, localName, qName);
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // handle "expr" attribute if exists
        String test = attributes.getValue(A_TEST);
        String expr = attributes.getValue(A_EXPR);
        CodeWriter out = getTranslaterContext().getCodeWriter();
        out.printIndent().println("if(" + test + ") {");
        out.indentPlus();
        if (null != expr && expr.length() > 0) {
            out.printIndent().println("sfsContentHandler.characters(" + expr.trim() + ");");
        }
    }

    // TODO: use processorContent for this
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
        CodeWriter out = getTranslaterContext().getCodeWriter();
        out.endBlock();
    }

}
