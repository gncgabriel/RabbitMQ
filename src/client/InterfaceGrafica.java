package client;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.PrintWriter;
import java.io.StringWriter;
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
    private String[][] ativos;
    // private char[] nameBroker = new char[4];
    private String nameBrokerS = "";
    private static Thread t1;

    public InterfaceGrafica() {
        super("Client - Broker");

        while (nameBrokerS.equals("") || nameBrokerS.equals(null)) {
            try {
                nameBrokerS = JOptionPane.showInputDialog(this, "Digite sua identificação de 4 caracteres");

            } catch (Exception e) {
            }

        }
        // nameBroker = nameBrokerS.toCharArray();

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

        try {
            Arquivo arq = new Arquivo();
            ativos = arq.listarArquivo();
        } catch (Exception e) {

        }

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
                    JTable tabela = new JTable(InterfaceGrafica.this.ativos, header);
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
        text2 = new JLabel(nameBrokerS + " - Conectado a " + adressServer);
        text2.setFont(new Font("Calibri", Font.ITALIC, 13));
        panel2.add(text2);

        JPanel panel3 = new JPanel();

        JButton button2 = new JButton("Nova Oferta");

        panel3.add(button2);

        button2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JDialog oferta = new JDialog(InterfaceGrafica.this, "Nova oferta", true);
                String[] s = new String[InterfaceGrafica.this.ativos.length];
                for (int i = 0; i < InterfaceGrafica.this.ativos.length; i++) {
                    s[i] = InterfaceGrafica.this.ativos[i][0];
                }
                JComboBox<String> itens = new JComboBox<String>(s);
                JPanel panel = new JPanel();
                panel.setLayout(new FlowLayout());
                JLabel ativo = new JLabel("Ativo: ");
                panel.add(ativo);
                panel.add(itens);
                JLabel qtd = new JLabel("Quantidade: ");
                JTextField qtdField = new JTextField(7);
                panel.add(qtd);
                panel.add(qtdField);
                JLabel valor = new JLabel("Valor: ");
                JTextField valorField = new JTextField(7);
                panel.add(valor);
                panel.add(valorField);
                String[] opcao = new String[2];
                opcao[0] = "compra";
                opcao[1] = "venda";
                JComboBox<String> op = new JComboBox<String>(opcao);
                JLabel labelOpcao = new JLabel("Operação: ");
                panel.add(labelOpcao);
                panel.add(op);

                JButton sub = new JButton("Enviar oferta");

                JPanel panel2 = new JPanel();
                panel2.setLayout(new FlowLayout());
                panel2.add(sub);

                sub.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String msgOferta = qtdField.getText() + "," + valorField.getText() + "," + nameBrokerS;
                        String topicoOferta = op.getSelectedItem() + "." + itens.getSelectedItem();
                        ClientSend cs = new ClientSend(adressServer, msgOferta, topicoOferta);
                        Thread tSendMsg = new Thread(cs);
                        tSendMsg.start();

                        oferta.setVisible(false);

                    }
                });

                oferta.add(panel, BorderLayout.NORTH);
                oferta.add(panel2, BorderLayout.SOUTH);
                oferta.setSize(600, 120);
                oferta.setResizable(true);
                oferta.setLocationRelativeTo(null);
                oferta.setVisible(true);

            }
        });
        panel4 = new JPanel();
        panel4.setLayout(new BoxLayout(panel4, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(panel4);
        scrollPane.setSize(100, 500);
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        add(panel3, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panel2, BorderLayout.SOUTH);
        setSize(450, 600);
        setVisible(true);
        setResizable(false);
        setLocationRelativeTo(null);

        t1 = new Thread(getRunnable());
        t1.start();

        servidorItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                JDialog servDialog = new JDialog(InterfaceGrafica.this, "Configuração do servidor", true);
                servDialog.setLayout(new BorderLayout());
                JPanel panelServer = new JPanel();
                JLabel sConfigTitle = new JLabel("Servidor: ");
                JButton btnSetServer = new JButton("Definir Servidor");

                btnSetServer.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        t1.interrupt();
                        adressServer = fieldServer.getText();
                        t1 = new Thread(getRunnable());
                        t1.start();
                        text2.setText(nameBrokerS + " - Conectado a " + adressServer);
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

    }

    public Runnable getRunnable() {
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
                        count = count + 1;
                        JLabel topico = new JLabel(delivery.getEnvelope().getRoutingKey());
                        topico.setForeground(Color.WHITE);
                        topico.setFont(new Font("Calibri", Font.ITALIC, 15));
                        JPanel panelTopico = new JPanel(new FlowLayout());
                        panelTopico.add(topico);
                        panelTopico.setBackground(new Color(0, 0, 0));
                        JLabel msg = new JLabel(message);
                        msg.setFont(new Font("Calibri", Font.BOLD, 15));
                        JPanel panelMsg = new JPanel(new FlowLayout());
                        panelMsg.setBackground(new Color(255, 255, 255));
                        panelMsg.add(msg);

                        JPanel panelMsgTopico = new JPanel();
                        panelMsgTopico.setLayout(new BorderLayout());
                        panelMsgTopico.add(panelTopico, BorderLayout.NORTH);
                        panelMsgTopico.add(panelMsg, BorderLayout.SOUTH);
                        panelMsgTopico.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
                        panelMsgTopico.setMaximumSize(new Dimension(250, 70));

                        panel4.add(panelMsgTopico);
                        panel4.revalidate();
                        panel4.repaint();
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