/*
 * ao-tld-parser - Parses JSP tag library *.tld files.
 * Copyright (C) 2019, 2020, 2021, 2022, 2024, 2025  AO Industries, Inc.
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

import com.aoapps.collections.AoArrays;
import java.time.ZonedDateTime;
import java.util.regex.Pattern;
import org.w3c.dom.Element;

/**
 * TLD files may provide dates within special comments inside the XML.
 * <ol>
 * <li><code>&lt;!-- dateCreated = "<var><a href="https://www.iso.org/iso-8601-date-and-time-format.html">ISO 8601</a></var>" --&gt;</code>: {@link Dates#getCreated()}</li>
 * <li><code>&lt;!-- datePublished = "<var><a href="https://www.iso.org/iso-8601-date-and-time-format.html">ISO 8601</a></var>" --&gt;</code>: {@link Dates#getPublished()}</li>
 * <li><code>&lt;!-- dateModified = "<var><a href="https://www.iso.org/iso-8601-date-and-time-format.html">ISO 8601</a></var>" --&gt;</code>: {@link Dates#getModified()}</li>
 * <li><code>&lt;!-- dateReviewed = "<var><a href="https://www.iso.org/iso-8601-date-and-time-format.html">ISO 8601</a></var>" --&gt;</code>: {@link Dates#getReviewed()}</li>
 * </ol>
 */
public class Dates {

  private static final String DATE_CREATED   = "dateCreated";
  private static final String DATE_PUBLISHED = "datePublished";
  private static final String DATE_MODIFIED  = "dateModified";
  private static final String DATE_REVIEWED  = "dateReviewed";

  /**
   * A constant for a set of unknown dates.
   */
  public static final Dates UNKNOWN = new Dates(null, null, null, null);

  /**
   * Gets or creates an instance of {@link Dates}.
   */
  public static Dates valueOf(
      ZonedDateTime created,
      ZonedDateTime published,
      ZonedDateTime modified,
      ZonedDateTime reviewed
  ) {
    if (created == null && published == null && modified == null && reviewed == null) {
      return UNKNOWN;
    }
    return new Dates(
        created,
        published,
        modified,
        reviewed
    );
  }

  /**
   * Parses dates from within direct child comments of the given element.
   *
   * @param elem  The element to find direct child comments of
   * @param defaultDates  The optional default dates for when no date-comments found
   */
  public static Dates fromComments(Element elem, Dates defaultDates) {
    ZonedDateTime created   = parseComments(elem, DATE_CREATED_PATTERN,   DATE_CREATED);
    ZonedDateTime published = parseComments(elem, DATE_PUBLISHED_PATTERN, DATE_PUBLISHED);
    ZonedDateTime modified  = parseComments(elem, DATE_MODIFIED_PATTERN,  DATE_MODIFIED);
    ZonedDateTime reviewed  = parseComments(elem, DATE_REVIEWED_PATTERN,  DATE_REVIEWED);
    // Use defaults when no date-comments found
    if (
        defaultDates != null
            && created   == null
            && published == null
            && modified  == null
            && reviewed  == null
    ) {
      return defaultDates;
    }
    return valueOf(
        created,
        published,
        modified,
        reviewed
    );
  }

  static final Pattern DATE_CREATED_PATTERN   = Pattern.compile(XmlHelper.PATTERN_PRE + DATE_CREATED   + XmlHelper.PATTERN_POST);
  static final Pattern DATE_PUBLISHED_PATTERN = Pattern.compile(XmlHelper.PATTERN_PRE + DATE_PUBLISHED + XmlHelper.PATTERN_POST);
  static final Pattern DATE_MODIFIED_PATTERN  = Pattern.compile(XmlHelper.PATTERN_PRE + DATE_MODIFIED  + XmlHelper.PATTERN_POST);
  static final Pattern DATE_REVIEWED_PATTERN  = Pattern.compile(XmlHelper.PATTERN_PRE + DATE_REVIEWED  + XmlHelper.PATTERN_POST);

  /**
   * Parse dates from special comments directly within a given {@link Element}.
   */
  private static ZonedDateTime parseComments(Element elem, Pattern pattern, String varName) {
    String value = XmlHelper.getVariable(elem, pattern, varName);
    return (value == null) ? null : ZonedDateTime.parse(value);
  }

  private final ZonedDateTime created;
  private final ZonedDateTime published;
  private final ZonedDateTime modified;
  private final ZonedDateTime reviewed;

  private Dates(
      ZonedDateTime created,
      ZonedDateTime published,
      ZonedDateTime modified,
      ZonedDateTime reviewed
  ) {
    this.created   = created;
    this.published = published;
    this.modified  = modified;
    this.reviewed  = reviewed;
  }

