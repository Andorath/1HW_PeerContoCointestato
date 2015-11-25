package peer1hw;

import communication.Forwarder;
import communication.Message;
import communication.OperationMessage;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Scanner;

/**
 *
 * @author Damiano Di Stefano, Marco Giuseppe Salafia
 */
public class ClientHandler implements Runnable
{
    private HashSet<InetSocketAddress> myNeighbours;
    private VectorClock myVectorClock;
    private Conto conto; 
    private InetSocketAddress myInetSocketAddress;

    public ClientHandler(InetSocketAddress myInetSocketAddress, 
                         HashSet<InetSocketAddress> myNeighbours, 
                         VectorClock myVectorClock, 
                         Conto conto)
    {
        this.myInetSocketAddress = myInetSocketAddress;
        this.myNeighbours = myNeighbours;
        this.myVectorClock = myVectorClock;
        this.conto = conto;
    }
    
    @Override
    public void run()
    {
        Scanner scanner = new Scanner(System.in);
        int choice;
        
        while (true)
        {
            printMenu();
            choice = scanner.nextInt();
            switch(choice)
            {
                case 1:
                    withdraw();
                    break;
                case 2:
                    deposit();
                    break;
                case 3:
                    printTotal();
                    break;
                case 4:
                    takeGlobalSnapshot();
                    break;
                case 5:
                    printLog();
                    break;
                default:
                    System.out.println("Comando non contemplato!");
            }
        }
    }
    
    public void printMenu()
    {
        System.out.println("--------------MENU--------------");
        System.out.println("\n\t1) Preleva");
        System.out.println("\t2) Deposita");
        System.out.println("\t3) Stampa il saldo attuale");
        System.out.println("\t4) Global Snapshot");
        System.out.println("\t5) Stampa Log\n");
        System.out.println("--------------------------------");
        System.out.println("\nInserisci il comando:\n");
    }

    
    
    private void causalOrderMulticast(OperationMessage.OperationType operationType,
                                      double amount)
    {
        myVectorClock.updateVectorClock();
        for(InetSocketAddress receiver: myNeighbours)
        {
            Message m = new OperationMessage(myInetSocketAddress, 
                                             receiver, 
                                             myVectorClock, 
                                             operationType, 
                                             amount);
            Forwarder.sendMessage(m);
        }
        
    }
    
    private void deposit()
    {
        double amount = getAmount();
        causalOrderMulticast(OperationMessage.OperationType.DEPOSIT, amount);
        conto.deposit(myInetSocketAddress, amount);
    }
    
    private void withdraw()
    {
        //Possiamo avere saldo negativo.
        //Non c'è controllo sul quantitativo che possiamo prelevare;
        double amount = getAmount();
        causalOrderMulticast(OperationMessage.OperationType.WITHDRAW, amount);
        conto.withdraw(myInetSocketAddress, amount);
    }
    
    private double getAmount()
    {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\t> Amount: ");
        return scanner.nextDouble();
    }

    private void printTotal()
    {
        System.out.println("SALDO DISPONIBILE :> " + conto.getTotal());
    }
    
    //TODO: che non ti devi proprio

    private void takeGlobalSnapshot()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void printLog()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
