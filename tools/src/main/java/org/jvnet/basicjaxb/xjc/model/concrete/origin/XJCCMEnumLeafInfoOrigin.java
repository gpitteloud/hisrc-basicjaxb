package org.jvnet.basicjaxb.xjc.model.concrete.origin;

import org.hisrc.xml.xsom.SchemaComponentAware;
import org.jvnet.basicjaxb.xjc.generator.MEnumOutlineGenerator;
import org.jvnet.basicjaxb.xjc.generator.concrete.CMEnumOutlineGenerator;
import org.jvnet.basicjaxb.xjc.generator.concrete.EnumOutlineGeneratorFactory;
import org.jvnet.basicjaxb.xml.bind.model.concrete.origin.CMEnumLeafInfoOrigin;

import com.sun.tools.xjc.model.CEnumLeafInfo;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.xsom.XSComponent;

public class XJCCMEnumLeafInfoOrigin extends
		CMEnumLeafInfoOrigin<NType, NClass, CEnumLeafInfo> implements
		EnumOutlineGeneratorFactory, SchemaComponentAware {

	public XJCCMEnumLeafInfoOrigin(CEnumLeafInfo source) {
		super(source);
	}

	@Override
	public MEnumOutlineGenerator createGenerator(Outline outline) {
		return new CMEnumOutlineGenerator(outline, getSource());
	}
	
	@Override
	public XSComponent getSchemaComponent() {
		return getSource().getSchemaComponent();
	}
}
