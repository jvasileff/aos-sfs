package org.anodyneos.sfs.util;

public class SfsCoerce {

    public static final Object coerceToObject(char x)       { return new Character(x); }
    public static final Object coerceToObject(byte x)       { return new Byte(x); }
    public static final Object coerceToObject(boolean x)    { return new Boolean(x); }
    public static final Object coerceToObject(int x)        { return new Integer(x); }
    public static final Object coerceToObject(long x)       { return new Long(x); }
    public static final Object coerceToObject(float x)      { return new Float(x); }
    public static final Object coerceToObject(double x)     { return new Double(x); }
    public static final Object coerceToObject(Object x)     { return x; }

    public static final String coerceToString(char x)       { return String.valueOf(x); }
    public static final String coerceToString(char[] x)     { return String.valueOf(x); }
    public static final String coerceToString(byte x)       { return String.valueOf(x); }
    public static final String coerceToString(byte[] x)     { return new String(x); }
    public static final String coerceToString(boolean x)    { return String.valueOf(x); }
    public static final String coerceToString(int x)        { return String.valueOf(x); }
    public static final String coerceToString(long x)       { return String.valueOf(x); }
    public static final String coerceToString(float x)      { return String.valueOf(x); }
    public static final String coerceToString(double x)     { return String.valueOf(x); }
    public static final String coerceToString(Object x)     { if (null == x) return ""; else return x.toString(); }

}
