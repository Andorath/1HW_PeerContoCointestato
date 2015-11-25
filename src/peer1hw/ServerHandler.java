package peer1hw;

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

/**
 *
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
    private ArrayList<OperationMessage> messageBuffer;
    
    
    public ServerHandler(InetSocketAddress myInetSocketAddress,
                         Socket incomingSocket, 
                         HashSet<InetSocketAddress> myNeighbours, 
                         VectorClock myVectorClock,
                         Conto conto,
                         ArrayList<OperationMessage> messageBuffer)
    {
        this.myInetSocketAddress = myInetSocketAddress;
        this.myNeighbours = myNeighbours;
        this.incomingSocket = incomingSocket;
        this.myVectorClock = myVectorClock;
        this.conto = conto;
        this.messageBuffer = messageBuffer;
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
            case GLOBALSNAPSHOT:
                //TODO
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
    
}
