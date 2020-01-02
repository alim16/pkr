import org.scalatest._
import pkr._ //make a package for card

class CardSpec extends FlatSpec {

    "Comparison operator" should "only check Rank in cards" in {
      val jack = Card(Jack,Spade) 
      val jack2 = Card(Jack,Diamond)
      val ten = Card(Ten,Diamond)
      val three = Card(Three,Diamond) 
      assert(jack > ten)
      assert(three < ten)
      assertResult(false){
          jack > jack
      }
      //assert(stack.pop() === 1)
    }
  
}  