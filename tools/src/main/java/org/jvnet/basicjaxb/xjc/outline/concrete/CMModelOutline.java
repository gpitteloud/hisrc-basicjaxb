package org.jvnet.basicjaxb.xjc.outline.concrete;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.jvnet.basicjaxb.xjc.outline.MClassOutline;
import org.jvnet.basicjaxb.xjc.outline.MElementOutline;
import org.jvnet.basicjaxb.xjc.outline.MEnumOutline;
import org.jvnet.basicjaxb.xjc.outline.MModelOutline;
import org.jvnet.basicjaxb.xjc.outline.MPackageOutline;
import org.jvnet.basicjaxb.xml.bind.model.MClassInfo;
import org.jvnet.basicjaxb.xml.bind.model.MElementInfo;
import org.jvnet.basicjaxb.xml.bind.model.MEnumLeafInfo;
import org.jvnet.basicjaxb.xml.bind.model.MModelInfo;
import org.jvnet.basicjaxb.xml.bind.model.MPackageInfo;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;

public class CMModelOutline implements MModelOutline {

	private final MModelInfo<NType, NClass> target;
	private final JCodeModel code;

	private final List<MPackageOutline> packageOutlines = new ArrayList<MPackageOutline>();
	private final List<MPackageOutline> _packageOutlines = Collections
			.unmodifiableList(packageOutlines);
	private final Map<MPackageInfo, MPackageOutline> packageOutlinesMap = new IdentityHashMap<MPackageInfo, MPackageOutline>();
	private final List<MElementOutline> elementOutlines = new ArrayList<MElementOutline>();
	private final List<MElementOutline> _elementOutlines = Collections
			.unmodifiableList(elementOutlines);
	private final Map<MElementInfo<NType, NClass>, MElementOutline> elementOutlinesMap = new IdentityHashMap<MElementInfo<NType, NClass>, MElementOutline>();
	private final List<MClassOutline> classOutlines = new ArrayList<MClassOutline>();
	private final List<MClassOutline> _classOutlines = Collections
			.unmodifiableList(classOutlines);
	private final Map<MClassInfo<NType, NClass>, MClassOutline> classOutlinesMap = new IdentityHashMap<MClassInfo<NType, NClass>, MClassOutline>();
	private final List<MEnumOutline> enumOutlines = new ArrayList<MEnumOutline>();
	private final List<MEnumOutline> _enumOutlines = Collections
			.unmodifiableList(enumOutlines);
	private final Map<MEnumLeafInfo<NType, NClass>, MEnumOutline> enumOutlinesMap = new IdentityHashMap<MEnumLeafInfo<NType, NClass>, MEnumOutline>();

	public CMModelOutline(MModelInfo<NType, NClass> target, JCodeModel code) {
		Validate.notNull(target);
		Validate.notNull(code);
		this.target = target;
		this.code = code;
	}

	@Override
	public MModelInfo<NType, NClass> getTarget() {
		return target;
	}

	@Override
	public JCodeModel getCode() {
		return code;
	}

	@Override
	public Collection<MPackageOutline> getPackageOutlines() {
		return _packageOutlines;
	}

	@Override
	public MPackageOutline getPackageOutline(MPackageInfo target) {
		return packageOutlinesMap.get(target);
	}

	public void addPackageOutline(MPackageOutline packageOutline) {
		Validate.notNull(packageOutline);
		this.packageOutlines.add(packageOutline);
		this.packageOutlinesMap.put(packageOutline.getTarget(), packageOutline);
	}

	@Override
	public Collection<MClassOutline> getClassOutlines() {
		return _classOutlines;
	}

	@Override
	public MClassOutline getClassOutline(MClassInfo<NType, NClass> target) {
		return classOutlinesMap.get(target);
	}

	public void addClassOutline(MClassOutline classOutline) {
		Validate.notNull(classOutline);
		this.classOutlines.add(classOutline);
		this.classOutlinesMap.put(classOutline.getTarget(), classOutline);
	}

	@Override
	public Collection<MEnumOutline> getEnumOutlines() {
		return _enumOutlines;
	}

	@Override
	public MEnumOutline getEnumOutline(MEnumLeafInfo<NType, NClass> target) {
		return enumOutlinesMap.get(target);
	}

	public void addEnumOutline(MEnumOutline enumOutline) {
		Validate.notNull(enumOutline);
		this.enumOutlines.add(enumOutline);
		this.enumOutlinesMap.put(enumOutline.getTarget(), enumOutline);
	}

	@Override
	public Collection<MElementOutline> getElementOutlines() {
		return _elementOutlines;
	}

	@Override
	public MElementOutline getElementOutline(MElementInfo<NType, NClass> target) {
		return elementOutlinesMap.get(target);
	}

	public void addElementOutline(MElementOutline elementOutline) {
		Validate.notNull(elementOutline);
		this.elementOutlines.add(elementOutline);
		this.elementOutlinesMap.put(elementOutline.getTarget(), elementOutline);
	}

}
