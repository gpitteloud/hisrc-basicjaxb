package org.jvnet.basicjaxb.xml.bind.model.concrete.origin;

import org.jvnet.basicjaxb.lang.Validate;
import org.jvnet.basicjaxb.xml.bind.model.origin.MElementOrigin;

import org.glassfish.jaxb.core.v2.model.core.Element;

public class CMElementOrigin<T, C, E extends Element<T, C>>
		implements MElementOrigin, ElementOrigin<T, C, E> {

	private final E source;

	public CMElementOrigin(E source) {
		Validate.notNull(source);
		this.source = source;
	}

	@Override
	public E getSource() {
		return source;
	}

}
