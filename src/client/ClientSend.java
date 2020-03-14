package client;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class ClientSend implements Runnable {
    private static final String EXCHANGE_NAME = "BOLSADEVALORES";
    private String serverAdress;
    private String mensagem;
    private String topico;

    public ClientSend(String serverAdress, String mensagem, String topico) {
        this.serverAdress = serverAdress;
        this.mensagem = mensagem;
        this.topico = topico;
    }

   

    @Override
    public void run() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(serverAdress);
            try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {

                channel.exchangeDeclare(EXCHANGE_NAME, "topic");

                String routingKey = this.topico;
                String message = this.mensagem;

                channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + routingKey + "':'" + message + "'");
            }
        } catch (Exception e) {
            System.out.println("Erro na Envia da Bolsa "+e.getMessage());
        }

    }
}