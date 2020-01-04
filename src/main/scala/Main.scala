package example

import cats.effect.IO
import cats._
import cats.data._
import cats.implicits._
import cats.mtl.MonadState
import cats.mtl.implicits._

import cats.MonadError
import cats.mtl.{ApplicativeAsk, MonadState}

import pkr._ //what's a better way to do this
import pkr.DeckService.{newDeck, shuffleDeck, Deck, drawCard, burnCard}


//https://efekahraman.github.io/2019/07/monad-transformers-and-cats-mtl
//https://www.slideshare.net/RyanAdams12/jamie-pullar-cats-mtl-in-action

package object mainPackage {

  final case class Failure(message: String)

  type GameResult = GameState
  type EitherFailure[A] = Failure Either A
  type Db = String
  case class Player(name:String,hand:Hand,stackAmount:Int) //add PlayerTypes for playerType

  case class GameState (
    numberOfPlayers: Int,
    currentPlayers: Seq[Player],
    winner: Option[Player],
    currentDeckLength: Int,
    // currentDeck: Deck,
    potAmount: Double,
    currentRoundNumber: Int,
    roundStage: String, // (shiftPositions, collectBlinds, dealCards, preFlopBets, flop, turn, river, showdown)
    dbConnectionString: Db, //just a test remove later
  )

  type ReaderTEither[A, B] = ReaderT[EitherFailure, A, B]

  type StateTReaderTEither[A, B] = StateT[ReaderTEither[A, ?], GameState, B]

  trait PkrService {
    def game[F[_]](nRounds: Int)(
      implicit S: MonadState[F, GameState],
              A: ApplicativeAsk[F, Db],
              E: MonadError[F, Failure]
      ): F[GameResult] = for {
      resultState <- S.get
      readval <- A.reader(_.s.map(s => s))
      // count       =  queryCounts.getOrElse(id, 0L) + 1L
      // _           <- S.set(queryCounts + (id -> count))
      // result      <- E.rethrow(A.reader(_.user(id).map(u => (u, count))))
    } yield resultState

    //import cats.mtl.implicits._
    def materialisedGame(nRounds:Int) = game[StateT[ReaderT[EitherFailure, Db, ?],GameState,?]](nRounds)
  }

  type Hand = Seq[Card]
  //type GameResult = (Player, Hand)

  val suits = Seq(Diamond, Heart, Club, Spade)
  val ranks = Seq(Two, Three, Four, Five, Six, Seven, Eight, Nine, Ten, Jack, Queen, King, Ace)
  val deck:Deck = newDeck(ranks,suits)
  val shuffledDeck = shuffleDeck(deck)

}

import mainPackage._
object Program extends App with PkrService{
  //type Game[A] = StateT[IO,Deck,A]
  val initialState: GameState = GameState(
    numberOfPlayers = 0, //start with 4 players later
    currentPlayers = Seq(), //need to add all players at start
    winner = None,
    currentDeckLength = 52,
    // currentDeck = shuffledDeck,
    potAmount = 0,
    currentRoundNumber = 1,
    roundStage = "startRound", //  startRound (shiftPositions?, collectBlinds), dealCards, preFlopBets, flop, turn, river, showdown
    dbConnectionString = "",
  )
  
  def result: StateTReaderTEither[Db, GameResult] = for {
      res  <- materialisedGame(1)
  } yield res

  val g3 = Program.result.run(initialState).run("someText")
  println(g3)
}
  