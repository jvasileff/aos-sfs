package org.anodyneos.sfs;

import org.xml.sax.ContentHandler;

public abstract class SfsFactory {
    private static final String DEFAULT_FACTORY = "org.anodyneos.sfs.impl.SfsFactoryImpl";

    public static SfsFactory getDefaultFactory() {
        // TODO: is this the best way to get cl?
        ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
        ClassLoader cl2 = SfsFactory.class.getClassLoader();

        Class sfsFactoryClass = null;
        if (null != cl1) {
            sfsFactoryClass = loadClass(cl1, DEFAULT_FACTORY);
        }
        if (null == sfsFactoryClass) {
            sfsFactoryClass = loadClass(cl2, DEFAULT_FACTORY);
        }
        if (null == sfsFactoryClass) {
            return null;
        } else {
            try {
                SfsFactory sfsFactory = (SfsFactory) sfsFactoryClass.newInstance();
                return sfsFactory;
            } catch (InstantiationException e) {
                return null;
            } catch (IllegalAccessException e) {
                return null;
            }
        }
    }

    private static Class loadClass(ClassLoader cl, String name) {
        try {
            Class clazz = cl.loadClass(name);
            return clazz;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public abstract SfsContext getSfsContext(ContentHandler ch);
    public abstract void releaseSfsContext(SfsContext sfsContext);

}

