/*
 * ao-tld-parser - Parses JSP tag library *.tld files.
 * Copyright (C) 2017, 2019, 2020, 2021, 2022  AO Industries, Inc.
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
 * along with ao-tld-parser.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aoapps.tldparser;

import com.aoapps.collections.AoCollections;
import com.aoapps.lang.xml.XmlUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.lang3.NotImplementedException;
import org.w3c.dom.Element;

/**
 * Models one tag within the *.tld file.
 */
public class Tag {

  private final Taglib taglib;
  private final Dates dates;
  private final Boolean allowRobots;
  private final List<String> descriptions;
  private final List<String> displayNames;
  private final String name;
  private final String tagClass;
  private final String teiClass;
  private final String bodyContent;
  private final Map<String, Attribute> attribute;
  private final List<Attribute> attributes;
  private final boolean dynamicAttributes;
  // TODO: Variables
  private final String example;

  private final String descriptionSummary;

  /**
   * Creates a new tag.
   */
  public Tag(
      String summaryClass,
      Taglib taglib,
      Element tagElem
  ) throws XPathExpressionException {
    this.taglib = taglib;

    this.name = XmlUtils.getChildTextContent(tagElem, "name");

    this.dates = Dates.fromComments(tagElem, taglib.getDates());
    this.dates.checkNotBefore(taglib.getTldPath() + "/" + name, taglib.getTldPath(), taglib.getDates());

    this.allowRobots = XmlHelper.parseAllowRobots(tagElem);

    List<String> newDescriptions = new ArrayList<>();
    for (Element descriptionElem : XmlUtils.iterableChildElementsByTagName(tagElem, "description")) {
      newDescriptions.add(descriptionElem.getTextContent());
    }
    this.descriptions = AoCollections.optimalUnmodifiableList(newDescriptions);

    List<String> newDisplayNames = new ArrayList<>();
    for (Element displayNameElem : XmlUtils.iterableChildElementsByTagName(tagElem, "display-name")) {
      newDisplayNames.add(displayNameElem.getTextContent());
    }
    this.displayNames = AoCollections.optimalUnmodifiableList(newDisplayNames);

    this.tagClass = XmlUtils.getChildTextContent(tagElem, "tag-class");
    this.teiClass = XmlUtils.getChildTextContent(tagElem, "tei-class");
    this.bodyContent = XmlUtils.getChildTextContent(tagElem, "body-content");

    Map<String, Attribute> newAttributes = new LinkedHashMap<>();
    for (Element attributeElem : XmlUtils.iterableChildElementsByTagName(tagElem, "attribute")) {
      Attribute newAttribute = new Attribute(summaryClass, this, attributeElem);
      String attributeName = newAttribute.getName();
      if (newAttributes.put(attributeName, newAttribute) != null) {
        throw new IllegalArgumentException("Duplicate attribute name: " + attributeName);
      }
    }
    this.attribute = AoCollections.optimalUnmodifiableMap(newAttributes);
    this.attributes = AoCollections.optimalUnmodifiableList(new ArrayList<>(newAttributes.values()));

    this.dynamicAttributes = Boolean.parseBoolean(XmlUtils.getChildTextContent(tagElem, "dynamic-attributes"));
    if (XmlUtils.iterableChildElementsByTagName(tagElem, "variable").iterator().hasNext()) {
      throw new NotImplementedException("TODO: Document variables when first needed.  We don't use any variables at this time.");
    }
    this.example = XmlUtils.getChildTextContent(tagElem, "example");

    try {
      this.descriptionSummary = descriptions.isEmpty() ? null : HtmlSnippet.getSummary(summaryClass, descriptions.get(0));
    } catch (XPathExpressionException e) {
      XPathExpressionException wrapped = new XPathExpressionException(taglib.getTldPath() + "/" + name + "/description: " + e.getMessage());
      wrapped.initCause(e);
      throw wrapped;
    }
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

  public String getTagClass() {
    return tagClass;
  }

  public String getTeiClass() {
    return teiClass;
  }

  public String getBodyContent() {
    return bodyContent;
  }

  @SuppressWarnings("ReturnOfCollectionOrArrayField") // Returning unmodifiable
  public Map<String, Attribute> getAttribute() {
    return attribute;
  }

  @SuppressWarnings("ReturnOfCollectionOrArrayField") // Returning unmodifiable
  public List<Attribute> getAttributes() {
    return attributes;
  }

  public boolean getDynamicAttributes() {
    return dynamicAttributes;
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
