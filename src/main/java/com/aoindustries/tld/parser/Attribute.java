/*
 * ao-tld-parser - Parses JSP tag library *.tld files.
 * Copyright (C) 2017, 2019, 2020  AO Industries, Inc.
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

import com.aoindustries.collections.AoCollections;
import com.aoindustries.xml.XmlUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Element;

/**
 * Models one attribute within a tag within the *.tld file.
 * <p>
 * See <a href="https://docs.oracle.com/cd/E19879-01/819-3669/bnani/index.html">Declaring Tag Attributes for Tag Handlers (The Java EE 5 Tutorial)</a>.
 * </p>
 * <p>
 * TLD files may provide generics within special comments inside the XML, where the value must match
 * <code>type</code>, but with the addition of {@code <…>} segments.
 * </p>
 * <pre>&lt;!-- type = "…" --&gt;</pre>
 */
public class Attribute {

	private final Tag tag;
	private final List<String> descriptions;
	private final String name;
	private final boolean required;
	private final boolean rtexprvalue;
	private final boolean fragment;
	private final String type;
	private final DeferredMethod deferredMethod;
	private final DeferredValue deferredValue;

	private final String descriptionSummary;

	private final static Pattern TYPE_PATTERN = Pattern.compile(XmlHelper.PATTERN_PRE + "type" + XmlHelper.PATTERN_POST);

	public Attribute(
		String summaryClass,
		Tag tag,
		Element attributeElem
	) throws XPathExpressionException {
		this.tag = tag;

		List<String> newDescriptions = new ArrayList<>();
		for(Element descriptionElem : XmlUtils.iterableChildElementsByTagName(attributeElem, "description")) {
			newDescriptions.add(descriptionElem.getTextContent());
		}
		this.descriptions = AoCollections.optimalUnmodifiableList(newDescriptions);

		this.name = XmlUtils.getChildTextContent(attributeElem, "name");
		this.required = Boolean.parseBoolean(XmlUtils.getChildTextContent(attributeElem, "required"));
		this.rtexprvalue = Boolean.parseBoolean(XmlUtils.getChildTextContent(attributeElem, "rtexprvalue"));
		this.fragment = Boolean.parseBoolean(XmlUtils.getChildTextContent(attributeElem, "fragment"));
		this.type = XmlHelper.getChildWithGenerics(attributeElem, "type", TYPE_PATTERN, "type");

		Element deferredMethodElem = XmlUtils.getChildElementByTagName(attributeElem, "deferred-method");
		this.deferredMethod = deferredMethodElem == null ? null : new DeferredMethod(this, deferredMethodElem);

		Element deferredValueElem = XmlUtils.getChildElementByTagName(attributeElem, "deferred-value");
		this.deferredValue = deferredValueElem == null ? null : new DeferredValue(this, deferredValueElem);

		try {
			this.descriptionSummary = descriptions.isEmpty() ? null : HtmlSnippet.getSummary(summaryClass, descriptions.get(0));
		} catch(XPathExpressionException e) {
			XPathExpressionException wrapped = new XPathExpressionException(tag.getTaglib().getTldPath() + "/" + tag.getName() + "/" + name + "/description: " + e.getMessage());
			wrapped.initCause(e);
			throw wrapped;
		}
	}

	public Tag getTag() {
		return tag;
	}

	public List<String> getDescriptions() {
		return descriptions;
	}

	public String getName() {
		return name;
	}

	public boolean getRequired() {
		return required;
	}

	public boolean getRtexprvalue() {
		return rtexprvalue;
	}

	public boolean getFragment() {
		return fragment;
	}

	public String getType() {
		return type;
	}

	public DeferredMethod getDeferredMethod() {
		return deferredMethod;
	}

	public DeferredValue getDeferredValue() {
		return deferredValue;
	}

	/**
	 * Gets a summary of the description.
	 * If there is more than once description, only the first is used in generating the summary.
	 * If there are no descriptions, returns {@code null}.
	 *
	 * @see  HtmlSnippet#getSummary(java.lang.String, java.lang.String)
	 */
	public String getDescriptionSummary() {
		return descriptionSummary;
	}
}
