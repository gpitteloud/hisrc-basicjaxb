package org.jvnet.basicjaxb.plugin.defaultvalue;

import static java.lang.String.format;
import static org.jvnet.basicjaxb.plugin.defaultvalue.Customizations.IGNORED_ELEMENT_NAME;
import static org.jvnet.basicjaxb.plugin.util.FieldOutlineUtils.filter;
import static org.jvnet.basicjaxb.xmlschema.XmlSchemaConstants.ANYSIMPLETYPE;
import static org.jvnet.basicjaxb.xmlschema.XmlSchemaConstants.BASE64BINARY;
import static org.jvnet.basicjaxb.xmlschema.XmlSchemaConstants.HEXBINARY;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.jvnet.basicjaxb.dom.DOMUtils;
import org.jvnet.basicjaxb.lang.ValueUtils;
import org.jvnet.basicjaxb.plugin.AbstractParameterizablePlugin;
import org.jvnet.basicjaxb.plugin.Customizations;
import org.jvnet.basicjaxb.plugin.CustomizedIgnoring;
import org.jvnet.basicjaxb.plugin.Ignoring;
import org.jvnet.basicjaxb.xml.namespace.util.QNameUtils;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.EnumConstantOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;

import jakarta.xml.bind.DatatypeConverter;

/**
 * <p>
 * Modifies the JAXB code model to set default values to the schema "default" attribute.
 * Currently, the following field types can be initialized:
 * </p>
 * <ul>
 *   <li>Enumerations (see {@link Enum})</li>
 *   <li>{@link String}</li>
 *   <li>Descendants of {@link java.lang.Number}
 *     <ul>
 *     <li>{@link Byte}</li>
 *     <li>{@link Double}</li>
 *     <li>{@link Float}</li>
 *     <li>{@link Integer}</li>
 *     <li>{@link Long}</li>
 *     <li>{@link Short}</li>
 *     </ul>
 *   </li>
 *   <li>{@link Boolean}</li>
 * </ul>
 *
 * <p>
 * Created: Mon Apr 24 22:04:25 2006
 * </p>
 * 
 * @author <a href="mailto:hari@dcis.net">Hari Selvarajan</a>
 * @author <a href="mailto:Juergen.Lukasczyk@web.de">Jürgen Lukasczyk</a>
 */
public class DefaultValuePlugin extends AbstractParameterizablePlugin
{
	/** Name of Option to enable this plugin. */
	private static final String OPTION_NAME = "XdefaultValue";
	
	/** Description of Option to enable this plugin. */
	private static final String OPTION_DESC = "enable rewriting of classes to set default values for fields as specified in XML schema";

	/** Represents the arguments of a method without parameters. */
	private static final JType[] NO_ARGS = new JType[0];
	
	/** Creates a new <code>DefaultValuePlugin</code> instance. */
	public DefaultValuePlugin()
	{
	}

	/** DefaultValuePlugin uses "-" + OPTION_NAME as the XJC argument */
	public String getOptionName()
	{
		return OPTION_NAME;
	}

	/** Return usage information for plugin */
	public String getUsage()
	{
		return format(USAGE_FORMAT, OPTION_NAME, OPTION_DESC);
	}

	private Ignoring ignoring = new CustomizedIgnoring
	(
		IGNORED_ELEMENT_NAME,
		Customizations.IGNORED_ELEMENT_NAME,
		Customizations.GENERATED_ELEMENT_NAME
	);
	public Ignoring getIgnoring()
	{
		return ignoring;
	}
	public void setIgnoring(Ignoring ignoring)
	{
		this.ignoring = ignoring;
	}

	@Override
	public Collection<QName> getCustomizationElementNames()
	{
		return Arrays.asList
		(
			IGNORED_ELEMENT_NAME,
			Customizations.IGNORED_ELEMENT_NAME,
			Customizations.GENERATED_ELEMENT_NAME
		);
	}
	
