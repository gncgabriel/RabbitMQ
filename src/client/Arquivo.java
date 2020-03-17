package client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Arquivo {
    private static String nomeArquivo = "lista.txt";
    private static ArrayList<String> codigo;
    private static ArrayList<String> pregao;
    private static ArrayList<String> atividade;

    public static void main(String[] args) throws Exception {
       // listarArquivo();
    }

    public String[][] listarArquivo() throws Exception {
        codigo = new ArrayList<String>();
        pregao = new ArrayList<String>();
        atividade = new ArrayList<String>();

        FileInputStream stream = new FileInputStream(nomeArquivo);
        InputStreamReader streamReader = new InputStreamReader(stream);
        BufferedReader reader = new BufferedReader(streamReader);
        String line = null;
        reader.readLine();

        while ((line = reader.readLine()) != null) {
            StringTokenizer tok = new StringTokenizer(line, ";");

            while (tok.hasMoreTokens()) {

                codigo.add(tok.nextToken());
                pregao.add(tok.nextToken());
                atividade.add(tok.nextToken());

            }

        }
        reader.close();
        streamReader.close();
        stream.close();

        String[][] tabelaAtivos = new String[codigo.size()][3];

        for (int i = 0; i < codigo.size(); i++) {
            tabelaAtivos[i][0] = codigo.get(i);
        }

        for (int i = 0; i < codigo.size(); i++) {
            tabelaAtivos[i][1] = pregao.get(i);
        }

        for (int i = 0; i < codigo.size(); i++) {
            tabelaAtivos[i][2] = atividade.get(i);
        }

        return tabelaAtivos;
    }

}
