package iBei.aux;


import java.io.*;

public class FicheiroDeObjeto {
    private ObjectInputStream iS;
    private ObjectOutputStream oS;

    public boolean abreLeitura(String nomeDoFicheiro) throws IOException {
        try {
            iS = new ObjectInputStream(new FileInputStream(nomeDoFicheiro));
            return true;
        } catch (IOException e) {
            File f = new File(nomeDoFicheiro);
            f.createNewFile();
            iS = new ObjectInputStream(new FileInputStream(nomeDoFicheiro));
            return false;
        }
    }

    public void abreEscrita(String nomeDoFicheiro) throws IOException {
        oS = new ObjectOutputStream(new FileOutputStream(nomeDoFicheiro));
    }

    public Object leObjeto() throws IOException, ClassNotFoundException {
        return iS.readObject();
    }

    public void escreveObjeto(Object o) throws IOException {
        oS.writeObject(o);
    }

    public void fechaLeitura() throws IOException {
        iS.close();
    }

    public void fechaEscrita() throws IOException {
        oS.close();
    }
}
