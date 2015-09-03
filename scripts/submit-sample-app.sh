#!/bin/sh
spark-submit --packages org.elasticsearch:elasticsearch-spark_2.10:2.1.1 --class SampleApp --master local[2] target/scala-2.10/sample-spark-project-titanic-data_2.10-1.0.jar
