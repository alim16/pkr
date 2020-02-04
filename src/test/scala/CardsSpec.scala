import org.scalatest._
import pkr.cards._

class CardSpec extends FlatSpec {
    val jackSpade = Card(Jack,Spade) 
    val jackDiamond = Card(Jack,Diamond)
    val tenDiamond1 = Card(Ten,Diamond)
    val tenDiamond2 = Card(Ten,Diamond)
    val threeDiamond = Card(Three,Diamond) 

    "A Card" should "only be equal to itself " in {
        assert(tenDiamond1 == tenDiamond2)
        assert(jackSpade != jackDiamond)
    }

    "Cards" should "be comparable by rank" in {
      assert(jackSpade > tenDiamond1)
      assert(threeDiamond < tenDiamond1)
      assert(jackSpade.rank == jackDiamond.rank)
      // assertResult(false){
      //     jackSpade < jackDiamond
      // }
    }

    "Cards" should "be comparable by suit" in {
      assert(jackSpade.suit != jackDiamond.suit)
      assert(tenDiamond1.suit == threeDiamond.suit)
    }
  
}  