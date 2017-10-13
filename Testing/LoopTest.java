package Testing;

import Grids.AgentSQ2;
import Grids.Grid2;
import Tools.Utils;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by bravorr on 6/20/17.
 */


class LoopAgent extends AgentSQ2<LoopTest> {

}

public class LoopTest extends Grid2<LoopAgent> {
    final Random rn=new Random();
    final ArrayList<LoopAgent> agents=new ArrayList<>();
    public LoopTest() {
        super(10, 10, LoopAgent.class);
        while(true){
            System.out.println(GetPop());
            NewAgent(4,4);
            for (LoopAgent a : this) {
                a.Move(Utils.BoundVal(rn.nextInt(3)-1,0,xDim),Utils.BoundVal(rn.nextInt(3)-1,0,yDim));
                GetAgents(agents,a.Isq());
                if(rn.nextDouble()<0.01){a.Dispose();}
            };
            CleanShuffInc(rn);
        }
    }

    public static void main(String[] args) {
        LoopTest lp=new LoopTest();
    }
}
