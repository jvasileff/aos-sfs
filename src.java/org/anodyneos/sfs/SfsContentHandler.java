package org.anodyneos.sfs;

import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class SfsContentHandler implements ContentHandler {

    private static enum Event {
        NONE,
        CHARACTERS,
        END_DOCUMENT,
        END_ELEMENT,
        END_PREFIX_MAPPING,
        IGNORABLE_WHITESPACE,
        PROCESSING_INSTRUCTION,
        SKIPPED_ENTITY,
        START_DOCUMENT,
        START_ELEMENT,
        START_PREFIX_MAPPING,
        PUSH_PHANTOM_PREFIX_MAPPING,
        POP_PHANTOM_PREFIX_MAPPING
    }

    public static final Log log = LogFactory.getLog(SfsContentHandler.class);

    private static final String NULL_STRING = "null";

    /* support for SAX feature "http://xml.org/sax/features/namespace-prefixes" */
    private boolean namespacePrefixes = false;

    private NamespaceMappings namespaceMappings = new NamespaceMappings();

    private Event lastEvent = Event.NONE;

    // values for the next element, set by startElement().  When flush() is called, these values
    // are passed to the wrapped ContentHandler's startElemement() methods and then set to null.
    private String bufferedElLocalName;
    private String bufferedElQName;
    private String bufferedElNamespaceURI;
    private AttributesImpl bufferedElAttributes = new AttributesImpl();

    private ContentHandler wrappedContentHandler;

    public SfsContentHandler(ContentHandler wrappedHandler) {
        this.wrappedContentHandler = wrappedHandler;
    }

    public SfsContentHandler(ContentHandler wrappedHandler, boolean namespacePrefixes) {
        this(wrappedHandler);
        this.namespacePrefixes = namespacePrefixes;
    }

    ////////////////////////////////////////////////////////////////////////////////
    //
    // phantom prefix push/pop
    //
    ////////////////////////////////////////////////////////////////////////////////

    public void pushPhantomPrefixMapping(String prefix, String uri) throws SAXException {
        if(log.isTraceEnabled()) {
            log.trace("pushPhantomPrefixMapping(\"" + prefix + "\", \"" + uri + "\") called");
        }
        flush(Event.PUSH_PHANTOM_PREFIX_MAPPING);
        namespaceMappings.pushPhantomPrefix(prefix, uri);
    }

    public void popPhantomPrefixMapping() throws SAXException {
        log.trace("popPhantomPrefixMapping() called");
        flush(Event.POP_PHANTOM_PREFIX_MAPPING);
        namespaceMappings.popPhantomPrefix();
    }


    ////////////////////////////////////////////////////////////////////////////////
    //
    // SAX Methods (managed)
    //
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * startPrefixMapping() must be followed only by zero or more startPrefixMapping
     * calls followed by a startElement() call
     */
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if(log.isTraceEnabled()) {
            log.trace("startPrefixMapping(\"" + prefix + "\", \"" + uri + "\") called");
        }
        flush(Event.START_PREFIX_MAPPING);

        namespaceMappings.declarePrefix(prefix, uri);
    }

    public void startElement( String namespaceURI, String localName, String qName, Attributes atts)
    throws SAXException {

        // ** WARNING ** changes made here should also be made in the other startElement() method.

        if(log.isTraceEnabled()) {
            log.trace("startElement("
                    + namespaceURI
                    + ", " + localName
                    + ", " + qName
                    + ", " + atts
                    + ") called");
        }
        flush(Event.START_ELEMENT);

        // buffer this element to allow attributes to be added
        bufferedElNamespaceURI = namespaceURI;
        bufferedElLocalName = localName;
        bufferedElQName = qName;
        bufferedElAttributes.clear();
        if (atts != null) {
            bufferedElAttributes.setAttributes(atts);
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        if(log.isTraceEnabled()) {
            log.trace("endElement("
                    + namespaceURI
                    + ", " + localName
                    + ", " + qName
                    + ") called");
        }
        flush(Event.END_ELEMENT);

        // call endElement on the wrappedContentHandler
        if(log.isTraceEnabled()) {
            log.trace("   wrappedContentHandler.endElement(" + namespaceURI + ", " + localName + ", " + qName + ")");
        }
        wrappedContentHandler.endElement(namespaceURI, localName, qName);

        // endPrefixMapping calls for wrappedContentHandler
        Enumeration prefixes = namespaceMappings.getDeclaredPrefixes(false);

        while (prefixes.hasMoreElements()) {
            String prefix = (String) prefixes.nextElement();
            if(log.isTraceEnabled()) {
                log.trace("   wrappedContentHandler.endPrefixMapping('" + prefix + "')");
            }
            wrappedContentHandler.endPrefixMapping(prefix);
        }

        namespaceMappings.popContext();
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        if(log.isTraceEnabled()) {
            log.trace("endPrefixMapping(\"" + prefix + "\") called");
        }
        flush(Event.END_PREFIX_MAPPING);
        // noop: we handle endPrefixMapping calls to the wrapped contentHandler automatically
    }

    ////////////////////////////////////////////////////////////////////////////////
    //
    // Convenience Methods (managed)
    //
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * This method tries to be forgiving within reason; the following rules apply:
     *
     * 1. When uri == null && qName has no prefix: The qName is used as is with
     * no namespace URI.
     *
     * 2. When uri == null && qName has a prefix: The prefix must be in scope.
     * The namespace URI in the output will be that of the namespace associated
     * with the prefix. If the prefix is not in scope, a SAXException is thrown.
     *
     * 3. When uri == "": If a prefix exists, an exception is thrown.  Otherwise,
     * the attribute will have no namespace and be output without a prefix.
     *
     * 4. When uri == someURI: A prefix will be given to the attribute in the
     * following priority:
     *
     * 4.A) the provided prefix if one was provided and it is currently mapped to
     * the uri.
     *
     * 4.B) any prefix that is currently mapped to the URI.
     *
     * 4.C) the provided prefix if it is not currently mapped to some other URI.
     *
     * 4.D) a generated prefix. In the case of C or D, a new namespace mapping
     * will be created.
     */
    public void addAttribute(final String uri, final String qName, final String value) throws SAXException {

        // this method currently must be in this class as it violates the contract that startPrefixMapping() must
        // not occur after the startElement() it applies to.  This contract cannot be changed as it would break
        // SAX compatibility.  In addition, this method needs direct access to the internal bufferedAttributes
        // structure.

        if(log.isTraceEnabled()) {
            log.trace("addAttribute(" + uri + ", " + qName + ", value) called");
        }

        if (null == bufferedElLocalName) {
            throw new SAXException("Cannot addAttribute() unless immediately after startElement().");
        } else if (qName.equals("xmlns") || qName.startsWith("xmlns:")) {
            // do nothing.  flush() automatically handles namespace prefixes.  We already know about the namespace
            // from startPrefixMapping().
            return;
        } else {
            // lets put this code here, not in flush().  This way the internal state is kept current and we get
            // immediate feedback on errors.

            final String myQName;
            final String myURI;
            final String prefix = parsePrefix(qName);
            final String localName = parseLocalName(qName);

            if (localName.length() == 0) {
                throw new SAXException("Could not determine localName for attribute: '" + qName + "'.");
            }

            if (null == uri) {
                if (prefix.length() == 0) {
                    myQName = localName;
                    myURI = "";
                } else {
                    // a prefix was provided
                    // the prefix must be in scope to determine URI
                    // the prefix may be a phantom
                    final String u = namespaceMappings.getURI(prefix, true);
                    if (null == u) {
                        throw new SAXException("Cannot find URI for '" + qName + "' and none was provided.");
                    } else {
                        final String existingURI = namespaceMappings.getURI(prefix, false);
                        if (null == existingURI) {
                            myQName = prefix + ":" + localName;
                            myURI = u;
                            // declare the phantom prefix
                            namespaceMappings.declarePrefix(prefix, myURI);
                        } else if (existingURI.equals(u)) {
                            // no need to make new declaration
                            myQName = prefix + ":" + localName;
                            myURI = u;
                        } else {
                            // existingURI does not match calculated URI for provided prefix
                            final String genP = genPrefix();
                            myQName = genP + ":" + localName;
                            myURI = u;
                            // declare the generated prefix
                            namespaceMappings.declarePrefix(genP, myURI);
                        }
                    }
                }
            } else if (uri.length() == 0) {
                if (prefix.length() != 0) {
                    throw new SAXException("Prefix not allowed for '" + qName + "' when namespace URI = ''.");
                }
                // else, use "" URI and no prefix
                myQName = localName;
                myURI = "";
            } else {
                // we have a URI, lets find a good prefix
                // don't search phantoms yet
                if (prefix.length() != 0 && uri.equals(namespaceMappings.getURI(prefix, false))) {
                    // Case A: prefix was provided and URI matches
                    myQName = qName;
                    myURI = uri;
                } else {
                    // don't search phantoms yet
                    final String existingPrefix = namespaceMappings.getPrefix(uri, false);
                    if (null != existingPrefix) {
                        // Case B: we already have a perfectly good prefix
                        myQName = existingPrefix + ":" + localName;
                        myURI = uri;
                    // don't search phantoms yet
                    } else if (prefix.length() != 0 && (null == namespaceMappings.getURI(prefix, false))) {
                        // Case C: the provided prefix will do; create new namespace mapping
                        if (log.isDebugEnabled()) {
                            log.debug("   addAttribute calls declarePrefix('" + prefix + "', '" + uri + "')");
                        }
                        namespaceMappings.declarePrefix(prefix, uri);
                        myQName = qName;
                        myURI = uri;
                    } else {
                        // Case D1: try a phantom prefix
                        // Case D2: or, generate a new prefix
                        String p = namespaceMappings.getPrefix(uri, true);
                        // p may be a phantom-prefix; if it conflicts with a non-phantom prefix, we can't use it
                        if (null == p || null != namespaceMappings.getURI(p, false)) {
                            p = genPrefix();
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("   addAttribute calls declarePrefix('" + p + "', '" + uri + "')");
                        }
                        namespaceMappings.declarePrefix(p, uri);
                        myQName = p + ":" + localName;
                        myURI = uri;
                    }
                }
            }
            bufferedElAttributes.addAttribute(myURI, localName, myQName, "CDATA", value);
        }
    }

    /**
     * This method tries to be forgiving, the following rules apply:
     *
     * 1. When uri == null && qName has a prefix: The prefix must be in scope.
     * The namespace URI in the output will be that of the namespace associated
     * with the prefix. If the prefix is not in scope, a SAXException is thrown.
     *
     * 2. When uri == null && qName has no prefix: The qName is used as is with
     * the current default namespace URI.
     *
     * 3. When uri == "": The element will have no namespace and the default xmlns
     * will be set to "".
     *
     * 4. When uri == someURI: A prefix will be given to the attribute in the
     * following priority:
     *
     * 4.a) If the uri is the current default namespace, no prefix will be used.
     *
     * 4.b) the provided prefix if one was provided and it is currently mapped to
     * the uri.
     *
     * 4.c) a prefix that is currently mapped to the URI.
     *
     * 4.d) the provided prefix if it is not currently mapped to another URI.  A new
     * mapping will be created.
     *
     * 4.e) a generated prefix. In the case of D or E, a new namespace mapping
     * will be created.
     */
    public void startElement(String uri, String qName) throws SAXException {
        // we don't have to call flush() as long as we let other methods do the SAX specific work.

        if(log.isTraceEnabled()) {
            log.trace("startElement(" + uri + ", " + qName + ") called");
        }
        String[] elData;
        elData = resolveElementPrefix(uri, qName);

        String myURI = elData[0];
        String localName = elData[1];
        String myQName = elData[2];
        String prefix = parsePrefix(myQName);

        // this will flush() and take care of the mapping
        startPrefixMapping(prefix, myURI);

        // this takes care of buffering the element.
        startElement(myURI, localName, myQName, null);
    }

    /**
     * This method corresponds to startElement(uri, qName);
     *
     * @param uri
     * @param qName
     */
    public void endElement(String uri, String qName) throws SAXException {
        // TODO: make sure qName is the same as what was used for startElement... currently this is buggy.
        // possible fixes include writing a ns mapper like NamespaceHelper that can make a guarantee on
        // getPrefix(uri), perhaps using a TreeMap to store namespace -> uri.  Otherwise, we'll simply have to
        // maintain a stack of qNames for start/end element.

        String[] elData;
        elData = resolveElementPrefix(uri, qName);

        String myURI = elData[0];
        String localName = elData[1];
        String myQName = elData[2];

        endElement(myURI, localName, myQName);
    }

    // this method may return undeclared or phantom prefixes
    private String[] resolveElementPrefix(final String uri, final String qName) throws SAXException {
        final String myQName;
        final String myURI;
        final String localName = parseLocalName(qName);
        final String prefix = parsePrefix(qName);

        if (localName.length() == 0) {
            throw new SAXException("Could not determine localName for element: '" + qName + "'.");
        }

        if (null == uri) {
            if (prefix.length() == 0) {
                myQName = localName;
                String u = namespaceMappings.getURI("", true);
                if (null == u) {
                    u = "";
                }
                myURI = u;
            } else {
                // FIXME: this is ok for startElement, but are there any issues for endElement?  Really endElement
                // needs to be improved anyway to make sure the same prefix is used on both ends.
                myURI = namespaceMappings.getURI(prefix, true);
                if (null == myURI) {
                    throw new SAXException("Cannot find URI for '" + qName + "' and none was provided.");
                }
                myQName = qName;
            }
        } else if (uri.length() == 0) {
            // FIXME should probably throw exception if prefix is set, but URI is "".

            // FIXME resetting the default namespace will cause problems unless we restore the old default, and even
            //    then need to think through "when to restore the default namespace" to avoid problems with code
            //    such as extension functions that may rely on the default namespace.  More generally, we may need to
            //    avoid masking namespaces (default or otherwise) programatically as this will through off function
            //    namespace mappings.  Perhaps this was the reason for code to check namespace compatibility...
            //    allowing arbitrary code to run (even XP code through a function) may change runtime namespaces
            //    while calling code expects namespaces to match xp document namespace declarations.  Perhaps this is
            //    less of an issue or there are other workarounds.  Need to work through xp and sfs use cases.

            // use "" URI and no prefix
            myQName = localName;
            myURI = "";
        } else {
            // we have a URI, lets find a good prefix
            if (uri.equals(namespaceMappings.getURI("", true))) {
                // Case A: the uri is currently the default namespace; don't use a prefix
                myQName = localName;
                myURI = uri;
            } else if (prefix.length() != 0 && uri.equals(namespaceMappings.getURI(prefix, true))) {
                // Case B: prefix was provided and uri matches
                myQName = qName;
                myURI = uri;
            } else {
                String p = namespaceMappings.getPrefix(uri, true);
                if (null != p) {
                    // Case C: we already have a perfectly good prefix
                    myQName = p + ":" + localName;
                    myURI = uri;
                } else if (prefix.length() != 0 && (null == namespaceMappings.getURI(prefix, true))) {
                    // Case D: the provided prefix will do; create new namespace mapping
                    myQName = qName;
                    myURI = uri;
                } else {
                    // Case E: punt... generate a new prefix for the attribute
                    p = genPrefix();
                    myQName = p + ":" + localName;
                    myURI = uri;
                }
            }
        }

        return new String[] {myURI, localName, myQName};
    }


    private static final String parsePrefix(String qName) {
        if (null == qName || qName.length() == 0) {
            return "";
        } else {
            int colon = qName.indexOf(':');
            if (-1 == colon) {
                return "";
            } else {
                return qName.substring(0, colon);
            }
        }
    }

    private static final String parseLocalName(String qName) {
        if (null == qName || qName.length() == 0) {
            return "";
        } else {
            int colon = qName.indexOf(':');
            if (-1 == colon) {
                return qName;
            } else {
                return qName.substring(colon + 1);
            }
        }
    }

    private int prefixNum = 100;
    private String genPrefix() {
        String prefix;
        do {
            // comment this out. Better to be repeatable.
            //prefix = "n" + Integer.toString((int) (Math.random() *
            // Integer.MAX_VALUE), 36);
            prefix = "n" + prefixNum++;
        // we may as well consider phantoms here in order to keep them around (not mask them) in case someone cares
        } while (null != namespaceMappings.getURI(prefix, true));

        return prefix;
    }


    ////////////////////////////////////////////////////////////////////////////////
    //
    // SAX Methods (simple pass through)
    //
    ////////////////////////////////////////////////////////////////////////////////

    public void characters(char[] ch, int start, int length) throws SAXException {
        flush(Event.CHARACTERS);
        if (ch != null) {
            wrappedContentHandler.characters(ch, start, length);
        }
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        flush(Event.IGNORABLE_WHITESPACE);
        wrappedContentHandler.ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        flush(Event.PROCESSING_INSTRUCTION);
        wrappedContentHandler.processingInstruction(target, data);
    }

    public void skippedEntity(String name) throws SAXException {
        flush(Event.SKIPPED_ENTITY);
        wrappedContentHandler.skippedEntity(name);
    }

    public void setDocumentLocator(Locator locator) {
        wrappedContentHandler.setDocumentLocator(locator);
    }

    public void endDocument() throws SAXException {
        flush(Event.END_DOCUMENT);
        // should calls to this method be ignored?
    }

    public void startDocument() throws SAXException {
        flush(Event.START_DOCUMENT);
        // should calls to this method be ignored?
    }

    ////////////////////////////////////////////////////////////////////////////////
    //
    // Sfs specific getters/setters
    //
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * This method should be used carefully; output should not be made directly
     * to the wrapped <code>ContentHandler</code>.
     *
     * @return the wrapped <code>ContentHandler</code>
     */
    public ContentHandler getWrappedContentHandler() {
        return wrappedContentHandler;
    }

    public boolean isNamespacePrefixes() {
        return namespacePrefixes;
    }

    public void setNamespacePrefixes(boolean namespacePrefixes) {
        this.namespacePrefixes = namespacePrefixes;
    }

    ////////////////////////////////////////////////////////////////////////////////
    //
    // characters(xxx) convenience methods
    //
    ////////////////////////////////////////////////////////////////////////////////

    public void characters(String s) throws SAXException {
        if (null != s) {
            characters(s.toCharArray(), 0, s.length());
        } else {
            characters(NULL_STRING.toCharArray(), 0, NULL_STRING.length());
        }
    }

    public void characters(Object x) throws SAXException {
        if (null != x) {
            characters(x.toString());
        } else {
            characters((String) null);
        }
    }

    public void characters(char x) throws SAXException {
        characters(String.valueOf(x));
    }

    public void characters(byte x) throws SAXException {
        characters(String.valueOf(x));
    }

    public void characters(boolean x) throws SAXException {
        characters(String.valueOf(x));
    }

    public void characters(int x) throws SAXException {
        characters(String.valueOf(x));
    }

    public void characters(long x) throws SAXException {
        characters(String.valueOf(x));
    }

    public void characters(float x) throws SAXException {
        characters(String.valueOf(x));
    }

    public void characters(double x) throws SAXException {
        characters(String.valueOf(x));
    }


    ////////////////////////////////////////////////////////////////////////////////
    //
    // private utility methods
    //
    ////////////////////////////////////////////////////////////////////////////////

    private void flush(final Event event) throws SAXException {
        // NOTE: to handle http://xml.org/sax/features/namespace-prefixes, we will first
        // remove all "xmlns" and xmlns:xxx" attributes, then add required attributes from
        // namespaceSupport if the feature is set to "true"

        // This method is called at the start of _every_ event except addAttribute.
        // There are two main concerns:
        //
        // 1. if bufferedElLocalName != null we need to declare prefixes and output
        // the element with accumulated attributes.
        //
        // 2. if another element has come or is about to come: We need to pushContext()
        // if we haven't yet, but wait until we process bufferedEl if it exits.

        try {

            if (Event.START_PREFIX_MAPPING == lastEvent &&
                    Event.START_PREFIX_MAPPING != event && Event.START_ELEMENT != event) {
                throw new IllegalStateException("Only startPrefixMapping() or startElement()" +
                        " SAX events may follow startPrefixMapping()");
            }

            if (Event.PUSH_PHANTOM_PREFIX_MAPPING == event || Event.POP_PHANTOM_PREFIX_MAPPING == event) {
                // do nothing; these can be called at any time except between startPrefixMapping and startElement
                return;
            }

            // Note: flush() is called by startElement PRIOR to setting bufferedElLocalName.  So, this test is for a
            // a bufferedElLocalName set by a previous startElement call.

            if (null != bufferedElLocalName) {
                if(log.isTraceEnabled()) {
                    log.trace("   outputing bufferd element");
                }
                for (int i = 0; i < bufferedElAttributes.getLength(); i++) {
                    String qName = bufferedElAttributes.getQName(i);
                    if (qName.equals("xmlns") || qName.startsWith("xmlns:")) {
                        bufferedElAttributes.removeAttribute(i);
                    }
                }

                // start prefix mappings

                // namespaceMappings.push() was already called by the startElement()
                // that saved the bufferedElLocalName we are about to output

                // we don't want to output new phantoms; since push() was already called, phantoms
                // that are relevent to bufferedEl have already been "promoted" to declared status

                Enumeration e = namespaceMappings.getDeclaredPrefixes(false);
                while (e.hasMoreElements()) {
                    String prefix = (String) e.nextElement();
                    String uri = namespaceMappings.getURI(prefix, false);
                    if (null == uri) {
                        uri = "";
                    }
                    if(log.isTraceEnabled()) {
                        log.trace("      wrappedContentHandler.startPrefixMapping("
                                + "'"   + prefix + "'"
                                + ", '" + uri + "'"
                                + ")");
                    }
                    wrappedContentHandler.startPrefixMapping(prefix, uri);
                    if (namespacePrefixes) {
                        String qName;
                        if (prefix.length() == 0) {
                            qName = "xmlns";
                        } else {
                            qName = "xmlns:" + prefix;
                        }
                        if(log.isTraceEnabled()) {
                            log.trace("      adding namespace-prefix attribute " + qName + "= '" + uri + "')");
                        }
                        bufferedElAttributes.addAttribute("", "", qName, "CDATA", uri);
                    }
                }
                if(log.isTraceEnabled()) {
                    log.trace("      wrappedContentHandler.startElement("
                            + "'"   + bufferedElNamespaceURI + "'"
                            + ", '" + bufferedElLocalName + "'"
                            + ", '" + bufferedElQName + "'"
                            + ", bufferedElAttributes"
                            + ")");
                }

                wrappedContentHandler.startElement(bufferedElNamespaceURI, bufferedElLocalName,
                        bufferedElQName, bufferedElAttributes);
                bufferedElNamespaceURI = null;
                bufferedElLocalName = null;
                bufferedElQName = null;
                bufferedElAttributes.clear();
            }

            if (Event.START_PREFIX_MAPPING != lastEvent) {
                switch (event) {
                    case START_PREFIX_MAPPING:
                        namespaceMappings.pushContext();
                        break;
                    case START_ELEMENT:
                        namespaceMappings.pushContext();
                        break;
                }
            }
        } finally {
            lastEvent = event;
        }
    }

}
