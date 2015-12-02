package peer1hw;

import communication.OperationMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Damiano Di Stefano, Marco Giuseppe Salafia
 */
public class Peer
{
    //Magic Numbers
    private static final int nThread = 100;
    private static final int N_PEER = 4;
    
    private int myPort;
    private InetSocketAddress myInetSocketAddress;
    private HashSet<InetSocketAddress> myNeighbours;
    
    private Conto conto;
    private VectorClock myVectorClock;
    private ArrayList<OperationMessage> messageBuffer;
    
    private State stato;
    private TreeMap<Marker, Recorder> markerMap;
    
    private Logger logger;

    //Questo peer presuppone che la rete sia a regime per poter fare
    //operazioni sul conto. (Tutti i conti non hanno saldo quando vengono creati
    //i peer).
    public Peer(int myPort)
    {
        this.myPort = myPort;
        this.conto = new Conto();
        this.myVectorClock = new VectorClock(N_PEER, myPort % 10);
        this.messageBuffer = new ArrayList<>();
        this.stato = new State();
        
        assert stato != null;
        
        this.logger = initLog();
        
        this.markerMap = new TreeMap<>();
    }
    
    private Logger initLog()
    {
        Logger l = Logger.getLogger(Peer.class.getName());
        
        StateHandler sh = new StateHandler(this.stato);
        try
        {
            FileHandler fh = new FileHandler("./src/peer1hw/log" + (myPort % 10));
            l.addHandler(fh);
        }
        catch (IOException | SecurityException ex)
        {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        l.addHandler(sh);
        l.setUseParentHandlers(false); // non logga nello stream System.out
        
        return l;
    }
    
    public void connect(String js_addr, int js_port)
    {
        try (Socket socket = new Socket(js_addr, js_port))
        {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            
            myInetSocketAddress = new InetSocketAddress(socket.getLocalAddress(), myPort);
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
        new Thread(new ClientHandler(myInetSocketAddress,
                                     myNeighbours, 
                                     myVectorClock, 
                                     conto,
                                     stato,
                                     markerMap,
                                     logger)).start();
    }

    private void startServerSide()
    {
        try (ServerSocket myServerSocket = new ServerSocket(myPort))
        {
            Executor executor = Executors.newFixedThreadPool(nThread);
            
            while (true)
            {
                Socket incomingSocket = myServerSocket.accept();
                ServerHandler worker = new ServerHandler(myInetSocketAddress, 
                                                         incomingSocket, 
                                                         myNeighbours, 
                                                         myVectorClock,
                                                         conto,
                                                         stato,
                                                         messageBuffer,
                                                         markerMap,
                                                         logger);
                executor.execute(worker);
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static boolean peersAreEqual(InetSocketAddress p1, InetSocketAddress p2)
    {
        return p1.getHostString().equalsIgnoreCase(p2.getHostString()) &&
               p1.getPort() == p2.getPort();
    }
    
}
