package org.anodyneos.sfs;

public interface SfsContext {

    SfsContentHandler getSfsContentHandler();

    void initialize(SfsContentHandler sfsCh);
    void release();

}
