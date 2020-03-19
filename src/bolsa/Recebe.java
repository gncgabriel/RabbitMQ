package bolsa;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Recebe implements Runnable {
    private static final String EXCHANGE_NAME = "BOLSADEVALORES";
    private String adressServer;
    private String topico;
    private ArrayList<Oferta> ofertasDeCompra;
    private ArrayList<Oferta> ofertasDeVenda;

    public Recebe(String serverName, String topico) {
        adressServer = serverName;
        this.topico = topico;
        ofertasDeCompra = new ArrayList<Oferta>();
        ofertasDeVenda = new ArrayList<Oferta>();
    }

    @Override
    public void run() {

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(adressServer);
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(EXCHANGE_NAME, "topic");
            String queueName = channel.queueDeclare().getQueue();

            channel.queueBind(queueName, EXCHANGE_NAME, topico);

            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                String topicReceived = delivery.getEnvelope().getRoutingKey();
                System.out.println(" [x] Received '" + topicReceived + "':'" + message + "'");
                Envia envia = new Envia(adressServer, message, topicReceived);
                Thread t1 = new Thread(envia);
                t1.start();
                StringTokenizer tok = new StringTokenizer(message, ",");
                Oferta oferta = new Oferta(tok.nextToken(), tok.nextToken(), tok.nextToken());
              

            };

            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });
        } catch (Exception e) {
            System.out.println("Erro no Recebe da Bolsa " + e.getMessage());
        }

    }

    public void novaTransacao(Oferta compra, Oferta venda) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date date = new Date();
        String dataHora = formatter.format(date);
        String corrVenda = venda.broker;
        String corrCompra = compra.broker;
        int qtd = venda.qtd;
        float valor = venda.valor;
        String message = dataHora + "," + corrVenda + "," + corrCompra + "," + qtd + "," + valor;
        Envia envia = new Envia(adressServer, message, "transacao.*");
        Thread t1 = new Thread(envia);
        t1.start();
    }
}