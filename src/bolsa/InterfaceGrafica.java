package bolsa;

import java.awt.BorderLayout;
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import java.util.StringTokenizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.*;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class InterfaceGrafica extends JFrame {

    static final long serialVersionUID = 1L;
    static String adressServer = "localhost";
    static JPanel panel;
    static JScrollPane scrollPane;
    static final String EXCHANGE_NAME = "BOLSADEVALORES";
    static Thread t1;
    private ArrayList<Oferta> ofertasDeCompra = new ArrayList<Oferta>();
    private ArrayList<Oferta> ofertasDeVenda = new ArrayList<Oferta>();

    private ArrayList<Oferta> livroDeOfertas = new ArrayList<Oferta>();

    public InterfaceGrafica() {
        super("Configuração inicial - Bolsa de Valores");
        JLabel labelServer = new JLabel("Servidor: ");
        JTextField fieldServer = new JTextField(17);
        JButton btnInit = new JButton("Iniciar");
        fieldServer.setText("localhost");
        JPanel panelInit = new JPanel();
        panelInit.setLayout(new FlowLayout());
        panelInit.add(labelServer);
        panelInit.add(fieldServer);
        JPanel panelBtn = new JPanel();
        panelBtn.add(btnInit);

        btnInit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fieldServer.getText().length() == 0) {
                    JOptionPane.showMessageDialog(InterfaceGrafica.this, "Preencha os campos corretamente");
                } else {
                    adressServer = fieldServer.getText();
                    setVisible(false);
                    iniciar();

                }
            }
        });

        add(panelInit, BorderLayout.NORTH);
        add(panelBtn, BorderLayout.SOUTH);
        setSize(465, 100);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);

    }

    public Runnable getRunnable(String server, String topico) {

        Runnable r1 = new Runnable() {
            @Override
            public void run() {

                try {
                    ConnectionFactory factory = new ConnectionFactory();
                    factory.setHost(server);
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

                        panel.add(new JLabel(message + " | " + topicReceived));
                        panel.repaint();
                        panel.revalidate();
                        scrollPane.getViewport().setViewPosition(new Point(0, Integer.MAX_VALUE));

                        StringTokenizer tok = new StringTokenizer(message, ",");
                        StringTokenizer tok2 = new StringTokenizer(topicReceived, ".");

                        Oferta oferta = new Oferta(tok.nextToken(), tok.nextToken(), tok.nextToken(), tok2.nextToken(),
                                tok2.nextToken());
                        if (verifica(oferta) != null) {
                            if (oferta.operacao.equals("compra")) {
                                novaTransacao(oferta, verifica(oferta));
                            } else {
                                novaTransacao(verifica(oferta), oferta);
                            }

                        } else {
                            livroDeOfertas.add(oferta);
                        }

                        Envia envia = new Envia(server, message, topicReceived);
                        Thread t1 = new Thread(envia);
                        t1.start();

                    };

                    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
                    });
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(InterfaceGrafica.scrollPane, e.getMessage(), "Erro",
                            JOptionPane.WARNING_MESSAGE);
                }
            }

        };

        return r1;
    }

    public void iniciar() {
        JFrame bolsaDialog = new JFrame("Bolsa de Valores Em Execução");
        panel = new JPanel();
        BoxLayout b = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(b);
        scrollPane = new JScrollPane(panel);

        t1 = new Thread(getRunnable(adressServer, "#"));

        t1.start();
        bolsaDialog.add(scrollPane);
        bolsaDialog.setSize(400, 300);
        bolsaDialog.setLocationRelativeTo(null);
        bolsaDialog.setDefaultCloseOperation(EXIT_ON_CLOSE);
        bolsaDialog.setVisible(true);
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
        Envia envia = new Envia(adressServer, message, "transacao." + compra.ativo);
        Thread t1 = new Thread(envia);
        t1.start();

        panel.add(new JLabel(message + " | " + "transacao." + compra.ativo));
        panel.repaint();
        panel.revalidate();
        scrollPane.getViewport().setViewPosition(new Point(0, Integer.MAX_VALUE));
        try {
            livroDeOfertas.remove(compra);
            livroDeOfertas.remove(venda);
        } catch (Exception e) {

        }
    }

    public Oferta verifica(Oferta oferta) {
        if (oferta.operacao.equals("compra")) {
            for (Oferta o : livroDeOfertas) {
                if (o.aceitaVender(oferta)) {

                    return o;
                }
            }
        } else {
            for (Oferta o : livroDeOfertas) {
                if (oferta.aceitaVender(o)) {

                    return o;
                }
            }
        }

        return null;
    }
}