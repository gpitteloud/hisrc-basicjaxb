package org.jvnet.basicjaxb.xml.bind.model.util;

import javax.xml.namespace.QName;

public interface QNameCollector {

	public void element(QName name);

	public void attribute(QName name);
}
