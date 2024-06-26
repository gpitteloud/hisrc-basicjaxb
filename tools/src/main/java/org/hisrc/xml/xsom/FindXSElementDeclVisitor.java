package org.hisrc.xml.xsom;

import static java.util.Objects.requireNonNull;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.visitor.XSVisitor;

public class FindXSElementDeclVisitor implements XSVisitor {

	private final QName name;

	private XSElementDecl elementDecl = null;

	public FindXSElementDeclVisitor(final QName name) {
		requireNonNull(name);
		this.name = name;
	}

	public XSElementDecl getElementDecl() {
		return elementDecl;
	}

	@Override
	public void annotation(XSAnnotation ann) {
	}

	@Override
	public void attGroupDecl(XSAttGroupDecl decl) {
	}

	@Override
	public void attributeDecl(XSAttributeDecl decl) {
	}

	@Override
	public void attributeUse(XSAttributeUse use) {
	}

	@Override
	public void complexType(XSComplexType type) {
	}

	@Override
	public void facet(XSFacet facet) {
	}

	@Override
	public void identityConstraint(XSIdentityConstraint decl) {
	}

	@Override
	public void notation(XSNotation notation) {
	}

	@Override
	public void schema(XSSchema schema) {
	}

	@Override
	public void xpath(XSXPath xp) {
	}

	@Override
	public void elementDecl(XSElementDecl decl) {
		final QName declName = StringUtils.isEmpty(decl.getTargetNamespace()) ? new QName(
				decl.getName()) : new QName(decl.getTargetNamespace(),
				decl.getName());
		if (this.name.equals(declName)) {
			this.elementDecl = decl;
		}
	}

	@Override
	public void modelGroup(XSModelGroup group) {
		for (XSParticle child : group.getChildren()) {
			child.visit(this);
		}
	}

	@Override
	public void modelGroupDecl(XSModelGroupDecl decl) {
		decl.getModelGroup().visit(this);
	}

	@Override
	public void wildcard(XSWildcard wc) {
	}

	@Override
	public void empty(XSContentType empty) {
	}

	@Override
	public void particle(XSParticle particle) {
		particle.getTerm().visit(this);
	}

	@Override
	public void simpleType(XSSimpleType simpleType) {
	}
}
