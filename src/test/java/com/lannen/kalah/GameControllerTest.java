package com.lannen.kalah;

import static org.junit.jupiter.api.Assertions.*;

import com.lannen.kalah.domain.NewGame;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import com.lannen.kalah.domain.GameBoard;
import com.lannen.kalah.domain.GameMove;
import com.lannen.kalah.domain.NextPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

class GameControllerTest {
  private static GameController gameController;

  @BeforeEach
  void setUp() {
    gameController = new GameController();
  }

  @AfterEach
  void tearDown() {
  }


  @Test
  void newGame() {
    final ResponseEntity<NewGame> response = gameController.newGame();
    final NewGame newGame = response.getBody();
    final GameBoard gameBoard = newGame.getGameBoard();
    final String playerId = newGame.getPlayerId();

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertFalse(StringUtils.isEmpty(playerId));
    assertNotNull(gameBoard);

    if (!playerId.equals(gameBoard.getNorthPlayerId())) {
      if (!playerId.equals(gameBoard.getSouthPlayerId())) {
        fail("playerId is not set as either north or south player");
      } else {
        assertNull(gameBoard.getNorthPlayerId());
      }
    } else {
      assertNull(gameBoard.getSouthPlayerId());
    }

    for (int i=0; i<GameBoard.KALAH_INDEX; ++i) {
      assertEquals(GameBoard.INITIAL_STONE_COUNT, gameBoard.getNorthPits()[i]);
      assertEquals(GameBoard.INITIAL_STONE_COUNT, gameBoard.getSouthPits()[i]);
    }

    assertEquals(0, gameBoard.getNorthPits()[GameBoard.KALAH_INDEX]);
    assertEquals(0, gameBoard.getNorthPits()[GameBoard.KALAH_INDEX]);

    assertEquals(GameBoard.PlayerPosition.NORTH, gameBoard.getNextPlayer());
  }

  @Test
  void joinGame() {
    GameBoard gameBoard = gameController.newGame().getBody().getGameBoard();
    final String gameId = gameBoard.getGameId();

    final boolean firstPlayerIsNorth = !StringUtils.isEmpty(gameBoard.getNorthPlayerId());

    final ResponseEntity<NewGame> response = gameController.joinGame(gameId);
    final NewGame newGame = response.getBody();
    gameBoard = newGame.getGameBoard();
    final String playerId = newGame.getPlayerId();

    assertFalse(StringUtils.isEmpty(playerId));

    if (firstPlayerIsNorth) {
      assertEquals(playerId, gameBoard.getSouthPlayerId());
    }
    else {
      assertEquals(playerId, gameBoard.getNorthPlayerId());
    }
  }

  @Test
  void getNextPlayer() {
    final GameBoard gameBoard = gameController.newGame().getBody().getGameBoard();
    gameController.joinGame(gameBoard.getGameId());

    ResponseEntity<NextPlayer> response = gameController.getNextPlayer(gameBoard.getGameId());
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(GameBoard.PlayerPosition.NORTH, response.getBody().getNextPlayer());
  }

  @Test
  void playGame() {
    List<GameBoard.PlayerPosition> winners = new ArrayList<>();

    final int NO_OF_GAMES_TO_PLAY = 1000;

    for (int i=0; i<NO_OF_GAMES_TO_PLAY; i++) {
      GameBoard gameBoard = gameController.newGame().getBody().getGameBoard();
      final String gameId = gameBoard.getGameId();
      gameBoard = gameController.joinGame(gameId).getBody().getGameBoard();

      while (gameBoard.getCurrentGameStatus() != GameBoard.GameStatus.GAME_OVER) {
        final int pitId = getNonEmptyPitForNextPlayer(gameBoard);
        final GameMove gameMove = new GameMove(gameBoard.getNextPlayerId(), pitId);

        ResponseEntity<GameBoard> playGameResponse = gameController.playGame(gameId, gameMove);
        gameBoard = playGameResponse.getBody();
      }

      final int northKalahStones = gameBoard.getNorthPits()[GameBoard.KALAH_INDEX];
      final int southKalahStones = gameBoard.getSouthPits()[GameBoard.KALAH_INDEX];

      if (northKalahStones > southKalahStones) {
        assertEquals(GameBoard.PlayerPosition.NORTH, gameBoard.getWinner());
      } else if (northKalahStones == southKalahStones) {
        assertNull(gameBoard.getWinner());
      } else {
        assertEquals(GameBoard.PlayerPosition.SOUTH, gameBoard.getWinner());
      }

      winners.add(gameBoard.getWinner());
    }

    System.out.println(winners);

  }

  private int getNonEmptyPitForNextPlayer(GameBoard gameBoard) {
    int[] pits;

    final String nextPlayerId = gameBoard.getNextPlayerId();
    if (nextPlayerId.equals(gameBoard.getNorthPlayerId())) {
      pits = gameBoard.getNorthPits();
    }
    else {
      pits = gameBoard.getSouthPits();
    }

    int pitId = -1;

    while (pitId == -1) {
      int randomPit = ThreadLocalRandom.current().nextInt(0, 6);

      if (pits[randomPit] > 0) {
        pitId = randomPit;
      }
    }
    return pitId;
  }

}