package peer1hw;

import communication.Forwarder;
import communication.GlobalSnapshotMessage;
import communication.JSMessage;
import communication.Message;
import communication.OperationMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.TreeMap;
import java.util.Map;
import static communication.OperationMessage.getRecordForMessage;
import static peer1hw.Peer.peersAreEqual;

/**
 * Server Handler con ACK
 * @author Damiano Di Stefano, Marco Giuseppe Salafia
 */

//Questo thread gestisce solamente Peers o il Join Server.
class ServerHandler implements Runnable
{
    private InetSocketAddress myInetSocketAddress;
    private HashSet<InetSocketAddress> myNeighbours;
    private Socket incomingSocket;
    
    private VectorClock myVectorClock;
    private Conto conto; 
    private State stato;
    private ArrayList<OperationMessage> messageBuffer;
    private final TreeMap<Marker, Recorder> markerMap;
    private Logger logger;
    
    
    public ServerHandler(InetSocketAddress myInetSocketAddress,
                         Socket incomingSocket, 
                         HashSet<InetSocketAddress> myNeighbours, 
                         VectorClock myVectorClock,
                         Conto conto,
                         State stato,
                         ArrayList<OperationMessage> messageBuffer,
                         TreeMap<Marker, Recorder> markerMap,
                         Logger logger)
    {
        this.myInetSocketAddress = myInetSocketAddress;
        this.myNeighbours = myNeighbours;
        this.incomingSocket = incomingSocket;
        this.myVectorClock = myVectorClock;
        this.conto = conto;
        this.stato = stato;
        this.messageBuffer = messageBuffer;
        this.markerMap = markerMap;
        this.logger = logger;
    }

