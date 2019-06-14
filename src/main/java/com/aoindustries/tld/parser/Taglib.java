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

import com.aoindustries.util.AoCollections;
import com.aoindustries.xml.XmlUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Models the *.tld file overall.
 */
public class Taglib {

	private final String tldPath;
	private final Dates dates;
	private final List<String> descriptions;
	private final List<String> displayNames;
	private final String tlibVersion;
	private final String shortName;
	private final String uri;
	private final Map<String,Tag> tag;
	private final List<Tag> tags;
	private final Dates tagsEffectiveDates;
	private final Map<String,Function> function;
	private final List<Function> functions;
	private final Dates functionsEffectiveDates;
	private final Dates taglibEffectiveDates;

	public Taglib(
		String summaryClass,
		String tldPath,
		Dates defaultDates,
		Document tldDoc,
		Map<String,String> apiLinks
	) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		this.tldPath = tldPath;

		Element taglibElem = tldDoc.getDocumentElement();

		this.dates = Dates.fromComments(taglibElem, defaultDates);

		List<String> newDescriptions = new ArrayList<>();
		for(Element descriptionElem : XmlUtils.iterableChildElementsByTagName(taglibElem, "description")) {
			newDescriptions.add(descriptionElem.getTextContent());
		}
		this.descriptions = AoCollections.optimalUnmodifiableList(newDescriptions);

		List<String> newDisplayNames = new ArrayList<>();
		for(Element displayNameElem : XmlUtils.iterableChildElementsByTagName(taglibElem, "display-name")) {
			newDisplayNames.add(displayNameElem.getTextContent());
		}
		this.displayNames = AoCollections.optimalUnmodifiableList(newDisplayNames);

		this.tlibVersion = XmlUtils.getChildTextContent(taglibElem, "tlib-version");
		this.shortName = XmlUtils.getChildTextContent(taglibElem, "short-name");
		this.uri = XmlUtils.getChildTextContent(taglibElem, "uri");

		Map<String,Tag> newTags = new LinkedHashMap<>();
		Dates newTagsEffectiveDates = null;
		for(Element tagElem : XmlUtils.iterableChildElementsByTagName(taglibElem, "tag")) {
			Tag newTag = new Tag(summaryClass, this, tagElem);
			String tagName = newTag.getName();
			if(newTags.put(tagName, newTag) != null) throw new IllegalArgumentException("Duplicate tag name: " + tagName);
			newTagsEffectiveDates = Dates.merge(newTagsEffectiveDates, newTag.getDates());
		}
		this.tag = AoCollections.optimalUnmodifiableMap(newTags);
		this.tags = AoCollections.optimalUnmodifiableList(new ArrayList<>(newTags.values()));
		this.tagsEffectiveDates = newTagsEffectiveDates;

		Map<String,Function> newFunctions = new LinkedHashMap<>();
		Dates newFunctionsEffectiveDates = null;
		for(Element functionElem : XmlUtils.iterableChildElementsByTagName(taglibElem, "function")) {
			Function newFunction = new Function(summaryClass, this, functionElem, apiLinks);
			String functionName = newFunction.getName();
			if(newFunctions.put(functionName, newFunction) != null) throw new IllegalArgumentException("Duplicate function name: " + functionName);
			newFunctionsEffectiveDates = Dates.merge(newFunctionsEffectiveDates, newFunction.getDates());
		}
		this.function = AoCollections.optimalUnmodifiableMap(newFunctions);
		this.functions = AoCollections.optimalUnmodifiableList(new ArrayList<>(newFunctions.values()));
		this.functionsEffectiveDates = newFunctionsEffectiveDates;
		this.taglibEffectiveDates = Dates.merge(
			Dates.merge(this.dates, this.tagsEffectiveDates),
			this.functionsEffectiveDates
		);
	}

	public String getTldPath() {
		return tldPath;
	}

	public Dates getDates() {
		return dates;
	}

	public List<String> getDescriptions() {
		return descriptions;
	}

	public List<String> getDisplayNames() {
		return displayNames;
	}

	public String getTlibVersion() {
		return tlibVersion;
	}

	public String getShortName() {
		return shortName;
	}

	public String getUri() {
		return uri;
	}

	public Map<String,Tag> getTag() {
		return tag;
	}

	public List<Tag> getTags() {
		return tags;
	}

	/**
	 * Gets the effective dates for the all tags.
	 *
	 * @return the effective dates or {@code null} when there are no tags
	 */
	public Dates getTagsEffectiveDates() {
		return tagsEffectiveDates;
	}

	public Map<String,Function> getFunction() {
		return function;
	}

	public List<Function> getFunctions() {
		return functions;
	}

	/**
	 * Gets the effective dates for the all functions.
	 *
	 * @return the effective dates or {@code null} when there are no functions
	 */
	public Dates getFunctionsEffectiveDates() {
		return functionsEffectiveDates;
	}

	/**
	 * Gets the effective dates for the taglib overall, including itself along with all tags and functions.
	 */
	public Dates getTaglibEffectiveDates() {
		return taglibEffectiveDates;
	}
}
