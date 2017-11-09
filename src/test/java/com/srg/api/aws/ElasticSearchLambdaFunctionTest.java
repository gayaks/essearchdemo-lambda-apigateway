package com.srg.api.aws;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

/**
 * This is the basic unit test 
 * TODO implemenation of other test cases
 * @author sgayakwad
 *
 */
public class ElasticSearchLambdaFunctionTest {
	
	private ElasticSearchLambdaFunction subject;
	private Context testContext;
	private InputStream inputStream;
	private OutputStream outputStream;
	

	@Before
	public void setUp() throws Exception {
		subject = new ElasticSearchLambdaFunction();
		inputStream = mock(InputStream.class);
		outputStream = mock(OutputStream.class);
		testContext = mock(Context.class);
	}
	
	@Test
    public void should_handle_request() throws IOException {
        LambdaLogger mockLogger = mock(LambdaLogger.class);
        Mockito.when(testContext.getLogger()).thenReturn(mockLogger);
        subject.initializeLogger(testContext);
        Mockito.verify(testContext).getLogger();
        
    }

}
