package fvtc.edu.tictactoe;

import java.time.LocalDate;
import java.util.UUID;

public class Game {
    public UUID getId() {
        return Id;
    }

    public void setId(UUID id) {
        Id = id;
    }

    private UUID Id;

    public String getConnectionId() {
        return ConnectionId;
    }

    public void setConnectionId(String connectionId) {
        ConnectionId = connectionId;
    }

    public String getPlayer1() {
        return Player1;
    }

    public void setPlayer1(String player1) {
        Player1 = player1;
    }

    public String getPlayer2() {
        return Player2;
    }

    public void setPlayer2(String player2) {
        Player2 = player2;
    }

    public String getWinner() {
        return Winner;
    }

    public void setWinner(String winner) {
        Winner = winner;
    }

    public Boolean getCompleted() {
        return Completed;
    }

    public void setCompleted(Boolean completed) {
        Completed = completed;
    }

    public LocalDate getLastUpdateDate() {
        return LastUpdateDate;
    }

    public void setLastUpdateDate(LocalDate lastUpdateDate) {
        LastUpdateDate = lastUpdateDate;
    }

    public String getGameState() {
        return GameState;
    }

    public void setGameState(String gameState) {
        GameState = gameState;
    }

    private String ConnectionId;
    private String Player1;
    private String Player2;
    private String Winner;
    private Boolean Completed;
    private LocalDate LastUpdateDate;
    private String GameState;

    public Game(String player1, String name, String player2)
    {
        this.setPlayer1(player1);
        this.setPlayer2(player2);
        this.setConnectionId(name);
        this.setGameState("E|E|E|E|E|E|E|E|E");
        this.setCompleted(false);
    }
    public Game()
    {
        this.setGameState("E|E|E|E|E|E|E|E|E");
        this.setCompleted(false);
    }
}
