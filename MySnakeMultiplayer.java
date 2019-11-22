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

    private boolean up;
    private boolean down;
    private boolean left;
    private boolean right;

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

    private MySnakeMultiplayerOpponent getOpponentByClientId(int clientId) {
        for(MySnakeMultiplayerOpponent opponent : opponents) {
            if(opponent.getId() == clientId) return opponent;
        }
        return null;
    }

    private int receiveUntilNotSync() {
        int request;
        while(true) {
            request = client.recvInt();
            if(request != 0xC0) break;
        }
        return request;
    }


    public class RequestThread extends Thread {
        Client client;

        //Client side possible requests:
        //C0h : sync request: handle all logic once
        //1 : updated direction for client n
        //  - recv client id
        //  - recv direction
        //2 : new client connected!
        //  - recv their id
        //  - recv their x
        //  - recv their y
        //3 : apple eaten
        //  - recv id of eating opponent
        //4 : new apple
        //  - recv new apple x
        //  - recv new apple y
        //5 : globalsync
        //  -the number of players, besides them
        //  for every player:
        //      -client id
        //      -the current direction
        //      -the number of tails in the list
        //  for every tail:
        //      -the tail x pos
        //      -the tail y pos
        //  the number 1908 to mark end of transmission!
        //6 : youdead
        //7 : playerdied
        //  - recv clientId


        public void run() {
            GlobalLogger.log(this, LogLevel.INFO, "STARTING RequestThread");
            while(true) {
                int request = client.recvInt();
                if (request == -2147483648) {
                    return;
                }
                int i;
                int d;
                int id;
                int x;
                int y;
                switch(request) {
                    case 0xC0:
                        mustSync = true;
                        break;
                    case 1:
                        i = receiveUntilNotSync();
                        d = receiveUntilNotSync();
                        GlobalLogger.log(this, LogLevel.INFO, "Updating direction of client %d to %d",i, d);
                        if(getOpponentByClientId(i) != null) getOpponentByClientId(i).setDirection(d);
                        break;
                    case 2:
                        id = receiveUntilNotSync();
                        x = receiveUntilNotSync();
                        y = receiveUntilNotSync();
                        MySnakeMultiplayerOpponent newopponent = new MySnakeMultiplayerOpponent(id);
                        GlobalLogger.log(this, LogLevel.INFO, "New opponent at id %d, on pos (%d,%d)",id,x,y);
                        newopponent.addPiece(new MySnakePiece(x,y));
                        newopponent.addPiece(new MySnakePiece(x+1,y));
                        opponents.add(newopponent);
                        break;
                    case 3:
                        id = receiveUntilNotSync();
                        GlobalLogger.log(this, LogLevel.INFO, "Apple eaten by opponent %d!", id);
                        if(getOpponentByClientId(id) != null) getOpponentByClientId(id).addPiece(new MySnakePiece(getOpponentByClientId(id).getSnake().get(getOpponentByClientId(id).getSnake().size()-1).getX(), getOpponentByClientId(id).getSnake().get(getOpponentByClientId(id).getSnake().size()-1).getY()));
                        break;
                    case 4:
                        x = receiveUntilNotSync();
                        y = receiveUntilNotSync();
                        GlobalLogger.log(this, LogLevel.INFO, "New apple pos is (%d,%d)",x,y);
                        apple.x = x;
                        apple.y = y;
                        break;
                    case 5:
                        int playern = receiveUntilNotSync();
                        // GlobalLogger.log(this, LogLevel.INFO, "Global sync received");
                        // GlobalLogger.log(this, LogLevel.INFO, "%d players currently connected (besides you)", playern);
                        for(int k=0; k<playern; k++) {
                            id = receiveUntilNotSync();
                            MySnakeMultiplayerOpponent currentOpponent = getOpponentByClientId(id);
                            if(currentOpponent != null) {
                                currentOpponent.setDirection(client.recvInt());
                                int tailsn = receiveUntilNotSync();
                                for(int j=0; j<tailsn; j++) {
                                    int tailx = receiveUntilNotSync();
                                    int taily = receiveUntilNotSync();
                                    currentOpponent.getSnake().get(j).setX(tailx);
                                    currentOpponent.getSnake().get(j).setY(taily);
                                }
                            }
                        }

                        int controlcode = receiveUntilNotSync();
                        if(controlcode != 1908) {
                            GlobalLogger.log(this, LogLevel.SEVERE, "Something went horribly wrong. Got %d for control code", controlcode);
                        } else {
                            // GlobalLogger.log(this, LogLevel.INFO, "Seems like everything went smoothly :D! Noice!");
                        }
                        break;
                    case 6:
                        fuckingDead = true;
                        break;

                    case 7:
                        id = receiveUntilNotSync();
                        opponents.remove(getOpponentByClientId(id));
                        break;

                    default:
                        GlobalLogger.log(this, LogLevel.SEVERE, "Invalid request %d received from server", request);
                        this.stop();
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
            GlobalLogger.log(this, LogLevel.INFO, "Connection successful!");
        }
        client.sendString("MySnakeMultiplayer by Nim");

        mustSync = false;
        syncedLogic = false;
        syncedDisplay = false;
        opponents = new ArrayList<MySnakeMultiplayerOpponent>();

        up = false;
        down = false;
        left = false;
        right = false;

        //receive your snake position
        int x = receiveUntilNotSync();
        int y = receiveUntilNotSync();
        snake.add(new MySnakePiece(x, y));
        snake.add(new MySnakePiece(x+1, y));

        direction = -1;

        //we now receive:
        // -the apple x pos
        // -the apple y pos
        // -the number of players, besides them
        // for every player:
        //   -client id
        //   -the current direction
        //   -the number of tails in the list
        //   for every tail:
        //     -the tail x pos
        //     -the tail y pos
        //the number 1908 to mark end of transmission!

        apple = new Vector2I(0,0);
        apple.x = receiveUntilNotSync();
        apple.y = receiveUntilNotSync();

        int playern = receiveUntilNotSync();
        GlobalLogger.log(this, LogLevel.INFO, "%d players currently connected (besides you)", playern);
        for(int i=0; i<playern; i++) {
            int id = receiveUntilNotSync();
            MySnakeMultiplayerOpponent currentOpponent = new MySnakeMultiplayerOpponent(id);
            currentOpponent.setDirection(client.recvInt());
            int tailsn = receiveUntilNotSync();
            for(int j=0; j<tailsn; j++) {
                int tailx = receiveUntilNotSync();
                int taily = receiveUntilNotSync();
                currentOpponent.addPiece(new MySnakePiece(tailx,taily));
            }
            opponents.add(currentOpponent);
        }

        int controlcode = receiveUntilNotSync();
        if(controlcode != 1908) {
            GlobalLogger.log(this, LogLevel.SEVERE, "Something went horribly wrong. Got %d for control code", controlcode);
        } else {
            GlobalLogger.log(this, LogLevel.INFO, "Seems like everything went smoothly :D! Noice!");
        }

        addPiece = false;
        fuckingDead = false;

        requestThread = new RequestThread(client);
        requestThread.start();
        // ImageHandler.registerImage(this, "apple", "/home/Nim/MySnake/res/img/apple.png");

        GlobalLogger.log(this, LogLevel.INFO, "Starting...");

        FontHandler.registerFont(this, "8bit", System.getProperty("user.dir")+"/../res/fonts/8bit.ttf");
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
            // GlobalLogger.log(this, LogLevel.INFO, "SYNCING COMPLETE");
            mustSync = false;
            syncedLogic = false;
            syncedDisplay = false;
        }

        if(!up && !down && !left && !right) {
            up = this.keyboard.isKeyDown(Keyboard.KEY_UP);
            down = this.keyboard.isKeyDown(Keyboard.KEY_DOWN);
            left = this.keyboard.isKeyDown(Keyboard.KEY_LEFT);
            right = this.keyboard.isKeyDown(Keyboard.KEY_RIGHT);
        }

        if(mustSync && !syncedLogic) {
            // GlobalLogger.log(this, LogLevel.INFO, "SYNCING LOGIC");

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

            up = false;
            down = false;
            left = false;
            right = false;

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
                        //if everything is right, the server knows we ate the apple, so no need to regenerate another.
                        //we just wait for the newapple request
                        snake.add(new MySnakePiece(snake.get(snake.size()-1).getX(), snake.get(snake.size()-1).getY()));
                        addPiece = false;
                    }
                }


                //update opponent snakes
                // for(MySnakeMultiplayerOpponent opponent : opponents) {
                //     previous = new MySnakePiece(opponent.getSnake().get(0).getX(), opponent.getSnake().get(0).getY());
                //     switch(opponent.getDirection()) {
                //         case -1: default: break;
                //         case 0: opponent.getSnake().get(0).changeX(-1); break;
                //         case 1: opponent.getSnake().get(0).changeY(-1); break;
                //         case 2: opponent.getSnake().get(0).changeX(1); break;
                //         case 3: opponent.getSnake().get(0).changeY(1); break;
                //     }
                //
                //     if (opponent.getDirection() != -1) {
                //         for(int i = 1; i < opponent.getSnake().size(); i += 1) {
                //             MySnakePiece piece = opponent.getSnake().get(i);
                //             MySnakePiece temp = new MySnakePiece(piece.getX(), piece.getY());
                //             piece.setX(previous.getX());
                //             piece.setY(previous.getY());
                //             previous = temp;
                //         }
                //     }
                //
                // }


            }
            syncedLogic = true;
        }
    }

    @Override
    public void render(Graphics graphics) {
        Screen.setGraphics(graphics);
        // DRAW

        if(mustSync && !syncedDisplay) {
            // GlobalLogger.log(this, LogLevel.INFO, "SYNCING DISPLAY");

            //CLEAR SCREEN
            Screen.clear(this.window);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);


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
                Screen.drawCenteredString("YOU DEAD", 0, 0, SCREEN_WIDTH,SCREEN_HEIGHT, FontHandler.retrieveFont(this, "8bit").deriveFont(80f), Color.RED);
            }


            // if(waitingForOpponent) {
            //     Screen.drawCenteredString("Waiting for opponent...", 0, 0, SCREEN_WIDTH,SCREEN_HEIGHT, FontHandler.retrieveFont(this, "8bit").deriveFont(70f), Color.BLACK);
            // }

            syncedDisplay = true;
        }
    }
}
