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
    
    static public String getRecordForMessage(OperationMessage message)
    {
        OperationMessage.OperationType type = message.getOperationType();
        String record = null;
        
        switch (type)
        {
            case DEPOSIT:
                record = "[PEER: " + message.getSender() +
                         " deposita " + message.getBody() + "]";
                break;
            case WITHDRAW:
                record = "[PEER: " + message.getSender() +
                         " preleva " + message.getBody() + "]";
                break;
            default:
                record = "FORMATO MESSAGGIO NON RICONOSCIUTO";
        }
        
        return record;
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

    public OperationType getOperationType()
    {
        return operationType;
    }
    
    
}


