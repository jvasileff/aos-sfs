package org.anodyneos.sfs;

public interface SfsLifecycle {

    void initialize();
    void beforeUse();
    void afterUse();
    void release();

}
