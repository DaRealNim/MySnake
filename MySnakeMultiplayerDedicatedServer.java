import com.iddej.gingerbread2.logging.GlobalLogger;
import com.iddej.gingerbread2.logging.GlobalLogger.LogLevel;
import com.iddej.gingerbread2.util.fixedpoint.Vector2I;
import java.util.Random;
import java.util.ArrayList;
import java.util.Date;


public class MySnakeMultiplayerDedicatedServer {

    public static final int CELL_SIZE = 20;
    public static final int BOARD_WIDTH = 40;
    public static final int BOARD_HEIGHT = 40;
    public static final int SCREEN_WIDTH = CELL_SIZE*BOARD_WIDTH;
    public static final int SCREEN_HEIGHT = CELL_SIZE*BOARD_HEIGHT;
    public static final double REFRESH_RATE = 60.0;

    private static Server server;
    private static final Random rand = new Random();
    private static ArrayList<MySnakeMultiplayerOpponent> players;
    private static Vector2I apple;

    private static int port;

    private static SyncThread syncThread;
    private static AcceptThread acceptThread;

    private static int syncCount;



    private static void waitNMillis(int n) {
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0L;

        while (elapsedTime < n) {
            //perform db poll/check
            elapsedTime = (new Date()).getTime() - startTime;
        }
    }

    private static Vector2I generateApple() {
        return new Vector2I(rand.nextInt(BOARD_WIDTH),rand.nextInt(BOARD_HEIGHT));
    }


    private static MySnakeMultiplayerOpponent getOpponentByClientId(int clientId) {
        for(MySnakeMultiplayerOpponent opponent : players) {
            if(opponent.getId() == clientId) return opponent;
        }
        return null;
    }



    public static class RequestThread extends Thread {
        Server server;
        int clientId;

        public void run() {
            while(true) {
                //server side possible requests:
                //0: update direction of my snake
                //  - recv direction (int)

                // GlobalLogger.log(this, LogLevel.INFO, "Listening for request from client %d",clientId);
                int request = server.recvInt(clientId);
                if (request == -2147483648) {
                    return;
                }
                switch(request) {
                    case 0:
                        int direction = server.recvInt(clientId);
                        if (0 <= direction && direction <= 3) {
                            getOpponentByClientId(clientId).setDirection(direction);
                            // for(MySnakeMultiplayerOpponent player : players) {
                            //     if(player == getOpponentByClientId(clientId)) continue;
                            //     server.sendInt(player.getId(),1);
                            //     server.sendInt(player.getId(),clientId);
                            //     server.sendInt(player.getId(),direction);
                            // }
                        } else {
                            GlobalLogger.log(this, LogLevel.SEVERE, "Invalid direction %d received from client %d (%s)", direction, clientId, server.getClientIPById(clientId));
                        }
                        break;
                    default:
                        GlobalLogger.log(this, LogLevel.SEVERE, "Invalid request %d received from client %d (%s)", request, clientId, server.getClientIPById(clientId));
                        this.stop();
                        break;
                }
            }
        }

        public RequestThread(Server server, int clientId) {
            this.server = server;
            this.clientId = clientId;
        }
    }


    public static class SyncThread extends Thread {
        int timeUntilSync;

