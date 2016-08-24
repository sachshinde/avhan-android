package org.linphone.xmlrpc.serializer;

import org.w3c.dom.Element;

import org.linphone.xmlrpc.XMLRPCException;
import org.linphone.xmlrpc.XMLUtil;
import org.linphone.xmlrpc.xmlcreator.XmlElement;

/**
 *
 * @author Tim Roes
 */
public class StringSerializer implements Serializer {

	public Object deserialize(Element content) throws XMLRPCException {
		return XMLUtil.getOnlyTextContent(content.getChildNodes());
	}

	public XmlElement serialize(Object object) {
		return XMLUtil.makeXmlTag(SerializerHandler.TYPE_STRING,
				object.toString());
	}

}