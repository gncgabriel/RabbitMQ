package client;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class InterfaceGrafica extends JFrame {

    private static final long serialVersionUID = 1987050267612288774L;
    private BorderLayout layout;
    private JPanel panel1;
    private String adressServer = "localhost";
    private static final String EXCHANGE_NAME = "BROKER";
    private ArrayList<String> topicos = new ArrayList<String>();
    JTextField fieldServer = new JTextField(20);
    JLabel text2;
    JScrollPane scrollPane;
    JScrollBar sb;
    static JPanel panel4;
    private int count = 0;

    public InterfaceGrafica() {
        super("Client - Broker");
        topicos.add("#");
        layout = new BorderLayout(5, 5);
        Container c = getContentPane();
        c.setLayout(layout);

        JMenuBar barra = new JMenuBar();
        setJMenuBar(barra);
        JMenu servidor = new JMenu("Servidor");
        servidor.setMnemonic('S');
        JMenuItem servidorItem = new JMenuItem("Configurar");
        servidor.add(servidorItem);

        servidorItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JDialog servDialog = new JDialog(InterfaceGrafica.this, "Configuração do servidor", true);
                servDialog.setLayout(new BorderLayout());
                JPanel panelServer = new JPanel();
                JLabel sConfigTitle = new JLabel("Servidor: ");
                JButton btnSetServer = new JButton("Definir Servidor");

                btnSetServer.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        adressServer = fieldServer.getText();
                        text2.setText("Conectado a " + adressServer);
                    }
                });

                fieldServer.setText(adressServer);
                panelServer.add(sConfigTitle);
                panelServer.add(fieldServer);
                panelServer.add(btnSetServer);

                servDialog.add(panelServer);
                servDialog.setSize(300, 120);
                servDialog.setResizable(false);
                servDialog.setLocationRelativeTo(null);
                servDialog.setVisible(true);

            }
        });

        JMenu ativos = new JMenu("Ativos");
        ativos.setMnemonic('A');

        JMenuItem listaAtivos = new JMenuItem("Lista de ativos");
        ativos.add(listaAtivos);

        listaAtivos.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JDialog listaAtivosDialog = new JDialog(InterfaceGrafica.this, "Lista dos Ativos", true);
                listaAtivosDialog.setLayout(new BorderLayout());
                String[] header = new String[3];
                header[0] = "Codigo";
                header[1] = "Pregao";
                header[2] = "Atividade";
                
                try {
                    Arquivo arq = new Arquivo();
                    String[][] ativos = arq.listarArquivo();
                    
                    JTable tabela = new JTable(ativos, header);
                    JScrollPane scrollPane = new JScrollPane(tabela);
                    tabela.setFillsViewportHeight(true);
                    listaAtivosDialog.add(scrollPane);
                    listaAtivosDialog.setSize(500, 720);
                    listaAtivosDialog.setResizable(true);
                    listaAtivosDialog.setLocationRelativeTo(null);
                    listaAtivosDialog.setVisible(true);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        });

        JMenuItem seguirAtivos = new JMenuItem("Seguir ativos");
        ativos.add(seguirAtivos);

        barra.add(servidor);
        barra.add(ativos);
        panel1 = new JPanel();
        panel1.setLayout(new FlowLayout());
        add(panel1, BorderLayout.CENTER);

        JPanel panel2 = new JPanel();
        text2 = new JLabel("Conectado a " + adressServer);
        text2.setFont(new Font("Calibri", Font.ITALIC, 13));
        panel2.add(text2);

        JPanel panel3 = new JPanel();

        JButton button2 = new JButton("Nova Oferta");

        panel3.add(button2);

        button2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                ClientSend cs = new ClientSend(adressServer, "TESTE ENVIA", "acao.compra");
                Thread tSendMsg = new Thread(cs);
                tSendMsg.start();

            }
        });
        panel4 = new JPanel();
        panel4.setLayout(new BoxLayout(panel4, BoxLayout.Y_AXIS));
        panel4.setAlignmentX(CENTER_ALIGNMENT);
        scrollPane = new JScrollPane(panel4);
        

        add(panel3, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panel2, BorderLayout.SOUTH);
        setSize(450, 600);
        setVisible(true);
        setResizable(false);
        setLocationRelativeTo(null);

        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                Thread.currentThread();

                try {
                    ConnectionFactory factory = new ConnectionFactory();
                    factory.setHost(adressServer);
                    Connection connection = factory.newConnection();
                    Channel channel = connection.createChannel();

                    channel.exchangeDeclare(EXCHANGE_NAME, "topic");
                    String queueName = channel.queueDeclare().getQueue();
                    for (String topico : topicos) {
                        channel.queueBind(queueName, EXCHANGE_NAME, topico);
                    }

                    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

                    DeliverCallback deliverCallback = (consumerTag, delivery) -> {

                        String message = new String(delivery.getBody(), "UTF-8");
                        System.out.println(
                                " [x] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
                        count = count +1;
                        /*
                         * if(count % 50 == 0){ panel4.removeAll(); panel4.revalidate();
                         * panel4.repaint(); }
                         */
                        JLabel topico = new JLabel(delivery.getEnvelope().getRoutingKey());
                        topico.setForeground(Color.WHITE);
                        topico.setFont(new Font("Calibri", Font.ITALIC, 15));
                        JPanel panelTopico = new JPanel(new FlowLayout());
                        panelTopico.add(topico);
                        panelTopico.setBackground(new Color(0, 0, 0));
                        JLabel msg = new JLabel(message);
                        msg.setFont(new Font("Calibri", Font.PLAIN, 15));
                        JPanel panelMsg = new JPanel(new FlowLayout());
                        panelMsg.setBackground(new Color(255, 255, 255));
                        panelMsg.add(msg);

                        JPanel panelMsgTopico = new JPanel();
                        panelMsgTopico.setLayout(new FlowLayout());
                        panelMsgTopico.add(panelTopico);
                        panelMsgTopico.add(panelMsg);
                        panelMsgTopico.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

                        scrollPane.getViewport().setViewPosition(new Point(0, Integer.MAX_VALUE));

                        panel4.add(panelMsgTopico);
                        panel4.revalidate();
                        panel4.repaint();

                    };
                    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
                    });
                } catch (Exception e) {
                    System.out.println("Erro no Recebe do Client " + e.getMessage());
                }

            }
        };

        Thread t1 = new Thread(r1);
        t1.start();

    }

}