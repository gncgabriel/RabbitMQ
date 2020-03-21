package client;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class InterfaceGrafica extends JFrame {

    private static final long serialVersionUID = 1987050267612288774L;

    private String adressServer;
    private static final String EXCHANGE_NAME = "BROKER";
    private ArrayList<String> topicos = new ArrayList<String>();

    static JScrollPane scrollPane;
    static JPanel panel4;
    JDialog brokerFrameDialog;

    private String[][] ativos;
    private String nameBrokerS;

    public InterfaceGrafica() {
        super("Configuração inicial - Broker");

        try {
            Arquivo arq = new Arquivo();
            ativos = arq.listarArquivo();
        } catch (Exception e) {

        }

        JLabel labelServer = new JLabel("Servidor: ");
        JLabel labelName = new JLabel("Nome (4 caracteres) :");
        JTextField fieldServer = new JTextField(17);
        fieldServer.setText("localhost");
        JTextField fieldName = new JTextField(4);
        JButton btnInit = new JButton("Iniciar");

        JPanel panelInit = new JPanel();
        panelInit.setLayout(new FlowLayout());
        panelInit.add(labelServer);
        panelInit.add(fieldServer);
        panelInit.add(labelName);
        panelInit.add(fieldName);

        JPanel panelBtn = new JPanel();
        panelBtn.add(btnInit);

        btnInit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fieldName.getText().length() != 4 || fieldServer.getText().length() == 0) {
                    JOptionPane.showMessageDialog(InterfaceGrafica.this, "Preencha os campos corretamente");
                } else {
                    adressServer = fieldServer.getText();
                    nameBrokerS = fieldName.getText();
                    if (topicos.size() == 0) {
                        gerenciarTopicos();
                        setVisible(false);
                        iniciar();
                    } else {
                        setVisible(false);
                        iniciar();
                        
                    }

                }
            }
        });

        JMenuBar barra = new JMenuBar();
        setJMenuBar(barra);
        JMenu jMenuTopicos = new JMenu("Topicos");
        jMenuTopicos.setMnemonic('T');
        JMenuItem seguirTopicos = new JMenuItem("Gerenciar topicos");
        jMenuTopicos.add(seguirTopicos);

        seguirTopicos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                InterfaceGrafica.this.gerenciarTopicos();
            }
        });

        JMenu ativos = new JMenu("Ativos");
        ativos.setMnemonic('A');
        JMenuItem listaAtivos = new JMenuItem("Lista de ativos");
        ativos.add(listaAtivos);

        barra.add(ativos);
        barra.add(jMenuTopicos);

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

        add(panelInit, BorderLayout.NORTH);
        add(panelBtn, BorderLayout.SOUTH);
        setSize(465, 150);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);

    }

    public Runnable getRunnable() {
        Runnable r1 = new Runnable() {
            @Override
            public void run() {
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
                        StringTokenizer tok = new StringTokenizer(message, ",");
                        String topicReceived = delivery.getEnvelope().getRoutingKey();
                        StringTokenizer tok2 = new StringTokenizer(topicReceived,".");
                        System.out.println(
                                " [x] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");

                        tok.nextToken();
                        tok.nextToken();

                        JLabel topico = new JLabel(delivery.getEnvelope().getRoutingKey());
                        JPanel panelTopico = new JPanel(new FlowLayout());
                        JLabel msg = new JLabel(message);
                        JPanel panelMsg = new JPanel(new FlowLayout());
                        JPanel panelMsgTopico = new JPanel();
                        
                        if (tok.nextToken().equals(nameBrokerS) && !tok2.nextToken().equals("transacao")) {
                            JLabel myLabel = new JLabel("<html>Minha Oferta: "+message+" | "+topicReceived+"</html>");
                            JLabel myLabel2 = new JLabel("<html><br></html>");
                            panelMsgTopico.add(myLabel);
                            panelMsgTopico.add(myLabel2);
                            panelMsgTopico.setMaximumSize(new Dimension(450, 25));
                        } else {

                            topico.setForeground(Color.WHITE);
                            topico.setFont(new Font("Calibri", Font.ITALIC, 15));

                            panelTopico.add(topico);
                            panelTopico.setBackground(new Color(0, 0, 0));

                            msg.setFont(new Font("Calibri", Font.BOLD, 15));

                            panelMsg.setBackground(new Color(255, 255, 255));
                            panelMsg.add(msg);

                            panelMsgTopico.setLayout(new BorderLayout());
                            panelMsgTopico.add(panelTopico, BorderLayout.NORTH);
                            panelMsgTopico.add(panelMsg, BorderLayout.SOUTH);
                            panelMsgTopico.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
                            panelMsgTopico.setMaximumSize(new Dimension(450, 80));

                        }

                        panel4.add(panelMsgTopico);
                        panel4.revalidate();
                        panel4.repaint();
                        scrollPane.getViewport().setViewPosition(new Point(0, Integer.MAX_VALUE));

                    };
                    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
                    });
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(InterfaceGrafica.panel4,
                            e.getMessage() + "\n \n" + "Essa janela será fechada");
                    brokerFrameDialog.setVisible(false);

                }

            }
        };
        return r1;
    }

    public void iniciar() {

        BorderLayout layout;
        JPanel panel1;
        JLabel text2;
        brokerFrameDialog = new JDialog(InterfaceGrafica.this, "Client", true);

        layout = new BorderLayout(5, 5);
        Container c = brokerFrameDialog.getContentPane();
        c.setLayout(layout);

        panel1 = new JPanel();
        panel1.setLayout(new FlowLayout());
        brokerFrameDialog.add(panel1, BorderLayout.CENTER);

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

        Thread t1 = new Thread(getRunnable());
        t1.start();

        brokerFrameDialog.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                System.exit(0);
            }
        });

        scrollPane = new JScrollPane(panel4);
        scrollPane.setSize(100, 500);
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        brokerFrameDialog.add(panel3, BorderLayout.NORTH);
        brokerFrameDialog.add(scrollPane, BorderLayout.CENTER);
        brokerFrameDialog.add(panel2, BorderLayout.SOUTH);
        brokerFrameDialog.setSize(450, 600);
        brokerFrameDialog.setLocationRelativeTo(null);
        brokerFrameDialog.setResizable(false);
        brokerFrameDialog.setVisible(true);

    }

    public void gerenciarTopicos() {
        JDialog gerTopicoDialog = new JDialog(InterfaceGrafica.this, "Gerenciar topicos", true);
        JPanel panel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(panel);
        JButton addBtn = new JButton("Adicionar topico");
        JButton removerBtn = new JButton("Remover topico");
        JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout());
        panel2.add(addBtn);
        panel2.add(removerBtn);

        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
        if (topicos.size() > 0) {
            for (String t : topicos) {
                JLabel labelTopic = new JLabel(t);
                panel3.add(labelTopic);
            }
        } else {
            JLabel label = new JLabel("Adicione um topico para acompanhar");
            JLabel label2 = new JLabel("Adicone # para ver todos os topicos");
            JLabel label3 = new JLabel(
                    "Para seguir um ativo específo adicione: 'operacao.ativo' | Exemplo: compra.PETR4");
            panel3.add(label);
            panel3.add(label2);
            panel3.add(label3);
        }

        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newTopic = JOptionPane.showInputDialog(null, "Digite um topico");
                if (!newTopic.equals(null) && !newTopic.equals("")) {
                    topicos.add(newTopic);
                    panel3.removeAll();
                    for (String t : topicos) {
                        JLabel labelTopic = new JLabel(t);
                        panel3.add(labelTopic);
                    }
                    panel3.repaint();
                    panel3.revalidate();
                }
            }
        });

        removerBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String topicoARemover = JOptionPane.showInputDialog(null, "Digite o topico a ser removido");
                if (!topicoARemover.equals(null) && !topicoARemover.equals("")) {
                    topicos.remove(topicoARemover);
                    panel3.removeAll();
                    for (String t : topicos) {
                        JLabel labelTopic = new JLabel(t);
                        panel3.add(labelTopic);
                    }
                    panel3.repaint();
                    panel3.revalidate();
                }
            }
        });

        JPanel panelBtnFin = new JPanel();
        JButton fin = new JButton("Finalizar");
        fin.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                gerTopicoDialog.setVisible(false);
            }
        });
        panelBtnFin.setLayout(new FlowLayout());
        panelBtnFin.add(fin);

        panel.add(panel3);
        gerTopicoDialog.add(panel2, BorderLayout.NORTH);
        gerTopicoDialog.add(scrollPane, BorderLayout.CENTER);
        gerTopicoDialog.add(panelBtnFin, BorderLayout.SOUTH);
        gerTopicoDialog.setSize(500, 300);
        gerTopicoDialog.setResizable(false);
        gerTopicoDialog.setLocationRelativeTo(null);
        gerTopicoDialog.setVisible(true);
    }
}