package org.anodyneos.sfs.impl.translater;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.sfs.impl.util.CodeWriter;
import org.xml.sax.SAXException;

class ProcessorExpr extends TranslaterProcessor {

    public static final String XP_EXPR = "expr";
    private StringBuffer sb = new StringBuffer();

    public ProcessorExpr(TranslaterContext ctx) {
        super(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        return super.getProcessorFor(uri, localName, qName);
    }

    /*
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        // do nothing
    }
    */

    public void characters(char[] ch, int start, int length) {
        // collect contents
        sb.append(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) {
        // this is </xp:expr>
        // we now have entire content
        CodeWriter out = getTranslaterContext().getCodeWriter();
        out.printIndent().println("sfsContentHandler.characters(" + sb.toString().trim() + ");");
    }
}
