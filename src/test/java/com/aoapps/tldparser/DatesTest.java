/*
 * ao-tld-parser - Parses JSP tag library *.tld files.
 * Copyright (C) 2019, 2021  AO Industries, Inc.
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

import java.util.regex.Matcher;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * @see Dates
 */
public class DatesTest {

	@Test
	public void testNoMatch() {
		assertFalse(Dates.DATE_CREATED_PATTERN.matcher("This has no match").matches());
	}

	@Test
	public void testMustBeStandaloneComment() {
		// Must be in own standalone comment
		assertFalse(Dates.DATE_CREATED_PATTERN.matcher("foo dateCreated=\"AO\"").matches());
		assertFalse(Dates.DATE_CREATED_PATTERN.matcher("dateCreated=\"AO\" bar").matches());
		assertFalse(Dates.DATE_CREATED_PATTERN.matcher("foo dateCreated=\"AO\" bar").matches());
	}

	@Test
	public void testQuoteMismatch() {
		// Also mismatched quote types should be ignored
		assertFalse(Dates.DATE_CREATED_PATTERN.matcher("dateCreated=\"AO'").matches());
		assertFalse(Dates.DATE_CREATED_PATTERN.matcher("dateCreated='AO\"").matches());
	}

	@Test
	public void testDoubleQuotedMatchFullString() {
		Matcher m = Dates.DATE_CREATED_PATTERN.matcher("dateCreated=\"AO\"");
		assertTrue(m.find());
		assertEquals(2, m.groupCount());
		assertEquals("AO", m.group(1));
		assertNull(m.group(2));
		assertFalse(m.find());
	}

	@Test
	public void testSingleQuotedMatchFullString() {
		Matcher m = Dates.DATE_CREATED_PATTERN.matcher("dateCreated='AO'");
		assertTrue(m.find());
		assertEquals(2, m.groupCount());
		assertNull(m.group(1));
		assertEquals("AO", m.group(2));
		assertFalse(m.find());
	}

	@Test
	public void testDoubleQuotedMatchPreWhitespace() {
		Matcher m = Dates.DATE_CREATED_PATTERN.matcher("   dateCreated=\"AO\"");
		assertTrue(m.find());
		assertEquals(2, m.groupCount());
		assertEquals("AO", m.group(1));
		assertNull(m.group(2));
		assertFalse(m.find());
	}

	@Test
	public void testSingleQuotedMatchPreWhitespace() {
		Matcher m = Dates.DATE_CREATED_PATTERN.matcher("   dateCreated='AO'");
		assertTrue(m.find());
		assertEquals(2, m.groupCount());
		assertNull(m.group(1));
		assertEquals("AO", m.group(2));
		assertFalse(m.find());
	}

	@Test
	public void testDoubleQuotedMatchPostWhitespace() {
		Matcher m = Dates.DATE_CREATED_PATTERN.matcher("dateCreated=\"AO\"   ");
		assertTrue(m.find());
		assertEquals(2, m.groupCount());
		assertEquals("AO", m.group(1));
		assertNull(m.group(2));
		assertFalse(m.find());
	}

	@Test
	public void testSingleQuotedMatchPostWhitespace() {
		Matcher m = Dates.DATE_CREATED_PATTERN.matcher("dateCreated='AO'   ");
		assertTrue(m.find());
		assertEquals(2, m.groupCount());
		assertNull(m.group(1));
		assertEquals("AO", m.group(2));
		assertFalse(m.find());
	}

	@Test
	public void testDoubleQuotedMatchMidLeftWhitespace() {
		Matcher m = Dates.DATE_CREATED_PATTERN.matcher("dateCreated   =\"AO\"");
		assertTrue(m.find());
		assertEquals(2, m.groupCount());
		assertEquals("AO", m.group(1));
		assertNull(m.group(2));
		assertFalse(m.find());
	}

	@Test
	public void testSingleQuotedMatchMidLeftWhitespace() {
		Matcher m = Dates.DATE_CREATED_PATTERN.matcher("dateCreated   ='AO'");
		assertTrue(m.find());
		assertEquals(2, m.groupCount());
		assertNull(m.group(1));
		assertEquals("AO", m.group(2));
		assertFalse(m.find());
	}

	@Test
	public void testDoubleQuotedMatchMidRightWhitespace() {
		Matcher m = Dates.DATE_CREATED_PATTERN.matcher("dateCreated=   \"AO\"");
		assertTrue(m.find());
		assertEquals(2, m.groupCount());
		assertEquals("AO", m.group(1));
		assertNull(m.group(2));
		assertFalse(m.find());
	}

	@Test
	public void testSingleQuotedMatchMidRightWhitespace() {
		Matcher m = Dates.DATE_CREATED_PATTERN.matcher("dateCreated=   'AO'");
		assertTrue(m.find());
		assertEquals(2, m.groupCount());
		assertNull(m.group(1));
		assertEquals("AO", m.group(2));
		assertFalse(m.find());
	}
}
