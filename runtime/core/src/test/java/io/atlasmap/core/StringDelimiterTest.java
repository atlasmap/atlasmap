package io.atlasmap.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import io.atlasmap.spi.StringDelimiter;

public class StringDelimiterTest {

    @Test
    public void testGetNameRegexValue() {
        assertEquals("Ampersand", StringDelimiter.AMPERSAND.getName());
        assertEquals("&", StringDelimiter.AMPERSAND.getRegex());
        assertEquals("&", StringDelimiter.AMPERSAND.getValue());
        assertEquals("AtSign", StringDelimiter.AT_SIGN.getName());
        assertEquals("@", StringDelimiter.AT_SIGN.getRegex());
        assertEquals("@", StringDelimiter.AT_SIGN.getValue());
        assertEquals("Backslash", StringDelimiter.BACKSLASH.getName());
        assertEquals("\\\\", StringDelimiter.BACKSLASH.getRegex());
        assertEquals("\\", StringDelimiter.BACKSLASH.getValue());
        assertEquals("Colon", StringDelimiter.COLON.getName());
        assertEquals(":", StringDelimiter.COLON.getRegex());
        assertEquals(":", StringDelimiter.COLON.getValue());
        assertEquals("Comma", StringDelimiter.COMMA.getName());
        assertEquals(",", StringDelimiter.COMMA.getRegex());
        assertEquals(",", StringDelimiter.COMMA.getValue());
        assertEquals("Dash", StringDelimiter.DASH.getName());
        assertEquals("-", StringDelimiter.DASH.getRegex());
        assertEquals("-", StringDelimiter.DASH.getValue());
        assertEquals("Equal", StringDelimiter.EQUAL.getName());
        assertEquals("=", StringDelimiter.EQUAL.getRegex());
        assertEquals("=", StringDelimiter.EQUAL.getValue());
        assertEquals("Hash", StringDelimiter.HASH.getName());
        assertEquals("#", StringDelimiter.HASH.getRegex());
        assertEquals("#", StringDelimiter.HASH.getValue());
        assertEquals("MultiSpace", StringDelimiter.MULTI_SPACE.getName());
        assertEquals("\\s+", StringDelimiter.MULTI_SPACE.getRegex());
        assertEquals("    ", StringDelimiter.MULTI_SPACE.getValue());
        assertEquals("Period", StringDelimiter.PERIOD.getName());
        assertEquals("\\.", StringDelimiter.PERIOD.getRegex());
        assertEquals(".", StringDelimiter.PERIOD.getValue());
        assertEquals("Pipe", StringDelimiter.PIPE.getName());
        assertEquals("\\|", StringDelimiter.PIPE.getRegex());
        assertEquals("|", StringDelimiter.PIPE.getValue());
        assertEquals("Semicolon", StringDelimiter.SEMICOLON.getName());
        assertEquals(";", StringDelimiter.SEMICOLON.getRegex());
        assertEquals(";", StringDelimiter.SEMICOLON.getValue());
        assertEquals("Slash", StringDelimiter.SLASH.getName());
        assertEquals("/", StringDelimiter.SLASH.getRegex());
        assertEquals("/", StringDelimiter.SLASH.getValue());
        assertEquals("Space", StringDelimiter.SPACE.getName());
        assertEquals("\\s", StringDelimiter.SPACE.getRegex());
        assertEquals(" ", StringDelimiter.SPACE.getValue());
        assertEquals("Underscore", StringDelimiter.UNDERSCORE.getName());
        assertEquals("_", StringDelimiter.UNDERSCORE.getRegex());
        assertEquals("_", StringDelimiter.UNDERSCORE.getValue());
    }

    @Test
    public void testFromName() {
        assertEquals(StringDelimiter.AMPERSAND, StringDelimiter.fromName("Ampersand"));
        assertEquals(StringDelimiter.AT_SIGN, StringDelimiter.fromName("AtSign"));
        assertEquals(StringDelimiter.BACKSLASH, StringDelimiter.fromName("Backslash"));
        assertEquals(StringDelimiter.COLON, StringDelimiter.fromName("Colon"));
        assertEquals(StringDelimiter.COMMA, StringDelimiter.fromName("Comma"));
        assertEquals(StringDelimiter.DASH, StringDelimiter.fromName("Dash"));
        assertEquals(StringDelimiter.EQUAL, StringDelimiter.fromName("Equal"));
        assertEquals(StringDelimiter.HASH, StringDelimiter.fromName("Hash"));
        assertEquals(StringDelimiter.MULTI_SPACE, StringDelimiter.fromName("MultiSpace"));
        assertEquals(StringDelimiter.PERIOD, StringDelimiter.fromName("Period"));
        assertEquals(StringDelimiter.PIPE, StringDelimiter.fromName("Pipe"));
        assertEquals(StringDelimiter.SEMICOLON, StringDelimiter.fromName("Semicolon"));
        assertEquals(StringDelimiter.SLASH, StringDelimiter.fromName("Slash"));
        assertEquals(StringDelimiter.SPACE, StringDelimiter.fromName("Space"));
        assertEquals(StringDelimiter.UNDERSCORE, StringDelimiter.fromName("Underscore"));
    }

    @Test
    public void testGetAllRegexes() {
        List<String> values = StringDelimiter.getAllRegexes();
        assertNotNull(values);
        assertEquals(new Integer(15), new Integer(values.size()));
    }

    @Test
    public void testGetAllNames() {
        List<String> names = StringDelimiter.getAllNames();
        assertNotNull(names);
        assertEquals(new Integer(15), new Integer(names.size()));
    }

}
