package communication;

import java.net.InetSocketAddress;
import peer1hw.VectorClock;

/**
 *
 * @author Damiano Di Stefano, Marco Giuseppe Salafia
 */

public class OperationMessage extends Message<Double>
{   
    private VectorClock senderVectorClock;
    private OperationType operationType;
    
    public OperationMessage(InetSocketAddress sender, 
                            InetSocketAddress receiver, 
                            VectorClock senderVectorClock,
                            OperationType operationType,
                            Double body)
    {
        super(sender, receiver, body);
        this.senderVectorClock = senderVectorClock;
        this.operationType = operationType;
    }

    public VectorClock getSenderVectorClock()
    {
        return senderVectorClock;
    } 
    
    public enum OperationType
    {
        DEPOSIT,
        WITHDRAW,
        GLOBALSNAPSHOT;
    }
    
    
}


