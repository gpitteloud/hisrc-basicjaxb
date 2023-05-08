package org.jvnet.basicjaxb.lang.tests;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.IdentityHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.jvnet.basicjaxb.lang.CopyStrategy;
import org.jvnet.basicjaxb.lang.CopyTo;
import org.jvnet.basicjaxb.lang.JAXBCopyStrategy;
import org.jvnet.basicjaxb.locator.DefaultRootObjectLocator;
import org.jvnet.basicjaxb.locator.ObjectLocator;
import org.jvnet.basicjaxb.locator.util.LocatorUtils;

public class CyclicTests
{
	public interface CopyToInstance extends CopyTo
	{
	}

	public static class A implements CopyToInstance
	{
		public B b;

		@Override
		public Object createNewInstance()
		{
			return new A();
		}

		@Override
		public Object copyTo(Object target)
		{
			ObjectLocator thisLocator = null;
			JAXBCopyStrategy copyStrategy = JAXBCopyStrategy.getInstance();
	        if ( copyStrategy.isDebugEnabled() )
	            thisLocator = new DefaultRootObjectLocator(this);
	        return copyTo(thisLocator, target, copyStrategy);
		}

		@Override
		public Object copyTo(ObjectLocator locator, Object target, CopyStrategy copyStrategy)
		{
			final A that = (A) target;
			that.b = (B) copyStrategy.copy(LocatorUtils.property(locator, "b", this.b), this.b, this.b != null);
			return that;
		}
		
		@Override
		public Object clone()
		{
			return copyTo(createNewInstance());
		}
	}

	public static class B implements CopyToInstance
	{
		public A a;

		@Override
		public Object createNewInstance()
		{
			return new B();
		}

		@Override
		public Object copyTo(Object target)
		{
			ObjectLocator thisLocator = null;
			JAXBCopyStrategy copyStrategy = JAXBCopyStrategy.getInstance();
	        if ( copyStrategy.isDebugEnabled() )
	            thisLocator = new DefaultRootObjectLocator(this);
	        return copyTo(thisLocator, target, copyStrategy);
		}

		@Override
		public Object copyTo(ObjectLocator locator, Object target, CopyStrategy copyStrategy)
		{
			final B that = (B) target;
			that.a = (A) copyStrategy.copy(LocatorUtils.property(locator, "a", this.a), this.a, this.a != null);
			return that;
		}
		
		@Override
		public Object clone()
		{
			return copyTo(createNewInstance());
		}
	}

	@Test
	public void testCycle() throws Exception
	{
		final A a = new A();
		final B b = new B();
		a.b = b;
		b.a = a;
		final A a1 = (A) new JAXBCopyStrategy()
		{
			private Map<Object, Object> copies = new IdentityHashMap<Object, Object>();

			@Override
			public Object copy(ObjectLocator locator, Object object, boolean objectSet)
			{
				final Object existingCopy = copies.get(object);
				if (existingCopy != null)
					return observe(locator, existingCopy);
				else
				{
					if (object instanceof CopyToInstance)
					{
						final CopyToInstance source = (CopyToInstance) object;
						final Object newCopy = source.createNewInstance();
						copies.put(object, newCopy);
						source.copyTo(locator, newCopy, this);
						return newCopy;
					}
					else
					{
						final Object newCopy = super.copy(locator, object, objectSet);
						copies.put(object, newCopy);
						return newCopy;
					}
				}
			}
		}.copy(new DefaultRootObjectLocator(a), a, true);
		
		assertSame(a1.b.a.b, a1.b);
		
		// BUG: See https://github.com/highsource/jaxb2-basics/issues/92
		// assertSame(a1.b.a, a1);
	}
}
