import java.util.ArrayList;

class MySnakeMultiplayerOpponent {
    private ArrayList<MySnakePiece> snake;
    private int clientId;

    public MySnakeMultiplayerOpponent(int id) {
        this.clientId = id;
        snake = new ArrayList<MySnakePiece>();
    }

    public int getId() {
        return clientId;
    }

    public ArrayList<MySnakePiece> getSnake() {
        return snake;
    }

    public void addPiece(MySnakePiece piece) {
        snake.add(piece);
    }
}
