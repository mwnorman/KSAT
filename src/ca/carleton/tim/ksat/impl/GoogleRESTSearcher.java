/*
 * This software is licensed under the terms of the ISC License.
 * (ISCL http://www.opensource.org/licenses/isc-license.txt
 * It is functionally equivalent to the 2-clause BSD licence,
 * with language "made unnecessary by the Berne convention" removed).
 * 
 * Copyright (c) 2009, Mike Norman
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER
 * RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE
 * USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package ca.carleton.tim.ksat.impl;

//javase imports
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

//KSAT imports
import ca.carleton.tim.ksat.json.JSONObject;
import ca.carleton.tim.ksat.model.Analysis;
import ca.carleton.tim.ksat.model.KeywordExpression;
import ca.carleton.tim.ksat.model.Site;

/**
 * 
 * This helper class uses the Google REST search API in order to search for
 * keywords on websites.
 * 
 * @author mwnorman
 *
 */
public class GoogleRESTSearcher {

    public final static String TIM_REFERER = 
        "http://www.carleton.ca/tim/";
    public final static int POOL_SIZE = 40;
    public final static int MAX_WAIT_TIME = 1024;
    public final static int TIMEOUT = 30000;
    static Random generator = new Random();
    
    public final static String GOOGLE_SEARCH_API_PREFIX = 
        "http://ajax.googleapis.com/ajax/services/search/web?start=0&lr=lang_en&v=1.0&q=";
    public final static String GOOGLE_SEARCH_API_SITE = "+site%3A";
    protected ExecutorService threadPool;
    protected Analysis analysis;
    protected List<GoogleRESTResult> rESTResults = new ArrayList<GoogleRESTResult>();

    public GoogleRESTSearcher(Analysis analysis) {
        this.analysis = analysis;
        threadPool = Executors.newFixedThreadPool(POOL_SIZE);
    }
    
