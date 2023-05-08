package org.jvnet.basicjaxb.xjc.outline.concrete;

import org.apache.commons.lang3.Validate;
import org.jvnet.basicjaxb.xjc.outline.MEnumConstantOutline;
import org.jvnet.basicjaxb.xjc.outline.MEnumOutline;
import org.jvnet.basicjaxb.xml.bind.model.MEnumConstantInfo;

import com.sun.codemodel.JEnumConstant;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;

public class CMEnumConstantOutline implements MEnumConstantOutline {

	private final MEnumOutline enumOutline;

	private final MEnumConstantInfo<NType, NClass> target;

	private final JEnumConstant code;

	public CMEnumConstantOutline(MEnumOutline enumOutline,
			MEnumConstantInfo<NType, NClass> target, JEnumConstant code) {
		Validate.notNull(enumOutline);
		Validate.notNull(target);
		Validate.notNull(code);
		this.enumOutline = enumOutline;
		this.target = target;
		this.code = code;
	}

	@Override
	public MEnumOutline getEnumOutline() {
		return enumOutline;
	}

	@Override
	public MEnumConstantInfo<NType, NClass> getTarget() {
		return target;
	}

	@Override
	public JEnumConstant getCode() {
		return code;
	}

}
