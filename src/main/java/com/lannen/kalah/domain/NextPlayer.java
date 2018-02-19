package com.lannen.kalah.domain;

/**
 * Used to return the next player position (north or south) for a game.
 */
public class NextPlayer {
  private GameBoard.PlayerPosition nextPlayer;

  public NextPlayer(GameBoard.PlayerPosition nextPlayer) {
    this.nextPlayer = nextPlayer;
  }

  public GameBoard.PlayerPosition getNextPlayer() {
    return nextPlayer;
  }
}
