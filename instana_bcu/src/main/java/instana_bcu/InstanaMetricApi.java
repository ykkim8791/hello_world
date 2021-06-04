package instana_bcu;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.mongodb.BasicDBObject;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class InstanaMetricApi {
	private final String token = "h1GTl3arMxIXJKqp";
	private final Logger logger = Logger.getLogger(getClass());
	
	public void memMetric(long fromDate, long interval) {
		infraMetric("memory", fromDate, interval);
	}
	
	public void cpuMetric(long fromDate, long interval) {
		infraMetric("cpu", fromDate, interval);

	}
	
	public void appMetric(long fromDate, long interval) {
		try {

			String metricQueryName = "calls";
			JSONObject post = new JSONObject();

			
	        
			JSONObject group = new JSONObject();
			group.put("groupbyTag", "call.http.host");
			group.put("groupbyTagEntity", "NOT_APPLICABLE");
	        
			post.put("group", group);
			
			
			List<JSONObject> metrics = new ArrayList<JSONObject>();
			JSONObject metric = new JSONObject();
			metric.put("aggregation", "SUM");
			metric.put("metric", "calls");
			metrics.add(metric);
			post.put("metrics", metrics);
			
	        post.put("rollup", interval);
	        
	        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        long toDate = System.currentTimeMillis()-180000;

	        JSONObject timeframe = new JSONObject();
	        timeframe.put("windowSize", interval);
	        timeframe.put("to", fromDate + interval - 1000);

	        post.put("timeFrame", timeframe);

			String postQuery = post.toJSONString();
			
			logger.info("[" + format.format(fromDate)  +"] " + postQuery);
			
			String api = "/api/application-monitoring/analyze/call-groups";
			String host = "https://production-bcu.instana.io";
			OkHttpClient client = new OkHttpClient();
		    Request request = new Request.Builder()
		            .url(host + api)
		            .header("Authorization", "apiToken " + token)
		            .post(RequestBody.create(MediaType.parse("application/json"), postQuery)) //POST로 전달할 내용 설정 
					.build();

	        Response responses = client.newCall(request).execute();
	        String resultString = responses.body().string();
	        logger.info(resultString);

	        JSONParser parser = new JSONParser();
	        JSONObject result = (JSONObject) parser.parse(resultString);
	        JSONArray eachMetrics = (JSONArray)result.get("items");
	        
	        List<Document> documents = new ArrayList<Document> ();
			SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat timeOnly = new SimpleDateFormat("HH:mm:ss");
			
	        for(int i=0, size = eachMetrics.size(); i < size; i++) {

	        	JSONObject anObject = (JSONObject) eachMetrics.get(i);
	        	String appName = (String) anObject.get("				");
	        	JSONObject metricObject = (JSONObject) anObject.get("metrics");
	        	JSONArray mapValues = (JSONArray) metricObject.get("calls.sum");
	        	for(int x = 0, xSize = mapValues.size(); x < xSize; x++) {
	        		
	        		List<Object> rawValue = (List<Object>) mapValues.get(x);
	        		long timestamp = (Long)rawValue.get(0);
	        		String date = dateOnly.format(timestamp);
	        		String time = timeOnly.format(timestamp);
	        		double value = (Double)rawValue.get(1);
	        		
	        		Document  document = new Document ();
					document.put("application", appName);
					document.put("date", date);
					document.put("call", value);
					documents.add(document);
					
					logger.info(date + " : " + appName +" : " + value);
	        	}
	        	logger.debug("metric size : " + mapValues.size()	+ ": documents size : " + documents.size());

	        }
	        
	        CreateDao dao = new CreateDao();
        	dao.appInsert(documents);

		}catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private void infraMetric(String metric, long fromDate, long interval) {
		try {
			
			String metricQueryName = metric + ".used";
			
			JSONObject post = new JSONObject();

			List<String> metrics = new ArrayList<String>();
			metrics.add(metricQueryName);
			
			post.put("metrics", metrics);
	        post.put("plugin", "host");
	        post.put("query", "entity.zone:*");
	        post.put("rollup", 300);
	        
	        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        long toDate = System.currentTimeMillis()-180000;

	        JSONObject timeframe = new JSONObject();
	        timeframe.put("windowSize", interval);
	        timeframe.put("to", fromDate + interval);

	        post.put("timeFrame", timeframe);

			String postQuery = post.toJSONString();
			
			logger.info("[" + format.format(fromDate)  +"] " + postQuery);
			
			String api = "/api/infrastructure-monitoring/metrics";
			String host = "https://production-bcu.instana.io";
			OkHttpClient client = new OkHttpClient();
		    Request request = new Request.Builder()
		            .url(host + api)
		            .header("Authorization", "apiToken " + token)
		            .post(RequestBody.create(MediaType.parse("application/json"), postQuery)) //POST로 전달할 내용 설정 
					.build();

	        Response responses = client.newCall(request).execute();
	        String resultString = responses.body().string();
	        logger.info(resultString);

	        JSONParser parser = new JSONParser();
	        JSONObject result = (JSONObject) parser.parse(resultString);
	        JSONArray eachMetrics = (JSONArray)result.get("items");
	        
	        List<Document> documents = new ArrayList<Document> ();
			SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat timeOnly = new SimpleDateFormat("HH:mm:ss");
			
	        for(int i=0, size = eachMetrics.size(); i < size; i++) {

	        	JSONObject anObject = (JSONObject) eachMetrics.get(i);
	        	String hostName = (String) anObject.get("label");
	        	JSONObject metricObject = (JSONObject) anObject.get("metrics");
	        	JSONArray mapValues = (JSONArray) metricObject.get(metricQueryName);
	        	for(int x = 0, xSize = mapValues.size(); x < xSize; x++) {
	        		
	        		if(x == xSize - 1)	break;
	        		
	        		List<Object> rawValue = (List<Object>) mapValues.get(x);
	        		long timestamp = (Long)rawValue.get(0);
	        		String date = dateOnly.format(timestamp);
	        		String time = timeOnly.format(timestamp);
	        		double value = (Double)rawValue.get(1);
	        		
	        		Document  document = new Document ();
					document.put("host", hostName);
					document.put("date", date);
					document.put("time", time);
					document.put("used", value * 100);
					documents.add(document);
	        	}
	        	logger.info("metric size : " + mapValues.size()	+ ": documents size : " + documents.size());

	        }
	        
	        CreateDao dao = new CreateDao();
	        if(metric.equals("cpu")) {
	        	dao.cpuInsert(documents);
	        }else if(metric.equals("memory")) {
	        	dao.memoryInsert(documents);
	        }

		}catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
