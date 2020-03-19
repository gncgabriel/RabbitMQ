package bolsa;
import java.awt.event.*;
public class Bolsa {

    public static void main(String[] args) {
        InterfaceGrafica ui = new InterfaceGrafica();
        ui.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                System.exit(0);
            }
        });

    }

}