  /**
   * <a href="https://schema.org/dateCreated">https://schema.org/dateCreated</a>.
   *
   * @see  Dates#getPublished()  When created and published are the same date, prefer
   *                            published because it seems to have more use overall than created.
   */
  // Matches com.semanticcms.core.model.Page.getDateCreated()
  public ZonedDateTime getCreated() {
    return created;
  }

  /**
   * <a href="https://schema.org/datePublished">https://schema.org/datePublished</a>.
   *
   * @see  Dates#getCreated()  When created and published are the same date, prefer
   *                          published because it seems to have more use overall than created.
   */
  // Matches com.semanticcms.core.model.Page.getDatePublished()
  public ZonedDateTime getPublished() {
    return published;
  }

  /**
   * <a href="https://schema.org/dateModified">https://schema.org/dateModified</a>.
   */
  // Matches com.semanticcms.core.model.Page.getDateModified()
  public ZonedDateTime getModified() {
    return modified;
  }

  /**
   * This has no equivalent in <a href="https://schema.org/">https://schema.org/</a>, however
   * we feel it is important to actively review content to ensure its accuracy, even when it
   * has not been modified.
   */
  // Matches com.semanticcms.core.model.Page.getDateReviewed()
  public ZonedDateTime getReviewed() {
    return reviewed;
  }

  /**
   * Checks that one date is not before another date.
   *
   * @throws IllegalArgumentException when the date is before the other date
   */
  private static void checkNotBefore(String field1, ZonedDateTime dt1, String field2, ZonedDateTime dt2) throws IllegalArgumentException {
    if (dt1 != null && dt2 != null && dt1.compareTo(dt2) < 0) {
      throw new IllegalArgumentException(field1 + " < " + field2 + ": " + dt1 + " < " + dt2);
    }
  }

  /**
   * Checks that this set of dates is not before the given set of dates.
   * Compares both {@link Dates#getCreated()} and {@link Dates#getPublished()}.
   *
   * @throws IllegalArgumentException when any field of this is before the same field of the given set of dates.
   */
  void checkNotBefore(String fields, String otherFields, Dates other) throws IllegalArgumentException {
    checkNotBefore(fields + '/' + DATE_CREATED,   getCreated(),   otherFields + '/' + DATE_CREATED,   other.getCreated());
    checkNotBefore(fields + '/' + DATE_PUBLISHED, getPublished(), otherFields + '/' + DATE_PUBLISHED, other.getPublished());
  }

  /**
   * Gets the older of two dates or {@code null} when either date is {@code null}.
   */
  private static ZonedDateTime older(ZonedDateTime dt1, ZonedDateTime dt2) {
    if (dt1 == null || dt2 == null) {
      return null;
    }
    return dt1.compareTo(dt2) <= 0 ? dt1 : dt2;
  }

  /**
   * Gets the newer of two dates or {@code null} when either date is {@code null}.
   */
  private static ZonedDateTime newer(ZonedDateTime dt1, ZonedDateTime dt2) {
    if (dt1 == null || dt2 == null) {
      return null;
    }
    return dt1.compareTo(dt2) >= 0 ? dt1 : dt2;
  }

  /**
   * Merges one date with another date, taking the older created, older published, newer modified, and older reviewed.
   *
   * <p>If both dates are {@code null}, {@code null} is returned.
   * If only one dates is {@code null}, the non-{@code null} is returned.</p>
   *
   * <p>If the individual field of either is {@code null}, the field result is {@code null}.
   * This results in an unknown date being propagates as {@code null}.</p>
   */
  static Dates merge(Dates d1, Dates d2) {
    if (d1 == null && d2 == null) {
      return null;
    }
    if (d1 == null) {
      return d2;
    }
    if (d2 == null) {
      return d1;
    }
    ZonedDateTime created1   = d1.getCreated();
    ZonedDateTime created2   = d2.getCreated();
    ZonedDateTime published1 = d1.getPublished();
    ZonedDateTime published2 = d2.getPublished();
    ZonedDateTime modified1  = d1.getModified();
    ZonedDateTime modified2  = d2.getModified();
    if (modified1 != null || modified2 != null) {
      if (modified1 == null) {
        modified1 = AoArrays.maxNonNull(
            created1,
            published1
        );
      }
      if (modified2 == null) {
        modified2 = AoArrays.maxNonNull(
            created2,
            published2
        );
      }
    }
    return valueOf(
        older(created1,   created2),
        older(published1, published2),
        newer(modified1,  modified2),
        older(d1.reviewed,  d2.reviewed)
    );
  }
}
