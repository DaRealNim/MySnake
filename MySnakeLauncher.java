import com.iddej.gingerbread2.Gingerbread2;
import java.util.Random;

public class MySnakeLauncher {

    public static int[] fillOneAndZeroes(int n) {
        Random rand = new Random();
        int[] ret = new int[n];
        for(int i=0;i<n-1;i++) {
            ret[i] = rand.nextInt(1);
        }
        ret[ret.length-1] = 0;
        return ret;
    }


    public static void main(String[] args) {
        Gingerbread2.setDebuggingState(false);
        MySnake mysnake = MySnake.getInstance();
        mysnake.createRegularWindow("Nim's Snake", MySnake.SCREEN_WIDTH, MySnake.SCREEN_HEIGHT, 2);
        mysnake.start();
    }
}