	/**
	 * <p>
	 * Run the plugin. We perform the following steps:
	 * </p>
	 *
	 * <ul>
	 *   <li>Look for qualifying fields, fields qualify that:
	 *     <ul>
	 *       <li>Are generated from XSD description</li>
	 *       <li>The XSD description is of type <code>xsd:element</code>
	 *           (code level default values are not necessary for fields 
	 *           generated from attributes)</li>
	 *       <li>A default value is specified</li>
	 *       <li>Map to one of the supported types</li>
	 *     </ul>
	 *   </li>
	 *   <li>Add a new initialization expression to every qualifying accessor or field:
	 *     <ul>
	 *       <li>An element accessor qualifies when the field is nullable;</li>
	 *       <li>Otherwise, the field qualifies to receive the initialization expression</li>
	 *     </ul>
	 *   </li>
	 *   
	 * </ul>
	 *
     * @param outline
     *      This object allows access to various generated code.
     * 
     * @param options
     * 		The invocation configuration for XJC.
     * 
     * @param errorHandler
     *      Errors should be reported to this handler.
     * 
     * @return
     *      If the add-on executes successfully, return true.
     *      If it detects some errors but those are reported and
     *      recovered gracefully, return false.
     *
     * @throws SAXException
     *      After an error is reported to {@link ErrorHandler}, the
     *      same exception can be thrown to indicate a fatal irrecoverable
     *      error. {@link ErrorHandler} itself may throw it, if it chooses
     *      not to recover from the error.
	 */
	@Override
	public boolean run(Outline outline, Options options, ErrorHandler errorHandler)
		throws SAXException
	{
		try
		{
			// For all Classes generated
			for (final ClassOutline classOutline : outline.getClasses())
			{
				if (!getIgnoring().isIgnored(classOutline))
					processClassOutline(outline, options, classOutline);
			}
			return true;
		}
		catch ( Exception ex )
		{
			SAXParseException saxex = new SAXParseException( "Error running plugin.", null, ex);
			errorHandler.error(saxex);
			throw saxex;
		}
	}

	/**
	 * Process the XJC {@link Outline} instance. The goal is to add a default value to
	 * initialize all non-ignored fields from the given {@link Outline} instance.
	 * 
	 * @param outline An outline from the XJC framework.
	 * @param options The invocation configuration for XJC.
	 * @param classOutline A class outline from the XJC framework.
     * 
	 */
	protected void processClassOutline(Outline outline, Options options, ClassOutline classOutline)
	{
		// Filter out the ignored class outline's fields.
		FieldOutline[] declaredFilteredFields = filter(classOutline.getDeclaredFields(), getIgnoring());
		
		// check all Fields in Class
		for (FieldOutline fieldOutline : declaredFilteredFields)
		{
			// Handle primitive types via boxed representation (treat boolean as java.lang.Boolean)
			JType fieldRawType = fieldOutline.getRawType();
			boolean fieldIsPrimitive = fieldRawType.isPrimitive();
			JType fieldType = (fieldIsPrimitive) ? fieldRawType.boxify() : fieldRawType;
			
			// Get the field type's full name.
			String typeFullName = fieldType.fullName();

			// Represent the XML schema element's or attribute's default value.
			String defaultValue = null;
			
			// Do nothing if Field is not created from an or XSAttributeUse an XSParticle
			// Set the defaultValue ONLY when this plugin needs to provide the initialization.
			CPropertyInfo fieldInfo = fieldOutline.getPropertyInfo();
			QName schemaType = fieldInfo.getSchemaType();
			if ( fieldInfo.getSchemaComponent() instanceof XSAttributeUse )
			{
				// An XSAttributeUse provides isRequired, defaultValue and fixedValue for an XSAttributeDecl.
				XSAttributeUse attribute = (XSAttributeUse) fieldInfo.getSchemaComponent();
				
				// Some attribute types need a default value from this plugin, however;
				// most attributes are initialized in the getter by the XJC engine.
				if
				(
					XMLGregorianCalendar.class.getName().equals(typeFullName) ||
					Duration.class.getName().equals(typeFullName) ||
					(List.class.getName()+"<"+BigDecimal.class.getName()+">").equals(typeFullName) ||
					(List.class.getName()+"<"+BigInteger.class.getName()+">").equals(typeFullName) ||
					(List.class.getName()+"<"+Boolean.class.getName()+">").equals(typeFullName) ||
					(List.class.getName()+"<"+Byte.class.getName()+">").equals(typeFullName) ||
					(List.class.getName()+"<"+Double.class.getName()+">").equals(typeFullName) ||
					(List.class.getName()+"<"+Duration.class.getName()+">").equals(typeFullName) ||
					(List.class.getName()+"<"+Float.class.getName()+">").equals(typeFullName) ||
					(List.class.getName()+"<"+Integer.class.getName()+">").equals(typeFullName) ||
					(List.class.getName()+"<"+Long.class.getName()+">").equals(typeFullName) ||
					(List.class.getName()+"<"+Short.class.getName()+">").equals(typeFullName) ||
					(List.class.getName()+"<"+String.class.getName()+">").equals(typeFullName) ||
					(List.class.getName()+"<"+XMLGregorianCalendar.class.getName()+">").equals(typeFullName)
				)
				{
					if (attribute.getDefaultValue() != null)
					{
						// Get the default value from the XSD attribute as a String.
						defaultValue = attribute.getDefaultValue().value;
						if ( schemaType == null )
						{
							XSAttributeDecl attrDecl = attribute.getDecl();
							if ( attrDecl != null )
								schemaType = new QName(attrDecl.getTargetNamespace(), attrDecl.getName());
						}
					}
				}
			}
			else if ( fieldInfo.getSchemaComponent() instanceof XSParticle )
			{
				// An XSParticle provides min/max occurs cardinality for an XSTerm.
				XSParticle particle = (XSParticle) fieldInfo.getSchemaComponent();
				
				// Default values only necessary for fields derived from an xsd:element
				XSTerm term = particle.getTerm();
				if (term.isElementDecl())
				{
					// Do nothing if no default value.
					// Continue loop to next FieldOutline instance.
					XSElementDecl element = term.asElementDecl();
					if (element.getDefaultValue() != null)
					{
						// Get the default value from the XSD element as a String.
						defaultValue = element.getDefaultValue().value;
						if ( schemaType == null )
						{
							XSType elementType = element.getType();
							if ( elementType != null )
								schemaType = new QName(elementType.getTargetNamespace(), elementType.getName());
						}
					}
				}
			}
			
			// Provide initialization for the default value, when non-null.
			if ( defaultValue != null )
				processDefaultValue(outline, options, classOutline, fieldInfo, fieldType, fieldIsPrimitive, typeFullName, defaultValue, schemaType);
		} // for FieldOutline
	}

