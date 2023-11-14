package org.jvnet.basicjaxb.plugin.codegenerator;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.Validate;
import org.jvnet.basicjaxb.codemodel.JCMType;
import org.jvnet.basicjaxb.codemodel.JCMTypeFactory;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JType;

import jakarta.xml.bind.JAXBElement;

/**
 * Concrete extension of {@link AbstractCodeGenerator} for {@link JAXBElement} types.
 *
 * @param <A> The generic type of the plugin arguments.
 */
public class JAXBElementCodeGenerator<A extends Arguments<A>> extends AbstractCodeGenerator<A>
{
	// Represents a JCM type factory
	private final JCMTypeFactory typeFactory;
	private JCMTypeFactory getTypeFactory()
	{
		return typeFactory;
	}
	
	/**
	 * Construct {@link JAXBElementCodeGenerator} with a {@link CodeGenerator}, {@link CodeGenerationImplementor}
	 * and a {@link JCMTypeFactory}.
	 * 
	 * @param codeGenerator Interface to generate code for a XJC plugin.
	 * @param implementor Interface for the plugin's code generation implementor.
	 * @param typeFactory Factory to produce {@link JCMType} for the given {@link JType}.
	 */
	public JAXBElementCodeGenerator(CodeGenerator<A> codeGenerator, CodeGenerationImplementor<A> implementor,
		JCMTypeFactory typeFactory)
	{
		super(codeGenerator, implementor);
		this.typeFactory = requireNonNull(typeFactory);
	}

	/**
	 * Generate code for a XJC plugin using the plugin's arguments for the current target field.
	 */
	@Override
	public void generate(JBlock block, JType type, Collection<JType> possibleTypes, boolean isAlwaysSet, A arguments)
	{
		Validate.isInstanceOf(JClass.class, type);
		final JClass _class = (JClass) type;
		// Get the T from JAXBElement<T>
		final JClass valueType = getValueType(_class);
		// Gather possible values types
		final Set<JType> possibleValueTypes = getPossibleValueTypes(possibleTypes);
		onJAXBElement(block, valueType, possibleValueTypes, isAlwaysSet, arguments);
	}

	private JClass getValueType(final JClass _class)
	{
		final JClass valueType;
		final List<JClass> typeParameters = _class.getTypeParameters();
		if (typeParameters.size() == 1)
		{
			valueType = typeParameters.get(0);
		}
		else
		{
			valueType = getCodeModel().ref(Object.class).wildcard();
		}
		return valueType;
	}

	private Set<JType> getPossibleValueTypes(Collection<JType> possibleTypes)
	{
		final Set<JType> possibleValueTypes = new HashSet<JType>();
		for (JType possibleType : possibleTypes)
		{
			Validate.isInstanceOf(JClass.class, possibleType);
			final JClass possibleClass = (JClass) possibleType;
			if (possibleClass.getTypeParameters().size() == 1)
			{
				possibleValueTypes.add(possibleClass.getTypeParameters().get(0));
			}
			else
			{
				possibleValueTypes.add(getCodeModel().ref(Object.class));
			}
		}
		return possibleValueTypes;
	}

	private void onJAXBElement(JBlock block, JType valueType, Collection<JType> possibleValueTypes, boolean isAlwaysSet,
		A arguments)
	{
		block = arguments.ifHasSetValue(block, isAlwaysSet, true);
		append(block, "Name", "getName", arguments, getCodeModel().ref(QName.class));
		append(block, "Value", "getValue", valueType, possibleValueTypes, arguments);
		append(block, "DeclaredType", "getDeclaredType", arguments, getCodeModel().ref(Class.class).narrow(valueType));
		append(block, "Scope", "getScope", arguments,
			getCodeModel().ref(Class.class).narrow(getCodeModel().ref(Object.class).wildcard()));
		append(block, "Nil", "isNil", arguments, getCodeModel().BOOLEAN);
	}

	private void append(JBlock block, String propertyName, String method, A arguments, final JType type)
	{
		append(block, propertyName, method, type, Collections.<JType> singleton(type), arguments);
	}

	private void append(JBlock block, String propertyName, String propertyMethod, JType propertyType,
		Collection<JType> possiblePropertyTypes, A arguments)
	{
		block = block.block();
		final JType declarablePropertyType = getTypeFactory().create(propertyType).getDeclarableType();
		// We assume that primitive properties are always set
		boolean isAlwaysSet = propertyType.isPrimitive();
		getCodeGenerator().generate(block, propertyType, possiblePropertyTypes, isAlwaysSet, arguments.property(block,
			propertyName, propertyMethod, declarablePropertyType, declarablePropertyType, possiblePropertyTypes));
	}
}
