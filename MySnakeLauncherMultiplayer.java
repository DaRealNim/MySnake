import com.iddej.gingerbread2.Gingerbread2;

public class MySnakeLauncherMultiplayer {

    public static void main(String[] args) {

        if(args.length < 3) {
            System.out.println("You need to pass the following arguments: server/client ip port");
            return;
        }

        Gingerbread2.setDebuggingState(false);
        MySnakeMultiplayer mysnake = MySnakeMultiplayer.getInstance();
        mysnake.createRegularWindow("Nim's Snake", MySnakeMultiplayer.SCREEN_WIDTH, MySnakeMultiplayer.SCREEN_HEIGHT, 2);
        mysnake.passArgs(args);
        mysnake.start();
    }
}
