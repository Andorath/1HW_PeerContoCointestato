package peer1hw;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 *
 * @author Damiano Di Stefano, Marco Giuseppe Salafia
 */
public class StateHandler extends Handler
{
    private final State stato;

    public StateHandler(State stato)
    {
        this.stato = stato;
    }

    @Override
    public void publish(LogRecord record)
    {
        stato.recordOperation(record.getMessage());    
    }

    @Override
    public void flush(){ }

    @Override
    public void close() throws SecurityException
    {
    }
    
}