    public  List<GoogleRESTResult> getRESTResults() {
        if (!threadPool.isShutdown()) {
            ArrayList<Callable<GoogleRESTResult>> calls =  new ArrayList<Callable<GoogleRESTResult>>();
            for (Site site : analysis.getSites()) {
                String siteUrlString = site.getUrl();
                boolean reachable = isSiteAlive(siteUrlString);
                if (reachable) {
                    long estimatedSitePageCount = getEstimatedSitePageCount(siteUrlString);
                    for (KeywordExpression expression : analysis.getExpressions()) {
                        Callable<GoogleRESTResult> nextCall = new GoogleRESTCall(siteUrlString,
                            estimatedSitePageCount, expression.getExpression(), randomSleepTime());
                        calls.add(nextCall);
                    }
                }
            }
            List<Future<GoogleRESTResult>> futureRESTResults = null;
            try {
                futureRESTResults = threadPool.invokeAll(calls);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            if (futureRESTResults != null) {
               for (Future<GoogleRESTResult> futureRESTResult : futureRESTResults) {
                   try {
                       GoogleRESTResult rESTResult = futureRESTResult.get();
                       rESTResults.add(rESTResult);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
               }
            }
            threadPool.shutdown();
        }
        return rESTResults;
    }

    static long randomSleepTime() {
        return generator.nextInt(MAX_WAIT_TIME);
    }
    
    static boolean isSiteAlive(String site) {
        boolean isSiteAlive = false;
        HttpURLConnection httpConnection = null;
        try {
            URL url = new URL(site);
            URLConnection connection = url.openConnection();
            if (connection instanceof HttpURLConnection) {
                httpConnection = (HttpURLConnection)connection;
                httpConnection.connect();
                int response = httpConnection.getResponseCode();
                switch (response) {
                    case HttpURLConnection.HTTP_OK:
                    case HttpURLConnection.HTTP_CREATED:
                    case HttpURLConnection.HTTP_ACCEPTED:
                    case HttpURLConnection.HTTP_NOT_AUTHORITATIVE:
                    case HttpURLConnection.HTTP_NO_CONTENT:
                    case HttpURLConnection.HTTP_RESET:
                    case HttpURLConnection.HTTP_PARTIAL:
                    case 207: //HTTP_MULTI_STATUS
                        isSiteAlive = true;
                        break;
                    default:
                        isSiteAlive = false;
                }
            }
            else {
                isSiteAlive = false;
            }
        }
        catch (Exception e) {
            isSiteAlive = false;
        }
        finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
        return isSiteAlive;
    }
    
    static long getEstimatedSitePageCount(String site) {
        long estimatedSitePageCount = 0l;
        URLConnection connection = null;
        try {
            URL url = new URL(GOOGLE_SEARCH_API_PREFIX + GOOGLE_SEARCH_API_SITE + site);
            connection = url.openConnection();
            connection.setRequestProperty("Referer", TIM_REFERER);
            String line;
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            JSONObject json = new JSONObject(builder.toString());
            JSONObject json2 = json.getJSONObject("responseData").getJSONObject("cursor");
            if (json2.has("estimatedResultCount")) {
                estimatedSitePageCount = json2.getLong("estimatedResultCount");
            }
        }
        catch (Exception e) {
            estimatedSitePageCount = -1;
        }
        finally {
            if (connection != null && connection instanceof HttpURLConnection) {
                ((HttpURLConnection)connection).disconnect();
            }
        }
        return estimatedSitePageCount;
    }
    
    public class GoogleRESTResult {
        String site;
        String expression;
        long estimatedSitePageCount;
        long estimatedPageCount;
        public GoogleRESTResult(String site, String expression, long estimatedPageCount,
            long estimatedSitePageCount) {
            this.site = site;
            this.expression = expression;
            this.estimatedPageCount = estimatedPageCount;
            this.estimatedSitePageCount = estimatedSitePageCount;
        }
        public String getSite() {
            return site;
        }
        public String getExpression() {
            return expression;
        }
        public long getEstimatedSitePageCount() {
            return estimatedSitePageCount;
        }
        public long getEstimatedPageCount() {
            return estimatedPageCount;
        }
        @Override
        public String toString() {
            return "GoogleRESTResult [estimatedPageCount=" + estimatedPageCount + ", expression="
                + expression + ", site=" + site + " (estimatedSitePageCount=" +
                estimatedSitePageCount + ")]";
        }
    }
    
    class GoogleRESTCall implements Callable<GoogleRESTResult> {
        String site; // target web-site to run search against
        long estimatedSitePageCount = 0L;
        String expression; // search term(s)
        long delay; // random 0-5 second delay
        long estimatedPageCount = 0L;
        public GoogleRESTCall(String site, long estimatedSitePageCount, String expression, long delay) {
            this.site = site;
            this.estimatedSitePageCount = estimatedSitePageCount;
            this.expression = expression;
            this.delay = delay;
        }
        public String getSite() {
            return site;
        }
        public String getExpression() {
            return expression;
        }
        public long getEstimatedSitePageCount() {
            return estimatedSitePageCount;
        }
        public long getEstimatedExpressionPageCount() {
            return estimatedPageCount;
        }
        @Override
        public GoogleRESTResult call() throws Exception {
            URLConnection connection = null;
            try {
                Thread.sleep(delay);
                URL url = new URL(GOOGLE_SEARCH_API_PREFIX + expression +
                    GOOGLE_SEARCH_API_SITE + site);
                connection = url.openConnection();
                connection.setRequestProperty("Referer", TIM_REFERER);
                String line;
                StringBuilder builder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                JSONObject jsonRoot = new JSONObject(builder.toString());
                JSONObject jsonResponseDataCursor = 
                    jsonRoot.getJSONObject("responseData").getJSONObject("cursor");
                if (jsonResponseDataCursor.has("estimatedResultCount")) {
                    estimatedPageCount = jsonResponseDataCursor.getLong("estimatedResultCount");
                }
            }
            catch (Exception e) {
                estimatedPageCount = -1;
            }
            finally {
                if (connection != null && connection instanceof HttpURLConnection) {
                    ((HttpURLConnection)connection).disconnect();
                }
            }
            return new GoogleRESTResult(site, expression, estimatedPageCount, estimatedSitePageCount);
        }
    }
}