	private void processDefaultValue(Outline outline, Options options, ClassOutline classOutline,
		CPropertyInfo fieldInfo, JType fieldType, boolean fieldIsPrimitive,
		String typeFullName, String defaultValue, QName schemaType)
	{
		// Get handle to JModel representing the field
		JDefinedClass theClass = classOutline.implClass;
		
		// Get the field variable for the given fieldInfo private name
		// from the map of fields for theClass. .
		Map<String, JFieldVar> fields = theClass.fields();
		JFieldVar var = fields.get(fieldInfo.getName(false));
		
		// Get the accessor for the given fieldInfo public name
		// from the list of methods for theClass.
		String publicName = fieldInfo.getName(true);
		String accessorName = "get" + publicName;
		JMethod accessor = theClass.getMethod(accessorName, NO_ARGS);
		if ( accessor == null )
		{
			accessorName = "is" + publicName;
			accessor = theClass.getMethod(accessorName, NO_ARGS);
		}
		
		// Get handle to JCodeModel owning this class.
		JCodeModel theCodeModel = theClass.owner();
		
		// PROCESS: Create an appropriate default expression depending on type
		if ((fieldType instanceof JDefinedClass) && (((JDefinedClass) fieldType).getClassType() == ClassType.ENUM))
		{
			// Find Enumeration constant
			JEnumConstant literalValue = findEnumConstant(fieldType, defaultValue, outline);
			if (literalValue != null)
			{
				initializer(var, accessor, fieldIsPrimitive, literalValue);
				info(options, "Initializing enum variable " + fieldInfo.displayName() + " with constant " + literalValue.getName());
			}
		}
		else if (String.class.getName().equals(typeFullName))
		{
			JExpression literalValue = JExpr.lit(defaultValue);
			initializer(var, accessor, fieldIsPrimitive, literalValue);
			info(options, "Initializing String variable " + fieldInfo.displayName() + " to \"" + defaultValue + "\"");
		}
		else if (Boolean.class.getName().equals(typeFullName))
		{
			JExpression literalValue = JExpr.lit(Boolean.valueOf(defaultValue));
			initializer(var, accessor, fieldIsPrimitive, literalValue);
			info(options, "Initializing Boolean variable " + fieldInfo.displayName() + " to " + defaultValue + "");
		}
		else if
		(
			(Byte.class.getName().equals(typeFullName)) ||
			(Short.class.getName().equals(typeFullName)) ||
			(Integer.class.getName().equals(typeFullName))
		)
		{
			// CodeModel does not distinguish between Byte, Short and Integer literals
			JExpression literalValue = JExpr.lit(Integer.valueOf(defaultValue));
			initializer(var, accessor, fieldIsPrimitive, literalValue);
			info(options, "Initializing Integer variable " + fieldInfo.displayName() + " to " + defaultValue + "");
		}
		else if (Long.class.getName().equals(typeFullName))
		{
			JExpression literalValue = JExpr.lit(Long.valueOf(defaultValue));
			initializer(var, accessor, fieldIsPrimitive, literalValue);
			info(options, "Initializing Long variable " + fieldInfo.displayName() + " to " + defaultValue + "");
		}
		else if (Float.class.getName().equals(typeFullName))
		{
			JExpression literalValue = JExpr.lit(Float.valueOf(defaultValue));
			initializer(var, accessor, fieldIsPrimitive, literalValue);
			info(options, "Initializing Float variable "	+ fieldInfo.displayName() + " to " + defaultValue + "");
		}
		else if (Double.class.getName().equals(typeFullName))
		{
			JExpression literalValue = JExpr.lit(Double.valueOf(defaultValue));
			initializer(var, accessor, fieldIsPrimitive, literalValue);
			info(options, "Initializing Double variable " + fieldInfo.displayName() + " to " + defaultValue + "");
		}
		else if (BigDecimal.class.getName().equals(typeFullName))
		{
			JExpression literalValue = JExpr.direct("new BigDecimal(\"" + defaultValue + "\")");
			initializer(var, accessor, fieldIsPrimitive, literalValue);
			info(options, "Initializing BigDecimal variable " + fieldInfo.displayName() + " to " + defaultValue + "");
		}
		else if (BigInteger.class.getName().equals(typeFullName))
		{
			JExpression literalValue = JExpr.direct("new BigInteger(\"" + defaultValue + "\")");
			initializer(var, accessor, fieldIsPrimitive, literalValue);
			info(options, "Initializing BigInteger variable " + fieldInfo.displayName() + " to " + defaultValue + "");
		}
		else if
		(
			byte[].class.getCanonicalName().equals(typeFullName) && (schemaType == null) ||
			byte[].class.getCanonicalName().equals(typeFullName) && BASE64BINARY.equals(schemaType) 
		)
		{
			JClass refDatatypeConverter = theCodeModel.ref(DatatypeConverter.class);
			JExpression literalValue = refDatatypeConverter.staticInvoke("parseBase64Binary").arg(defaultValue);
			initializer(var, accessor, fieldIsPrimitive, literalValue);
			info(options, "Initializing Base64Binary variable " + fieldInfo.displayName() + " to " + defaultValue + "");
		}
		else if ( byte[].class.getCanonicalName().equals(typeFullName) && HEXBINARY.equals(schemaType) )
		{
			JExpression literalValue = JExpr.direct("new HexBinaryAdapter().unmarshal(\"" + defaultValue + "\")");
			initializer(var, accessor, fieldIsPrimitive, literalValue);
			info(options, "Initializing HexBinary variable " + fieldInfo.displayName() + " to " + defaultValue + "");
		}
		else if (XMLGregorianCalendar.class.getName().equals(typeFullName))
		{
			// XMLGregorianCalender is constructed by DatatypeFactory,
			// so we have to have an instance of that once per class.
			JFieldVar dtf = theClass.fields().get("DATATYPE_FACTORY");
			if (dtf == null)
				dtf = installDatatypeFactory(theClass);
			if ( dtf != null )
			{
				// Use our DtF instance to generate the initialization expression
				JExpression literalValue = JExpr.invoke(dtf, "newXMLGregorianCalendar").arg(defaultValue);
				initializer(var, accessor, fieldIsPrimitive, literalValue);
				info(options, "Initializing XMLGregorianCalendar variable " + fieldInfo.displayName() + " with value of " + defaultValue);
			}
		}
		else if (Duration.class.getName().equals(typeFullName))
		{
			// Duration is constructed by DatatypeFactory,
			// so we have to have an instance of that once per class.
			JFieldVar dtf = theClass.fields().get("DATATYPE_FACTORY");
			if (dtf == null)
				dtf = installDatatypeFactory(theClass);
			if ( dtf != null )
			{
				// Use our DtF instance to generate the initialization expression
				JExpression literalValue = JExpr.invoke(dtf, "newDuration").arg(defaultValue);
				initializer(var, accessor, fieldIsPrimitive, literalValue);
				info(options, "Initializing Duration variable " + fieldInfo.displayName() + " with value of " + defaultValue);
			}
		}
		else if (QName.class.getName().equals(typeFullName))
		{
			// ALT?: javax.xml.namespace.QName parseQName(String lexicalXSDQName, javax.xml.namespace.NamespaceContext nsc);
			JFieldVar ofField = theClass.fields().get("OBJECT_FACTORY");
			if (ofField == null)
				ofField = installObjectFactory(theClass);
			JClass refQNameUtils = theCodeModel.ref(QNameUtils.class);
			JInvocation jaxbElement = JExpr.invoke(ofField, "create" + publicName).arg(JExpr._null());
			JExpression literalValue = refQNameUtils.staticInvoke("toName").arg(jaxbElement).arg(defaultValue);
			initializer(var, accessor, fieldIsPrimitive, literalValue);
			info(options, "Initializing QName variable " + fieldInfo.displayName() + " to " + defaultValue + "");
		}
		else if (Object.class.getName().equals(typeFullName) && ANYSIMPLETYPE.equals(schemaType))
		{
			// ALT?: jakarta.xml.bind.DatatypeConverter.parseAnySimpleType(String)
			JFieldVar ofField = theClass.fields().get("OBJECT_FACTORY");
			if (ofField == null)
				ofField = installObjectFactory(theClass);
			JClass refDOMUtils = theCodeModel.ref(DOMUtils.class);
			JInvocation jaxbElement = JExpr.invoke(ofField, "create" + publicName).arg(defaultValue);
			JExpression literalValue = refDOMUtils.staticInvoke("toNode").arg(jaxbElement);
			initializer(var, accessor, fieldIsPrimitive, literalValue);
			info(options, "Initializing Object variable " + fieldInfo.displayName() + " to \"" + defaultValue + "\"");
		}
		else if ((List.class.getName()+"<"+BigDecimal.class.getName()+">").equals(typeFullName))
		{
			JClass refValueUtils = theCodeModel.ref(ValueUtils.class);
			JExpression literalValue = refValueUtils.staticInvoke("toBigDecimalList").arg(defaultValue);
			initializerList(var, accessor, fieldIsPrimitive, literalValue);
			info(options, "Initializing List<BigDecimal> variable " + fieldInfo.displayName() + " to " + defaultValue + "");
		}
		else if ((List.class.getName()+"<"+BigInteger.class.getName()+">").equals(typeFullName))
		{
			JClass refValueUtils = theCodeModel.ref(ValueUtils.class);
			JExpression literalValue = refValueUtils.staticInvoke("toBigIntegerList").arg(defaultValue);
			initializerList(var, accessor, fieldIsPrimitive, literalValue);
			info(options, "Initializing List<BigInteger> variable " + fieldInfo.displayName() + " to " + defaultValue + "");
		}
		else if ((List.class.getName()+"<"+Boolean.class.getName()+">").equals(typeFullName))
		{
			JClass refValueUtils = theCodeModel.ref(ValueUtils.class);
			JExpression literalValue = refValueUtils.staticInvoke("toBooleanList").arg(defaultValue);
			initializerList(var, accessor, fieldIsPrimitive, literalValue);
			info(options, "Initializing List<Boolean> variable " + fieldInfo.displayName() + " to " + defaultValue + "");
		}
		else if ((List.class.getName()+"<"+Byte.class.getName()+">").equals(typeFullName))
		{
			JClass refValueUtils = theCodeModel.ref(ValueUtils.class);
			JExpression literalValue = refValueUtils.staticInvoke("toByteList").arg(defaultValue);
			initializerList(var, accessor, fieldIsPrimitive, literalValue);
			info(options, "Initializing List<Byte> variable " + fieldInfo.displayName() + " to " + defaultValue + "");
		}
		else if ((List.class.getName()+"<"+Double.class.getName()+">").equals(typeFullName))
		{
			JClass refValueUtils = theCodeModel.ref(ValueUtils.class);
			JExpression literalValue = refValueUtils.staticInvoke("toDoubleList").arg(defaultValue);
			initializerList(var, accessor, fieldIsPrimitive, literalValue);
			info(options, "Initializing List<Double> variable " + fieldInfo.displayName() + " to " + defaultValue + "");
		}
		else if ((List.class.getName()+"<"+Duration.class.getName()+">").equals(typeFullName))
		{
			JClass refValueUtils = theCodeModel.ref(ValueUtils.class);
			JExpression literalValue = refValueUtils.staticInvoke("toDurationList").arg(defaultValue);
			initializerList(var, accessor, fieldIsPrimitive, literalValue);
			info(options, "Initializing List<Duration> variable " + fieldInfo.displayName() + " to " + defaultValue + "");
		}
		else if ((List.class.getName()+"<"+Float.class.getName()+">").equals(typeFullName))
		{
			JClass refValueUtils = theCodeModel.ref(ValueUtils.class);
			JExpression literalValue = refValueUtils.staticInvoke("toFloatList").arg(defaultValue);
			initializerList(var, accessor, fieldIsPrimitive, literalValue);
			info(options, "Initializing List<Float> variable " + fieldInfo.displayName() + " to " + defaultValue + "");
		}
		else if ((List.class.getName()+"<"+Integer.class.getName()+">").equals(typeFullName))
		{
			JClass refValueUtils = theCodeModel.ref(ValueUtils.class);
			JExpression literalValue = refValueUtils.staticInvoke("toIntegerList").arg(defaultValue);
			initializerList(var, accessor, fieldIsPrimitive, literalValue);
			info(options, "Initializing List<Integer> variable " + fieldInfo.displayName() + " to " + defaultValue + "");
		}
		else if ((List.class.getName()+"<"+Long.class.getName()+">").equals(typeFullName))
		{
			JClass refValueUtils = theCodeModel.ref(ValueUtils.class);
			JExpression literalValue = refValueUtils.staticInvoke("toLongList").arg(defaultValue);
			initializerList(var, accessor, fieldIsPrimitive, literalValue);
			info(options, "Initializing List<Long> variable " + fieldInfo.displayName() + " to " + defaultValue + "");
		}
		else if ((List.class.getName()+"<"+Short.class.getName()+">").equals(typeFullName))
		{
			JClass refValueUtils = theCodeModel.ref(ValueUtils.class);
			JExpression literalValue = refValueUtils.staticInvoke("toShortList").arg(defaultValue);
			initializerList(var, accessor, fieldIsPrimitive, literalValue);
			info(options, "Initializing List<Short> variable " + fieldInfo.displayName() + " to " + defaultValue + "");
		}
		else if ((List.class.getName()+"<"+String.class.getName()+">").equals(typeFullName))
		{
			JClass refValueUtils = theCodeModel.ref(ValueUtils.class);
			JExpression literalValue = refValueUtils.staticInvoke("toStringList").arg(defaultValue);
			initializerList(var, accessor, fieldIsPrimitive, literalValue);
			info(options, "Initializing List<String> variable " + fieldInfo.displayName() + " to " + defaultValue + "");
		}
		else if ((List.class.getName()+"<"+XMLGregorianCalendar.class.getName()+">").equals(typeFullName))
		{
			JClass refValueUtils = theCodeModel.ref(ValueUtils.class);
			JExpression literalValue = refValueUtils.staticInvoke("toXMLGregorianCalendarList").arg(defaultValue);
			initializerList(var, accessor, fieldIsPrimitive, literalValue);
			info(options, "Initializing List<XMLGregorianCalendar> variable " + fieldInfo.displayName() + " to " + defaultValue + "");
		}
		// Don't know how to create default for this type
		else
		{
			warn("Did not create default value for field "	+ fieldInfo.displayName()
				+ ". Don't know how to create default value expression for fields of type "+ typeFullName
				+ " with schema type " + schemaType
				+ ". Default value of \"" + defaultValue + "\" specified in schema");
		}
	}

