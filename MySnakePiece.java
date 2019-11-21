class MySnakePiece {
    private int x;
    private int y;

    public MySnakePiece(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void changeX(int dx) {
        this.x += dx;
    }

    public void changeY(int dy) {
        this.y += dy;
    }

    @Override
    public String toString() {
        return "("+x+","+y+")";
    }
}
