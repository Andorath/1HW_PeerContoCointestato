package peer1hw;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Damiano Di Stefano, Marco Giuseppe Salafia
 */
class Peer
{
    private static final int nThread = 100;
    
    private int myPort;
    HashSet<InetSocketAddress> myNeighbours;

    public Peer(int myPort)
    {
        this.myPort = myPort;
    }
    
    public void connect(String js_addr, int js_port)
    {
        try (Socket socket = new Socket(js_addr, js_port))
        {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            
            InetSocketAddress myInetSocketAddress = new InetSocketAddress(socket.getLocalAddress(), myPort);
            out.writeObject(myInetSocketAddress);
            myNeighbours = (HashSet<InetSocketAddress>) in.readObject();
        }
        catch (IOException | ClassNotFoundException ex)
        {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void start()
    {
        startClientSide();
        
        startServerSide();
    }
    
    private void startClientSide()
    {
        //new ClientHandler(myNeighbours).start();
    }

    private void startServerSide()
    {
        try (ServerSocket myServerSocket = new ServerSocket(myPort))
        {
            Executor executor = Executors.newFixedThreadPool(nThread);
            
            while (true)
            {
                Socket incomingSocket = myServerSocket.accept();
                ServerHandler worker = new ServerHandler(incomingSocket, myNeighbours);
                executor.execute(worker);
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
