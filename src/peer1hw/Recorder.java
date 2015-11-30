package peer1hw;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import static peer1hw.Peer.peersAreEqual;

/**
 *
 * @author Damiano Di Stefano, Marco Giuseppe Salafia
 */
public class Recorder
{
    private int counter;
    private Snapshot snapshot;
    private LinkedList<InetSocketAddress> closedChannels;

    public Recorder(Snapshot snapshot)
    {
        this.snapshot = snapshot;
        this.counter = 0;
        this.closedChannels = new LinkedList<>();
    }

    public Snapshot getSnapshot()
    {
        return snapshot;
    }
    
    public int getCounter()
    {
        return counter;
    }
    
    public InetSocketAddress getCommitter()
    {
        return closedChannels.getFirst();
    }
    
    public void incrementCounter()
    {
        counter++;
    }
    
    public void excludeChannelFromRecord(InetSocketAddress i)
    {
        closedChannels.add(i);
    }
    
    public boolean shouldBeRegistered(InetSocketAddress sender)
    {
        for(InetSocketAddress isa: closedChannels)
            if (peersAreEqual(isa, sender))
                return false;
        return true;
    }
}
