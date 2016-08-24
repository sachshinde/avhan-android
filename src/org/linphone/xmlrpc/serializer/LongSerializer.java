package org.linphone.xmlrpc.serializer;

import org.w3c.dom.Element;

import org.linphone.xmlrpc.XMLRPCException;
import org.linphone.xmlrpc.XMLUtil;
import org.linphone.xmlrpc.xmlcreator.XmlElement;

/**
 *
 * @author Tim Roes
 */
class LongSerializer implements Serializer {

	public Object deserialize(Element content) throws XMLRPCException {
		return Long.parseLong(XMLUtil.getOnlyTextContent(content.getChildNodes()));
	}

	public XmlElement serialize(Object object) {
		return XMLUtil.makeXmlTag(SerializerHandler.TYPE_LONG,
				((Long)object).toString());
	}

}
