package org.jvnet.basicjaxb.xml.bind.model.concrete.origin;

import org.jvnet.basicjaxb.lang.Validate;
import org.jvnet.basicjaxb.xml.bind.model.origin.MWildcardTypeInfoOrigin;

import org.glassfish.jaxb.core.v2.model.core.WildcardTypeInfo;

public class CMWildcardTypeInfoOrigin<T, C, WTI extends WildcardTypeInfo<T, C>>
		implements MWildcardTypeInfoOrigin, WildcardTypeInfoOrigin<T, C, WTI> {

	private final WTI source;

	public CMWildcardTypeInfoOrigin(WTI source) {
		Validate.notNull(source);
		this.source = source;
	}

	@Override
	public WTI getSource() {
		return source;
	}

}
