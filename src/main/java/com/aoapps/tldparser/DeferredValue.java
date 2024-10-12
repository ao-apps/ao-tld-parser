/*
 * ao-tld-parser - Parses JSP tag library *.tld files.
 * Copyright (C) 2017, 2019, 2020, 2021, 2022, 2024  AO Industries, Inc.
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

import java.util.regex.Pattern;
import org.w3c.dom.Element;

/**
 * Models <a href="https://docs.oracle.com/cd/E19575-01/819-3669/bnahr/index.html">deferred values</a>.
 * <p>
 * TLD files may provide generics within special comments inside the XML, where the value must match
 * <code>type</code>, but with the addition of {@code <…>} segments.
 * </p>
 * <pre>&lt;!-- type = "…" --&gt;</pre>
 */
public class DeferredValue {

  private final Attribute attribute;
  private final String type;

  private static final Pattern TYPE_PATTERN = Pattern.compile(XmlHelper.PATTERN_PRE + "type" + XmlHelper.PATTERN_POST);

  /**
   * Creates a new {@link DeferredValue}.
   */
  public DeferredValue(
      Attribute attribute,
      Element deferredValueElem
  ) {
    this.attribute = attribute;
    this.type = XmlHelper.getChildWithGenerics(deferredValueElem, "type", TYPE_PATTERN, "type");
  }

  public Attribute getAttribute() {
    return attribute;
  }

  public String getType() {
    return type;
  }
}
