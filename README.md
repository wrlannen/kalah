
# 6 Stone Kalah

The game is implemented as a REST API with the following routes (in [GameController](https://github.com/wrlannen/kalah/blob/master/src/main/java/com/lannen/kalah/GameController.java)). There is no UI.


----------


**POST /game**

Allows a player to start a new game.

*Parameters:*
 
 - None
  
*Returns:*
 - A ResponseEntity containing the [NewGame](https://github.com/wrlannen/kalah/blob/master/src/main/java/com/lannen/kalah/domain/NewGame.java) object, giving the player their id, position (north or south) and the [GameBoard](https://github.com/wrlannen/kalah/blob/master/src/main/java/com/lannen/kalah/domain/GameBoard.java). HTTP status CREATED (204).


----------


**POST /game/{gameId}**

Allows a player to join an existing game with the given gameId.

*Parameters:*

 - gameId - the id of the game to join

*Returns:*

 - On success, a ResponseEntity containing the [NewGame](https://github.com/wrlannen/kalah/blob/master/src/main/java/com/lannen/kalah/domain/NewGame.java) object, giving the player their id, position (north or south) and the [GameBoard](https://github.com/wrlannen/kalah/blob/master/src/main/java/com/lannen/kalah/domain/GameBoard.java). HTTP status OK (200). 
 - Failing calls will return a ResponseEntity with a suitable HTTP error code: 
	 - NOT_FOUND (404): game not found with the given id. 
	 - BAD_REQUEST (400): the game is already in progress.


----------


**GET /game/{gameId}**

Allows a player to check whether the next player is north or south.

*Parameters:*
gameId - the id of the game to check

*Returns:*

 - On success, a ResponseEntity containing a [NextPlayer](https://github.com/wrlannen/kalah/blob/master/src/main/java/com/lannen/kalah/domain/NextPlayer.java) object, giving the player the next player position. HTTP status OK (200). 
 - Failing calls will return a ResponseEntity with a suitable HTTP error code: 
	 - NOT_FOUND (404): game not found with the given id. 
	 - BAD_REQUEST (400): the game is not in progress.


----------


**POST /game/{gameId}/play**

Allows a player to play a move by sending a [GameMove](https://github.com/wrlannen/kalah/blob/master/src/main/java/com/lannen/kalah/domain/GameMove.java) object with their playerId and chosen pitId (0-5) to pick up stones from.

*Parameters:*

 - gameId - the id of the game to join 
 - gameMove - the suitably populated
   GameMove object

*Returns:*

 - On success, a ResponseEntity containing the updated [GameBoard](https://github.com/wrlannen/kalah/blob/master/src/main/java/com/lannen/kalah/domain/GameBoard.java) object. 
 - Failing calls will return a ResponseEntity with a suitable HTTP error code:
	 - BAD_REQUEST (400): the GameMove object is not suitably populated, or the chosen pit has no stones, or is not in the allowable range (0-5). 
	 - NOT_FOUND (404): game not found with the given id. 
	 - UNAUTHORIZED (401): the playerId given on the GameMove object is not the next player.


----------


Please refer to the Javadocs in the docs directory for further info.

The implementation uses SpringBoot and can therefore be run with ./gradlew bootRun.

Unit tests can be found in [GameControllerTest](https://github.com/wrlannen/kalah/blob/master/src/test/java/com/lannen/kalah/GameControllerTest.java).


----------


TODOs:

 1. There should be a WebSocket in order to push down the updated game board to the player during player, and to notify them of their turn.
 2. Unit tests should be added to test the error conditions for the routes, i.e. passing invalid data, etc.
 3. The playGame unit test tests 1000 plays of the game and checks that the winner is correct based on the kalah stone counts, however it is not actually testing that the mechanics of the game are working correctly.
