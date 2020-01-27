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

package object gameTypes {
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
    //currentDeck: Deck,
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
          val n = nRounds
          for {
            _ <- S.modify(modState_emptyDeck(_))
            state <- S.get
           _ <- repeatNtimes(runRound[F],5)
            readval <- A.reader(_.s.map(s => s))
            //resultState <- E.raise[GameState](Failure("problem!"))
            //resultState <- E.raise[GameState](someError())
            resultState <- S.get
            _ <- IO(println("####value from the reader is: "+readval)).to[F]
            _ <- insertValues(state.gameInfo.initialNumberOfPlayers, "someName",
                      state.gameInfo.currentRoundNumber).to[F] //TODO: add check for succes and if not raise dbFailure
        } yield resultState
    }

 
    def runRound[F[_]: Monad: LiftIO]()(
      implicit S: MonadState[F, GameState],
              E: FunctorRaise[F, Failure]
      ): F[GameState] = {
        for{
            currentState <- S.get
            _ <- IO(println(s"current round is: ##### ${currentState.gameInfo.currentRoundNumber}")).to[F]
            _ <- S.modify(modState_incrementRound(_))
            _ <- S.modify(modState_updateBoardCards(_))
            res <- S.get
            _ <- IO(println(res)).to[F]
        } yield res
    }

    def modState_emptyDeck(state:GameState):GameState = {//TODO: change this to actually empty the card deck
      val roundInfo: Lens[GameState, RoundInfo] = GenLens[GameState](_.roundInfo)
      val deckLength: Lens[RoundInfo,Int] = GenLens[RoundInfo](_.currentDeckLength)
      (deckLength compose roundInfo ).set( 0)(state)
      ///state.copy(roundInfo=state.roundInfo.copy(currentDeckLength=0)) 
    }
    def modState_incrementRound(state:GameState):GameState = {
      val gameInfo: Lens[GameState, GameInfo] = GenLens[GameState](_.gameInfo)
      val roundInfo: Lens[GameState, RoundInfo] = GenLens[GameState](_.roundInfo)
      val currentRound: Lens[GameInfo,Int] = GenLens[GameInfo](_.currentRoundNumber)
      (currentRound compose gameInfo ).modify(_ + 1)(state)
    }

    def modState_updateBoardCards(state:GameState):GameState = { //TODO: drawCards from deck and update deck
      val roundInfo: Lens[GameState, RoundInfo] = GenLens[GameState](_.roundInfo)
      val boardCards: Lens[RoundInfo,Seq[Card]] = GenLens[RoundInfo](_.boardCards)
      val cardList: Seq[Card] = List(Card(Three,Heart), Card(Jack, Diamond), Card(Ten,Club))
     // val someCards: Seq[Card] = List(drawCard(state.roundInfo.currentDeck)._2)
      (boardCards compose roundInfo ).set(state.randGen.shuffle(cardList))(state)
    }

    def repeatNtimes[F[_]: Monad](f: () => F[GameState], n: Int)( //TODO: replace this function with something decent
      implicit S: MonadState[F, GameState]): F[GameState]= { 
      n match {
        case n if n <= 1 => for {s <- S.get} yield s  
        case _ => for {
          _ <- repeatNtimes(f,n-1)
          res <- f()
        } yield res
      }
    }
}

object GameService extends GameServiceInterface