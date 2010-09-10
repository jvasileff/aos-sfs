package org.anodyneos.sfs;

import java.util.EmptyStackException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.ArrayStack;
import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * Functions similarly to org.xml.sax.NamespaceSupport, except for the
 * following:
 *
 * 1. declarePrefix() first checks to see if the prefix is already mapped to the
 * URI (and not masked), and if so, does nothing.
 *
 * 2. Extra checking is performed to ensure the NamespaceSupport contract
 * disallowing declarePrefix() after pop() but before push() is followed.
 *
 * @author jvas
 */
public final class NamespaceMappings {

    private static final Log logger = LogFactory.getLog(NamespaceMappings.class);

    private NamespaceSupport namespaceSupport = new NamespaceSupport();

    private boolean declareOk = true;

    // this only holds items when it is being used. It holds null values
    // whenever possible. Entries are stacks that hold String[2] entries
    // for prefix to URI mappings.
    private ArrayStack phantomPrefixes = new ArrayStack();

    public void pushPhantomPrefix(String prefix, String uri) {
        // This may be called when phantomPrefixes is empty - if so, add an
        // entry for the current context. If not empty, the contents may be null.
        // If so, pop the null and push a new ArrayStack.

        ArrayStack prefixStack;
        if (phantomPrefixes.isEmpty()) {
            prefixStack = new ArrayStack();
            phantomPrefixes.push(prefixStack);
        } else {
            prefixStack = (ArrayStack) phantomPrefixes.peek();
            if (null == prefixStack) {
                prefixStack = new ArrayStack();
                phantomPrefixes.pop();
                phantomPrefixes.push(prefixStack);
            }
        }
        prefixStack.push(new String[] { prefix, uri });
    }

    public void popPhantomPrefix() throws EmptyStackException {
        // we will require no arguments and provide no checking - we trust the calling code.
        ArrayStack prefixStack = (ArrayStack) phantomPrefixes.peek();
        prefixStack.pop();
    }

    public boolean declarePrefix(String prefix, String uri) {
        // Note: calls to declarePrefix may over-write phantomPrefixes that were
        // added during push(). This should be OK; the phantomPrefix is being masked

        if (!declareOk) {
            throw new IllegalStateException("Cannot declarePrefix after pop() without first calling push()");
        } else if ("xml".equals(prefix) || "xmlns".equals(prefix)) {
            // "xml" and "xmlns" are not tracked by NamespaceSupport
            return false;
        } else if (uri.equals(namespaceSupport.getURI(prefix))) {
            // do nothing if prefix to URI is not already set.
            return false;
        } else {
            return namespaceSupport.declarePrefix(prefix, uri);
        }
    }

    private boolean phantomsExistForCurrentContext() {
        if (phantomPrefixes.isEmpty()) {
            return false;
        } else {
            ArrayStack stack = (ArrayStack) phantomPrefixes.peek();
            return (! (stack == null || stack.isEmpty()));
        }
    }

    // only prefixes declared in this context
    public Enumeration getDeclaredPrefixes(boolean includePhantom) {
        // Consider changing this class to return both prefixes and URIs since
        // that would be the normal use case.
        if (!includePhantom || !phantomsExistForCurrentContext()) {
            return namespaceSupport.getDeclaredPrefixes();
        } else {
            // if a prefix is declared multiple times in phantom or in both
            // phantom and namespaceSupport, that is ok, but just return it once.
            Set prefixes = new HashSet();
            ArrayStack prefixStack = (ArrayStack) phantomPrefixes.peek();
            for (int i = 0; i < prefixStack.size(); i++) {
                prefixes.add(((String[]) prefixStack.get(i))[0]);
            }
            Enumeration e = namespaceSupport.getDeclaredPrefixes();
            while (e.hasMoreElements()) {
                prefixes.add((String) e.nextElement());
            }
            return new IteratorEnumeration(prefixes.iterator());
        }
    }

