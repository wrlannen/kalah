# 6 Stone Kalah

The game is implemented as a REST API with the following routes (in GameController). There is no UI.


**POST /game**: Allows a player to start a new game.

**POST /game/{gameId}**: Allows a player to join an existing game with the given gameId.

**GET /game/{gameId}**: Allows a player to check whether the next player is north or south.

**POST /game/{gameId}/play**: Allows a player to play a move by sending a GameMove object with their playerId and chosen pitId (0-5) to pick up stones from.


Please refer to the Javadocs in the docs directory for further info.

The implementation uses SpringBoot and can therefore be run with ./gradlew bootRun.


----------


TODOs:

 1. There should be a WebSocket in order to push down the updated game board to the player during player, and to notify them of their turn.
 2. Unit tests should be added to test the error conditions for the routes, i.e. passing invalid data, etc.
 3. The playGame unit test tests 1000 plays of the game and checks that the winner is correct based on the kalah stone counts, however it is not actually testing that the mechanics of the game are working correctly.
