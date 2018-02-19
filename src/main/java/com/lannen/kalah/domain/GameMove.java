package com.lannen.kalah.domain;

/**
 * Used to send a game move into the game, giving the player's id and their chosen pit (0-5, left to
 * right, as shown below).
 *
 *     5  4  3  2  1  0
 *  6                    6
 *     0  1  2  3  4  5
 *
 */
public class GameMove {
  private String playerId;
  private int pitId;

  public GameMove() {
  }

  public GameMove(String playerId, int pitId) {
    this.playerId = playerId;
    this.pitId = pitId;
  }

  public String getPlayerId() {
    return playerId;
  }

  public int getPitId() {
    return pitId;
  }

  @Override
  public String toString() {
    return "GameMove{" +
        "playerId='" + playerId + '\'' +
        ", pitId=" + pitId +
        '}';
  }
}
