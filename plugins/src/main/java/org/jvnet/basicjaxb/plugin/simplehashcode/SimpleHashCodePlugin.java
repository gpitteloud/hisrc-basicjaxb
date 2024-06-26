package org.jvnet.basicjaxb.plugin.simplehashcode;

import static com.sun.codemodel.JExpr._this;
import static java.lang.String.format;
import static org.jvnet.basicjaxb.plugin.hashcode.Customizations.IGNORED_ELEMENT_NAME;
import static org.jvnet.basicjaxb.plugin.util.AttributeWildcardArguments.FIELD_NAME;
import static org.jvnet.basicjaxb.plugin.util.AttributeWildcardArguments.HAS_SET_VALUE;
import static org.jvnet.basicjaxb.plugin.util.AttributeWildcardArguments.IS_ALWAYS_SET;
import static org.jvnet.basicjaxb.plugin.util.OutlineUtils.filter;
import static org.jvnet.basicjaxb.plugin.util.StrategyClassUtils.superClassNotIgnored;
import static org.jvnet.basicjaxb.util.FieldUtils.getPossibleTypes;
import static org.jvnet.basicjaxb.util.LocatorUtils.toLocation;

import java.util.Collection;

import javax.xml.namespace.QName;

import org.jvnet.basicjaxb.plugin.codegenerator.AbstractCodeGeneratorPlugin;
import org.jvnet.basicjaxb.plugin.codegenerator.CodeGenerator;
import org.jvnet.basicjaxb.plugin.util.AttributeWildcardArguments;
import org.jvnet.basicjaxb.xjc.outline.FieldAccessorEx;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;

/**
 * Generate reflection-free runtime-free 'hashCode' methods.
 */
public class SimpleHashCodePlugin extends AbstractCodeGeneratorPlugin<HashCodeArguments>
{
	/** Name of Option to enable this plugin. */
	private static final String OPTION_NAME = "XsimpleHashCode";
	
	/** Description of Option to enable this plugin. */
	private static final String OPTION_DESC = "generate reflection-free runtime-free 'hashCode' methods";

	@Override
	public String getOptionName()
	{
		return OPTION_NAME;
	}

	@Override
	public String getUsage()
	{
		return format(USAGE_FORMAT, OPTION_NAME, OPTION_DESC);
	}

	@Override
	protected QName getSpecialIgnoredElementName()
	{
		return IGNORED_ELEMENT_NAME;
	}
	// Plugin Processing
	
	@Override
	protected void beforeRun(Outline outline) throws Exception
	{
		if ( isInfoEnabled() )
		{
			StringBuilder sb = new StringBuilder();
			sb.append(LOGGING_START);
			sb.append("\nParameters");
			sb.append("\n  Verbose.: " + isVerbose());
			sb.append("\n  Debug...: " + isDebug());
			info(sb.toString());
		}
	}
	
	@Override
	protected void afterRun(Outline outline) throws Exception
	{
		if ( isInfoEnabled() )
		{
			StringBuilder sb = new StringBuilder();
			sb.append(LOGGING_FINISH);
			sb.append("\nResults");
			sb.append("\n  HadError.: " + hadError(outline.getErrorReceiver()));
			info(sb.toString());
		}
	}

	@Override
	protected CodeGenerator<HashCodeArguments> createCodeGenerator(JCodeModel codeModel)
	{
		return new HashCodeCodeGenerator(codeModel);
	}

	@Override
	protected void generate(ClassOutline classOutline, JDefinedClass theClass)
	{
		final JCodeModel codeModel = theClass.owner();
		final JMethod object$hashCode = theClass.method(JMod.PUBLIC, codeModel.INT, "hashCode");
		object$hashCode.annotate(Override.class);
		{
			final JBlock body = object$hashCode.body();
			final JExpression currentHashCodeExpression = JExpr.lit(1);
			final JVar currentHashCode = body.decl(codeModel.INT, "currentHashCode", currentHashCodeExpression);
			
			final Boolean superClassImplementsHashCode = superClassNotIgnored(classOutline, getIgnoring());
			if (superClassImplementsHashCode != null)
			{
				body.assign(currentHashCode,
					currentHashCode.mul(JExpr.lit(getMultiplier())).plus(JExpr._super().invoke("hashCode")));
			}
			
			final FieldOutline[] declaredFields = filter(classOutline.getDeclaredFields(), getIgnoring());
			for (final FieldOutline fieldOutline : declaredFields)
			{
				final CPropertyInfo fieldInfo = fieldOutline.getPropertyInfo();
				
				final FieldAccessorEx fieldAccessor =
					getFieldAccessorFactory().createFieldAccessor(fieldOutline, JExpr._this());
				if ( !fieldAccessor.isConstant() )
				{
					final JBlock block = body.block();
					block.assign(currentHashCode, currentHashCode.mul(JExpr.lit(getMultiplier())));
					final String propertyName = fieldInfo.getName(true);
					
					final JType exposedType = fieldAccessor.getType();
					final JVar value = block.decl(exposedType, "the" + propertyName);
					fieldAccessor.toRawValue(block, value);
					
					// final JExpression hasSetValue = exposedType.isPrimitive() ? JExpr.TRUE : value.ne(JExpr._null());
					final JExpression hasSetValue = (fieldAccessor.isAlwaysSet() || fieldAccessor.hasSetValue() == null)
						? JExpr.TRUE : fieldAccessor .hasSetValue();
					
					final HashCodeArguments arguments = new HashCodeArguments
					(
						codeModel,
						currentHashCode,
						getMultiplier(),
						value,
						hasSetValue
					);
					
					final Collection<JType> possibleTypes = getPossibleTypes(fieldOutline, Aspect.EXPOSED);
					final boolean isAlwaysSet = fieldAccessor.isAlwaysSet();
					
					getCodeGenerator().generate
					(
						block,
						exposedType,
						possibleTypes,
						isAlwaysSet,
						arguments
					);
				}
				
				trace("{}, generate; Class={}, Field={}",
					toLocation(fieldOutline.getPropertyInfo().getLocator()), theClass.name(), fieldInfo.getName(false));
			}
			
			if ( classOutline.target.declaresAttributeWildcard() )
			{
				final AttributeWildcardArguments awa =
					new AttributeWildcardArguments(classOutline);

				final JBlock block = body.block();
				final JVar theValue = awa.fieldVar(block, _this(), "the");
				
				final HashCodeArguments arguments = new HashCodeArguments
				(
					codeModel,
					currentHashCode,
					getMultiplier(),
					theValue,
					HAS_SET_VALUE
				);
				
				getCodeGenerator().generate
				(
					block,
					awa.getExposedType(),
					awa.getPossibleTypes(),
					IS_ALWAYS_SET,
					arguments
				);
				
				trace("{}, generate; Class={}, Field={}",
					toLocation(classOutline), theClass.name(), FIELD_NAME);
			}
			
			body._return(currentHashCode);
		}
		debug("{}, generate; Class={}", toLocation(theClass.metadata), theClass.name());
	}

	private int multiplier = 31;
	private int getMultiplier()
	{
		return multiplier;
	}
}
