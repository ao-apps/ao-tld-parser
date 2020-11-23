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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Models one function within the *.tld file.
 * <p>
 * TLD files may provide generics within special comments inside the XML, where the value must match
 * <code>function-signature</code>, but with the addition of {@code <…>} segments.
 * </p>
 * <pre>&lt;!-- functionSignature = "…" --&gt;</pre>
 */
public class Function {

	private final Taglib taglib;
	private final Dates dates;
	private final Boolean allowRobots;
	private final List<String> descriptions;
	private final List<String> displayNames;
	private final String name;
	private final String functionClass;
	private final String functionSignature;
	private final String example;

	private final String descriptionSummary;

	private final static Pattern FUNCTION_SIGNATURE_PATTERN = Pattern.compile(XmlHelper.PATTERN_PRE + "functionSignature" + XmlHelper.PATTERN_POST);

	public Function(
		String summaryClass,
		Taglib taglib,
		Element functionElem
	) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		this.taglib = taglib;

		this.name = XmlUtils.getChildTextContent(functionElem, "name");

		this.dates = Dates.fromComments(functionElem, taglib.getDates());
		this.dates.checkNotBefore(taglib.getTldPath() + "/" + name, taglib.getTldPath(), taglib.getDates());

		this.allowRobots = XmlHelper.parseAllowRobots(functionElem);

		List<String> newDescriptions = new ArrayList<>();
		for(Element descriptionElem : XmlUtils.iterableChildElementsByTagName(functionElem, "description")) {
			newDescriptions.add(descriptionElem.getTextContent());
		}
		this.descriptions = AoCollections.optimalUnmodifiableList(newDescriptions);

		List<String> newDisplayNames = new ArrayList<>();
		for(Element displayNameElem : XmlUtils.iterableChildElementsByTagName(functionElem, "display-name")) {
			newDisplayNames.add(displayNameElem.getTextContent());
		}
		this.displayNames = AoCollections.optimalUnmodifiableList(newDisplayNames);

		this.functionClass = XmlUtils.getChildTextContent(functionElem, "function-class");
		this.functionSignature = XmlHelper.getChildWithGenerics(functionElem, "function-signature", FUNCTION_SIGNATURE_PATTERN, "functionSignature");
		this.example = XmlUtils.getChildTextContent(functionElem, "example");

		try {
			this.descriptionSummary = descriptions.isEmpty() ? null : HtmlSnippet.getSummary(summaryClass, descriptions.get(0));
		} catch(XPathExpressionException e) {
			XPathExpressionException wrapped = new XPathExpressionException(taglib.getTldPath() + "/" + name + "/description: " + e.getMessage());
			wrapped.initCause(e);
			throw wrapped;
		}
	}

	/**
	 * @deprecated  {@code apiLinks} is unused, please use {@link #Function(java.lang.String, com.aoindustries.tld.parser.Taglib, org.w3c.dom.Element)} instead.
	 */
	@Deprecated
	public Function(
		String summaryClass,
		Taglib taglib,
		Element functionElem,
		Map<String,String> apiLinks
	) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		this(summaryClass, taglib, functionElem);
	}

	public Taglib getTaglib() {
		return taglib;
	}

	public Dates getDates() {
		return dates;
	}

	public Boolean getAllowRobots() {
		return allowRobots;
	}

	@SuppressWarnings("ReturnOfCollectionOrArrayField") // Returning unmodifiable
	public List<String> getDescriptions() {
		return descriptions;
	}

	@SuppressWarnings("ReturnOfCollectionOrArrayField") // Returning unmodifiable
	public List<String> getDisplayNames() {
		return displayNames;
	}

	public String getName() {
		return name;
	}

	public String getFunctionClass() {
		return functionClass;
	}

	public String getFunctionSignature() {
		return functionSignature;
	}

	public String getExample() {
		return example;
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
