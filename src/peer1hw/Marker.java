package peer1hw;

import java.io.Serializable;
import java.net.InetSocketAddress;
import static peer1hw.Peer.peersAreEqual;

/**
 *
 * @author Damiano Di Stefano, Marco Giuseppe Salafia
 */
public class Marker implements Serializable, Comparable<Marker>
{
    private InetSocketAddress initiator;
    private int globalSnapshotNumber;

    public Marker(InetSocketAddress initiator, int globalSnapshotNumber)
    {
        this.initiator = initiator;
        this.globalSnapshotNumber = globalSnapshotNumber;
    }

    public InetSocketAddress getInitiator()
    {
        return initiator;
    }

    public void setInitiator(InetSocketAddress initiator)
    {
        this.initiator = initiator;
    }

    @Override
    public int compareTo(Marker o)
    {   
        if(peersAreEqual(initiator, o.getInitiator())
           && globalSnapshotNumber == o.globalSnapshotNumber)
            return 0;
        else 
            return -1;
    }
    
    @Override
    public String toString()
    {
        return "[MARKER] Initiator: " + initiator + " GlobalShotNumber: " + globalSnapshotNumber;
    }
}
