package communication;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Damiano Di Stefano, Marco Giuseppe Salafia
 */
public class Forwarder
{
    public static void sendMessage(Message m)
    {
        InetSocketAddress receiver = m.getReceiver();
        
        try(Socket socket = new Socket(receiver.getAddress(), 
                                       receiver.getPort()))
        {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(m);
        }
        catch (IOException ex)
        {
            Logger.getLogger(Forwarder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
