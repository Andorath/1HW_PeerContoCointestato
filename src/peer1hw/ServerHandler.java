package peer1hw;

import communication.JSMessage;
import communication.Message;
import communication.OperationMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private ArrayList<OperationMessage> messageBuffer;
    
    
    public ServerHandler(InetSocketAddress myInetSocketAddress,
                         Socket incomingSocket, 
                         HashSet<InetSocketAddress> myNeighbours, 
                         VectorClock myVectorClock,
                         ArrayList<OperationMessage> messageBuffer)
    {
        this.myInetSocketAddress = myInetSocketAddress;
        this.myNeighbours = myNeighbours;
        this.incomingSocket = incomingSocket;
        this.myVectorClock = myVectorClock;
        this.messageBuffer = messageBuffer;
    }

    @Override
    public void run()
    {
        //Lasciare solo l'input stream
        try (ObjectOutputStream out = new ObjectOutputStream(incomingSocket.getOutputStream()); 
            ObjectInputStream in = new ObjectInputStream(incomingSocket.getInputStream()))
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
        if (m instanceof JSMessage)  updateNeighbours(((JSMessage) m).getNeighbours());
    }
    
    private void updateNeighbours(HashSet<InetSocketAddress> group)
    {
        myNeighbours = group;
        System.out.println("I miei vicini sono: ");
        System.out.println(myNeighbours);
    }
    
}
