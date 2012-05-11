package net.bpelunit.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

public class QNameUtil {
	
	private static final Pattern p = Pattern.compile("^\\{[^\\}]*\\}(.+)");

	/**
	 * Checks on whether the string conforms to a String representation of a QName.
	 * A QName is denoted as {namespace}local-name
	 * 
	 * @param name String to be checked
	 * @return true if the string represents a QName, false if not
	 */
	public static boolean isQName(String name) {
		return (name != null) && p.matcher(name).matches();
	}

	/**
	 * Converts the given String to a QName
	 * 
	 * @param name String represention of a QName
	 * @return QName with namespace and local-name parts
	 * @see #isQName(String)
	 */
	public static QName parseUtil(String name) {
		// TODO Switch to Pattern and groups 
		int indexOfClosingBrace = name.indexOf('}');
		
		String namespace =  name.substring(1, indexOfClosingBrace);
		String localName = name.substring(indexOfClosingBrace + 1);
		return new QName(namespace, localName);
	}
}
