package org.anodyneos.sfs;

public interface SfsPage {

    void sfsService(SfsContext sfsCtx, Object[] args) throws SfsException;

}
