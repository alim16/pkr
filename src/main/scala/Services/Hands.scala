package pkr.hands

import pkr.cards._



trait HandsInterface {
    type Hand = Seq[Card]

    //maybe param should be map of player and hand, to identify winner
    def declareWinner(hands: Seq[Hand]):Hand = ???
        //pick the first in the list, compare with all others, if another hand beats it
        //remove this hand from list and repeat with winning hand

    def compareTwoHands(hand1: Hand, hand2: Hand): Hand = ???

    def sortHand(hand:Hand):Hand = ??? //just sort the cards in the hand
    
}

object Hands extends HandsInterface