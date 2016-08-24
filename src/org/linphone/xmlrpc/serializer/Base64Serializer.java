package org.linphone.xmlrpc.serializer;

import org.w3c.dom.Element;

import org.linphone.xmlrpc.XMLRPCException;
import org.linphone.xmlrpc.XMLUtil;
import org.linphone.xmlrpc.xmlcreator.XmlElement;
import org.linphone.xmlrpc.Base64;

/**
 *
 * @author Tim Roes
 */
public class Base64Serializer implements Serializer {

	public Object deserialize(Element content) throws XMLRPCException {
		return Base64.decode(XMLUtil.getOnlyTextContent(content.getChildNodes()));
	}

	public XmlElement serialize(Object object) {
		return XMLUtil.makeXmlTag(SerializerHandler.TYPE_BASE64,
				Base64.encode((Byte[])object));
	}

}