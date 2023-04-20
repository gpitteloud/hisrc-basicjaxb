package org.jvnet.basicjaxb.plugin.codegenerator;

import org.apache.commons.lang3.Validate;

import com.sun.codemodel.JCodeModel;

public abstract class AbstractCodeGenerationImplementor<A extends Arguments<A>>
	implements CodeGenerationImplementor<A>
{
	private final JCodeModel codeModel;
	@Override
	public JCodeModel getCodeModel()
	{
		return codeModel;
	}

	public AbstractCodeGenerationImplementor(JCodeModel codeModel)
	{
		this.codeModel = Validate.notNull(codeModel);
	}
}
