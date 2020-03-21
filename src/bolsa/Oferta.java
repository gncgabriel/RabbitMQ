package bolsa;

public class Oferta {
    String ativo;
    int qtd;
    float valor;
    String broker;
    String operacao;

    public Oferta(String qtd, String valor, String broker, String operacao, String ativo) {
        this.ativo = ativo;
        this.qtd = Integer.parseInt(qtd);
        this.valor = Float.parseFloat(valor);
        this.broker = broker;
        this.operacao = operacao;
    }

    @Override
    public String toString() {
        return ativo + "," + qtd + "," + valor + "," + broker;
    }

    public boolean aceitaVender(Oferta o) {
        if (this.ativo.equals(o.ativo) && o.valor >= this.valor && this.qtd == o.qtd && !this.broker.equals(o.broker) && !this.operacao.equals(o.operacao)) {
            return true;
        }
        return false;
    }
}