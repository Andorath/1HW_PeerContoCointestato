/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peer1hw;

/**
 *
 * @author Marco
 */
public final class VectorClock 
{
    private int processVector[];
    private int myIndex;
    
    public VectorClock(int size, int index)
    {
        this.processVector = new int[size];
        this.myIndex = index;
    }
    
    synchronized public int [] getProcessVector()
    {
        return this.processVector;
    }
    
    synchronized public void printVectorClock()
    {
        for(int e : processVector)
        {
            System.out.print(e + "\t");
        }
        System.out.println();
    }
    
    synchronized public void updateVectorClockForProcess(int i)
    {
        this.processVector[i]++;
    }

    synchronized public void updateVectorClock()
    {
        this.processVector[myIndex]++;
    }
    
    //Rispetto la Causalità
    synchronized public boolean isCausalHappenedBefore(VectorClock vc)
    {
        if (this.processVector[myIndex] != vc.getProcessVector()[myIndex] - 1)
            return false;
        else
            for (int i = 0; i < processVector.length; ++i)
                if((vc.myIndex != this.myIndex) && (this.processVector[i] > vc.getProcessVector()[i]))
                    return false;
        
        return true;
    }
}
