package com.srg.api.aws;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.services.lambda.runtime.Context; 
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

/**
 * This is AWS Lambda Function implementation for elastic search 
 * exposed as rest end point through AWS api gateway
 * @author sgayakwad
 *
 */


public class ElasticSearchLambdaFunction implements RequestStreamHandler{

	
	
	// Server_uri must be set through lambda environment variables
	private static final String SERVER_URI_KEY="SERVER_URI";	
	private static final String DEFAULT_INDEX = "planinfo";
	
	private static final String QUERY_PARAMETERS = "queryStringParameters";

	// Json parser to parse the input json string
	private JSONParser parser = new JSONParser();
	
	/**
	 * This is the entry point of Lambda Function. The external requests are 
	 * served through AWS api geteway and proxied to this lambda function to
	 * invoke search functionality on configured elastic search cluster.
	 * 
	 */
	
	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream,
			Context context) throws IOException {		
		

	LambdaLogger logger = context.getLogger();	
	String serverURI = System.getenv(SERVER_URI_KEY);
	if (serverURI == null) {
		throw new RuntimeException(String.format("Environment Variable \"%s\" must be set.", SERVER_URI_KEY));
	}
	
	
	BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
	BoolQueryBuilder bQueryBuilder = new BoolQueryBuilder();
	JSONObject response = new JSONObject();
	
	try {
		// Parse the request input stream to fetch queryString parameters
		JSONObject event = (JSONObject)parser.parse(reader);
		logger.log("Event : \n\n");
		logger.log(event.toString());		
		
		// Parse the queryStringParameters to setup the Query Parameters
		if (event.get(QUERY_PARAMETERS) != null) {						
			JSONObject queryParameters = (JSONObject)event.get(QUERY_PARAMETERS);
			logger.log(String.format("Query Parametes :  %s", queryParameters.toJSONString()));
			
			logger.log("\n\nSetting up query builder\n");
			queryParameters.forEach((queryKey, queryValue)-> {
				
				bQueryBuilder.must(QueryBuilders.matchQuery((String)queryKey, queryValue));
				logger.log(String.format("Query Parameter : \"%s , %s\" set.\n", queryKey, queryValue));
			});
			logger.log("Setting query buider completed\n");
		} else {
			logger.log("\nNo Input Query Parameters.\n");
		}
		
		Search search = new Search.Builder(String.format("{\"query\":%s}", bQueryBuilder.toString()))
							.addIndex(this.getIndex(event))
							.build();
		
		
		// Initialize the Jestclient 
		JestClientFactory factory = new JestClientFactory();
		HttpClientConfig clientConfig = new HttpClientConfig
									.Builder(serverURI)
									.multiThreaded(true)
									.defaultMaxTotalConnectionPerRoute(10)
									.maxTotalConnection(10)
									.build();
		
		
		factory.setHttpClientConfig(clientConfig);
		JestClient client = factory.getObject();
		
		logger.log("Executing Search Operation.\n");
		JestResult result = client.execute(search);
		logger.log("Search Completed.\n");	
		
		logger.log("Building search response...\n");
		JSONObject header = new JSONObject();
		response.put("isBase64Encoded", true);
		response.put("statusCode", 200);
		header.put("Content-Type", "application/json");
		response.put("headers",header);		
		response.put("body", result.getJsonString());
		
		logger.log("Search Response Completed...\n");
		
		}catch(ParseException pex){
			logger.log("Failed to parse incoming request...\n");
			response.put("statusCode", "400");
			response.put("exception", pex);
			logger.log(pex.getMessage());
		}
		
		logger.log("Writing Json Response to output stream\n");
		OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
		writer.write(response.toJSONString());
		writer.close();
		logger.log("Lambda Function Execution Completed.\n");
	}
	
	/**
	 * 
	 * @param context Lamba Execution Context
	 * @return Lambda logger
	 */
	protected LambdaLogger initializeLogger(Context context) {
		return context.getLogger();
	}
	
	/**
	 * TODO implement search for different index
	 * @param event parsed JSONObject
	 * @return default index
	 */
	private String getIndex(JSONObject event) {
		return DEFAULT_INDEX;
	}
}
