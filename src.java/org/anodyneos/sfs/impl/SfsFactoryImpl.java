package org.anodyneos.sfs.impl;

import org.anodyneos.sfs.SfsContentHandler;
import org.anodyneos.sfs.SfsContext;
import org.anodyneos.sfs.SfsFactory;
import org.xml.sax.ContentHandler;

public class SfsFactoryImpl extends SfsFactory {

    public SfsFactoryImpl() {
        // super();
    }

    public SfsContext getSfsContext(ContentHandler ch) {
        SfsContext sfsCtx = new SfsContextImpl();
        sfsCtx.initialize(new SfsContentHandler(ch));
        return sfsCtx;
    }

    public void releaseSfsContext(SfsContext sfsCtx) {
        sfsCtx.release();
    }

}

