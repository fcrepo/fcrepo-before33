
package fedora.server.journal.xmlhelpers;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;

import fedora.server.storage.types.DSBinding;
import fedora.server.storage.types.DSBindingMap;

/**
 * <p>
 * <b>Title:</b> BindingMapXmlWriter.java
 * </p>
 * <p>
 * <b>Description:</b> Write an entire <code>DSBindingMap</code> instance to
 * the Journal file.
 * </p>
 * 
 * @author jblake@cs.cornell.edu
 * @version $Id: BindingMapXmlWriter.java 5025 2006-09-01 22:08:17 +0000 (Fri,
 *          01 Sep 2006) cwilper $
 */

public class BindingMapXmlWriter
        extends AbstractXmlWriter {

    public void writeBindingMap(String key,
                                DSBindingMap bindingMap,
                                XMLEventWriter writer)
            throws XMLStreamException {
        putStartTag(writer, QNAME_TAG_ARGUMENT);
        putAttribute(writer, QNAME_ATTR_NAME, key);
        putAttribute(writer, QNAME_ATTR_TYPE, ARGUMENT_TYPE_BINDING_MAP);
        writeMap(bindingMap, writer);
        putEndTag(writer, QNAME_TAG_ARGUMENT);
    }

    private void writeMap(DSBindingMap bindingMap, XMLEventWriter writer)
            throws XMLStreamException {
        putStartTag(writer, QNAME_TAG_DS_BINDING_MAP);
        putAttributeIfNotNull(writer,
                              QNAME_ATTR_DS_BIND_MAP_ID,
                              bindingMap.dsBindMapID);
        putAttributeIfNotNull(writer,
                              QNAME_ATTR_DS_BIND_MECHANISM_PID,
                              bindingMap.dsBindMechanismPID);
        putAttributeIfNotNull(writer,
                              QNAME_ATTR_DS_BIND_MAP_LABEL,
                              bindingMap.dsBindMapLabel);
        putAttributeIfNotNull(writer, QNAME_ATTR_STATE, bindingMap.state);

        if (bindingMap.dsBindings != null) {
            for (DSBinding element : bindingMap.dsBindings) {
                writingBinding(element, writer);
            }
        }
        putEndTag(writer, QNAME_TAG_DS_BINDING_MAP);
    }

    private void writingBinding(DSBinding binding, XMLEventWriter writer)
            throws XMLStreamException {
        putStartTag(writer, QNAME_TAG_DS_BINDING);
        putAttributeIfNotNull(writer,
                              QNAME_ATTR_BIND_KEY_NAME,
                              binding.bindKeyName);
        putAttributeIfNotNull(writer, QNAME_ATTR_BIND_LABEL, binding.bindLabel);
        putAttributeIfNotNull(writer,
                              QNAME_ATTR_DATASTREAM_ID,
                              binding.datastreamID);
        putAttributeIfNotNull(writer, QNAME_ATTR_SEQ_NO, binding.seqNo);
        putEndTag(writer, QNAME_TAG_DS_BINDING);
    }

}