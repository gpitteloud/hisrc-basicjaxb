package org.jvnet.basicjaxb.xml.bind.model.concrete;

import java.text.MessageFormat;

import javax.xml.namespace.QName;

import org.jvnet.basicjaxb.lang.Validate;
import org.jvnet.basicjaxb.xml.bind.model.MCustomizations;
import org.jvnet.basicjaxb.xml.bind.model.MList;
import org.jvnet.basicjaxb.xml.bind.model.MTypeInfo;
import org.jvnet.basicjaxb.xml.bind.model.MTypeInfoVisitor;

public class CMList<T, C extends T> implements MList<T, C> {

	private final MTypeInfo<T, C> itemTypeInfo;
	private final T targetType;
	private final MCustomizations customizations = new CMCustomizations();
	private final QName typeName;

	public CMList(T targetType, MTypeInfo<T, C> itemTypeInfo, QName typeName) {
		Validate.notNull(targetType);
		Validate.notNull(itemTypeInfo);
		this.targetType = targetType;
		this.itemTypeInfo = itemTypeInfo;
		this.typeName = typeName;
	}

	@Override
	public MCustomizations getCustomizations() {
		return customizations;
	}

	@Override
	public T getTargetType() {
		return targetType;
	}
	
	@Override
	public QName getTypeName() {
		return typeName;
	}
	
	@Override
	public boolean isSimpleType() {
		return true;
	}

	@Override
	public MTypeInfo<T, C> getItemTypeInfo() {
		return itemTypeInfo;
	}

	@Override
	public String toString() {
		return MessageFormat.format("List [{0}]", getItemTypeInfo());
	}

	@Override
	public <V> V acceptTypeInfoVisitor(MTypeInfoVisitor<T, C, V> visitor) {
		return visitor.visitList(this);
	}
}
