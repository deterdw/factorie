package cc.factorie.epistemodb

import com.mongodb.{DB, MongoClient}

/**
 * Created by beroth on 2/11/15.
 */

class LoadTacDataOptions extends cc.factorie.util.DefaultCmdOptions {
  val tacData = new CmdOption("tac-data", "", "FILE", "tab separated file with TAC training data")
  val mongoHost = new CmdOption("mongo-host","localhost","STRING","host with running mongo db")
  val mongoPort = new CmdOption("mongo-port", 27017, "INT", "port mongo db is running on")
  val dbname = new CmdOption("db-name", "tac", "STRING", "name of mongo db to write data into")
}

object LoadTacDataIntoMongo {

  val opts = new LoadTacDataOptions

  def main(args: Array[String]) : Unit = {
    opts.parse(args)

    val tReadStart = System.currentTimeMillis
    val kb = KBMatrix.fromTsv(opts.tacData.value)
    val tRead = (System.currentTimeMillis - tReadStart)/1000.0
    println(f"Reading from file took $tRead%.2f s")

    println("Stats:")
    println("Num Rows:" + kb.numRows())
    println("Num Cols:" + kb.numCols())
    println("Num cells:" + kb.nnz())

    val tWriteStart = System.currentTimeMillis
    val mongoClient = new MongoClient( opts.mongoHost.value , opts.mongoPort.value )
    val db:DB = mongoClient.getDB( opts.dbname.value )
    kb.writeToMongo(db)
    val tWrite = (System.currentTimeMillis - tWriteStart)/1000.0
    println(f"Writing to mongo took $tWrite%.2f s")

    val tReadMongoStart = System.currentTimeMillis
    val kb2 = KBMatrix.fromMongo(db)
    val tReadMongo = (System.currentTimeMillis - tReadMongoStart)/1000.0
    println(f"Reading from mongo took $tReadMongo%.2f s")

    val tCompareStart = System.currentTimeMillis
    val haveSameContent = kb.hasSameContent(kb2)
    val tCompare = (System.currentTimeMillis - tCompareStart)/1000.0
    println(f"Comparison of kbs took $tCompare%.2f s")

    if (haveSameContent) {
      println("OK: matrix in mongo has same content as matrix on disk")
    } else {
      println("FAILURE: matrix in mongo has different content as matrix on disk")
      println("Stats for mongo matrix:")
      println("Num Rows:" + kb2.numRows())
      println("Num Cols:" + kb2.numCols())
      println("Num cells:" + kb2.nnz())
    }

  }
}