	private void initializerList(JFieldVar var, JMethod accessor, boolean fieldIsPrimitive, JExpression literalValue)
	{
		if ( (accessor != null) && !fieldIsPrimitive )
		{
			accessor.body().pos(1);
			JConditional ifVarIsEmpty = accessor.body()._if(JExpr.invoke(var, "isEmpty"));
			ifVarIsEmpty._then().assign(var, literalValue);
		}
		else
			var.init(literalValue);
	}
	
	private void initializer(JFieldVar var, JMethod accessor, boolean fieldIsPrimitive, JExpression literalValue)
	{
		if ( (accessor != null) && !fieldIsPrimitive )
		{
			accessor.body().pos(0);
			JConditional ifVarIsNull = accessor.body()._if(var.eq(JExpr._null()));
			ifVarIsNull._then()._return(literalValue);
		}
		else
			var.init(literalValue);
	}
	
	/**
	 * Retrieve the enum constant that correlates to the string value.
	 * 
	 * @param enumType Type identifying an Enum in the code model
	 * @param enumStringValue Lexical value of the constant to search
	 * @param outline Outline of the code model
	 * 
	 * @return The matching Constant from the enum type or NULL if not found
	 */
	private JEnumConstant findEnumConstant(JType enumType, String enumStringValue, Outline outline)
	{
		JEnumConstant ec = null;
		
		// Search all Enums generated
		for (EnumOutline eo : outline.getEnums())
		{
			// Is it the type of my variable?
			if (eo.clazz == enumType)
			{
				// Search all Constants of that enum
				for (EnumConstantOutline eco : eo.constants)
				{
					// Is the enum generated from the XML defaut value string?
					if (eco.target.getLexicalValue().equals(enumStringValue))
					{
						ec = eco.constRef;
						break;
					}
				} // for Constants
				
				if ( ec == null )
				{
					warn("Could not find EnumConstant for value: " + enumStringValue);
					break;
				}
			}
		}
		
		if ( ec == null )
			warn("Could not find Enum class for type: " + enumType.fullName());
		
		return ec;
	}

