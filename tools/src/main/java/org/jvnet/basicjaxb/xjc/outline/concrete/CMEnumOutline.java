package org.jvnet.basicjaxb.xjc.outline.concrete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.jvnet.basicjaxb.xjc.outline.MEnumConstantOutline;
import org.jvnet.basicjaxb.xjc.outline.MEnumOutline;
import org.jvnet.basicjaxb.xjc.outline.MModelOutline;
import org.jvnet.basicjaxb.xjc.outline.MPackageOutline;
import org.jvnet.basicjaxb.xml.bind.model.MEnumLeafInfo;

import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;

public class CMEnumOutline implements MEnumOutline {

	private final MModelOutline parent;
	private final MPackageOutline packageOutline;
	private final MEnumLeafInfo<NType, NClass> target;
	private final JDefinedClass code;

	private final List<MEnumConstantOutline> enumConstantOutlines = new ArrayList<MEnumConstantOutline>();
	private final List<MEnumConstantOutline> _enumConstantOutlines = Collections
			.unmodifiableList(enumConstantOutlines);

	public CMEnumOutline(MModelOutline parent, MPackageOutline packageOutline,
			MEnumLeafInfo<NType, NClass> target, JDefinedClass code) {
		Validate.notNull(parent);
		Validate.notNull(packageOutline);
		Validate.notNull(target);
		Validate.notNull(code);
		this.parent = parent;
		this.packageOutline = packageOutline;
		this.target = target;
		this.code = code;
	}

	@Override
	public MModelOutline getParent() {
		return parent;
	}

	@Override
	public MPackageOutline getPackageOutline() {
		return packageOutline;
	}

	@Override
	public MEnumLeafInfo<NType, NClass> getTarget() {
		return target;
	}

	@Override
	public JDefinedClass getCode() {
		return code;
	}

	@Override
	public List<MEnumConstantOutline> getEnumConstantOutlines() {
		return _enumConstantOutlines;
	}

	public void addEnumConstantOutline(MEnumConstantOutline enumConstantOutline) {
		Validate.notNull(enumConstantOutline);
		Validate.isTrue(enumConstantOutline.getEnumOutline() == this);
		this.enumConstantOutlines.add(enumConstantOutline);
	}

}