        public void run() {
            while(true) {
                waitNMillis(timeUntilSync);
                // GlobalLogger.log(this, LogLevel.INFO, "Syncing!");

                if(syncCount % 1 == 0) {       // Every 10 sync (2 seconds) we update positions to be sure there are no discrepencies
                    // GlobalLogger.log(this, LogLevel.INFO, "Sending global sync");
                    for(MySnakeMultiplayerOpponent currentclient : players) {
                        int clientId = currentclient.getId();
                        server.sendInt(clientId, 5);
                        server.sendInt(clientId, players.size()-1);
                        for(MySnakeMultiplayerOpponent player : players) {
                            if(player == currentclient) continue;
                            server.sendInt(clientId, player.getId());
                            server.sendInt(clientId, player.getDirection());
                            server.sendInt(clientId, player.getSnake().size());
                            for(MySnakePiece piece : player.getSnake()) {
                                server.sendInt(clientId, piece.getX());
                                server.sendInt(clientId, piece.getY());
                            }
                        }
                        server.sendInt(clientId, 1908);
                    }
                }

                //let's sync: we compute new positions based on directions, to distribute to new players
                for(MySnakeMultiplayerOpponent opponent : players) {
                    boolean addPiece = false;

                    addPiece = (opponent.getSnake().get(0).getX() == apple.x && opponent.getSnake().get(0).getY() == apple.y );

                    MySnakePiece previous = new MySnakePiece(opponent.getSnake().get(0).getX(), opponent.getSnake().get(0).getY());
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

                    //if the head is on the apple position, generate the new tail, a new apple position
                    //and send everyone the appleaten request, along with the id of the eating opponent (to add tail),
                    //as well as the new apple pos

                    if(addPiece) {
                        opponent.addPiece(new MySnakePiece(opponent.getSnake().get(opponent.getSnake().size()-1).getX(), opponent.getSnake().get(opponent.getSnake().size()-1).getY()));
                        apple = generateApple();
                        for(MySnakeMultiplayerOpponent otherplayer : players) {
                            server.sendInt(otherplayer.getId(), 4);
                            server.sendInt(otherplayer.getId(), apple.x);
                            server.sendInt(otherplayer.getId(), apple.y);
                            if(otherplayer == opponent) continue;
                            server.sendInt(otherplayer.getId(), 3);
                            server.sendInt(otherplayer.getId(), opponent.getId());

                        }
                    }

                    //we check if the head of any player is colliding with any tail

                    for(MySnakeMultiplayerOpponent currentplayer : players) {
                        int headX = currentplayer.getSnake().get(0).getX();
                        int headY = currentplayer.getSnake().get(0).getY();
                        for(MySnakeMultiplayerOpponent otherplayer : players) {
                            for(MySnakePiece piece : otherplayer.getSnake()) {
                                if(piece == currentplayer.getSnake().get(0)) continue;
                                if(headX == piece.getX() && headY == piece.getY()) {
                                    server.sendInt(currentplayer.getId(), 6);
                                    for(MySnakeMultiplayerOpponent ppppppplayerrrr : players) {
                                        if(currentplayer == ppppppplayerrrr) continue;
                                        server.sendInt(ppppppplayerrrr.getId(), 7);
                                        server.sendInt(ppppppplayerrrr.getId(), currentplayer.getId());
                                    }
                                }
                            }
                        }
                    }

                }

                //now we send the sync packet.
                for(MySnakeMultiplayerOpponent client : players) {
                    // GlobalLogger.log(this, LogLevel.INFO, "Sending sync to %d",client.getId());
                    server.sendInt(client.getId(), 0xC0);
                }

                syncCount++;
            }
        }

        public SyncThread (int timeUntilSync) { //in ms
            this.timeUntilSync = timeUntilSync;
        }
    }


    public static class AcceptThread extends Thread {
        Server server;
        public void run(){
            server.start();
            GlobalLogger.log(this, LogLevel.INFO, "STARTING AcceptThread");
            while(true) {
                int clientId = server.waitForClient();
                GlobalLogger.log(this, LogLevel.INFO, "Receiving connection");
                String receivedHeader = server.recvString(clientId, 25); //Header should be "MySnakeMultiplayer by Nim"
                if(!receivedHeader.equals("MySnakeMultiplayer by Nim")) {
                    GlobalLogger.log(this, LogLevel.INFO, "Invalid header, dropping");
                    server.dropConn(clientId);
                }

                //generating random start pos for this player
                int x = rand.nextInt(BOARD_WIDTH);
                int y = rand.nextInt(BOARD_HEIGHT);
                //sending them
                server.sendInt(clientId, x);
                server.sendInt(clientId, y);

                //we now send:
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

                server.sendInt(clientId, apple.x);
                server.sendInt(clientId, apple.y);
                GlobalLogger.log(this, LogLevel.INFO, "Sending playern %d",players.size());
                server.sendInt(clientId, players.size());
                for(MySnakeMultiplayerOpponent player : players) {
                    server.sendInt(clientId, player.getId());
                    server.sendInt(clientId, player.getDirection());
                    server.sendInt(clientId, player.getSnake().size());
                    for(MySnakePiece piece : player.getSnake()) {
                        server.sendInt(clientId, piece.getX());
                        server.sendInt(clientId, piece.getY());
                    }
                }
                server.sendInt(clientId, 1908);

                //now we need to send every player the newopponent request, along with your, your x and your y
                for(MySnakeMultiplayerOpponent player : players) {
                    server.sendInt(player.getId(), 2);
                    server.sendInt(player.getId(), clientId);
                    server.sendInt(player.getId(), x);
                    server.sendInt(player.getId(), y);
                }


                MySnakeMultiplayerOpponent opponent = new MySnakeMultiplayerOpponent(clientId);
                opponent.addPiece(new MySnakePiece(x,y));
                opponent.addPiece(new MySnakePiece(x+1,y));
                players.add(opponent);
                new RequestThread(server,clientId).start();


            }
        }

        public AcceptThread(Server server) {
            this.server = server;
        }
    }



    public static void main(String[] args) {
        players = new ArrayList<MySnakeMultiplayerOpponent>();
        apple = generateApple();
        syncCount = 0;
        System.out.println("Apple placed at ("+apple.x+","+apple.y+")");
        port = Integer.valueOf(args[0]);
        server = new Server();
        System.out.println("MySnakeMultiplayerDedicatedServer started!");
        if(server.init(port, 10) != 0) {
            System.out.println("COULDN'T START SERVER ON PORT"+port);
            return;
        }
        syncThread = new SyncThread(200);
        syncThread.start();
        acceptThread = new AcceptThread(server);
        acceptThread.start();
    }
}
