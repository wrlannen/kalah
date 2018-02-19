package com.lannen.kalah.domain;

import java.util.Arrays;
import java.util.UUID;


/**
 * Represents a 6 stone Kalah game board.
 */
public class GameBoard {

  public enum GameStatus {
    NOT_STARTED, IN_PROGRESS, GAME_OVER
  }

  public enum PlayerPosition {
    NORTH, SOUTH;
  }

  public static final int TOTAL_PITS_EACH = 7; // including Kalah
  public static final int KALAH_INDEX = 6;
  public static final int INITIAL_STONE_COUNT = 6;

  /**
   * The current status of the game. When the status is GAME_OVER, the winner field will give the winner.
   *
   */
  private GameStatus currentGameStatus;

  /**
   * The unique id of the game.
   */
  private String gameId;

  /**
   * The uniquie id of the north player.
   */
  private String northPlayerId;

  /**
   * The unique id of the south player.
   */
  private String southPlayerId;

  /**
   * The position of the next player.
   */
  private PlayerPosition nextPlayer;

  /**
   * The position of the winner (when currentGameStatus is GAME_OVER). A value of null indicates a tie.
   */
  private PlayerPosition winner;

  /**
   * The player's pits, including their kalah, as shown below.
   *
   *     5  4  3  2  1  0
   *  6                    6
   *     0  1  2  3  4  5
   *
   */
  private int[] southPits = new int[TOTAL_PITS_EACH];
  private int[] northPits = new int[TOTAL_PITS_EACH];

  public GameBoard() {
    gameId = UUID.randomUUID().toString();

    for (int i = 0; i < KALAH_INDEX; ++i) {
      southPits[i] = northPits[i] = INITIAL_STONE_COUNT;
    }

    currentGameStatus = GameStatus.NOT_STARTED;
    nextPlayer = PlayerPosition.NORTH;
  }

  public String getGameId() {
    return gameId;
  }

  public String getSouthPlayerId() {
    return southPlayerId;
  }

  public String getNorthPlayerId() {
    return northPlayerId;
  }

  public PlayerPosition getNextPlayer() {
    return nextPlayer;
  }

  public PlayerPosition getWinner() {
    return winner;
  }

  public GameStatus getCurrentGameStatus() {
    return currentGameStatus;
  }

  public int[] getSouthPits() {
    return southPits;
  }

  public int[] getNorthPits() {
    return northPits;
  }

  public void setSouthPlayerId(String southPlayerId) {
    this.southPlayerId = southPlayerId;
  }

  public void setNorthPlayerId(String northPlayerId) {
    this.northPlayerId = northPlayerId;
  }

  public void setCurrentGameStatus(GameStatus currentGameStatus) {
    this.currentGameStatus = currentGameStatus;
  }

  public void setNextPlayer(PlayerPosition nextPlayer) {
    this.nextPlayer = nextPlayer;
  }

  public void setWinner(PlayerPosition winner) {
    this.winner = winner;
  }

  public String getNextPlayerId() {
    if (nextPlayer == PlayerPosition.NORTH) {
      return getNorthPlayerId();
    }
    else {
      return getSouthPlayerId();
    }
  }

  @Override
  public String toString() {
    return "GameBoard{" +
        "currentGameStatus=" + currentGameStatus +
        ", gameId='" + gameId + '\'' +
        ", southPlayerId='" + southPlayerId + '\'' +
        ", northPlayerId='" + northPlayerId + '\'' +
        ", nextPlayer=" + nextPlayer +
        ", winner=" + winner +
        ", southPits=" + Arrays.toString(southPits) +
        ", northPits=" + Arrays.toString(northPits) +
        '}';
  }
}
