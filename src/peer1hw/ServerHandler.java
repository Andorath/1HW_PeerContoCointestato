package peer1hw;

import communication.JSMessage;
import communication.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
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
    private HashSet<InetSocketAddress> myNeighbours;
    private Socket incomingSocket;
    
    public ServerHandler(Socket incomingSocket, HashSet<InetSocketAddress> myNeighbours)
    {
        this.myNeighbours = myNeighbours;
        this.incomingSocket = incomingSocket;
    }

    @Override
    public void run()
    {
        //Lasciare solo l'input stream
        try (ObjectOutputStream out = new ObjectOutputStream(incomingSocket.getOutputStream()); 
            ObjectInputStream in = new ObjectInputStream(incomingSocket.getInputStream()))
        {
            JSMessage m = (JSMessage) in.readObject();
            System.out.println(m.getBody());
            myNeighbours = m.getNeighbours();
            for (InetSocketAddress isa : myNeighbours)
                System.out.print(isa.getPort() + "\t");
            System.out.println();
        }
        catch (IOException | ClassNotFoundException ex)
        {
            Logger.getLogger(ServerHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
