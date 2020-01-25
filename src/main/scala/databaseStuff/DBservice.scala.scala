package pkr.db

import doobie._
import doobie.implicits._

import doobie.util.ExecutionContexts

import cats._
import cats.data._
import cats.implicits._
import cats.mtl.MonadState
import cats.mtl.implicits._

import cats.MonadError
import cats.mtl.{ApplicativeAsk, MonadState}

import cats.effect.IO
import scala.concurrent.ExecutionContext


//doobie
// val xa = Transactor.fromDriverManager[IO]("org.postgresql.Driver", "jdbc:postgresql:postgres", "postgres")
//https://blog.oyanglul.us/scala/3-layer-cake

trait DbServiceInterface {
    implicit val cs = IO.contextShift(ExecutionContext.global)

    //move this to SQL object
  

    val xa = Transactor.fromDriverManager[IO](
  "org.postgresql.Driver", "jdbc:postgresql://localhost:5432/postgres", "postgres", "docker")

    //def getValue() = SQL.dropTable.run.transact(xa)
    def insertValues(nPlayers: Int,winnerName: String, numberOfRounds: Int) = 
        SQL.insertGameInfo(nPlayers,winnerName,numberOfRounds).run.transact(xa)//.unsafeRunSync()
}

object DbService extends DbServiceInterface

object SQL {
    def createTable: Update0  = sql"""CREATE TABLE IF NOT EXISTS 
     GameInfo (GameId SERIAL PRIMARY KEY, initialNumberOfPlayers int, WinnerName varchar, 
        numberOfRounds int)""".update

    def dropTable: Update0 = sql"""Drop TABLE IF EXISTS 
     GameInfo2""".update

    def insertGameInfo(nPlayers: Int,winnerName: String, numberOfRounds: Int): Update0  =
     sql"""insert into GameInfo (initialNumberOfPlayers, WinnerName, numberOfRounds)
      values ($nPlayers, $winnerName, $numberOfRounds)""".update
}


