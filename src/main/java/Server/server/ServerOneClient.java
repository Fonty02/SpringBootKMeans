package Server.server;

import Server.data.Data;
import Server.data.OutOfRangeSampleSize;
import Server.database.DatabaseConnectionException;
import Server.database.EmptySetException;
import Server.database.NoValueException;
import Server.mining.KMeansMiner;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;

@RestController
class ServerOneClient {



    private void closeConnection() {
        try {
            String user = socket.getInetAddress().toString();
            socket.close();
            System.out.println("Comunicazione chiusa con " + user);
        } catch (IOException e) {
            System.err.println("Errore nella chiusura della comunicazione");
            e.printStackTrace();
            System.err.println();
        }
    }

    public void run() {
        String tablename = null, server = null, db = null, user = null, pass = null;
        Data data = null;
        int k = 0, numIter = -1, portaDatabase = 0, choice;
        String filename;
        while (true) {
            try {
                choice = (Integer) in.readObject();
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                System.err.println("Errore nella comunicazione");
                e.printStackTrace();
                System.err.println();
                this.closeConnection();
                return;
            }
            switch (choice) {
                case 0 -> { // Carica Dati da zero
                    String result = "OK";
                    try {
                        server = (String) in.readObject();
                        portaDatabase = (int) in.readObject();
                        db = (String) in.readObject();
                        tablename = (String) in.readObject();
                        user = (String) in.readObject();
                        pass = (String) in.readObject();
                    } catch (IOException | ClassNotFoundException | ClassCastException e) {
                        System.err.println("Errore nella comunicazione");
                        e.printStackTrace();
                        System.err.println();
                        this.closeConnection();
                    }
                    try {
                        data = new Data(server, portaDatabase, db, user, pass, tablename);
                    } catch (NoValueException | DatabaseConnectionException | EmptySetException | SQLException e) {
                        result = "SI E' VERIFICATO UN ERRORE DURANTE L'INTERROGAZIONE AL DATABASE -> " + e.getMessage();
                        e.printStackTrace();
                        System.err.println();
                    }
                    try {
                        out.writeObject(result);
                    } catch (IOException e) {
                        System.err.println("Errore nella comunicazione");
                        e.printStackTrace();
                        System.err.println();
                        return;
                    }

                }
                case 1 -> {
                    String result = "OK";
                    try {
                        k = (int) in.readObject();
                    } catch (IOException | ClassNotFoundException | ClassCastException e) {
                        System.err.println("Errore nella comunicazione");
                        e.printStackTrace();
                        System.err.println();
                        this.closeConnection();
                        return;
                    }
                    try {
                        kmeans = new KMeansMiner(k);
                        numIter = kmeans.kmeans(data);
                    } catch (OutOfRangeSampleSize e) {
                        result = "ERRORE NEL NUMERO DEI CLUSTER -> " + e.getMessage();
                        e.printStackTrace();
                        System.err.println();
                    }
                    try {
                        out.writeObject(result);
                        if (result.equals("OK")) {
                            out.writeObject("Numero di iterazioni: " + numIter);
                            out.writeObject(kmeans.getC().toString(data));
                        }
                    } catch (IOException e) {
                        System.err.println("Errore nella comunicazione");
                        e.printStackTrace();
                        System.err.println();
                        return;
                    }
                }
                case 2 -> {
                    String result = "OK";
                    filename = "Salvataggi\\" + db + tablename + k + ".dat";
                    try {
                        kmeans.salva(filename);
                    } catch (IOException e) {
                        result = "Impossibile effettuare salvataggio su file";
                        e.printStackTrace();
                        System.err.println();
                    }
                    try {
                        out.writeObject(result);
                    } catch (IOException e) {
                        System.err.println("Errore nella comunicazione");
                        e.printStackTrace();
                        System.err.println();
                        return;
                    }
                }
                case 3 -> {
                    String result = "OK";
                    try {
                        db = (String) in.readObject();
                        tablename = (String) in.readObject();
                        numIter = (Integer) in.readObject();
                    } catch (IOException | ClassNotFoundException | NullPointerException | ClassCastException e) {
                        System.err.println("Errore nella comunicazione");
                        e.printStackTrace();
                        System.err.println();
                        this.closeConnection();
                        return;
                    }
                    filename = "Salvataggi\\" + db + tablename + numIter + ".dat";
                    try {
                        kmeans = new KMeansMiner(filename);
                    } catch (IOException | ClassNotFoundException e) {
                        result = "Impossibile caricare il salvataggio";
                        e.printStackTrace();
                        System.err.println();
                    }
                    try {
                        out.writeObject(result);
                        if (result.equals("OK")) {
                            out.writeObject(kmeans.getC().toString());
                        }
                    } catch (IOException e) {
                        System.err.println("Errore nella comunicazione");
                        e.printStackTrace();
                        System.err.println();
                    }
                }
            }
        }
    }

}