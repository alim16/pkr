import org.scalatest._
import pkr.cards._
import pkr.hands._
import pkr.hands.Hands.{sortHand, Hand}

class HandSpec extends FlatSpec {

    "A hand" should "be sortable" in {
      val hand = sortHand(generateHand(5))
      
      assert(hand(3) == hand(2))
      assert(hand(4) > hand(3))
      assert(hand(1) > hand(0))
 
      //assert(stack.pop() === 1)
    }

    def generateHand(n: Int):Hand = { //TODO: change to automated generation of hand of cards
     return Seq( 
             Card(Jack,Spade), 
             Card(Jack,Diamond),
             Card(Ten,Diamond),
             Card(Three,Diamond),
             Card(King,Heart)
            )
    }
  
}  