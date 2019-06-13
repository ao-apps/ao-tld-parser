/*
 * ao-tld-parser - Parses JSP tag library *.tld files.
 * Copyright (C) 2017, 2019  AO Industries, Inc.
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
package com.aoindustries.tld.parser;

import com.aoindustries.xml.XmlUtils;
import org.w3c.dom.Element;

/**
 * A *.tld file is parsed entirely on start-up to maximize runtime performance.
 */
public class DeferredMethod {

	private final Attribute attribute;
	private final String methodSignature;

	public DeferredMethod(
		Attribute attribute,
		Element deferredMethodElem
	) {
		this.attribute = attribute;
		this.methodSignature = XmlUtils.getChildTextContent(deferredMethodElem, "method-signature");
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public String getMethodSignature() {
		return methodSignature;
	}
}
