package bolsa;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Recebe implements Runnable {
    private static final String EXCHANGE_NAME = "BOLSADEVALORES";
    private String adressServer;
    private String topico;

    public Recebe(String serverName, String topico) {
        adressServer = serverName;
        this.topico = topico;
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
                System.out.println(" [x] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
                Envia envia = new Envia(adressServer,message,delivery.getEnvelope().getRoutingKey());
                Thread t1 = new Thread(envia);
                t1.start();
            };
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });
        } catch (Exception e) {
            System.out.println("Erro no Recebe da Bolsa "+e.getMessage());
        }

    }
}