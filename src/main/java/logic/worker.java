package logic;

import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by jorgelima on 12/10/15.
 */
public class worker implements Callable {
    LinkedHashMap combined;
    int N;
    String msg;
    Languifier lan;

    public worker( int N, String msg, Languifier lan){

        this.N = N;
        this.msg = msg;
        this.lan = lan;
    }


    @Override
    public Object call() throws Exception {

            return lan.retrieveNGramsfromDB(N, msg);

    }
}
