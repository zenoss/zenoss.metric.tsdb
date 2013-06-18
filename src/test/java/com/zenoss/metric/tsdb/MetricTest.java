package com.zenoss.metric.tsdb;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: scleveland
 * Date: 6/18/13
 * Time: 3:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class MetricTest {

    Metric metric;

    @Before
    public void setUp() {
        metric = new Metric( "name", 0, 0.0);
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals("name", metric.getName());
    }

    @Test
    public void testGetTimestamp() throws Exception {
        assertEquals(0, metric.getTimestamp());
    }

    @Test
    public void testGetValue() throws Exception {
        assertEquals(0.0, metric.getValue(), 0.01);
    }

    @Test
    public void testGetTags() throws Exception {
        assertEquals(new HashMap<>(), metric.getTags());
    }

    @Test
    public void testGetTag() throws Exception {
        assertNull( metric.getTag( "tag"));
    }

    @Test
    public void testSetName() throws Exception {
        assertEquals("name", metric.getName());
        metric.setName( "new_name");
        assertEquals("new_name", metric.getName());
    }

    @Test
    public void testSetTimestamp() throws Exception {
        assertEquals(0, metric.getTimestamp());
        metric.setTimestamp(1);
        assertEquals(1, metric.getTimestamp());
    }

    @Test
    public void testSetValue() throws Exception {
        assertEquals( 0.0, metric.getValue(), 0.001);
        metric.setValue(1.0);
        assertEquals(1.0, metric.getValue(), 0.001);
    }

    @Test
    public void testSetTags() throws Exception {
        Map<String, String> tags = new HashMap<>();
        tags.put( "name", "value");
        assertNotEquals( tags, metric.getTags());
        metric.setTags(tags);
        assertEquals(tags, metric.getTags());
    }

    @Test
    public void testSetTag() throws Exception {
        assertNull( metric.getTag( "tag"));
        metric.setTag( "tag", "value");
        assertEquals( "value", metric.getTag( "tag"));
    }

    @Test
    public void testEquals() throws Exception {
        assertFalse( metric.equals( null));
        assertFalse( metric.equals( new Object()));
        assertFalse( metric.equals( new Metric( "nam", 0, 0.0)));
        assertFalse( metric.equals( new Metric( "name", 1, 0.0)));
        assertFalse( metric.equals( new Metric( "name", 0, 1.0)));
        assertTrue( metric.equals( new Metric( "name", 0, 0.0)));
    }

    @Test
    public void testHashCode() throws Exception {
        assertEquals( 1721857429, metric.hashCode());
    }

    @Test
    public void testToString() throws Exception {
        assertEquals( "Metric{name='name', timestamp=0, value=0.0, tags={}}", metric.toString());
    }
}
