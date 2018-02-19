package com.lannen.kalah;

import com.lannen.kalah.domain.GameBoard;
import com.lannen.kalah.domain.GameMove;
import com.lannen.kalah.domain.NewGame;
import com.lannen.kalah.domain.NextPlayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Provides 4 REST routes for playing six stone Kalah, as documented below.
 * @author William Lannen
 */
@RestController
public class GameController {
  private static final Logger LOG = LoggerFactory.getLogger(GameController.class);
  private static final String MEDIA_TYPE_APPLICATION_JSON = "application/json";
  private static final int OFFSET_FOR_OPPONENT_PIT = 5;

  private Map<String, GameBoard> games = new ConcurrentHashMap<>();

  /**
   * POST /game
   *
   * Allows a player to start a new game.
   *
   * @return a ResponseEntity containing the NewGame object, giving the player their id, position (north or south)
   * and the game board. HTTP status CREATED (204).
   */
  @RequestMapping(method = RequestMethod.POST, value = "/game", produces = MEDIA_TYPE_APPLICATION_JSON)
  public ResponseEntity<NewGame> newGame() {
    LOG.info("newGame");

    GameBoard gameBoard = new GameBoard();

    final String playerId = UUID.randomUUID().toString();

    // Randomly make the player north or south
    GameBoard.PlayerPosition playerPosition;
    if (ThreadLocalRandom.current().nextInt(0, 2) == 0) {
      gameBoard.setNorthPlayerId(playerId);
      playerPosition = GameBoard.PlayerPosition.NORTH;
    } else {
      gameBoard.setSouthPlayerId(playerId);
      playerPosition = GameBoard.PlayerPosition.SOUTH;
    }

    games.put(gameBoard.getGameId(), gameBoard);

    final ResponseEntity<NewGame> response
        = new ResponseEntity<>(new NewGame(playerId, playerPosition, gameBoard), HttpStatus.CREATED);

    LOG.debug("newGame: returning response: {}", response);

    return response;
  }

