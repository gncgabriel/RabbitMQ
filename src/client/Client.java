package client;
import java.awt.event.*;
public class Client{
    public static void main(String[] args) {
        InterfaceGrafica ui = new InterfaceGrafica();
        ui.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                System.exit(0);
            }
        });
    }
}