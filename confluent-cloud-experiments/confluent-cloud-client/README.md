- You will want to run the below for an "apache flink query" 
  - under a url that looks like:
    - https://confluent.cloud/workspaces/env-qpwkd2/AWS/us-east-2/workspace-2024-11-04-044959
- SELECT CAST(`key` AS STRING) AS key_str, CAST(`val` AS STRING) AS val_str
FROM `default`.`cluster_0_10312024`.`topic_0`
LIMIT 10;

- The above query will convert the data that is received for the key 
and the value into string data such that it is readable, and 
if you do not cast the columns then the data will show up as unreadable raw bytes. 

