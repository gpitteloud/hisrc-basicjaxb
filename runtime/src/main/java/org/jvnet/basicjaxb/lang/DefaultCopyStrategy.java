package org.jvnet.basicjaxb.lang;

import static org.jvnet.basicjaxb.lang.StringUtils.valueToString;
import static org.jvnet.basicjaxb.locator.util.LocatorUtils.item;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.xml.datatype.XMLGregorianCalendar;

import org.jvnet.basicjaxb.locator.ObjectLocator;
import org.jvnet.basicjaxb.locator.RootObjectLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultCopyStrategy implements CopyStrategy
{
	private static final DefaultCopyStrategy INSTANCE = new DefaultCopyStrategy();
	public static DefaultCopyStrategy getInstance()
	{
		return INSTANCE;
	}

	private Logger logger = LoggerFactory.getLogger(CopyStrategy.class);
	@Override
	public Logger getLogger()
	{
		return logger;
	}
	
	@Override
	public boolean isDebugEnabled()
	{
		return getLogger().isDebugEnabled();
	}
	
	@Override
	public boolean isTraceEnabled()
	{
		return getLogger().isTraceEnabled();
	}
	
	/**
	 * Subclasses can override this method to log the debug message, as desired.
	 * 
	 * @param message The debug message of copied values.
	 */
	public void debug(String message)
	{
		getLogger().debug(message);
	}
	
	/**
	 * Subclasses can override this method to log the trace message, as desired.
	 * 
	 * @param message The trace message of copied values.
	 */
	public void trace(String message)
	{
		getLogger().trace(message);
	}
	
	/**
	 * Observe an object and its locator.
	 * 
	 * @param <T> The object type.
	 * @param locator The object locator.
	 * @param obj The object.
	 * 
	 * @return The original object.
	 */
	protected <T> T observe(ObjectLocator locator, T obj)
	{
		return observe("COPY", locator, obj);
	}
	
	/**
	 * Observe an object and its locator.
	 * 
	 * @param <T> The object type.
	 * @param label A prefix for the observation message.
	 * @param locator The object locator.
	 * @param obj The object.
	 * 
	 * @return The original object.
	 */
	protected <T> T observe(String label, ObjectLocator locator, T obj)
	{
		if ( isTraceEnabled() )
			trace(buildMessage(label, locator, valueToString(obj)));
		else if ( isDebugEnabled() )
		{
			if ( locator instanceof RootObjectLocator )
				debug(buildMessage(label, locator, valueToString(obj)));
		}			
		return obj;
	}
	
	protected String buildMessage(String label, ObjectLocator locator, String value)
	{
		String message;
		if (locator != null)
			message = label + ": " + "{"+locator.getPathAsString()+"} -> " + value;
		else
			message = label + ": " + "{} -> " + value;
		return message;
	}
	
	@Override
	public Boolean shouldBeCopiedAndSet(ObjectLocator locator, boolean valueSet)
	{
		return valueSet;
	}

	protected Object copyInternal(ObjectLocator locator, Object object)
	{
		if (object == null)
			return observe(locator, null);
		else if (object instanceof String)
			return observe(locator, object);
		else if (object instanceof Number)
			return observe(locator, object);
		else if (object instanceof XMLGregorianCalendar)
			return copyInternal(locator, (XMLGregorianCalendar) object);
		else if (object instanceof CopyTo)
			return observe(locator, (((CopyTo) object).copyTo(locator, ((CopyTo) object).createNewInstance(), this)));
		else if (object instanceof Cloneable)
			return copyInternal(locator, (Cloneable) object);
		else
			return observe(locator, object);
	}

	protected Object copyInternal(ObjectLocator locator, XMLGregorianCalendar xgc)
	{
		return observe(locator, xgc.clone());
	}
	
	protected Object copy(ObjectLocator locator, Object value)
	{
		if (value == null)
			return observe(locator, null);
		
		// 'Switch' on non-array or type of array, to dispatch to 
		// the correct handler. This handles multidimensional arrays
		// of the same depth
		Class<?> lhsClass = value.getClass();
		if (!lhsClass.isArray())
			return copyInternal(locator, value);
		else if (value instanceof long[])
			return copy(locator, (long[]) value);
		else if (value instanceof int[])
			return copy(locator, (int[]) value);
		else if (value instanceof short[])
			return copy(locator, (short[]) value);
		else if (value instanceof char[])
			return copy(locator, (char[]) value);
		else if (value instanceof byte[])
			return copy(locator, (byte[]) value);
		else if (value instanceof double[])
			return copy(locator, (double[]) value);
		else if (value instanceof float[])
			return copy(locator, (float[]) value);
		else if (value instanceof boolean[])
			return copy(locator, (boolean[]) value);
		else
		{
			// Not an array of primitives
			return copy(locator, (Object[]) value);
		}
	}

	protected long copy(ObjectLocator locator, long value)
	{
		return observe(locator, value);
	}

	protected int copy(ObjectLocator locator, int value)
	{
		return observe(locator, value);
	}

	protected short copy(ObjectLocator locator, short value)
	{
		return observe(locator, value);
	}

	protected char copy(ObjectLocator locator, char value)
	{
		return observe(locator, value);
	}

	protected byte copy(ObjectLocator locator, byte value)
	{
		return observe(locator, value);
	}

	protected double copy(ObjectLocator locator, double value)
	{
		return observe(locator, value);
	}

	protected float copy(ObjectLocator locator, float value)
	{
		return observe(locator, value);
	}

	protected boolean copy(ObjectLocator locator, boolean value)
	{
		return observe(locator, value);
	}

	protected Object[] copy(ObjectLocator locator, Object[] array)
	{
		if (array == null)
			return observe(locator, null);
		
		final Object[] copy = new Object[array.length];
		for (int index = 0; index < array.length; index++)
		{
			final Object element = array[index];
			final Object elementCopy = copy(item(locator, index, element), element);
			copy[index] = elementCopy;
		}
		return copy;
	}

	protected long[] copy(ObjectLocator locator, long[] array)
	{
		if (array == null)
			return observe(locator, null);
		
		final long[] copy = new long[array.length];
		for (int index = 0; index < array.length; index++)
		{
			final long element = array[index];
			final long elementCopy = copy(item(locator, index, element), element);
			copy[index] = elementCopy;
		}
		return copy;
	}

	protected int[] copy(ObjectLocator locator, int[] array)
	{
		if (array == null)
			return observe(locator, null);
		
		final int[] copy = new int[array.length];
		for (int index = 0; index < array.length; index++)
		{
			final int element = array[index];
			final int elementCopy = copy(item(locator, index, element), element);
			copy[index] = elementCopy;
		}
		return copy;
	}

	protected short[] copy(ObjectLocator locator, short[] array)
	{
		if (array == null)
			return observe(locator, null);
		
		final short[] copy = new short[array.length];
		for (int index = 0; index < array.length; index++)
		{
			final short element = array[index];
			final short elementCopy = copy(item(locator, index, element), element);
			copy[index] = elementCopy;
		}
		return copy;
	}

	protected char[] copy(ObjectLocator locator, char[] array)
	{
		if (array == null)
			return observe(locator, null);
		
		final char[] copy = new char[array.length];
		for (int index = 0; index < array.length; index++)
		{
			final char element = array[index];
			final char elementCopy = copy(item(locator, index, element), element);
			copy[index] = elementCopy;
		}
		return copy;
	}

	protected byte[] copy(ObjectLocator locator, byte[] array)
	{
		if (array == null)
			return observe(locator, null);
		
		final byte[] copy = new byte[array.length];
		for (int index = 0; index < array.length; index++)
		{
			final byte element = array[index];
			final byte elementCopy = copy(item(locator, index, element), element);
			copy[index] = elementCopy;
		}
		return copy;
	}

	protected double[] copy(ObjectLocator locator, double[] array)
	{
		if (array == null)
			return observe(locator, null);
		
		final double[] copy = new double[array.length];
		for (int index = 0; index < array.length; index++)
		{
			final double element = array[index];
			final double elementCopy = copy(item(locator, index, element), element);
			copy[index] = elementCopy;
		}
		return copy;
	}

	protected float[] copy(ObjectLocator locator, float[] array)
	{
		if (array == null)
			return observe(locator, null);
		
		final float[] copy = new float[array.length];
		for (int index = 0; index < array.length; index++)
		{
			final float element = array[index];
			final float elementCopy = copy(item(locator, index, element), element);
			copy[index] = elementCopy;
		}
		return copy;
	}

	protected boolean[] copy(ObjectLocator locator, boolean[] array)
	{
		if (array == null)
			return observe(locator, null);

		final boolean[] copy = new boolean[array.length];
		for (int index = 0; index < array.length; index++)
		{
			final boolean element = array[index];
			final boolean elementCopy = copy(item(locator, index, element), element);
			copy[index] = elementCopy;
		}
		return copy;
	}

	protected Object copyInternal(ObjectLocator locator, Cloneable object)
	{
		Method method = null;
		try
		{
			method = object.getClass().getMethod("clone", (Class[]) null);
		}
		catch (NoSuchMethodException nsmex)
		{
			method = null;
		}
		if (method == null || !Modifier.isPublic(method.getModifiers()))
		{
			throw new UnsupportedOperationException("Could not clone object [" + object + "].",
				new CloneNotSupportedException(
					"Object class ["	+ object.getClass() + "] implements java.lang.Cloneable interface, "
					+ "but does not provide a public no-arg clone() method. "
					+ "By convention, classes that implement java.lang.Cloneable "
					+ "should override java.lang.Object.clone() method (which is protected) "
					+ "with a public method."));
		}
		final boolean wasAccessible = method.canAccess(object);
		try
		{
			if (!wasAccessible)
			{
				try
				{
					method.setAccessible(true);
				}
				catch (SecurityException ex)
				{
					getLogger().warn("copyInternal " + ex.getMessage(), ex);
				}
			}
			return observe(locator, method.invoke(object, (Object[]) null));
		}
		catch (Exception ex)
		{
			throw new UnsupportedOperationException("Could not clone the object ["	+ object
				+ "] as invocation of the clone() method has thrown an exception.", ex);
		}
		finally
		{
			if (!wasAccessible)
			{
				try
				{
					method.setAccessible(false);
				}
				catch (SecurityException ex)
				{
					getLogger().warn("copyInternal " + ex.getMessage(), ex);
				}
			}
		}
	}

	@Override
	public boolean copy(ObjectLocator locator, boolean value, boolean valueSet)
	{
		return copy(locator, value);
	}

	@Override
	public byte copy(ObjectLocator locator, byte value, boolean valueSet)
	{
		return copy(locator, value);
	}

	@Override
	public char copy(ObjectLocator locator, char value, boolean valueSet)
	{
		return copy(locator, value);
	}

	@Override
	public double copy(ObjectLocator locator, double value, boolean valueSet)
	{
		return copy(locator, value);
	}

	@Override
	public float copy(ObjectLocator locator, float value, boolean valueSet)
	{
		return copy(locator, value);
	}

	@Override
	public int copy(ObjectLocator locator, int value, boolean valueSet)
	{
		return copy(locator, value);
	}

	@Override
	public long copy(ObjectLocator locator, long value, boolean valueSet)
	{
		return copy(locator, value);
	}

	@Override
	public short copy(ObjectLocator locator, short value, boolean valueSet)
	{
		return copy(locator, value);
	}

	@Override
	public Object copy(ObjectLocator locator, Object value, boolean valueSet)
	{
		return copy(locator, value);
	}

	@Override
	public boolean[] copy(ObjectLocator locator, boolean[] value, boolean valueSet)
	{
		return copy(locator, value);
	}

	@Override
	public byte[] copy(ObjectLocator locator, byte[] value, boolean valueSet)
	{
		return copy(locator, value);
	}

	@Override
	public char[] copy(ObjectLocator locator, char[] value, boolean valueSet)
	{
		return copy(locator, value);
	}

	@Override
	public double[] copy(ObjectLocator locator, double[] value, boolean valueSet)
	{
		return copy(locator, value);
	}

	@Override
	public float[] copy(ObjectLocator locator, float[] value, boolean valueSet)
	{
		return copy(locator, value);
	}

	@Override
	public int[] copy(ObjectLocator locator, int[] value, boolean valueSet)
	{
		return copy(locator, value);
	}

	@Override
	public long[] copy(ObjectLocator locator, long[] value, boolean valueSet)
	{
		return copy(locator, value);
	}

	@Override
	public short[] copy(ObjectLocator locator, short[] value, boolean valueSet)
	{
		return copy(locator, value);
	}

	@Override
	public Object[] copy(ObjectLocator locator, Object[] value, boolean valueSet)
	{
		return copy(locator, value);
	}
}
