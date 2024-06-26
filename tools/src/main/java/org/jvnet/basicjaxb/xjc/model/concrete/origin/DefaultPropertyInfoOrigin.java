package org.jvnet.basicjaxb.xjc.model.concrete.origin;

import static java.util.Objects.requireNonNull;

import org.jvnet.basicjaxb.xjc.generator.MPropertyOutlineGenerator;
import org.jvnet.basicjaxb.xjc.generator.concrete.PropertyOutlineGeneratorFactory;
import org.jvnet.basicjaxb.xml.bind.model.origin.MPropertyInfoOrigin;

import com.sun.tools.xjc.outline.Outline;

public class DefaultPropertyInfoOrigin implements MPropertyInfoOrigin,
		PropertyOutlineGeneratorFactory {

	private final MPropertyOutlineGenerator generator;

	public DefaultPropertyInfoOrigin(MPropertyOutlineGenerator generator) {
		requireNonNull(generator);
		this.generator = generator;
	}

	@Override
	public MPropertyOutlineGenerator createGenerator(Outline outline) {
		return generator;
	}
}
