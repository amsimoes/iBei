import java.io.*;

public class FicheiroDeTexto {
    private BufferedReader fR;
    private BufferedWriter fW;

    public void abreLeitura(String nomeDoFicheiro) throws IOException {
        try {
            fR = new BufferedReader(new FileReader(nomeDoFicheiro));
        } catch (FileNotFoundException e) {
            File f = new File(nomeDoFicheiro);
            f.createNewFile();
            fR = new BufferedReader(new FileReader(nomeDoFicheiro));
        }
    }

    public void abreEscrita(String nomeDoFicheiro) throws IOException{
        fW = new BufferedWriter(new FileWriter(nomeDoFicheiro,true));
    }

    public String leLinha() throws IOException{
        return fR.readLine();
    }

    public void escreveLinha(String linha) throws IOException {
        fW.write(linha,0,linha.length());
    }

    public void escreveNovaLinha(String linha) throws IOException {
        fW.write(linha,0,linha.length());
        fW.newLine();
    }

    public void fechaLeitura() throws IOException {
        fR.close();
    }
    public void fechaEscrita() throws IOException {
        fW.close();
    }
}
