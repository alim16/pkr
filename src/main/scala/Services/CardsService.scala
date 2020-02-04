package pkr.cards

//import scala.util.Random.shuffle
import scala.util.Random

case class Card (rank:Rank,suit:Suit) extends Ordered[Card]{
    //isFaceCard: Boolean
   def compare(that: Card) = { //need to move this somewhere else
        if (this.rank.value == that.rank.value){
            0
        } else if (this.rank.value > that.rank.value) {
            1
        } else {
            -1
        }
    }
}

sealed trait Suit
case object Diamond extends Suit
case object Heart extends Suit
case object Club extends Suit
case object Spade extends Suit

sealed trait Rank extends Ordered[Rank]{
    def value: Int
    def compare(that: Rank): Int = this.value - that.value
}
case object Two extends Rank {val value = 2}
case object Three extends Rank  {val value = 3}
case object Four extends Rank  {val value = 4}
case object Five extends Rank  {val value = 5}
case object Six extends Rank  {val value = 6}
case object Seven extends Rank  {val value = 7}
case object Eight extends Rank  {val value = 8}
case object Nine extends Rank  {val value = 9}
case object Ten extends Rank  {val value = 10}
case object Jack extends Rank  {val value = 11}
case object Queen extends Rank  {val value = 12}
case object King extends Rank  {val value = 13}
case object Ace extends Rank  {val value = 14}


trait DeckServiceInterface {
    type Deck = Seq[Card]
    implicit val suits = Seq(Diamond, Heart, Club, Spade)
    implicit val ranks = Seq(Two, Three, Four, Five, Six, Seven, Eight, Nine, Ten, Jack, Queen, King, Ace)

    def newDeck():Deck = for {
        s <- suits
        r <- ranks
    } yield Card(r,s)

    def shuffleDeck(deck: Deck, randGen:Random):Deck = randGen.shuffle(deck) //TODO: check if shuffling correctly
    
    //TODO: write tests for this
    def drawCard:Function[Deck, Option[(Deck, Card)]] = { 
        case head +: tail => Some(tail, head)
        case _ => None
    }

    //TODO: write tests for this
    //TODO: why this returning Option[Deck] only work with this signature?
    def burnCard:Function[Deck, Option[Deck]] = {
        case head +: tail => Some(tail)
        case _ => None
    }

 }

 object DeckService extends DeckServiceInterface
