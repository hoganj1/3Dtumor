package Testing;

import Tools.Binomial;
import Tools.Utils;

import java.util.Random;

/**
 * Created by Rafael on 7/20/2017.
 */
public class BinomialTest {
    public static void main(String[] args) {
        Binomial bn=new Binomial(new Random());
        long[] res=new long[100];
        for (int i = 0; i < 100; i++) {
            res[i]=bn.generateBinomial(Long.MAX_VALUE,0.7);
        }
        System.out.println(Utils.ArrToString(res,","));
    }
}
