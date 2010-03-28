package org.anodyneos.sfs.impl.translater;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.xml.sax.SAXException;

class TranslaterProcessor extends ElementProcessor {

    public static final String URI_SFS = "http://www.anodyneos.org/namespaces/sfs";
    public static final String URI_NAMESPACES = "http://www.w3.org/2000/xmlns/";

    public TranslaterProcessor(TranslaterContext ctx) {
        super(ctx);
    }

    protected final TranslaterContext getTranslaterContext() {
        return (TranslaterContext) ctx;
    }

    public void startPrefixMapping(java.lang.String prefix, java.lang.String uri) throws SAXException {
        getTranslaterContext().bufferStartPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(java.lang.String prefix) throws SAXException {
        getTranslaterContext().bufferEndPrefix(prefix);
    }

}
