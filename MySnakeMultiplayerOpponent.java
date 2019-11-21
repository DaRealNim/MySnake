import java.util.ArrayList;

class MySnakeMultiplayerOpponent {
    private ArrayList<MySnakePiece> snake;
    private int direction;
    private int clientId;

    public MySnakeMultiplayerOpponent(int id) {
        this.clientId = id;
        snake = new ArrayList<MySnakePiece>();
        direction = -1;
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

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }
}
