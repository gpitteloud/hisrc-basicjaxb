package org.jvnet.basicjaxb.plugin.copyable.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jvnet.higherjaxb.mojo.AbstractHigherjaxbParmMojo;
import org.jvnet.higherjaxb.mojo.test.RunHigherJaxbMojo;

public class RunCopyablePlugin extends RunHigherJaxbMojo
{
	@Override
	public File getSchemaDirectory()
	{
		return new File(getBaseDir(), "src/test/resources");
	}

	@Override
	protected void configureMojo(AbstractHigherjaxbParmMojo<?> mojo)
	{
		super.configureMojo(mojo);
		mojo.setForceRegenerate(true);
	}

	@Override
	public List<String> getArgs()
	{
		final List<String> args = new ArrayList<String>(super.getArgs());
		args.add("-Xcopyable");
		return args;
	}
}
