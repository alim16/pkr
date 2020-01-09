package example

import cats.effect.IO
import cats._
import cats.data._
import cats.implicits._
import cats.mtl.MonadState
import cats.mtl.implicits._

import cats.MonadError
import cats.mtl.{ApplicativeAsk, MonadState}

import pkr.cards._ //what's a better way to do this
import pkr.cards.DeckService.{newDeck, shuffleDeck, Deck, drawCard, burnCard}

import pkr.players.PlayerService.{initialisePlayers}
import pkr.players._

import pkr.hands.Hands.{Hand}


//https://efekahraman.github.io/2019/07/monad-transformers-and-cats-mtl
//https://www.slideshare.net/RyanAdams12/jamie-pullar-cats-mtl-in-action

package object mainPackage {

  final case class Failure(message: String)

  type GameResult = GameState
  type EitherFailure[A] = Failure Either A
  type Db = String

  case class GameInfo(
     initialNumberOfPlayers: Int,
     currentRoundNumber: Int,
     dbConnectionString: Db, //TODO: just a test remove later
     roundWinner: Option[Seq[Player]], //TODO: maybe add to roundInfo
  )

  case class RoundInfo(
    currentPlayers: Seq[Player],
    //dealer: Player,
    // currentDeck: Deck,
    currentDeckLength: Int, //TODO: remove later, just for testing 
    potAmount: Double,
    boardCards: Seq[Card],
    roundStage: String, // (shiftPositions, collectBlinds, dealCards, preFlopBets, flop, turn, river, showdown)
  )

  case class GameState (
    gameInfo: GameInfo,
    roundInfo: RoundInfo
  )

  type ReaderTEither[A, B] = ReaderT[EitherFailure, A, B]

  type StateTReaderTEither[A, B] = StateT[ReaderTEither[A, ?], GameState, B]

  trait PkrService {
    def game[F[_]](nRounds: Int)(
      implicit S: MonadState[F, GameState],
              A: ApplicativeAsk[F, Db],
              E: MonadError[F, Failure]
      ): F[GameResult] = for {
      _ <- S.modify(modState_emptyDeck(_))
      resultState <- S.get
      readval <- A.reader(_.s.map(s => s))
    
    } yield resultState //.copy(currentDeckLength=0)

    def modState_emptyDeck(state:GameState):GameState = {
      state.copy(roundInfo=state.roundInfo.copy(currentDeckLength=0)) //TODO: change this to actually empty the card deck
    }

    //import cats.mtl.implicits._
    def materialisedGame(nRounds:Int) = game[StateT[ReaderT[EitherFailure, Db, ?],GameState,?]](nRounds)
  }

  //type GameResult = (Player, Hand)

  val suits = Seq(Diamond, Heart, Club, Spade)
  val ranks = Seq(Two, Three, Four, Five, Six, Seven, Eight, Nine, Ten, Jack, Queen, King, Ace)
  val deck:Deck = newDeck(ranks,suits)
  val shuffledDeck = shuffleDeck(deck)

}

import mainPackage._
object Program extends App with PkrService{

  val gameInfo = GameInfo(
     initialNumberOfPlayers = 4,
     currentRoundNumber = 1,
     roundWinner = None,
     dbConnectionString = ""
  )

  val roundInfo: RoundInfo = RoundInfo(
    currentPlayers = initialisePlayers(4), //need to add all players at start
    currentDeckLength = 52,
    // currentDeck = shuffledDeck,
    potAmount = 0,
    boardCards = Seq(),
    roundStage = "startRound"
  )

  //type Game[A] = StateT[IO,Deck,A]
  val initialState: GameState = GameState(
    gameInfo=gameInfo, roundInfo=roundInfo
  )
  
  def result: StateTReaderTEither[Db, GameResult] = for {
      res  <- materialisedGame(1)
  } yield res

  val g3 = Program.result.run(initialState).run("someText")
  println(g3)
}
  