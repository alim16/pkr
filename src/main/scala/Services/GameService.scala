package pkr.game

//import cats.effect.{IO,LiftIO}
import cats.effect._
import cats._
import cats.data._
import cats.implicits._
import cats.mtl.MonadState
import cats.mtl.implicits._

import cats.MonadError
import cats.mtl.{ApplicativeAsk, MonadState, FunctorRaise}

import pkr.players._
import pkr.cards._

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
}


trait GameServiceInterface{
    import pkr.game.gameTypes._

    def game[F[_]: Monad: LiftIO](nRounds: Int)(
      implicit S: MonadState[F, GameState],
              A: ApplicativeAsk[F, Db],
              E: FunctorRaise[F, Failure]
      ): F[GameState] = {
          val n = nRounds
          for {
            _ <- S.modify(modState_emptyDeck(_))
            _ <- S.modify(modState_incrementRound(_))
            state <- S.get
            _ <- state.gameInfo.currentRoundNumber match {
                    case 2 => S.get //TODO: change this to use nRounds param
                    case _ => game[F](state.gameInfo.currentRoundNumber)
                }
            readval <- A.reader(_.s.map(s => s))
            //resultState <- E.raise[GameState](Failure("problem!"))
            //resultState <- E.raise[GameState](someError())
            resultState <- S.get
            _ <- IO(println("printing something#####")).to[F]
            _ <- IO(println(resultState)).to[F]
        } yield resultState //.copy(currentDeckLength=0)

      }

    def modState_emptyDeck(state:GameState):GameState = {
    state.copy(roundInfo=state.roundInfo.copy(currentDeckLength=0)) //TODO: change this to actually empty the card deck
    }
    def modState_incrementRound(state:GameState):GameState = {
    state.copy(gameInfo=state.gameInfo.copy(currentRoundNumber=state.gameInfo.currentRoundNumber+1))
    }
}

object GameService extends GameServiceInterface