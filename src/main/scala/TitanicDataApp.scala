/* SampleApp.scala:
   This application simply counts the number of lines that contain "val" from itself
 */
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.elasticsearch.spark._
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.tree.model.RandomForestModel
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.{ SparseVector, DenseVector, Vector, Vectors }
import scala.util.control.NonFatal
import scala.collection.immutable.{ Map, HashMap }
import org.apache.spark.rdd.RDD

class TitanicEntry(line: String) {
  
  def toESData: Map[String, Any] = {
    val resultMap = Map("passengerId" -> this.features("passengerId").toInt, 
          "pclass" -> parseInt(this.features("pclass")),
          "name" -> this.features("name").replace("\"", ""), 
          "sex" -> this.features("sex"), 
          "age" -> parseDouble(this.features("age"), 30), 
          "sibSp" -> parseInt(this.features("sibSp")), 
          "parch" -> parseInt(this.features("parch")),
          "ticket" -> this.features("ticket"), 
          "fare" -> parseDouble(this.features("fare")), 
          "cabin" -> this.features("cabin"), 
          "embarked" -> this.features("embarked"))
          
    if(this.features.contains("survived")) {
      resultMap updated ("survived", this.features("survived").toInt)  
    }
    resultMap

  }
  
  var features: Map[String, String] = {
    val record = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1)
    if (record.size == 12) {
      var Array(passengerId, survived, pclass, name, sex, age, sibSp, parch, ticket, fare, cabin, embarked) = record
      if(age.length()==0)
        age = "30" // should really be something like average of all ages
      
      Map("passengerId" -> passengerId, "survived" -> survived, "pclass" -> pclass,
        "name" -> name.replace("\"", ""), "sex" -> sex, "age" -> age, "sibSp" -> sibSp, "parch" -> parch,
        "ticket" -> ticket, "fare" -> fare, "cabin" -> cabin, "embarked" -> embarked)
    } else if (record.size == 11) {
      
      var Array(passengerId, pclass, name, sex, age, sibSp, parch, ticket, fare, cabin, embarked) = record
      
      if(age.length()==0)
        age = "30" //should really be something like average of all ages
      
        Map("passengerId" -> passengerId, "pclass" -> pclass,
        "name" -> name.replace("\"", ""), "sex" -> sex, "age" -> age, "sibSp" -> sibSp, "parch" -> parch,
        "ticket" -> ticket, "fare" -> fare, "cabin" -> cabin, "embarked" -> embarked)
    } else {
      new HashMap() // TODO raise exception
    }
  }

  def parseDouble(s: String, default: Double = 0): Double = try { s.toDouble } catch { case NonFatal(_) => default }
  def parseInt(s: String, default: Int = 0): Int = try { s.toInt } catch { case NonFatal(_) => default }
  
  def parseSex(s: String): Double = (if (s == "male") 1d else 0d)

  def toVector(): Vector = {
    return Vectors.dense(
      parseDouble(features("pclass")),
      parseSex(features("sex")),
      parseDouble(features("age")),
      parseDouble(features("sibSp")),
      parseDouble(features("parch")),
      parseDouble(features("ticket")),
      parseDouble(features("fare")) //cabin, embarked
      )
  }
  
  def toLabeledPoint(): LabeledPoint = {
    // TODO check if the "survived" column is really there
    return LabeledPoint(parseDouble(features("survived")), toVector())
  }
  
}

object TitanicDataApp {
  def main(args: Array[String]) {
    val testFile = args(0)
    val trainFile = args(1)

    val conf = new SparkConf().setAppName("Titanic Data App")
    conf.set("es.index.auto.create", "true")

    val sc = new SparkContext(conf)

    println("===== Now reading test.csv file =====" + testFile)
    val testFileLines = sc.textFile(testFile)

    println("===== Now reading test.csv file =====" + testFile)
    val trainFileLines = sc.textFile(trainFile)

    // Load the "train.csv" file
    var trainCSV = (sc.textFile(trainFile)
      // filter out the header and empty line(s)
      .filter(line => !line.isEmpty() && line.charAt(0).isDigit)
      // interpret each line into a TitanicEntry
      .map(line => new TitanicEntry(line)))

    // Curate the data and transform it into a list of LabeledPoint
    var trainingData = (trainCSV
      // TODO data curation here
      .map(record => record.toLabeledPoint()))

    // Load the test.csv dataset
    var testCSV = (sc.textFile(testFile)
      // fiter out the header and empty line(s)
      .filter(line => !line.isEmpty() && line.charAt(0).isDigit)
      // interpert each line into a TitanicEntry
      .map(line => new TitanicEntry(line)))
    // Curate the data and transform it into a list of (passengerId, Vector) tuples
    var testData = (testCSV
      // TODO data curation here
      .map(record => (record.features("passengerId"), record.toVector())))

    val numClasses = 2
    // TODO enter the list of categorical features (like sex, embarked) and the number of elements
    val categoricalFeaturesInfo = Map[Int, Int]()
    val numTrees = 100
    val featureSubsetStrategy = "auto"
    val impurity = "gini"
    val maxDepth = 6
    val maxBins = 32

    val model = RandomForest.trainClassifier(
      trainingData, numClasses, categoricalFeaturesInfo,
      numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)

    val predictions = testData.map(p => {
      val prediction = model.predict(p._2)
      (p._1, prediction)
    })
    
    predictions.map(p => (p._1.toInt, p._2.toInt)).collect().foreach(p => println(p._1 + "," + p._2))
    
    //merge predictions with original data
    val predictionsMap = predictions.collect().toMap
    
    val finalTrainESData = trainCSV.map(_.toESData)
    
    val finalTestESData = testCSV.map { x => 
      { 
        val pid = x.features("passengerId")
        
        x.toESData updated ("survived", predictionsMap(pid.toString()).toInt)
        
      }}
    
    //save to elasticsearch
    println("FINAL DATA")
    println("====== saving to Elasticsearch now ======")
    //finalData.collect().foreach(println(_))        
    finalTestESData.saveToEs("titanic/passenger")
    finalTrainESData.saveToEs("titanic/passenger")
    
  }
}