    public String getPrefix(String uri, boolean includePhantom) {
        if (!includePhantom || !phantomsExistForCurrentContext()) {
            return namespaceSupport.getPrefix(uri);
        } else {
            // first search phantoms in LIFO order.
            ArrayStack prefixStack = (ArrayStack) phantomPrefixes.peek();
            for (int i = prefixStack.size() - 1; i >= 0; i--) {
                // IMPORTANT: Make sure not to return the "" prefix
                String[] entry = (String[]) prefixStack.get(i);
                if (entry[0].length() > 0 && entry[1].equals(uri)) {
                    return entry[0];
                }
            }
            // not a phantom, try namespaceSupport
            return namespaceSupport.getPrefix(uri);
        }
    }

    public Enumeration getPrefixes(boolean includePhantom) {
        if (!includePhantom || !phantomsExistForCurrentContext()) {
            return namespaceSupport.getPrefixes();
        } else {
            // if a prefix is declared multiple times in phantom or in both
            // phantom and namespaceSupport, that is ok, but just return it once.
            Set prefixes = new HashSet();
            ArrayStack prefixStack = (ArrayStack) phantomPrefixes.peek();
            for (int i = 0; i < prefixStack.size(); i++) {
                // IMPORTANT: Make sure not to return the "" prefix
                String prefix = ((String[]) prefixStack.get(i))[0];
                if (null != prefix && prefix.length() > 0) {
                    prefixes.add(prefix);
                }
            }
            Enumeration e = namespaceSupport.getPrefixes();
            while (e.hasMoreElements()) {
                prefixes.add((String) e.nextElement());
            }
            return new IteratorEnumeration(prefixes.iterator());
        }
    }

    public Enumeration getPrefixes(String uri, boolean includePhantom) {
        // FIXME need to suppress prefixes that are masked and no longer refer to the given URI.
        // how does namespaceSupport handle multiple mappings?  Need to suppress prefixes returned
        // by namespaceSupport that also exist in phantomMappings
        if (!includePhantom || !phantomsExistForCurrentContext()) {
            return namespaceSupport.getPrefixes(uri);
        } else {
            // if a prefix is declared multiple times in phantom or in both
            // phantom and namespaceSupport, that is ok, but just return it once.
            Set prefixes = new HashSet();
            ArrayStack prefixStack = (ArrayStack) phantomPrefixes.peek();
            for (int i = 0; i < prefixStack.size(); i++) {
                // IMPORTANT: Make sure not to return the "" prefix
                String prefix = ((String[]) prefixStack.get(i))[0];
                String[] entry = ((String[]) prefixStack.get(i));
                if (entry[0].length() > 0 && uri.equals(entry[1])) {
                    prefixes.add(entry[0]);
                }
            }
            Enumeration e = namespaceSupport.getPrefixes(uri);
            while (e.hasMoreElements()) {
                prefixes.add((String) e.nextElement());
            }
            return new IteratorEnumeration(prefixes.iterator());
        }
    }

    public String getURI(String prefix, boolean includePhantom) {
        if (!includePhantom || !phantomsExistForCurrentContext()) {
            return namespaceSupport.getURI(prefix);
        } else {
            // first search phantoms in LIFO order.
            ArrayStack prefixStack = (ArrayStack) phantomPrefixes.peek();
            for (int i = prefixStack.size() - 1; i >= 0; i--) {
                String[] entry = (String[]) prefixStack.get(i);
                if (entry[0].equals(prefix)) {
                    return entry[1];
                }
            }
            // not found yet, try namespaceSupport
            return namespaceSupport.getURI(prefix);
        }
    }

    public void popContext() {
        logger.debug("   NamespaceMappings popContext()");
        namespaceSupport.popContext();
        if (!phantomPrefixes.isEmpty()) {
            phantomPrefixes.pop();
        }
        declareOk = false;
    }

    public void pushContext() {
        logger.debug("   NamespaceMappings pushContext()");
        namespaceSupport.pushContext();
        declareOk = true;

        // preload this context with phantoms, then push an empty context to phantomPrefixes
        if (phantomsExistForCurrentContext()) {
            // add these in FIFO order so that the most recent ones will over-write older ones.
            ArrayStack prefixStack = (ArrayStack) phantomPrefixes.peek();
            for (int i = 0; i < prefixStack.size(); i++) {
                String[] entry = (String[]) prefixStack.get(i);
                if (null != entry) {
                    declarePrefix(entry[0], entry[1]);
                }
            }
        }
        if (!phantomPrefixes.isEmpty()) {
            phantomPrefixes.push(null);
        }
    }

}
