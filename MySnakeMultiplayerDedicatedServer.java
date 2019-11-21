import com.iddej.gingerbread2.logging.GlobalLogger;
import com.iddej.gingerbread2.logging.GlobalLogger.LogLevel;
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

    private static int port;

    private static SyncThread syncThread;
    private static AcceptThread acceptThread;

    //server side possible requests:
    //0: update direction of my snake
    //  - recv direction (int)



    private static void waitNMillis(int n) {
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0L;

        while (elapsedTime < n) {
            //perform db poll/check
            elapsedTime = (new Date()).getTime() - startTime;
        }
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
                GlobalLogger.log(this, LogLevel.INFO, "Listening for request from client %d",clientId);
                Integer request = server.recvInt(clientId);
                if (request == null) {
                    return;
                }
                switch(request) {
                    case 0:
                        int direction = server.recvInt(clientId);
                        if (0 <= direction && direction <= 3)
                            getOpponentByClientId(clientId).setDirection(direction);
                        else
                            GlobalLogger.log(this, LogLevel.SEVERE, "Invalid direction %d received from client %d (%s)", direction, clientId, server.getClientIPById(clientId));
                        break;
                    default:
                        GlobalLogger.log(this, LogLevel.SEVERE, "Invalid request %d received from client %d (%s)", request, clientId, server.getClientIPById(clientId));
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
                for(MySnakeMultiplayerOpponent client : players) {
                    server.sendInt(client.getId(), 0);
                }
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
        port = Integer.valueOf(args[0]);
        server = new Server();
        System.out.println("MySnakeMultiplayerDedicatedServer started!");
        if(server.init(port, 10) != 0) {
            System.out.println("COULDN'T START SERVER ON PORT"+port);
            return;
        }
        syncThread = new SyncThread(200);
        acceptThread = new AcceptThread(server);
        acceptThread.start();
    }
}
