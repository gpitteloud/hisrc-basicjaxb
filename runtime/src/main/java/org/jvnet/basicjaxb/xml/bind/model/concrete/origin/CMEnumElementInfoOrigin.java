package org.jvnet.basicjaxb.xml.bind.model.concrete.origin;

import org.jvnet.basicjaxb.lang.Validate;
import org.jvnet.basicjaxb.xml.bind.model.origin.MElementInfoOrigin;

import org.glassfish.jaxb.core.v2.model.core.EnumLeafInfo;

public class CMEnumElementInfoOrigin<T, C, ELI extends EnumLeafInfo<T, C>>
		implements MElementInfoOrigin, EnumLeafInfoOrigin<T, C, ELI> {

	private final ELI source;

	public CMEnumElementInfoOrigin(ELI source) {
		Validate.notNull(source);
		this.source = source;
	}

	@Override
	public ELI getSource() {
		return source;
	}

}
