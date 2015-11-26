package peer1hw;

import java.io.Serializable;
import java.util.LinkedList;

/**
 *
 * @author Damiano Di Stefano, Marco Giuseppe Salafia
 */
public class State implements Serializable
{
    private LinkedList<String> history;

    public State()
    {
        this.history = new LinkedList<>();
    }  
    
    synchronized protected void recordOperation(String record)
    {
        history.add(record);
    }
    
    protected void printInfrastacstrur()
    {
        for(String record: history)
            System.out.println(record);
    }
    
}