    @Override
    public void run()
    {
        //Lasciare solo l'input stream
        //Outputstream fa saltare tutti in aria
        try (ObjectInputStream in = new ObjectInputStream(incomingSocket.getInputStream()))
        {
            Message m = (Message) in.readObject();
            processMessage(m);
        }
        catch (IOException | ClassNotFoundException ex)
        {
            Logger.getLogger(ServerHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void processMessage(Message m)
    {
        if (m instanceof JSMessage)  
            updateNeighbours(((JSMessage) m).getNeighbours());
        else if (m instanceof OperationMessage) 
            processOperationMessage((OperationMessage) m);
        else if (m instanceof GlobalSnapshotMessage)
            processGlobalSnapshotMessage((GlobalSnapshotMessage) m);
        else 
            System.err.println("FORMATO DEL MESSAGGIO NON RICONOSCIUTO PER IL PROCESSAMENTO!");
    }
    
    synchronized private void updateNeighbours(HashSet<InetSocketAddress> group)
    {
        myNeighbours.addAll(group);
        System.out.println("I miei vicini sono: ");
        System.out.println(myNeighbours);
    }

    private void processOperationMessage(OperationMessage m)
    {
        switch (m.getOperationType())
        {
            case DEPOSIT:
            case WITHDRAW:
                receiveOperationMessage(m);
                break;
            default:
                System.err.println("OPERAZIONE INVALIDA!");
        }
    }

    private void receiveOperationMessage(OperationMessage m)
    {
        if (myVectorClock.isCausalHappenedBefore(m.getSenderVectorClock()))
        {
            deliverOperationMessage(m);
            checkMessageBuffer();
        }
        else
            enqueueOperationMessage(m);
    }

    private void deliverOperationMessage(OperationMessage m)
    {
        int processIndex = m.getSenderVectorClock().getProcessIndex();
        myVectorClock.updateVectorClockForProcess(processIndex);
        
        switch (m.getOperationType())
        {
            case DEPOSIT:
                conto.deposit(m.getSender(), m.getBody());
                break;
            case WITHDRAW:
                conto.withdraw(m.getSender(), m.getBody());
                break;
            default:
                System.err.println("FORMATO DELL'OPERAZIONE NON RICONOSCIUTO!");
        }
        
        String record = getRecordForMessage(m);
        
        //Controlla se sta registrando ancora messaggi da questo canale
        for(Map.Entry<Marker, Recorder> entry: markerMap.entrySet())
        {
            Recorder r = entry.getValue();
            InetSocketAddress sender = m.getSender();
            if(r.shouldBeRegistered(sender))
            {
                r.getSnapshot().updateChannelsState(record);
            }
        }
        
        logger.log(Level.INFO, record);
        
    }
    
    synchronized private void checkMessageBuffer()
    {
        boolean end = false;
        
        while (!end)
        {
            for(OperationMessage om : messageBuffer)
            {
                if (myVectorClock.isCausalHappenedBefore(om.getSenderVectorClock()))
                {
                    deliverOperationMessage(om);
                    messageBuffer.remove(om);
                    end = false;
                    break;
                }
                else
                    end = true;
            }
        }
    }

    synchronized private void enqueueOperationMessage(OperationMessage m)
    {
        messageBuffer.add(m);
    }

    private void processGlobalSnapshotMessage(GlobalSnapshotMessage m)
    {
        Marker marker = m.getMarker();
      
        synchronized(markerMap)
        {       
            if(markerMap.containsKey(marker))
            {
                Recorder recorder = markerMap.get(marker);
                recorder.incrementCounter();
                recorder.excludeChannelFromRecord(m.getSender());

                if(isMyMarker(marker))
                {
                    Snapshot snapshot = m.getBody();
                    GlobalSnapshot gs = (GlobalSnapshot) recorder.getSnapshot();
                    gs.addSnapshot(snapshot);
                }
                
                System.out.println("\nRicevuto da " + m.getSender() + " >>> " + marker 
                + " Contatore Marker Ricevuti >>> " + recorder.getCounter());
                
                if(recorder.getCounter() == myNeighbours.size())
                    completeSnapshotTask(marker, recorder);
            }
            else //marker ricevuto prima volta
            {
                //TODO CANCELLARE PRINT
                System.out.println("\n[PRIMO!] Ricevuto da " + m.getSender() + " >>> " + marker);
                State frozenState = stato.getCopy()  ;
                LocalSnapshot mySnapshot = new LocalSnapshot(myInetSocketAddress, frozenState); //freeze manuela
                Recorder recorder = new Recorder(mySnapshot);
                markerMap.put(marker, recorder);
                
                if(myNeighbours.size() == 1) //send stato
                    completeSnapshotTask(marker, recorder);
                else
                    inoltraMarker(marker, m.getSender());
            }
        }
    }

    
    private boolean isMyMarker(Marker marker)
    {
        return peersAreEqual(marker.getInitiator(), myInetSocketAddress);
    }

    private void completeSnapshotTask(Marker marker, Recorder recorder)
    {        
        if(isMyMarker(marker)) // stampo globalsnapshot
        {
            System.out.println(">>> [INITIATOR] HO COLLEZIONATO GLI STATI, STAMPO.");
            GlobalSnapshot gs = (GlobalSnapshot) recorder.getSnapshot();
            gs.printSnapshot();
        }
        else //invio mio stato all'initiator
        {
            System.out.println(">>> SEND STATO ALL'INITIATOR.");
            InetSocketAddress initiator = marker.getInitiator();
            Snapshot mySnapshot = markerMap.get(marker).getSnapshot();
            GlobalSnapshotMessage gsMessage = new GlobalSnapshotMessage(myInetSocketAddress,
                                                                        initiator, 
                                                                        marker, 
                                                                        mySnapshot);
            Forwarder.sendMessage(gsMessage);
        }
        
        markerMap.remove(marker);
    }

    private void inoltraMarker(Marker marker, InetSocketAddress excludedPeer)
    {
        for(InetSocketAddress neighbour: myNeighbours)
        {
            if(!peersAreEqual(neighbour, excludedPeer) && 
               !peersAreEqual(neighbour, marker.getInitiator()))
            {
                System.out.println(">>> INOLTRO MARKER A " + neighbour);
                GlobalSnapshotMessage gsMessage = new GlobalSnapshotMessage(myInetSocketAddress, neighbour, marker, null);
                Forwarder.sendMessage(gsMessage);
            }
        }
    }
    
}