	/**
	 * On the defined class, install a field for the relative ObjectFactory.
	 * 
	 * @param theClass The target class.
	 * 
	 * @return A field representing the ObjectFactory instance.
	 */
	private JFieldVar installObjectFactory(JDefinedClass theClass)
	{
		JClass ofClass = theClass.owner().ref(theClass.getPackage().name() + ".ObjectFactory");
		return theClass.field(JMod.STATIC | JMod.FINAL | JMod.PRIVATE, ofClass, "OBJECT_FACTORY", JExpr.direct("new ObjectFactory()"));
	}
	
	/**
	 * Enhance the CodeModel of a Class to include a {@link DatatypeFactory} as
	 * a static private field. The factory is needed to construct
	 * {@link XMLGregorianCalendar} from String representation.
	 * 
	 * @param theClass Class where the DatatypeFactory will be created
	 * 
	 * @return Reference to the created static field or null when DatatypeFactory can't be created.
	 */
	private JFieldVar installDatatypeFactory(final JDefinedClass theClass)
	{
		JFieldVar dtf = null;
		
		try
		{
			JCodeModel cm = theClass.owner();
			
			// Create a static variable of type DatatypeFactory
			JClass dtfClass = cm.ref(DatatypeFactory.class);
			dtf = theClass.field(JMod.STATIC | JMod.FINAL | JMod.PRIVATE, dtfClass, "DATATYPE_FACTORY");
			
			// Initialize variable in static block
			JBlock si = theClass.init();
			JTryBlock tryBlock = si._try();
			tryBlock.body().assign(dtf, dtfClass.staticInvoke("newInstance"));
			
			// Catch exception & rethrow as unchecked Exception
			JCatchBlock catchBlock = tryBlock._catch(cm.ref(DatatypeConfigurationException.class));
			JVar ex = catchBlock.param("ex");
			JClass runtimeException = cm.ref(RuntimeException.class);
			catchBlock.body()._throw(JExpr._new(runtimeException).arg("Unable to initialize DatatypeFactory").arg(ex));
		}
		catch (Exception ex)
		{
			// We don't want JAXB to break of any plugin error, do we?
			error("Failed to create code", ex);
		}
		
		// Return reference to initialized static field
		return dtf;
	}

	/**
	 * Conditionally log an informative message.
	 * 
	 * @param options The XJC invocation configuration.
	 * @param msg An informative message.
	 */
	private void info(Options options, String msg)
	{
		if ( options.debugMode )
			getLogger().debug(msg);
		else if ( options.verbose )
		{
			// verbose and debug modes are cross-wired ubiquitously,
			// use debug level when enabled for consistency.
			if ( getLogger().isDebugEnabled())
				getLogger().debug(msg);
			else
				getLogger().info(msg);
		}
	}

	/**
	 * Log a cautionary message.
	 * 
	 * @param msg A cautionary message.
	 */
	private void warn(String msg)
	{
		getLogger().warn(msg);
	}
	
	/**
	 * Log an error message.
	 * 
	 * @param msg An error message.
	 */
	private void error(String msg, Exception ex)
	{
		getLogger().error(msg, ex);
	}
}
