package org.anodyneos.sfs.impl.translater;

import org.anodyneos.sfs.impl.util.CodeWriter;
import org.anodyneos.sfs.impl.util.Util;
import org.xml.sax.SAXException;

public class HelperProcessorNS extends TranslaterProcessor {

    private boolean pushed = false;

    public HelperProcessorNS(TranslaterContext ctx) {
        super(ctx);
    }

    @Override
    public final void startPrefixMapping(String prefix, String uri) throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();
        out.printIndent().println(
              "sfsContentHandler.pushPhantomPrefixMapping("
            +       Util.escapeStringQuoted(prefix)
            + "," + Util.escapeStringQuoted(uri)
            + ");");
    }

    @Override
    public final void endPrefixMapping(String prefix) throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();
        out.printIndent().println("sfsContentHandler.popPhantomPrefixMapping();");
    }

    /*
    @Override
    public final void startPrefixMapping(String prefix, String uri) throws SAXException {
        NamespaceSupport ns = getTranslaterContext().getNamespaceSupport();
        if (! pushed) {
            ns.pushContext();
        }
        ns.declarePrefix(prefix, uri);
    }

    @Override
    public final void startElement(String uri, String localName, String qName, Attributes attributes)
    throws SAXException {
        NamespaceSupport ns = getTranslaterContext().getNamespaceSupport();
        if(! pushed) {
            ns.pushContext();
            pushed = true;
        } else {
            CodeWriter out = getTranslaterContext().getCodeWriter();
            Enumeration en = ns.getDeclaredPrefixes();
            while (en.hasMoreElements()) {
                String nsPrefix = (String) en.nextElement();
                String nsUri = (String) ns.getURI(nsPrefix);
                out.printIndent().println(
                      "sfsContentHandler.pushPhantomPrefixMapping("
                    +       Util.escapeStringQuoted(nsPrefix)
                    + "," + Util.escapeStringQuoted(nsUri)
                    + ");");
            }
        }
        startElement2(uri, localName, qName, attributes);
    }

    @Override
    public final void endElement(String uri, String localName, String qName) throws SAXException {
        NamespaceSupport ns = getTranslaterContext().getNamespaceSupport();
        CodeWriter out = getTranslaterContext().getCodeWriter();
        endElement2(uri, localName, qName);

        Enumeration en = ns.getDeclaredPrefixes();
        while (en.hasMoreElements()) {
            en.nextElement();
            out.printIndent().println("sfsContentHandler.popPhantomPrefixMapping();");
        }
        ns.popContext();
    }

    @Override
    public final void endPrefixMapping(String prefix) throws SAXException {
        // noop
    }

    /**
     * Subclasses should override this function.
     * /
    public void startElement2(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
    }

    /**
     * Subclasses should override this function.
     * /
    public void endElement2(String uri, String localName, String qName)
            throws SAXException {
    }
    */

}
