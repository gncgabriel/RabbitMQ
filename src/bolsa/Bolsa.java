package bolsa;



public class Bolsa {
   

    public static void main(String[] args) {

        Recebe recebe;
        if(args.length > 0){
            recebe = new Recebe(args[0], "#");
        }else{
            recebe = new Recebe("localhost", "#"); 
        }
        
        Thread t1 = new Thread(recebe);
        t1.start();

      
        System.out.println("[::] Bolsa de Valores em execucao");

    }

}