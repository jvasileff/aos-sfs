package org.anodyneos.sfs.impl.translater;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.sfs.impl.util.CodeWriter;
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
        CodeWriter out = getTranslaterContext().getCodeWriter();
        out.printIndent().println(
              "_sfsContentHandler.startPrefixMapping("
            + "\""   + prefix + "\""
            + ",\""  + uri + "\""
            + ");"
        );
    }

    public void endPrefixMapping(java.lang.String prefix) throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();
        out.printIndent().println(
              "_sfsContentHandler.endPrefixMapping("
            + "\""   + prefix + "\""
            + ");"
        );
    }

}
