package GridExtensions;

import Grids.Agent0;
import Grids.AgentBase;

/**
 * Created by bravorr on 6/26/17.
 */
@FunctionalInterface
public interface OverlapForceResponse {
    double CalcForce(double overlap);
}

