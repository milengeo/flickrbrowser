package com.example.flickr.engine;

import org.junit.Test;

import static org.junit.Assert.*;


public class PhotoDepotTest {



    @Test
    public void getInstance() {
        assertTrue(PhotoDepot.getInstance() instanceof PhotoDepot);
    }


    @Test
    public void getPageIndex() {
        final int TEST_VALUE = 11;
        PhotoDepot.getInstance().mPageIndex = TEST_VALUE;
        assertEquals(TEST_VALUE, PhotoDepot.getInstance().getPageIndex());
    }

    @Test
    public void getPageSize() {
        final int TEST_VALUE = 11;
        PhotoDepot.getInstance().mPageSize = TEST_VALUE;
        assertEquals(TEST_VALUE, PhotoDepot.getInstance().getPageSize());
    }

    @Test
    public void getPageCount() {
        final int TEST_VALUE = 11;
        PhotoDepot.getInstance().mPageCount = TEST_VALUE;
        assertEquals(TEST_VALUE, PhotoDepot.getInstance().getPageCount());
    }

    @Test
    public void getTotalCount() {
        final int TEST_VALUE = 11;
        PhotoDepot.getInstance().mTotalCount = TEST_VALUE;
        assertEquals(TEST_VALUE, PhotoDepot.getInstance().getTotalCount());
    }


}