  /**
   * POST /game/{gameId}
   *
   * Allows a player to join an existing game with the given gameId.
   *
   * @param gameId the id of the game to join
   *
   * @return on success, a ResponseEntity containing the NewGame object, giving the player their id, position
   * (north or south) and the game board. HTTP status OK (200).
   *
   * Failing calls will return a ResponseEntity with a suitable HTTP error code:
   *
   * NOT_FOUND (404): game not found with the given id.
   * BAD_REQUEST (400): the game is already in progress.
   */
  @RequestMapping(method = RequestMethod.POST, value = "/game/{gameId}", produces = MEDIA_TYPE_APPLICATION_JSON)
  public ResponseEntity<NewGame> joinGame(@PathVariable String gameId) {
    LOG.info("joinGame: gameId: {}", gameId);

    final GameBoard gameBoard = games.get(gameId);

    if (gameBoard == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    if (gameBoard.getCurrentGameStatus() != GameBoard.GameStatus.NOT_STARTED) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    final String playerId = UUID.randomUUID().toString();

    // Put the player in the free position
    GameBoard.PlayerPosition playerPosition;
    if (gameBoard.getSouthPlayerId() != null) {
        gameBoard.setNorthPlayerId(playerId);
        playerPosition = GameBoard.PlayerPosition.NORTH;
    }
    else {
      gameBoard.setSouthPlayerId(playerId);
      playerPosition = GameBoard.PlayerPosition.SOUTH;
    }

    gameBoard.setCurrentGameStatus(GameBoard.GameStatus.IN_PROGRESS);

    final ResponseEntity<NewGame> response
      = new ResponseEntity<>(new NewGame(playerId, playerPosition, gameBoard), HttpStatus.OK);

    LOG.debug("joinGame: returning response: {}", response);

    return response;
  }

  /**
   * GET /game/{gameId}
   *
   * Allows a player to check whether the next player is north or south. Can be polled in order to determine when to
   * play a move.
   *
   * @param gameId the id of the game to check
   *
   * @return on success, a ResponseEntity containing a NextPlayer object, giving the player the next player
   * position. HTTP status OK (200).
   *
   * Failing calls will return a ResponseEntity with a suitable HTTP error code:
   *
   * NOT_FOUND (404): game not found with the given id.
   * BAD_REQUEST (400): the game is not in progress.
   */
  @RequestMapping(method = RequestMethod.GET, value = "/game/{gameId}", produces = MEDIA_TYPE_APPLICATION_JSON)
  public ResponseEntity<NextPlayer> getNextPlayer(@PathVariable String gameId) {
    LOG.debug("getNextPlayer: gameId: {}", gameId);

    final GameBoard gameBoard = games.get(gameId);

    if (gameBoard == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    if (gameBoard.getCurrentGameStatus() != GameBoard.GameStatus.IN_PROGRESS) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    final ResponseEntity<NextPlayer> response
        = new ResponseEntity<NextPlayer>(new NextPlayer(gameBoard.getNextPlayer()), HttpStatus.OK);

    LOG.debug("getNextPlayer: returning response: {}", response);

    return response;
  }


  /**
   * POST /game/{gameId}/play
   *
   * Allows a player to play a move by sending a GameMove object with their playerId and chosen pitId (0-5) to pick
   * up stones from.
   *
   * @param gameId the id of the game to join
   * @param gameMove the suitably populated GameMove object
   *
   * @return on success, a ResponseEntity containing the updated GameBoard object.
   *
   * Failing calls will return a ResponseEntity with a suitable HTTP error code:
   *
   * BAD_REQUEST (400): the GameMove object is not suitably populated, or the chosen pit has no stones, or is
   * not in the allowable range (0-5).
   * NOT_FOUND (404): game not found with the given id.
   * UNAUTHORIZED (401): the playerId given on the GameMove object is not the next player.
   *
   */
  @RequestMapping(method = RequestMethod.POST, value = "/game/{gameId}/play", consumes = MEDIA_TYPE_APPLICATION_JSON, produces = MEDIA_TYPE_APPLICATION_JSON)
  public ResponseEntity<GameBoard> playGame(@PathVariable String gameId, @RequestBody GameMove gameMove) {
    LOG.debug("playGame: gameId: {}, gameMove: {}", gameId, gameMove);

    if (gameMove == null || StringUtils.isEmpty(gameMove.getPlayerId()) || StringUtils.isEmpty(gameMove.getPitId())) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    final GameBoard gameBoard = games.get(gameId);

    if (gameBoard == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    final String playerId = gameMove.getPlayerId();

    if (!gameBoard.getNextPlayerId().equals(playerId)) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    int pitId = gameMove.getPitId();

    if (pitId < 0 || pitId >= GameBoard.KALAH_INDEX) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    int[] playerPits;
    int[] opponentPits;

    GameBoard.PlayerPosition opponentPosition;
    if (gameBoard.getNorthPlayerId().equals(playerId)) {
      playerPits = gameBoard.getNorthPits();
      opponentPits = gameBoard.getSouthPits();
      opponentPosition = GameBoard.PlayerPosition.SOUTH;
    } else {
      opponentPits = gameBoard.getNorthPits();
      playerPits = gameBoard.getSouthPits();
      opponentPosition = GameBoard.PlayerPosition.NORTH;
    }

    // Take the stones from the chosen pit
    int stones = playerPits[pitId];
    playerPits[pitId] = 0;

    if (stones == 0) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    // Drop the stones to the right
    while (stones > 0) {
      pitId++;

      LOG.trace("Dropping stones: stones: {}, pitId: {}", stones, pitId);

      // Drop in any of the player's pits including the kalah
      if (pitId <= GameBoard.KALAH_INDEX) {
        playerPits[pitId]++;
        stones--;
      } else {
        final int opponentPitId = pitId - playerPits.length;

        LOG.trace("Dropping stones in opponents pits: stones: {}, pitId: {}, opponentPitId: {}",
            stones, pitId, opponentPitId);

        // If we get to the opponent's kalah, don't drop a stone and loop back around
        // to the player's pits
        if (opponentPitId == GameBoard.KALAH_INDEX) {
          pitId = 0;
        } else {
          // Drop a stone in the opponent's pit
          opponentPits[opponentPitId]++;
          stones--;
        }
      }
    }

    checkToTakeStonesFromOpponentPit(pitId, playerPits, opponentPits);
    checkForGameOver(gameBoard, playerPits, opponentPits);

    // If the game is over, set the winner, otherwise set the next player
    if (gameBoard.getCurrentGameStatus() == GameBoard.GameStatus.GAME_OVER) {
      setWinner(gameBoard, playerPits, opponentPits, opponentPosition);
    } else {
      gameBoard.setNextPlayer(opponentPosition);
    }

    final ResponseEntity<GameBoard> response = new ResponseEntity<>(gameBoard, HttpStatus.OK);

    if (gameBoard.getCurrentGameStatus() == GameBoard.GameStatus.GAME_OVER) {
      LOG.info("playGame: GAME OVER: returning response: {}", response);
    }
    else {
      LOG.debug("playGame: returning response: {}", response);
    }

    return response;
  }

  private void setWinner(final GameBoard gameBoard, final int[] playerPits, final int[] opponentPits,
      final GameBoard.PlayerPosition opponentPosition) {
    LOG.debug("setWinner: playerPits: {}, opponentPits: {}, opponentPosition: {}, gameBoard: {}",
        playerPits, opponentPits, opponentPosition, gameBoard);

    if (playerPits[GameBoard.KALAH_INDEX] > opponentPits[GameBoard.KALAH_INDEX]) {
      gameBoard.setWinner(gameBoard.getNextPlayer()); // current player as this hasn't been changed yet
    } else if (playerPits[GameBoard.KALAH_INDEX] == opponentPits[GameBoard.KALAH_INDEX]) {
      gameBoard.setWinner(null);
    } else {
      gameBoard.setWinner(opponentPosition);
    }
  }

  /**
   * Checks if it is game over, i.e. one of the player's has no stones left in their pits (excluding their kalah). If
   * it is game over, the game board status is updated accordingly.
   *
   * @param gameBoard the game board
   * @param playerPits the player's pits
   * @param opponentPits the opponent's pits
   */
  private void checkForGameOver(final GameBoard gameBoard, final int[] playerPits, final int[] opponentPits) {
    LOG.debug("checkForGameOver: playerPits: {}, opponentPits: {},  gameBoard: {}", playerPits, opponentPits, gameBoard);

    final int playerTotalStones = getStoneCountForPitsExcludingKalahs(playerPits);
    final int opponentTotalStones = getStoneCountForPitsExcludingKalahs(opponentPits);

    if (playerTotalStones == 0) {
      opponentPits[GameBoard.KALAH_INDEX] += opponentTotalStones;
      gameBoard.setCurrentGameStatus(GameBoard.GameStatus.GAME_OVER);
    } else if (opponentTotalStones == 0) {
      playerPits[GameBoard.KALAH_INDEX] += opponentTotalStones;
      gameBoard.setCurrentGameStatus(GameBoard.GameStatus.GAME_OVER);
    }
  }

  /**
   * If the final pit is one of the player's pits (excluding their kalah) and it was empty, they move the stone from
   * that pit and those from the opponent's opposite one to their kalah.
   * @param pitId the final pit
   * @param playerPits the player's pits
   * @param opponentPits the opponent's pits
   */
  private void checkToTakeStonesFromOpponentPit(final int pitId, final int[] playerPits, final int[] opponentPits) {
    LOG.debug("checkToTakeStonesFromOpponentPit: pitId: {}, playerPits: {},  opponentPits: {}",
        pitId, playerPits, opponentPits);

    if (pitId < GameBoard.KALAH_INDEX) {
      // If the pit just has the stone played in it, then it was an empty pit
      if (playerPits[pitId] == 1) {
        /**
         * As shown below, to get the index of the opposite pit, we need to subtract the current
         * pid from 5 (OFFSET_FOR_OPPONENT_PIT).
         *
         *     5  4  3  2  1  0
         *  6                    6
         *     0  1  2  3  4  5
         *
         */
        playerPits[GameBoard.KALAH_INDEX] += opponentPits[OFFSET_FOR_OPPONENT_PIT-pitId];

        // also take the stone from the pit itself
        playerPits[GameBoard.KALAH_INDEX] += 1;

        // both pits are now empty
        playerPits[pitId] = 0;
        opponentPits[OFFSET_FOR_OPPONENT_PIT-pitId] = 0;
      }
    }
  }

  /**
   * Gets the count of all stones from the given pits, excluding the kalah.
   * @param pits
   * @return the stones in the given pits, excluding the kalah (index 6)
   */
  int getStoneCountForPitsExcludingKalahs(final int[] pits) {
      int stoneCount = 0;

      for (int i=0; i<GameBoard.KALAH_INDEX; i++) {
        stoneCount += pits[i];
      }

      return stoneCount;
  }
}
