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

    private Vector2I apple;

    private boolean addPiece;
    private boolean fuckingDead;

    //multiplayer stuff
    private String IP;
    private int port;
    private Client client;
    private RequestThread requestThread;

    private boolean mustSync;
    private boolean syncedLogic;
    private boolean syncedDisplay;
    private ArrayList<MySnakeMultiplayerOpponent> opponents;

    private MySnakeMultiplayer() {
        super("mysnake", REFRESH_RATE);
        snake = new ArrayList<MySnakePiece>();

    }



    public class RequestThread extends Thread {
        Client client;

        //Client side possible requests:
        //0 : sync request: handle all logic once

        public void run() {
            GlobalLogger.log(this, LogLevel.INFO, "STARTING RequestThread");
            while(true) {
                Integer request = client.recvInt();
                if (request == null) {
                    return;
                }
                switch(request) {
                    case 0:
                        mustSync = true;
                        break;
                    default:
                        GlobalLogger.log(this, LogLevel.SEVERE, "Invalid request %d received from server", request);
                        break;
                }
            }
        }

        public RequestThread(Client client) {
            this.client = client;
        }
    }



    public void passArgs(String[] args) {
        IP = args[0];
        port = Integer.valueOf(args[1]);
    }

    @Override
    protected void create() {
        // DO YOUR STUFF

        GlobalLogger.log(this, LogLevel.INFO, "Starting game");
        client = new Client();
        if(client.connect(IP, port) != 0) {
            GlobalLogger.log(this, LogLevel.FATAL, "Could not connect to provided RHOST");
        } else {
            GlobalLogger.log(this, LogLevel.FATAL, "Connection successful!");
        }
        client.sendString("MySnakeMultiplayer by Nim");

        mustSync = false;
        syncedLogic = false;
        syncedDisplay = false;
        opponents = new ArrayList<MySnakeMultiplayerOpponent>();

        //receive your snake position
        int x = client.recvInt();
        int y = client.recvInt();

        snake.add(new MySnakePiece(x, y));
        snake.add(new MySnakePiece(x+1, y+1));

        apple = new Vector2I(rand.nextInt(BOARD_WIDTH), rand.nextInt(BOARD_HEIGHT));
        addPiece = false;
        fuckingDead = false;

        requestThread = new RequestThread(client);
        requestThread.start();
        // ImageHandler.registerImage(this, "apple", "/home/Nim/MySnake/res/img/apple.png");

        // GlobalLogger.log(this, LogLevel.INFO, "%s", IP);

        FontHandler.registerFont(this, "8bit", "/home/Nim/github_clones/MySnake/res/fonts/8bit.ttf");
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

        if(syncedLogic && syncedDisplay && mustSync) {
            mustSync = false;
        }

        if(mustSync && !syncedLogic) {
            boolean up = this.keyboard.isKeyDown(Keyboard.KEY_UP);
            boolean down = this.keyboard.isKeyDown(Keyboard.KEY_DOWN);
            boolean left = this.keyboard.isKeyDown(Keyboard.KEY_LEFT);
            boolean right = this.keyboard.isKeyDown(Keyboard.KEY_RIGHT);


            int previousdir = direction;
            if (up && !down && !left && !right) {
                if (direction != 3) direction = 1;
            } else if (!up && down && !left && !right) {
                if (direction != 1) direction = 3;
            } else if (!up && !down && left && !right) {
                if (direction != 2) direction = 0;
            } else if (!up && !down && !left && right) {
                if (direction != 0) direction = 2;
            }

            if(direction!=previousdir) {
                if (client.sendInt(0) != 0) { //update direction request
                    GlobalLogger.log(this, LogLevel.SEVERE, "Can't send request!");
                } else {
                    GlobalLogger.log(this, LogLevel.INFO, "Sending updatedirection request");
                }
                client.sendInt(direction);
            }

            if(snake.get(0).getX() == apple.x && snake.get(0).getY() == apple.y ) addPiece = true;

            for(MySnakePiece piece : snake) {
                if (piece == snake.get(0)) continue;
                if ((snake.get(0).getX() == piece.getX() && snake.get(0).getY() == piece.getY()) || (snake.get(0).getX() >= BOARD_WIDTH || snake.get(0).getX() < 0 || snake.get(0).getY() < 0 || snake.get(0).getY() >= BOARD_HEIGHT)) {
                    fuckingDead = true;
                }
            }

            //update your snake
            if(!fuckingDead) {
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
                    }
                }


                //update opponent snakes
                for(MySnakeMultiplayerOpponent opponent : opponents) {
                    previous = new MySnakePiece(opponent.getSnake().get(0).getX(), opponent.getSnake().get(0).getY());
                    switch(opponent.getDirection()) {
                        case -1: default: break;
                        case 0: opponent.getSnake().get(0).changeX(-1); break;
                        case 1: opponent.getSnake().get(0).changeY(-1); break;
                        case 2: opponent.getSnake().get(0).changeX(1); break;
                        case 3: opponent.getSnake().get(0).changeY(1); break;
                    }

                    if (opponent.getDirection() != -1) {
                        for(int i = 1; i < opponent.getSnake().size(); i += 1) {
                            MySnakePiece piece = opponent.getSnake().get(i);
                            MySnakePiece temp = new MySnakePiece(piece.getX(), piece.getY());
                            piece.setX(previous.getX());
                            piece.setY(previous.getY());
                            previous = temp;
                        }
                    }

                }


            }
            syncedLogic = true;
        }
    }

    @Override
    public void render(Graphics graphics) {
        Screen.setGraphics(graphics);
        Screen.clear(this.window);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        // DRAW

        if(mustSync && !syncedDisplay) {

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


            // if(waitingForOpponent) {
            //     Screen.drawCenteredString("Waiting for opponent...", 0, 0, SCREEN_WIDTH,SCREEN_HEIGHT, FontHandler.retrieveFont(this, "8bit").deriveFont(70f), Color.BLACK);
            // }

            syncedDisplay = true;
        }
    }
}
