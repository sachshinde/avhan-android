package org.linphone.xmlrpc.serializer;

import java.text.DecimalFormat;

import org.w3c.dom.Element;

import org.linphone.xmlrpc.XMLRPCException;
import org.linphone.xmlrpc.XMLUtil;
import org.linphone.xmlrpc.xmlcreator.XmlElement;

/**
 *
 * @author Tim Roes
 */
public class DoubleSerializer implements Serializer {

	public Object deserialize(Element content) throws XMLRPCException {
		return Double.parseDouble(XMLUtil.getOnlyTextContent(content.getChildNodes()));
	}

	public XmlElement serialize(Object object) {
		return XMLUtil.makeXmlTag(SerializerHandler.TYPE_DOUBLE,
				new DecimalFormat("#0.0#").format(((Double)object).doubleValue()));
	}

}
