package org.anodyneos.sfs.impl.translater;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.sfs.impl.util.CodeWriter;
import org.xml.sax.SAXException;

class ProcessorLogic extends TranslaterProcessor {

    /* TODO: need to think about nesting other <xp:xxx> elements within this
     * one.  Do we need checks to limit the ones available in ProcessesorContent?
     */

    private ProcessorContent processorContent;
    private StringBuffer sb;

    public ProcessorLogic(TranslaterContext ctx, ProcessorContent p) {
        super(ctx);
        this.processorContent = p;
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        // looks like a new element is comming, so flush characters.
        flushCharacters();
        return processorContent.getProcessorFor(uri, localName, qName);
    }

    /*
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        // do nothing yet.
    }
    */

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
                out.println(s);
            }
            sb = null;
        }
    }

    public void endElement(String uri, String localName, String qName) {
        flushCharacters();
    }
}
