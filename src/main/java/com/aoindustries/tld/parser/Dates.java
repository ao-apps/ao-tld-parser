/*
 * ao-tld-parser - Parses JSP tag library *.tld files.
 * Copyright (C) 2019  AO Industries, Inc.
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.time.DateTime;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * TLD files may provide dates within special comments inside the XML.
 * <ol>
 * <li><code>&lt;!-- dateCreated = "<var><a href="https://www.iso.org/iso-8601-date-and-time-format.html">ISO 8601</a></var>" --&gt;</code>: {@link #getCreated()}</li>
 * <li><code>&lt;!-- datePublished = "<var><a href="https://www.iso.org/iso-8601-date-and-time-format.html">ISO 8601</a></var>" --&gt;</code>: {@link #getPublished()}</li>
 * <li><code>&lt;!-- dateModified = "<var><a href="https://www.iso.org/iso-8601-date-and-time-format.html">ISO 8601</a></var>" --&gt;</code>: {@link #getModified()}</li>
 * <li><code>&lt;!-- dateReviewed = "<var><a href="https://www.iso.org/iso-8601-date-and-time-format.html">ISO 8601</a></var>" --&gt;</code>: {@link #getReviewed()}</li>
 * </ol>
 */
public class Dates {

	private static final String DATE_CREATED   = "dateCreated";
	private static final String DATE_PUBLISHED = "datePublished";
	private static final String DATE_MODIFIED  = "dateModified";
	private static final String DATE_REVIEWED  = "dateReviewed";

	private static final String PATTERN_PRE  = "^\\s*";
	private static final String PATTERN_POST = "\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)')\\s*$";

	/**
	 * A constant for a set of unknown dates.
	 */
	public static final Dates UNKNOWN = new Dates(null, null, null, null);

