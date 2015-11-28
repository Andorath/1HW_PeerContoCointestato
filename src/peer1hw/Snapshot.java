package peer1hw;

import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 *
 * @author Damiano Di Stefano, Marco Giuseppe Salafia
 */
public abstract class Snapshot implements Serializable
{
    protected InetSocketAddress ownerPeer;
    
    public abstract void printSnapshot();
    public abstract void updateChannelsState(String str);

    public Snapshot(InetSocketAddress ownerPeer)
    {
        this.ownerPeer = ownerPeer;
    }

    public InetSocketAddress getOwnerPeer()
    {
        return ownerPeer;
    }
    
    
}
