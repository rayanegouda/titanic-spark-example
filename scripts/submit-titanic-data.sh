#!/bin/sh
spark-submit --packages org.elasticsearch:elasticsearch-spark_2.10:2.1.1 --class TitanicDataApp --master local[2] target/scala-2.10/sample-spark-project-titanic-data_2.10-1.0.jar /Users/sarwar/dev/titanic-spark-example/src/main/resources/test.csv /Users/sarwar/dev/titanic-spark-example/src/main/resources/train.csv
