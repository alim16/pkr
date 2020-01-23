package pkr.main

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

import pkr.game.gameTypes._ //{GameResult, GameState, Db, Failure}

//https://www.beyondthelines.net/programming/introduction-to-tagless-final/
//https://medium.com/rahasak/doobie-and-cats-effects-d01230be5c38
// https://www.rea-group.com/blog/a-journey-into-extensible-effects-in-scala/
//https://efekahraman.github.io/2019/07/monad-transformers-and-cats-mtl
//https://www.slideshare.net/RyanAdams12/jamie-pullar-cats-mtl-in-action

package object mainPackage {
  trait PkrService {
    import pkr.game.GameService.{game}
    def materialisedGame(nRounds:Int) = game[StateTReaderTEither](nRounds)
  }
}


import mainPackage._
object Program extends App with PkrService{

  val initGameInfo = GameInfo(
     initialNumberOfPlayers = 4,
     currentRoundNumber = 1,
     roundWinner = None,
     dbConnectionString = ""
  )

  val initRoundInfo: RoundInfo = RoundInfo(
    currentPlayers = initialisePlayers(4), //need to add all players at start
    currentDeckLength = 52,
    // currentDeck = shuffledDeck,
    potAmount = 0,
    boardCards = Seq(),
    roundStage = "startRound"
  )

  //type Game[A] = StateT[IO,Deck,A]
  val initialState: GameState = GameState(
    gameInfo=initGameInfo, roundInfo=initRoundInfo
  )

  val suits = Seq(Diamond, Heart, Club, Spade)
  val ranks = Seq(Two, Three, Four, Five, Six, Seven, Eight, Nine, Ten, Jack, Queen, King, Ace)
  val deck:Deck = newDeck(ranks,suits)
  val shuffledDeck = shuffleDeck(deck)

  def result: StateTReaderTEither[GameState] = for {
      res  <- materialisedGame(5)
  } yield res
   val gameRes = Program.result.run((initialState)).run("some config info")
   gameRes.value.unsafeRunSync()

  //TODO: do the unsafe IO thing here //something.unsafeRunSync() //might require some type changes
  //gameRes.map(x => println(x._2))
 

  // import pkr.db.DbService.{getValue}
  // getValue.unsafeRunSync
}
  