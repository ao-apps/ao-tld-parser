/*
 * ao-tld-parser - Parses JSP tag library *.tld files.
 * Copyright (C) 2017, 2019, 2020, 2021  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-tld-parser.
 *
 * ao-tld-parser is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-tld-parser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-tld-parser.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoapps.tldparser;

import java.util.regex.Pattern;
import org.w3c.dom.Element;

/**
 * Models <a href="https://docs.oracle.com/cd/E19575-01/819-3669/bnahr/index.html">deferred methods</a>.
 * <p>
 * TLD files may provide generics within special comments inside the XML, where the value must match
 * <code>method-signature</code>, but with the addition of {@code <…>} segments.
 * </p>
 * <pre>&lt;!-- methodSignature = "…" --&gt;</pre>
 */
public class DeferredMethod {

	private final Attribute attribute;
	private final String methodSignature;

	private static final Pattern METHOD_SIGNATURE_PATTERN = Pattern.compile(XmlHelper.PATTERN_PRE + "methodSignature" + XmlHelper.PATTERN_POST);

	public DeferredMethod(
		Attribute attribute,
		Element deferredMethodElem
	) {
		this.attribute = attribute;
		this.methodSignature = XmlHelper.getChildWithGenerics(deferredMethodElem, "method-signature", METHOD_SIGNATURE_PATTERN, "methodSignature");
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public String getMethodSignature() {
		return methodSignature;
	}
}
