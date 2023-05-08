package org.jvnet.basicjaxb.xml.bind.model.concrete.origin;

import org.jvnet.basicjaxb.lang.Validate;
import org.jvnet.basicjaxb.xml.bind.model.origin.MModelInfoOrigin;

import org.glassfish.jaxb.core.v2.model.core.TypeInfoSet;

public class CMModelInfoOrigin<T, C, TIS extends TypeInfoSet<T, C, ?, ?>>
		implements MModelInfoOrigin, TypeInfoSetOrigin<T, C, TIS> {

	private final TIS source;

	public CMModelInfoOrigin(TIS source) {
		Validate.notNull(source);
		this.source = source;
	}

	@Override
	public TIS getSource() {
		return source;
	}

}
