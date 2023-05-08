package org.jvnet.basicjaxb.xjc.model.concrete.origin;

import org.hisrc.xml.xsom.SchemaComponentAware;
import org.jvnet.basicjaxb.xjc.generator.MElementOutlineGenerator;
import org.jvnet.basicjaxb.xjc.generator.concrete.CMElementOutlineGenerator;
import org.jvnet.basicjaxb.xjc.generator.concrete.ElementOutlineGeneratorFactory;
import org.jvnet.basicjaxb.xml.bind.model.concrete.origin.CMElementInfoOrigin;

import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.xsom.XSComponent;

public class XJCCMElementInfoOrigin extends
		CMElementInfoOrigin<NType, NClass, CElementInfo> implements
		ElementOutlineGeneratorFactory, SchemaComponentAware {

	private final XSComponent component;

	public XJCCMElementInfoOrigin(CElementInfo source) {
		super(source);
		component = source.getSchemaComponent() != null ? source
				.getSchemaComponent() : source.getProperty()
				.getSchemaComponent();
	}

	@Override
	public MElementOutlineGenerator createGenerator(Outline outline) {
		return new CMElementOutlineGenerator(outline, getSource());
	}

	@Override
	public XSComponent getSchemaComponent() {
		return component;
	}

}
