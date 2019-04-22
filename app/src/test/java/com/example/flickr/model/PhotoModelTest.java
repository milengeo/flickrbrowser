package com.example.flickr.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PhotoModelTest {

    PhotoModel tester;


    @Before
    public void setUp() throws Exception {
        tester = new PhotoModel();
    }


    @Test
    public void getId() {
        final String TEST_VALUE = "123";
        tester.setId(TEST_VALUE);
        assertEquals(TEST_VALUE, tester.getId());
    }


    @Test
    public void getTitle() {
        final String TEST_VALUE = "123";
        tester.setTitle(TEST_VALUE);
        assertEquals(TEST_VALUE, tester.getTitle());
    }


    @Test
    public void getOwner() {
        final String TEST_VALUE = "123";
        tester.setOwner(TEST_VALUE);
        assertEquals(TEST_VALUE, tester.getOwner());
    }


    @Test
    public void getSecret() {
        final String TEST_VALUE = "123";
        tester.setSecret(TEST_VALUE);
        assertEquals(TEST_VALUE, tester.getSecret());
    }


    @Test
    public void getServer() {
        final String TEST_VALUE = "123";
        tester.setServer(TEST_VALUE);
        assertEquals(TEST_VALUE, tester.getServer());
    }

    @Test
    public void getFarm() {
        final String TEST_VALUE = "123";
        tester.setFarm(TEST_VALUE);
        assertEquals(TEST_VALUE, tester.getFarm());
    }


}