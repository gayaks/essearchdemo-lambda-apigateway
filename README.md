# Aws Elastic Search Service 5.5 + logstash 5.6.2 + AWS Lambda + AWS API Gateway Proxy Integration 
This is a proof of concept to perform elastic search through ElasticSearch Service, Logstash, AWS Api Gateway and AWS Lambda 

# Problem Statement

Using Java, write a micro service that invokes AWS elastic search and make it available using API gateway.  

1. Test Data - http://askebsa.dol.gov/FOIA%20Files/2016/Latest/F_5500_2016_Latest.zip
2. Search should be allowed by Plan name, Sponsor name and Sponsor State
3. Use AWS best practices 

# Complete Walk through

1. Created a elastic search domain 5.5 to insert the test data with the default access policy.   

2. Downloaded logstash 5.6.2 to insert csv data to elastic search cluster
   Note : The latest logstash 5.6.3 has CSV parsing issue, hence downgraded to 5.6.2
  
3. Logstash output plugin to sign and export logstash events to Amazon Elasticsearch Service is configured as per the docs
   https://github.com/awslabs/logstash-output-amazon_es
  
4. Created a logstash pipeline configuration file logstash_f_5500_2016_latest.config

5. Run the following command to load the csv data to aws elastic search
    <logstash-home>/bin/logstash -f logstash_f_5500_2016_latest.config
    
6. The inserted data is verified through kibana dev tools using Query DSL of elastic search.
    Please see kibanaQueryDSL.txt
    
7. Created an AWS Lambda function with AWSLambdaBasicExecutionRole

8. Create a facade class which implements LambdaHandhler invoked through API Gateway.
    Check out the project structure.
    run gradle build
    
8. Grade build creates AWS Lambda deployment package 

9. Deployed the <Project>.zip for AWS Lambda through AWS Console
 
10. Created an AWS API Gateway which uses Lambda functions created in step 8.

11. Rest endpoint is deployed and is running here : 
https://epu5xjken8.execute-api.us-west-2.amazonaws.com/esearch

12. AWS lambda function can be unit tested by checking out the project and run the following command
    gradle test
    
    

# Sample Inputs for Elastic Search Endpoint; verifiable through Postman or any rest clients

1. PLAN_NAME=TRIO DOMINION

ie https://epu5xjken8.execute-api.us-west-2.amazonaws.com/esearch?PLAN_NAME=TRIO DOMINION

2. PLAN_NAME=TRIO DOMINION&SPONSOR_DFE_NAME=DOMINION

https://epu5xjken8.execute-api.us-west-2.amazonaws.com/esearch?PLAN_NAME=TRIO DOMINION&SPONSOR_DFE_NAME=DOMINION



    

