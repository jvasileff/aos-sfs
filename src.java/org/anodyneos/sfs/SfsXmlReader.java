package org.anodyneos.sfs;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public abstract class SfsXmlReader implements XMLReader {

    /*
    private SfsPage sfs;

    public SfsXmlReader(SfsPage sfs) {
        this.sfs = sfs;
    }
    */

    public abstract void parse(InputSource input) throws SAXException;
    /*
    {
        // create SfsContext
        SfsContext sfsContext = SfsFactory.getDefaultFactory().getSfsContext(getContentHandler());

        // process
        getContentHandler().startDocument();
        sfs.sfsService(sfsContext, new Object[] {});
        getContentHandler().endDocument();
    }
    */

    public SfsContext createSfsContext() {
        return SfsFactory.getDefaultFactory().getSfsContext(getContentHandler());
    }

    public void parse(String systemId) {
        throw new java.lang.UnsupportedOperationException();
    }

    // generic stuff
    protected ContentHandler contentHandler;
    protected DTDHandler dtdHandler;
    protected EntityResolver entityResolver;
    protected ErrorHandler errorHandler;

    public ContentHandler getContentHandler() {
        return contentHandler;
    }
    public DTDHandler getDTDHandler() {
        return dtdHandler;
    }
    public EntityResolver getEntityResolver() {
        return entityResolver;
    }
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
    public boolean getFeature(java.lang.String name) {
        //System.out.println("----------- getFeature " + name);
        return true;
    }
    public java.lang.Object getProperty(java.lang.String name) {
        return null;
    }
    public void setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
    }
    public void setDTDHandler(DTDHandler handler) {
        this.dtdHandler = handler;
    }
    public void setEntityResolver(EntityResolver resolver) {
        this.entityResolver = resolver;
    }
    public void setErrorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
    }
    public void setFeature(java.lang.String name, boolean value) {
        //System.out.println("----------- setFeature " + name);
    }
    public void setProperty(java.lang.String name, java.lang.Object value) {
    }

}
