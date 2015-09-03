/* SampleApp.scala:
   This application simply counts the number of lines that contain "val" from itself
 */
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.elasticsearch.spark._
 
object SampleApp {
  def main(args: Array[String]) {
    val txtFile = "/Users/sarwar/dev/spark-example/src/main/scala/SampleApp.scala"
    val conf = new SparkConf().setAppName("Sample Application")
    conf.set("es.nodes", "127.0.0.1")
    conf.set("es.port", "9200")
    conf.set("es.index.auto.create", "true")

    val sc = new SparkContext(conf)
    val txtFileLines = sc.textFile(txtFile , 2).cache()
    val numAs = txtFileLines .filter(line => line.contains("val")).count()
    println("Lines with val: %s".format(numAs))

    val numbers = Map("one" -> 1, "two" -> 2, "three" -> 3)
    val airports = Map("arrival" -> "Otopeni", "SFO" -> "San Francisco")

    sc.makeRDD(Seq(numbers, airports)).saveToEs("spark/docs")
    println("Saved RDD to Elasticsearch!")
  }
}
