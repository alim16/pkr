                        #### WORK IN PROGRESS #####
#Description
A purely functional texas hold'em poker simulation, with the option to
run it automatically with a chosen number of players or include one manual player and
make the choices. it saves the results of the game and rounds in a postgres db
- it's written in a modular programming style with services
- the main for expression uses a StateT monad with IO and GameState ///not sure about this
- 

#Game state includes
- number of players with their info (stack, position, pocketCards, hand)
- potAmount
- current player
- dealer
- current round number
- boardCards: Seq[Card]
- roundStage // startRound (shiftPositions?, collectBlinds), dealCards, preFlopBets, flop, turn, river, showdown
- 


#4 Player types
- tight-aggressive (probably best type of player)
- tight-passive
- loose-aggresive
- loose-passive