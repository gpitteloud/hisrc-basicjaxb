package org.jvnet.basicjaxb.xjc.model.concrete.origin;

import org.hisrc.xml.xsom.SchemaComponentAware;
import org.jvnet.basicjaxb.xjc.generator.MClassOutlineGenerator;
import org.jvnet.basicjaxb.xjc.generator.concrete.CMClassOutlineGenerator;
import org.jvnet.basicjaxb.xjc.generator.concrete.ClassOutlineGeneratorFactory;
import org.jvnet.basicjaxb.xml.bind.model.concrete.origin.CMClassInfoOrigin;

import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.xsom.XSComponent;

public class XJCCMClassInfoOrigin extends
		CMClassInfoOrigin<NType, NClass, CClassInfo> implements
		ClassOutlineGeneratorFactory, SchemaComponentAware {

	public XJCCMClassInfoOrigin(CClassInfo source) {
		super(source);
	}

	@Override
	public MClassOutlineGenerator createGenerator(Outline outline) {
		return new CMClassOutlineGenerator(outline, getSource());
	}

	@Override
	public XSComponent getSchemaComponent() {
		return getSource().getSchemaComponent();
	}
	

}
