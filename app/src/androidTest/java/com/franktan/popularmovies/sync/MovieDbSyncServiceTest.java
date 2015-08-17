package com.franktan.popularmovies.sync;

import android.test.AndroidTestCase;

/**
 * Created by tan on 16/08/2015.
 */
public class MovieDbSyncServiceTest extends AndroidTestCase {

    /**
     * Test API key can be retrieved from environment variable
     * (For CI only)
     */
    public void testApiKey () {
        String apiKey = MovieDbAPISyncService.getApiKey(mContext);
        assertTrue("API Key should not be null", (apiKey != null && apiKey != ""));
    }
}
