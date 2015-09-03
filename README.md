# Introduction

This is a really simple example showcasing how to use spark to read some data, running it through an algorithm, and then storing the results into Elasticsearch
via the elasticsearch-hadoop connector. 

The data has been taken from the Titanic dataset of the Kaggle website (www.kaggle.com) and a RandomForest classifier run on it to predict the survivors among
the titanic passengers based on various factors such as age, gender, fare, etc.

# Credits

- Kaggle (for the Titanic Dataaset)
- Julien Nauroy (on the initial Scala code for reading the data and running RandomForest on it)

# Pre-requisites

1. Install Scala (2.10.x)
2. Install SBT (0.13)
3. Apache Spark (1.4.1)
4. Add appropriate bin folders from above to $PATH
5. Elasticsearch Running on local machine on localhost:9200
6. Run ./scripts/create-titanic-templates.sh (to create the index templates for the titanic dataset)

# Building
Run in the top level folder

```
sbt clean package
```

# Running

Run in the top levle folder 

```
./scripts/submit-titanic-data.sh
```

