import com.iddej.gingerbread2.core.Game;
import com.iddej.gingerbread2.input.Keyboard;
import com.iddej.gingerbread2.display.Screen;
import com.iddej.gingerbread2.io.FontHandler;
import com.iddej.gingerbread2.logging.GlobalLogger;
import com.iddej.gingerbread2.logging.GlobalLogger.LogLevel;
import com.iddej.gingerbread2.util.fixedpoint.Vector2I;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.ArrayList;


public class MySnakeMultiplayer extends Game {
    private static MySnakeMultiplayer instance;

    public static MySnakeMultiplayer getInstance() {
        return instance == null ? instance = new MySnakeMultiplayer() : instance;
    }


    public static final int CELL_SIZE = 20;
    public static final int BOARD_WIDTH = 40;
    public static final int BOARD_HEIGHT = 40;
    public static final int SCREEN_WIDTH = CELL_SIZE*BOARD_WIDTH;
    public static final int SCREEN_HEIGHT = CELL_SIZE*BOARD_HEIGHT;
    public static final double REFRESH_RATE = 60.0;

    private final Random rand = new Random();

    private int direction; // 0 - left, 1 - up, 2 - right, 3 - down

    private ArrayList<MySnakePiece> snake;

    private ArrayList<MySnakeMultiplayerOpponent> opponents;
    private Vector2I apple;

    private int frameCounter;
    private boolean addPiece;
    private boolean fuckingDead;
    private boolean paused;

    private double speed;

    //multiplayer stuff
    private boolean isServer;
    private String IP;
    private int port;
    private Server server;
    private Client client;
    private boolean waitingForOpponent;
    private AcceptThread acceptThread;


    private MySnakeMultiplayer() {
        super("mysnake", REFRESH_RATE);
        snake = new ArrayList<MySnakePiece>();
        opponents = new ArrayList<MySnakeMultiplayerOpponent>();
    }


    public class AcceptThread extends Thread {
        Server server;
        public void run(){
            while(true) {
                GlobalLogger.log(this, LogLevel.INFO, "STARTING AcceptThread");
                int clientId = server.waitForClient();
                GlobalLogger.log(this, LogLevel.INFO, "Receiving connection");
                String receivedHeader = server.recvString(clientId, 25); //Header should be "MySnakeMultiplayer by Nim"
                if(!receivedHeader.equals("MySnakeMultiplayer by Nim")) {
                    GlobalLogger.log(this, LogLevel.INFO, "Invalid header, dropping");
                    server.dropConn(clientId);
                }
                //Header is correct. Can begin to send data c:
                waitingForOpponent = false;
                //generating random start pos for this player
                int x = rand.nextInt(BOARD_WIDTH);
                int y = rand.nextInt(BOARD_HEIGHT);
                MySnakeMultiplayerOpponent opponent = new MySnakeMultiplayerOpponent(clientId);
                opponent.addPiece(new MySnakePiece(x,y));
                opponent.addPiece(new MySnakePiece(x+1,y));
                opponents.add(opponent);
            }
        }

        public AcceptThread(Server server) {
            this.server = server;
        }
    }



    public void passArgs(String[] args) {
        GlobalLogger.log(this, LogLevel.FATAL, "%s", args[0]);
        if(args[0].equals("server")) isServer = true; else isServer = false;
        IP = args[1];
        port = Integer.valueOf(args[2]);
    }

    @Override
    protected void create() {
        // DO YOUR STUFF
        waitingForOpponent = true;
        frameCounter = 0;

        if(isServer) {
            GlobalLogger.log(this, LogLevel.INFO, "Starting game as Server side");
            server = new Server();
            if(server.init(port, 10) != 0) {
                GlobalLogger.log(this, LogLevel.FATAL, "COULDN'T START SERVER ON PORT %d", port);
                this.stop();
            } else {
                GlobalLogger.log(this, LogLevel.INFO, "SERVER CREATE AND BIND SUCCESS ON PORT %d", port);
            }
            acceptThread = new AcceptThread(server);
            acceptThread.start();
        } else {
            GlobalLogger.log(this, LogLevel.INFO, "Starting game as Client side");
            client = new Client();
            client.connect(IP, port);
            client.sendString("MySnakeMultiplayer by Nim");
        }



        snake.add(new MySnakePiece(rand.nextInt(BOARD_WIDTH/2)+BOARD_WIDTH/4, rand.nextInt(BOARD_HEIGHT/2)+BOARD_HEIGHT/4));
        snake.add(new MySnakePiece(snake.get(0).getX()+1, snake.get(0).getY()));

        apple = new Vector2I(rand.nextInt(BOARD_WIDTH), rand.nextInt(BOARD_HEIGHT));
        addPiece = false;
        fuckingDead = false;
        paused = false;
        speed = 4.0;
        // ImageHandler.registerImage(this, "apple", "/home/Nim/MySnake/res/img/apple.png");

        // GlobalLogger.log(this, LogLevel.INFO, "%s", IP);

        FontHandler.registerFont(this, "8bit", "/home/Nim/MySnake/res/fonts/8bit.ttf");

        direction = -1;

        super.create();
    }

    @Override
    protected void destroy() {
        super.destroy();
        // DO YOUR STUFF
        System.exit(Game.EXIT_SUCCESS);
    }