	public static Dates valueOf(
		DateTime created,
		DateTime published,
		DateTime modified,
		DateTime reviewed
	) {
		if(created == null && published == null && modified == null && reviewed == null) return UNKNOWN;
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
	 * @param defaultDates  The optional default dates for when comments not found
	 */
	public static Dates fromComments(Element elem, Dates defaultDates) {
		DateTime created   = parseComments(elem, DATE_CREATED_PATTERN,   DATE_CREATED);
		DateTime published = parseComments(elem, DATE_PUBLISHED_PATTERN, DATE_PUBLISHED);
		DateTime modified  = parseComments(elem, DATE_MODIFIED_PATTERN,  DATE_MODIFIED);
		DateTime reviewed  = parseComments(elem, DATE_REVIEWED_PATTERN,  DATE_REVIEWED);
		// Use defaults as-needed
		if(defaultDates != null) {
			if(created   == null) created   = defaultDates.getCreated();
			if(published == null) published = defaultDates.getPublished();
			if(modified  == null) modified  = defaultDates.getModified();
			if(reviewed  == null) reviewed  = defaultDates.getReviewed();
		}
		return valueOf(
			created,
			published,
			modified,
			reviewed
		);
	}

	final static Pattern DATE_CREATED_PATTERN   = Pattern.compile(PATTERN_PRE + DATE_CREATED   + PATTERN_POST);
	final static Pattern DATE_PUBLISHED_PATTERN = Pattern.compile(PATTERN_PRE + DATE_PUBLISHED + PATTERN_POST);
	final static Pattern DATE_MODIFIED_PATTERN  = Pattern.compile(PATTERN_PRE + DATE_MODIFIED  + PATTERN_POST);
	final static Pattern DATE_REVIEWED_PATTERN  = Pattern.compile(PATTERN_PRE + DATE_REVIEWED  + PATTERN_POST);

	/**
	 * Parse dates from special comments directly within a given {@link Element}.
	 */
	private static DateTime parseComments(Element elem, Pattern pattern, String field) {
		NodeList children = elem.getChildNodes();
		DateTime date = null;
		for(int i = 0, len = children.getLength(); i < len; i++) {
			Node child = children.item(i);
			if(child instanceof Comment) {
				Comment comment = (Comment)child;
				Matcher matcher = pattern.matcher(comment.getData());
				while(matcher.find()) {
					String doubleQuoted = matcher.group(1);
					String singleQuoted = matcher.group(2);
					String value;
					if(doubleQuoted != null) {
						if(singleQuoted != null) throw new IllegalArgumentException(field + ": Found both in double quotes (\") and single quotes ('): " + matcher.group());
						value = doubleQuoted;
					} else {
						assert singleQuoted != null : "At least one of the two capturing groups must have matched";
						value = singleQuoted;
					}
					DateTime newDate = new DateTime(value);
					if(date != null) {
						throw new IllegalArgumentException(field + ": More than one value found: \"" + date + "\" and \"" + newDate + "'");
					}
					date = newDate;
				}
			}
		}
		return date;
	}

	private final DateTime created;
	private final DateTime published;
	private final DateTime modified;
	private final DateTime reviewed;

	private Dates(
		DateTime created,
		DateTime published,
		DateTime modified,
		DateTime reviewed
	) {
		this.created   = created;
		this.published = published;
		this.modified  = modified;
		this.reviewed  = reviewed;
	}

	/**
	 * <a href="https://schema.org/dateCreated">https://schema.org/dateCreated</a>
	 *
	 * @see  #getPublished()  When created and published are the same date, prefer
	 *                            published because it seems to have more use overall than created.
	 */
	// Matches com.semanticcms.core.model.Page.getDateCreated()
	public DateTime getCreated() {
		return created;
	}

	/**
	 * <a href="https://schema.org/datePublished">https://schema.org/datePublished</a>
	 *
	 * @see  #getCreated()  When created and published are the same date, prefer
	 *                          published because it seems to have more use overall than created.
	 */
	// Matches com.semanticcms.core.model.Page.getDatePublished()
	public DateTime getPublished() {
		return published;
	}

	/**
	 * <a href="https://schema.org/dateModified">https://schema.org/dateModified</a>
	 */
	// Matches com.semanticcms.core.model.Page.getDateModified()
	public DateTime getModified() {
		return modified;
	}

	/**
	 * This has no equivalent in <a href="https://schema.org/">https://schema.org/</a>, however
	 * we feel it is important to actively review content to ensure its accuracy, even when it
	 * has not been modified.
	 */
	// Matches com.semanticcms.core.model.Page.getDateReviewed()
	public DateTime getReviewed() {
		return reviewed;
	}

	/**
	 * Checks that one date is not before another date.
	 *
	 * @throws IllegalArgumentException when the date is before the other date
	 */
	private static void checkNotBefore(String field1, DateTime dt1, String field2, DateTime dt2) throws IllegalArgumentException {
		if(dt1 != null && dt2 != null && dt1.compareTo(dt2) < 0) throw new IllegalArgumentException(field1 + " < " + field2 + ": " + dt1 + " < " + dt2);
	}

	/**
	 * Checks that this set of dates is not before the given set of dates.
	 * Compares both {@link #getCreated()} and {@link #getPublished()}.
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
	private static DateTime older(DateTime dt1, DateTime dt2) {
		if(dt1 == null || dt2 == null) return null;
		return dt1.compareTo(dt2) <= 0 ? dt1 : dt2;
	}

	/**
	 * Gets the newer of two dates or {@code null} when either date is {@code null}.
	 */
	private static DateTime newer(DateTime dt1, DateTime dt2) {
		if(dt1 == null || dt2 == null) return null;
		return dt1.compareTo(dt2) >= 0 ? dt1 : dt2;
	}

	/**
	 * Merges one date with another date, taking the older created, older published, newer modified, and older reviewed.
	 * <p>
	 * If both dates are {@code null}, {@code null} is returned.
	 * If only one dates is {@code null}, the non-{@code null} is returned.
	 * </p>
	 * <p>
	 * If the individual field of either is {@code null}, the field result is {@code null}.
	 * This results in an unknown date being propagates as {@code null}.
	 * </p>
	 */
	static Dates merge(Dates d1, Dates d2) {
		if(d1 == null && d2 == null) return null;
		if(d1 == null) return d2;
		if(d2 == null) return d1;
		return valueOf(
			older(d1.created,   d2.created),
			older(d1.published, d2.published),
			newer(d1.modified,  d2.modified),
			older(d1.reviewed,  d2.reviewed)
		);
	}
}
