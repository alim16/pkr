package pkr.game

//import cats.effect.{IO,LiftIO}
import cats.effect._
import cats._
import cats.data._
import cats.implicits._
import cats.mtl.MonadState
import cats.mtl.implicits._
import cats.{Monoid, MonoidK} //TODO: not used, remove

import cats.MonadError
import cats.mtl.{ApplicativeAsk, MonadState, FunctorRaise}

import monocle.macros.GenLens
import monocle.Lens
import scala.util.Random

import pkr.players._
import pkr.cards._
import pkr.cards.DeckService.{newDeck, shuffleDeck, Deck, drawCard}

//import pkr.main.mainPackage.{GameResult, GameState, Db, Failure} //TODO: change, this is stupid

 object gameTypes {
  sealed case class Failure()

  type GameResult = GameState
  type EitherFailure[A] = EitherT[IO, Failure,  A]
  type Db = String

  type ReaderTEither[A] = ReaderT[EitherFailure,Db, A]
  type StateTReaderTEither[A] = StateT[ReaderTEither, GameState, A]

  case class GameInfo(
     initialNumberOfPlayers: Int,
     currentRoundNumber: Int,
     dbConnectionString: Db, //TODO: just a test remove later
     roundWinner: Option[Seq[Player]], //TODO: maybe add to roundInfo
  )

  case class RoundInfo(
    currentPlayers: Seq[Player],
    //dealer: Player,
    currentDeck: Deck,
    currentDeckLength: Int, //TODO: remove later, just for testing 
    potAmount: Double,
    boardCards: Seq[Card],
    roundStage: String, // (shiftPositions, collectBlinds, dealCards, preFlopBets, flop, turn, river, showdown)
  )

  case class GameState (
    gameInfo: GameInfo,
    roundInfo: RoundInfo,
    randGen: Random
  )
}


trait GameServiceInterface{
    import pkr.game.gameTypes._
    import pkr.db.DbService.{insertValues}

    def game[F[_]: Monad: LiftIO](nRounds: Int)(
      implicit S: MonadState[F, GameState],
              A: ApplicativeAsk[F, Db],
              E: FunctorRaise[F, Failure]
      ): F[GameState] = {
          val n = 5
          for {
            _ <- IO(println("### Please enter number of rounds to run...")).to[F]
            n <- IO(scala.io.StdIn.readInt).to[F]
            _ <- S.modify(modState_emptyDeck(_))
            state <- S.get
           _ <- repeatNtimes(runRound[F],n)
            readval <- A.reader(_.s.map(s => s))
            //resultState <- E.raise[GameState](Failure("problem!"))
            //resultState <- E.raise[GameState](someError())
            resultState <- S.get
            _ <- IO(println("####value from the reader is: "+readval)).to[F]
           // _ <- insertValues(state.gameInfo.initialNumberOfPlayers, "someName",
             //         state.gameInfo.currentRoundNumber).to[F] //TODO: add check for succes and if not raise dbFailure
        } yield resultState
    }

 
    def runRound[F[_]: Monad: LiftIO]()(
      implicit S: MonadState[F, GameState],
              E: FunctorRaise[F, Failure]
      ): F[GameState] = {
        for{
            currentState <- S.get
            _ <- IO(println(s"current round is: ##### ${currentState.gameInfo.currentRoundNumber}")).to[F]
            _ <- IO(println(s"number of cards in deck at start of Round: ##### ${currentState.roundInfo.currentDeck.length}")).to[F]
            _ <- S.modify(modState_incrementRound(_))
            _ <- S.modify(modState_updateBoardCards(_))
            res <- S.get
          //  _ <- IO(println(res)).to[F]
        } yield res
    }

    def modState_emptyDeck(state:GameState):GameState = {//TODO: change this to actually empty the card deck
      import pkr.myLenses.stateLenses._
      (deckLength compose roundInfo ).set( 0)(state)
      ///state.copy(roundInfo=state.roundInfo.copy(currentDeckLength=0)) 
    }
    def modState_incrementRound(state:GameState):GameState = {
      import pkr.myLenses.stateLenses._
      (currentRound compose gameInfo ).modify(_ + 1)(state)
    }

    def modState_updateBoardCards(state:GameState):GameState = {
      import pkr.myLenses.stateLenses._
      val maybeCards: Option[(Seq[Card],Deck)] = for {
         (newDeck1,card1) <- drawCard(state.roundInfo.currentDeck)
         (newDeck2,card2) <- drawCard(newDeck1)
         (newDeck3,card3) <- drawCard(newDeck2)
      } yield (Seq(card1,card2,card3), newDeck3)

      val cardsAndDeck: (Seq[Card],Deck) = maybeCards.getOrElse((List(),
      (state.roundInfo.currentDeck))) //TODO: change, getOrElse bad

      (deck compose roundInfo ).set(cardsAndDeck._2)(state)

      //(boardCards compose roundInfo ).set(state.randGen.shuffle(cardsAndDeck._1))(state)
    }

    def repeatNtimes[F[_]: Monad](f: () => F[GameState], n: Int)( //TODO: replace this function with something decent
      implicit S: MonadState[F, GameState]): F[GameState]= { 
      n match {
        case n if n < 1 => for {s <- S.get} yield s  
        case _ => for {
          _ <- repeatNtimes(f,n-1)
          res <- f()
        } yield res
      }
    }
}

object GameService extends GameServiceInterface