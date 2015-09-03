#!/bin/sh

curl -X PUT localhost:9200/_template/titanic_template_1 -d '{
    "template": "titanic*",
    "settings": {
      "number_of_shards": 1,
      "number_of_replicas": 0   
    },
    "mappings": {
	"passenger": {
	  "dynamic": "strict",
	  "properties": {
		"passengerId": {"type": "integer"},
		"pclass": {"type": "integer"},
		"survived": {"type":"integer"},
		"name": {"type":"string", "index":"not_analyzed"},
		"sex": {"type":"string", "index":"not_analyzed"},
		"age": {"type":"double"},
		"sibSp": {"type":"integer"},
		"parch": {"type":"integer"},
		"ticket": {"type":"string", "index":"not_analyzed"},
		"fare": {"type":"double"},
		"cabin": {"type":"string", "index":"not_analyzed", "null_value":"na"},
		"embarked": {"type":"string", "index":"not_analyzed"}	
          }
	}
    }
}'
