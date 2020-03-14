package server;

public class Server{
    
    public static void main(String[] args) {
        
        String server = args.length > 0 ? args[0] : "localhost";
        ServerRecebe rcv = new ServerRecebe(server);
        Thread t1 = new Thread(rcv);
        t1.start();
        
    }
}