package pkr.players

import pkr.cards._
import pkr.hands.Hands.{Hand}

case class Player(name:String,bestHand:Hand,stackAmount:Int,pocketCards:Seq[Card]) //add PlayerTypes for playerType

trait PlayerServiceInterface {
   def initialisePlayers(nPlayers: Int):Seq[Player] = {
       val players:Seq[Player] = for (i <- 1 to nPlayers) yield Player(name=s"p${i}",bestHand=Seq(),
                            stackAmount=100,pocketCards=Seq())
       players
    }
}

object PlayerService extends PlayerServiceInterface