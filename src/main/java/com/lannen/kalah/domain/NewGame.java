package com.lannen.kalah.domain;

/**
 * Used to initially return a GameBoard to a player, along with their playerId and playerPosition (north or south).
 */
public class NewGame {

  private String playerId;
  private GameBoard.PlayerPosition playerPosition;

  private GameBoard gameBoard;

  public NewGame(String playerId, GameBoard.PlayerPosition playerPosition, GameBoard gameBoard) {
    this.playerId = playerId;
    this.playerPosition = playerPosition;
    this.gameBoard = gameBoard;
  }

  public String getPlayerId() {
    return playerId;
  }

  public GameBoard.PlayerPosition getPlayerPosition() {
    return playerPosition;
  }

  public GameBoard getGameBoard() {
    return gameBoard;
  }
}
