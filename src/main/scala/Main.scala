package example

import cats.effect.IO
//import cats.data.StateT
import pkr._ //what's a better way to do this
import pkr.DeckService.{newDeck, shuffleDeck, Deck, drawCard}

import cats._
import cats.data._
import cats.implicits._

//import parser.{doStuff => doParsingStuff}



object program extends App {
  type Game[A] = StateT[IO,Deck,A]

  val suits = Seq(Diamond, Heart, Club, Spade)
  val ranks = Seq(Two, Three, Four, Five, Six, Seven, Eight, Nine, Ten, Jack, Queen, King, Ace)
  val deck:Deck = newDeck(ranks,suits)
  val shuffledDeck = shuffleDeck(deck)

  val g: Game[Deck] = for {
    t <- StateT.get[IO,Deck]
    _ <- StateT.liftF[IO,Deck,Unit](IO {println(t)})
    //StateT[IO,Deck,Unit]{println(t)}
  } yield t

  g.run(shuffledDeck).unsafeRunSync()
  
  // val ioread = IO {scala.io.StdIn.readLine }

  
  // val (deck1, card1):(Deck,Card) = drawCard(shuffledDeck)
  // val (deck2, card2):(Deck,Card) = drawCard(deck1)

  // val program: IO[Unit] = 
  //   for {
    
  //     _ <- IO{println(card1)}
  //     _ <- IO{println(card2)}
  //     _ <- IO{println(shuffledDeck)}
  //    // _ <- IO{println(card1 < card2)} //need to implement card comparison
  //     _ <- IO{println(s"cards remaining ${deck2.length}")}
  //     _ <- IO{println(s"finished printing ###")}
  //   } yield ()

  //program.unsafeRunSync()

}