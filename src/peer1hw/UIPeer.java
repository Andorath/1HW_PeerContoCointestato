/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peer1hw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marco
 */
public class UIPeer
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        File file = new File("/Users/Marco/NetBeansProjects/1HW_PeerContoCointestato/src/peer1hw/port");
        int myPort = 0;
        try 
        {
            Scanner s = new Scanner(file);
            myPort = s.nextInt();
            
            PrintWriter p = new PrintWriter(file);
            p.print(myPort+1);
            p.flush();
            
            System.out.println("La mia porta è: " + myPort);
            
            Peer peer = new Peer(myPort);
            peer.connect();
            peer.start();
            
        } 
        catch (FileNotFoundException ex) 
        {
            Logger.getLogger(UIPeer.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
}
