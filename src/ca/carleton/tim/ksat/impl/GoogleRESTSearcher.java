/*
 * This software is licensed under the terms of the ISC License.
 * (ISCL http://www.opensource.org/licenses/isc-license.txt
 * It is functionally equivalent to the 2-clause BSD licence,
 * with language "made unnecessary by the Berne convention" removed).
 * 
 * Copyright (c) 2009, 2010, Mike Norman
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
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

//RCP imports
import org.eclipse.core.runtime.IProgressMonitor;

//KSAT domain imports
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

    public final static String KSAT_REFERER = 
        "http://github.com/mwnorman/KSAT/";
    public final static int POOL_SIZE = 40;
    public final static int MAX_WAIT_TIME = 1024;
    public final static int TIMEOUT = 30000;
    static Random generator = new Random();
    
    public final static String GOOGLE_SEARCH_API_PREFIX = 
        "http://ajax.googleapis.com/ajax/services/search/web?start=0&lr=lang_en&v=1.0&safe=off&q=";
    public final static String GOOGLE_SEARCH_API_SITE = "+site%3A";
    protected ExecutorService threadPool;
    protected Analysis analysis;
    protected Map<String, SitePageCount> rESTResults = new LinkedHashMap<String, SitePageCount>();

    public GoogleRESTSearcher(Analysis analysis) {
        this.analysis = analysis;
        threadPool = Executors.newFixedThreadPool(POOL_SIZE);
    }
    
    public  Map<String, SitePageCount> getRESTResults(IProgressMonitor monitor) {
        if (!threadPool.isShutdown()) {
            ArrayList<Callable<KeywordPageCount>> calls =  new ArrayList<Callable<KeywordPageCount>>();
            for (Site site : analysis.getSites()) {
                String siteUrlString = site.getUrl();
                boolean reachable = isSiteAlive(siteUrlString);
                if (reachable) {
                    long sitePageCount = getSitePageCount(siteUrlString);
                    SitePageCount spc = new SitePageCount(site, sitePageCount);
                    rESTResults.put(siteUrlString, spc);
                    for (KeywordExpression expression : analysis.getExpressions()) {
                        Callable<KeywordPageCount> nextCall = new GoogleRESTCall(siteUrlString,
                            expression, randomSleepTime());
                        calls.add(nextCall);
                    }
                }
                else {
                    SitePageCount spc = new SitePageCount(site, -1); // -1 indicates unreachable
                    rESTResults.put(siteUrlString, spc);
                }
                if (monitor.isCanceled()) {
                	break;
                }
            }
            List<Future<KeywordPageCount>> futureRESTResults = null;
            try {
            	if (!monitor.isCanceled()) {
            		futureRESTResults = threadPool.invokeAll(calls);
            	}
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            if (futureRESTResults != null && !monitor.isCanceled()) {
               for (Future<KeywordPageCount> futureRESTResult : futureRESTResults) {
                   try {
                       KeywordPageCount pageCount = futureRESTResult.get();
                       SitePageCount spc = rESTResults.get(pageCount.siteUrlString);
                       spc.addPageCount(pageCount.expression, pageCount.pageCount);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (monitor.isCanceled()) {
                    	break;
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
    
    static long getSitePageCount(String site) {
        long sitePageCount = 0l;
        URLConnection connection = null;
        try {
            URL url = new URL(GOOGLE_SEARCH_API_PREFIX + GOOGLE_SEARCH_API_SITE + site);
            connection = url.openConnection();
            connection.setRequestProperty("Referer", KSAT_REFERER);
            String line;
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            JSONObject json = new JSONObject(builder.toString());
            JSONObject json2 = json.getJSONObject("responseData").getJSONObject("cursor");
            if (json2.has("estimatedResultCount")) {
                sitePageCount = json2.getLong("estimatedResultCount");
            }
        }
        catch (Exception e) {
            sitePageCount = -2;
        }
        finally {
            if (connection != null && connection instanceof HttpURLConnection) {
                ((HttpURLConnection)connection).disconnect();
            }
        }
        return sitePageCount;
    }
    
    public class SitePageCount {
        Site site;
        long sitePageCount;
        ArrayList<KeywordPageCount> pageCounts = new ArrayList<KeywordPageCount>();
        public SitePageCount(Site site, long sitePageCount) {
            this.site = site;
            this.sitePageCount = sitePageCount;
        }
        public Site getSite() {
            return site;
        }
        public long getSitePageCount() {
            return sitePageCount;
        }
        public void addPageCount(KeywordExpression expression, long pageCount) {
            pageCounts.add(new KeywordPageCount(site.getUrl(), expression, pageCount));
        }
        public List<KeywordPageCount> getPageCounts() {
            Collections.<KeywordPageCount>sort(pageCounts, new Comparator<KeywordPageCount>(){
                public int compare(KeywordPageCount o1, KeywordPageCount o2) {
                    return o1.getExpression().getId() - o2.getExpression().getId();
                }
            });
            return pageCounts;
        }
        @Override
        public String toString() {
            return "SitePageCount['" + site.getUrl() + "']=" + sitePageCount;
        }
    }
    public class KeywordPageCount {
        String siteUrlString;
        KeywordExpression expression;
        long pageCount;
        public KeywordPageCount(String siteUrlString, KeywordExpression expression, long pageCount) {
            super();
            this.siteUrlString = siteUrlString;
            this.expression = expression;
            this.pageCount = pageCount;
        }
        public String getSiteUrlString() {
            return siteUrlString;
        }
        public KeywordExpression getExpression() {
            return expression;
        }
        public long getPageCount() {
            return pageCount;
        }
        @Override
        public String toString() {
            String decodedExpression = expression.getExpression();
            try {
                decodedExpression = URLDecoder.decode(decodedExpression, "UTF-8");
            }
            catch (Exception e) {
                // ignore
                decodedExpression = expression.getExpression();
            }
            return "KeywordPageCount['" + decodedExpression + "']=" + pageCount;
        }
    }
    
    class GoogleRESTCall implements Callable<KeywordPageCount> {
        String siteUrlString; // target web-site to run search against
        KeywordExpression expression; // search term(s)
        long delay; // random 0-1024 milliseconds delay
        long pageCount = 0L;
        public GoogleRESTCall(String siteUrlString, KeywordExpression expression, long delay) {
            this.siteUrlString = siteUrlString;
            this.expression = expression;
            this.delay = delay;
        }
        @Override
        public KeywordPageCount call() throws Exception {
            URLConnection connection = null;
            try {
                Thread.sleep(delay);
                URL url = new URL(GOOGLE_SEARCH_API_PREFIX + expression.getExpression() +
                    GOOGLE_SEARCH_API_SITE + siteUrlString);
                connection = url.openConnection();
                connection.setRequestProperty("Referer", KSAT_REFERER);
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
                    pageCount = jsonResponseDataCursor.getLong("estimatedResultCount");
                }
            }
            catch (Exception e) {
                pageCount = -1;
            }
            finally {
                if (connection != null && connection instanceof HttpURLConnection) {
                    ((HttpURLConnection)connection).disconnect();
                }
            }
            return new KeywordPageCount(siteUrlString, expression, pageCount);
        }
    }
}