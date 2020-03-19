package bolsa;

public class Oferta{
    String ativo;
    int qtd;
    float valor;
    String broker;

    public Oferta(String qtd, String valor, String broker){
        this.qtd = Integer.parseInt(qtd);
        this.valor = Float.parseFloat(valor);
        this.broker = broker;
    }
    @Override
    public String toString(){
        return qtd+","+valor+","+broker;
    }
}