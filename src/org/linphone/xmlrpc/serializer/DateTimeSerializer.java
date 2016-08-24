package org.linphone.xmlrpc.serializer;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.w3c.dom.Element;

import org.linphone.xmlrpc.XMLRPCException;
import org.linphone.xmlrpc.XMLUtil;
import org.linphone.xmlrpc.xmlcreator.XmlElement;

/**
 *
 * @author timroes
 */
public class DateTimeSerializer implements Serializer {

	private static final String DATETIME_FORMAT = "yyyyMMdd'T'HH:mm:ss";
	private static final SimpleDateFormat DATE_FORMATER = new SimpleDateFormat(DATETIME_FORMAT);

	public Object deserialize(Element content) throws XMLRPCException {
		try {
			return DATE_FORMATER.parse(XMLUtil.getOnlyTextContent(content.getChildNodes()));
		} catch (ParseException ex) {
			throw new XMLRPCException("Unable to parse given date.", ex);
		}
	}

	public XmlElement serialize(Object object) {
		return XMLUtil.makeXmlTag(SerializerHandler.TYPE_DATETIME,
				DATE_FORMATER.format(object));
	}

}
