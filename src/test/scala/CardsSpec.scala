import org.scalatest._
import pkr.cards._

class CardSpec extends FlatSpec {

    "Comparison operator" should "only check Rank in cards" in {
      val jack = Card(Jack,Spade) 
      val jack2 = Card(Jack,Diamond)
      val ten = Card(Ten,Diamond)
      val three = Card(Three,Diamond) 
      assert(jack > ten)
      assert(three < ten)
      assert(jack == jack2)
      // assertResult(false){
      //     jack < jack2
      // }
    }
  
}  