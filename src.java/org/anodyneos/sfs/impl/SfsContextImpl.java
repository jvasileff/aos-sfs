package org.anodyneos.sfs.impl;

import org.anodyneos.sfs.SfsContentHandler;
import org.anodyneos.sfs.SfsContext;

public class SfsContextImpl implements SfsContext {

    private SfsContentHandler sfsCh;

    public SfsContentHandler getSfsContentHandler() {
        return sfsCh;
    }

    public void initialize(SfsContentHandler sfsCh) {
        this.sfsCh = sfsCh;
    }

    public void release() {
        this.sfsCh = null;
    }

}
