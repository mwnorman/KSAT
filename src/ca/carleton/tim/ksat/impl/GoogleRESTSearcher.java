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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

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
        "http://cms.sce.carleton.ca/mod/resource/view.php?id=74";
    public final static int POOL_SIZE = 20;
    public final static int MAX_WAIT_TIME = 5000;
    public final static int TIMEOUT = 30000;
    static Random generator = new Random();
    
    public final static String GOOGLE_SEARCH_API_PREFIX = 
        "http://ajax.googleapis.com/ajax/services/search/web?start=0&lr=lang_en&v=1.0&q=";
    public final static String GOOGLE_SEARCH_API_SITE = "+site%3A";
    
    public GoogleRESTSearcher(Analysis analysis) {
        /*
        ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(POOL_SIZE);
        for (Site site : analysis.getSites()) {
            final Site copyOfSite = site;
            ScheduledFuture<Long> sitePageCountThread = threadPool.schedule(new Callable<Long>() {
                @Override
                public Long call() throws Exception {
                    URL url = new URL(GOOGLE_SEARCH_API_PREFIX + 
                            GOOGLE_SEARCH_API_SITE + copyOfSite.getUrl());
                    URLConnection connection = url.openConnection();
                    connection.setRequestProperty("Referer", TIM_REFERER);
                    String line;
                    StringBuilder builder = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    while((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    JSONObject json = new JSONObject(builder.toString());
                    return json.getJSONObject("responseData").getJSONObject("cursor").getLong("estimatedResultCount");
                }
            }, randomSleepTime(), MILLISECONDS);
            Long sitePageCount = null;
            try {
                sitePageCount = sitePageCountThread.get(TIMEOUT, MILLISECONDS);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            if (sitePageCount != null) {
                System.out.println(copyOfSite.getUrl() + " pageCount= " + sitePageCount);
                for (KeywordExpression expression : analysis.getExpressions()) {
                    final KeywordExpression copyOfExpression = expression;
                    ScheduledFuture<Long> expressionPageCountThread = threadPool.schedule(new Callable<Long>() {
                        @Override
                        public Long call() throws Exception {
                            URL url = new URL(GOOGLE_SEARCH_API_PREFIX + copyOfExpression.getExpression() +
                                    GOOGLE_SEARCH_API_SITE + copyOfSite.getUrl());
                            URLConnection connection = url.openConnection();
                            connection.setRequestProperty("Referer", TIM_REFERER);
                            String line;
                            StringBuilder builder = new StringBuilder();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            while((line = reader.readLine()) != null) {
                                builder.append(line);
                            }
                            JSONObject json = new JSONObject(builder.toString());
                            return json.getJSONObject("responseData").getJSONObject("cursor").getLong("estimatedResultCount");
                        }
                    }, randomSleepTime(), MILLISECONDS);
                    Long expressionPageCount = null;
                    try {
                        expressionPageCount = expressionPageCountThread.get(TIMEOUT, MILLISECONDS);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    String decodedEpression = copyOfExpression.getExpression();
                    try {
                        decodedEpression = URLDecoder.decode(decodedEpression, "UTF-8");
                    }
                    catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    System.out.println(decodedEpression + ": pageCount= " + expressionPageCount);
                }
            }
        }
        */
        for (Site site : analysis.getSites()) {
            System.out.println(site.getUrl());
            for (KeywordExpression expression : analysis.getExpressions()) {
                try {
                    String decodedExpression = expression.getExpression();
                    decodedExpression = URLDecoder.decode(decodedExpression, "UTF-8");
                    URL url = new URL(GOOGLE_SEARCH_API_PREFIX + expression.getExpression() +
                            GOOGLE_SEARCH_API_SITE + site.getUrl());
                    URLConnection connection = url.openConnection();
                    connection.setRequestProperty("Referer", TIM_REFERER);
                    String line;
                    StringBuilder builder = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    while((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    JSONObject json = new JSONObject(builder.toString());
                    JSONObject json2 = json.getJSONObject("responseData").getJSONObject("cursor");
                    if (json2.has("estimatedResultCount")) {
                        long l = json2.getLong("estimatedResultCount");
                        System.out.println("\t" + decodedExpression + ": estimated page count = " + l);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static long randomSleepTime() {
        return generator.nextInt(MAX_WAIT_TIME);
    }
}
