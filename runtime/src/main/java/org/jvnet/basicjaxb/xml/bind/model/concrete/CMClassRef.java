package org.jvnet.basicjaxb.xml.bind.model.concrete;

import javax.xml.namespace.QName;

import org.jvnet.basicjaxb.lang.Validate;
import org.jvnet.basicjaxb.xml.bind.model.MClassRef;
import org.jvnet.basicjaxb.xml.bind.model.MClassTypeInfoVisitor;
import org.jvnet.basicjaxb.xml.bind.model.MContainer;
import org.jvnet.basicjaxb.xml.bind.model.MCustomizations;
import org.jvnet.basicjaxb.xml.bind.model.MPackageInfo;
import org.jvnet.basicjaxb.xml.bind.model.MTypeInfoVisitor;
import org.jvnet.basicjaxb.xml.bind.model.origin.MClassRefOrigin;
import org.jvnet.basicjaxb.xml.bind.model.util.XmlTypeUtils;

public class CMClassRef<T, C extends T> implements MClassRef<T, C>
{
	private CMCustomizations customizations = new CMCustomizations();
	private final MClassRefOrigin origin;
	private final C targetType;
	private final MPackageInfo _package;
	private final String name;
	private final String localName;
	private final MContainer container;
	private final QName typeName;

	public CMClassRef(MClassRefOrigin origin, C targetType, Class<?> targetClass, MPackageInfo _package,
		MContainer container, String localName)
	{
		super();
		Validate.notNull(origin);
		Validate.notNull(targetType);
		Validate.notNull(_package);
		Validate.notNull(localName);
		this.origin = origin;
		this.targetType = targetType;
		this.name = _package.getPackagedName(localName);
		this.localName = localName;
		this._package = _package;
		this.container = container;
		this.typeName = targetClass == null ? null : XmlTypeUtils.getTypeName(targetClass);
	}

	@Override
	public MClassRefOrigin getOrigin()
	{
		return this.origin;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getLocalName()
	{
		return localName;
	}

	@Override
	public C getTargetType()
	{
		return targetType;
	}

	@Override
	public QName getTypeName()
	{
		return typeName;
	}

	@Override
	public boolean isSimpleType()
	{
		return false;
	}

	@Override
	public <V> V acceptTypeInfoVisitor(MTypeInfoVisitor<T, C, V> visitor)
	{
		return visitor.visitClassRef(this);
	}

	@Override
	public MCustomizations getCustomizations()
	{
		return customizations;
	}

	@Override
	public MPackageInfo getPackageInfo()
	{
		return _package;
	}

	@Override
	public MContainer getContainer()
	{
		return container;
	}

	@Override
	public String getContainerLocalName(String delimiter)
	{
		final String localName = getLocalName();
		if (localName == null)
			return null;
		else
		{
			final MContainer container = getContainer();
			if (container == null)
				return localName;
			else
			{
				final String containerLocalName = container.getContainerLocalName(delimiter);
				return containerLocalName == null ? localName : containerLocalName + delimiter + localName;
			}
		}
	}

	public C getTargetClass()
	{
		return targetType;
	}

	@Override
	public <V> V acceptClassTypeInfoVisitor(MClassTypeInfoVisitor<T, C, V> visitor)
	{
		return visitor.visitClassRef(this);
	}
}