    public static void p(Object o) {
        System.out.println(o);
    }

    @Override
    public void update(double delta) {
        boolean closeRequested = this.window.isCloseRequested();
        if (closeRequested) {
            this.stop();
        }
        // LOGIC GOES HERE


        boolean up = this.keyboard.isKeyDown(Keyboard.KEY_UP);
        boolean down = this.keyboard.isKeyDown(Keyboard.KEY_DOWN);
        boolean left = this.keyboard.isKeyDown(Keyboard.KEY_LEFT);
        boolean right = this.keyboard.isKeyDown(Keyboard.KEY_RIGHT);

        if(this.keyboard.isKeyDownInFrame(Keyboard.KEY_P)) {
            if (paused) paused = false; else paused = true;
        }

        if (up && !down && !left && !right) {
            if (direction != 3) direction = 1;
        } else if (!up && down && !left && !right) {
            if (direction != 1) direction = 3;
        } else if (!up && !down && left && !right) {
            if (direction != 2) direction = 0;
        } else if (!up && !down && !left && right) {
            if (direction != 0) direction = 2;
        }

        if(frameCounter >= REFRESH_RATE/6) {
            if(snake.get(0).getX() == apple.x && snake.get(0).getY() == apple.y ) addPiece = true;

            for(MySnakePiece piece : snake) {
                if (piece == snake.get(0)) continue;
                if ((snake.get(0).getX() == piece.getX() && snake.get(0).getY() == piece.getY()) || (snake.get(0).getX() >= BOARD_WIDTH || snake.get(0).getX() < 0 || snake.get(0).getY() < 0 || snake.get(0).getY() >= BOARD_HEIGHT)) {
                    fuckingDead = true;
                }
            }

            if(!fuckingDead && !paused && !waitingForOpponent) {
                MySnakePiece previous = new MySnakePiece(snake.get(0).getX(), snake.get(0).getY());
                switch(direction) {
                    case -1: default: break;
                    case 0: snake.get(0).changeX(-1); break;
                    case 1: snake.get(0).changeY(-1); break;
                    case 2: snake.get(0).changeX(1); break;
                    case 3: snake.get(0).changeY(1); break;
                }

                if (direction != -1) {
                    for(int i = 1; i < snake.size(); i += 1) {
                        MySnakePiece piece = snake.get(i);
                        MySnakePiece temp = new MySnakePiece(piece.getX(), piece.getY());
                        piece.setX(previous.getX());
                        piece.setY(previous.getY());
                        previous = temp;
                    }

                    if(addPiece) {
                        apple = new Vector2I(rand.nextInt(BOARD_WIDTH), rand.nextInt(BOARD_HEIGHT));
                        snake.add(new MySnakePiece(snake.get(snake.size()-1).getX(), snake.get(snake.size()-1).getY()));
                        addPiece = false;
                        speed+=.25;
                    }
                }
            }
            frameCounter = 0;
        }
        // GlobalLogger.log(this, LogLevel.INFO, "%d", frameCounter);
        frameCounter+=1;
    }

    @Override
    public void render(Graphics graphics) {
        Screen.setGraphics(graphics);
        Screen.clear(this.window);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        // DRAW


        //APPLE
        graphics.setColor(Color.GREEN);
        graphics.fillRect(apple.x*CELL_SIZE + 1, apple.y*CELL_SIZE + 1, CELL_SIZE - 1, CELL_SIZE - 1);

        //YOUR SNAKE
        graphics.setColor(Color.BLACK);
        for(MySnakePiece piece : snake) {
            int i = piece.getX();
            int j = piece.getY();
            graphics.fillRect(i * CELL_SIZE + 1, j * CELL_SIZE + 1, CELL_SIZE - 1, CELL_SIZE - 1);
        }

        //OPPONENTS
        for(MySnakeMultiplayerOpponent opponent : opponents) {
            for(MySnakePiece piece : opponent.getSnake()) {
                int i = piece.getX();
                int j = piece.getY();
                graphics.fillRect(i * CELL_SIZE + 1, j * CELL_SIZE + 1, CELL_SIZE - 1, CELL_SIZE - 1);
            }
        }



        //ON SCREEN TEXT
        if(fuckingDead) {
            Screen.drawCenteredString("YOU LOOSE", 0, 0, SCREEN_WIDTH,SCREEN_HEIGHT, FontHandler.retrieveFont(this, "8bit").deriveFont(80f), Color.RED);
        }

        if(paused) {
            Screen.drawCenteredString("PAUSED", 0, 0, SCREEN_WIDTH,SCREEN_HEIGHT, FontHandler.retrieveFont(this, "8bit").deriveFont(80f), Color.BLACK);
        }

        if(waitingForOpponent) {
            Screen.drawCenteredString("Waiting for opponent...", 0, 0, SCREEN_WIDTH,SCREEN_HEIGHT, FontHandler.retrieveFont(this, "8bit").deriveFont(70f), Color.BLACK);
        }


        // graphics.setFont(FontHandler.retrieveFont(this, "8bit").deriveFont(70f));
        // graphics.setColor(Color.BLACK);
        // graphics.drawString(String.valueOf(snake.size()), 0, 40);
    }
}
