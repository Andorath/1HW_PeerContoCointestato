package peer1hw;

import java.net.InetSocketAddress;
import java.util.LinkedList;

/**
 *
 * @author Damiano Di Stefano, Marco Giuseppe Salafia
 */
public class GlobalSnapshot extends Snapshot
{
    LinkedList<Snapshot> childSnapshots = new LinkedList<>();

    public GlobalSnapshot(InetSocketAddress ownerPeer)
    {
        super(ownerPeer);
    }

    @Override
    public void printSnapshot()
    {
        System.out.println(">>> Stampo Global Snapshot <<<");
        for(Snapshot sn : childSnapshots)
            sn.printSnapshot();
    }
    
    public void addSnapshot(Snapshot sn)
    {
        childSnapshots.add(sn);
    }

    @Override
    public void updateChannelsState(String str)
    {
        for(Snapshot snap : childSnapshots)
        {
            if (Peer.peersAreEqual(ownerPeer, snap.ownerPeer))
                snap.updateChannelsState(str);
        }
    }
}
