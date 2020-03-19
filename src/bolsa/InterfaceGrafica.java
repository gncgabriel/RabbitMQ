package bolsa;

import java.awt.BorderLayout;
import java.awt.*;
import java.awt.event.*;
import java.io.PrintWriter;
import java.io.StringWriter;


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

    public InterfaceGrafica() {
        super("Bolsa de valores");
        panel = new JPanel();
        BoxLayout b = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(b);
        scrollPane = new JScrollPane(panel);
        JMenuBar menu = new JMenuBar();
        JMenu servidorMenu = new JMenu("Servidor");
        servidorMenu.setMnemonic('S');
        JMenuItem configMenu = new JMenuItem("Configurar servidor");
        servidorMenu.add(configMenu);
        menu.add(servidorMenu);

        t1 = new Thread(getRunnable(adressServer, "#"));

        configMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JDialog serverDialog = new JDialog(InterfaceGrafica.this, "Configuração de servidor", true);
                JLabel lServidor = new JLabel("Endereço do servidor");
                JTextField fieldServidor = new JTextField(16);
                fieldServidor.setText(adressServer);
                JButton definirServidor = new JButton("Definir Servidor");
                definirServidor.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        t1.interrupt();
                        adressServer = fieldServidor.getText();
                        t1 = new Thread(getRunnable(adressServer, "#"));
                        t1.start();
                    }
                });
                JPanel panel2 = new JPanel();

                panel2.add(lServidor);
                panel2.add(fieldServidor);
                panel2.add(definirServidor);

                serverDialog.add(panel2);
                serverDialog.setSize(300, 120);
                serverDialog.setResizable(false);
                serverDialog.setLocationRelativeTo(null);
                serverDialog.setVisible(true);

            }
        });

        t1.start();
        setJMenuBar(menu);
        add(scrollPane);
        setSize(400, 300);
        setLocationRelativeTo(null);
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
                        Envia envia = new Envia(server, message, topicReceived);
                        Thread t1 = new Thread(envia);
                        t1.start();
                        /*
                         * StringTokenizer tok = new StringTokenizer(message, ","); Oferta oferta = new
                         * Oferta(tok.nextToken(), tok.nextToken(), tok.nextToken());
                         */
                        panel.add(new JLabel(message));
                        panel.repaint();
                        panel.revalidate();
                        scrollPane.getViewport().setViewPosition(new Point(0, Integer.MAX_VALUE));
                    };

                    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
                    });
                } catch (Exception e) {
                    JPanel panelE = new JPanel();
                    JScrollPane scrollPane = new JScrollPane(panelE);
                    JDialog dialog = new JDialog(InterfaceGrafica.this, "Erro", true);
                    JTextArea msgE = new JTextArea();

                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String sStackTrace = sw.toString();
                    String s = "" + e.getMessage() + "\n\n" + sStackTrace;
                    msgE.setText(s);
                    msgE.setEditable(false);
                    msgE.setLineWrap(true);
                    msgE.setColumns(23);
                    panelE.add(msgE);
                    dialog.add(scrollPane, BorderLayout.CENTER);
                    dialog.setSize(300, 300);
                    dialog.setResizable(false);
                    dialog.setLocationRelativeTo(null);
                    dialog.setVisible(true);
                }
            }

        };

        return r1;
    }

}