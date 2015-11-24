package communication;

import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 *
 * @author Damiano Di Stefano, Marco Giuseppe Salafia
 */
public abstract class Message<T> implements Serializable
{
    private InetSocketAddress sender;
    private InetSocketAddress receiver;
    private T body;

    public Message(InetSocketAddress sender, InetSocketAddress receiver, T body)
    {
        this.sender = sender;
        this.receiver = receiver;
        this.body = body;
    }

    public InetSocketAddress getSender()
    {
        return sender;
    }

    public void setSender(InetSocketAddress sender)
    {
        this.sender = sender;
    }

    public InetSocketAddress getReceiver()
    {
        return receiver;
    }

    public void setReceiver(InetSocketAddress receiver)
    {
        this.receiver = receiver;
    }

    public T getBody()
    {
        return body;
    }

    public void setBody(T body)
    {
        this.body = body;
    }
    
    
}
