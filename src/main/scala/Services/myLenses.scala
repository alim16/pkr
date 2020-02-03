package pkr.myLenses

import monocle.macros.GenLens
import monocle.Lens

import pkr.cards._
import pkr.cards.DeckService.{Deck}

import pkr.game.gameTypes._
object stateLenses{

      val gameInfo: Lens[GameState, GameInfo] = GenLens[GameState](_.gameInfo)
      val currentRound: Lens[GameInfo,Int] = GenLens[GameInfo](_.currentRoundNumber)

      val roundInfo: Lens[GameState, RoundInfo] = GenLens[GameState](_.roundInfo)
      val deckLength: Lens[RoundInfo,Int] = GenLens[RoundInfo](_.currentDeckLength)
      val boardCards: Lens[RoundInfo,Seq[Card]] = GenLens[RoundInfo](_.boardCards)
      val deck: Lens[RoundInfo,Deck] = GenLens[RoundInfo](_.currentDeck)
      
      //(deckLength compose roundInfo ).set( 0)(state)
}