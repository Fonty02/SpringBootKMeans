package com.example.kmeansspringboot.controller;

import ch.qos.logback.core.util.SystemInfo;
import com.example.kmeansspringboot.serverComponent.data.Data;
import com.example.kmeansspringboot.serverComponent.data.OutOfRangeSampleSize;
import com.example.kmeansspringboot.serverComponent.mining.KMeansMiner;
import jakarta.servlet.http.HttpSession;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

@Controller
public class MainController {

    public MainController() {
    }

    @GetMapping("/index")
    public String goToIndex() {
        return "index";
    }

    @GetMapping("/kmeans")
    public String goToKmeans() {
        return "kmeans";
    }

    @GetMapping("/newkmeans")
    public String newKmeans(Model model, HttpSession session) {
        try {
            Data data = new Data("localhost", 3306, "MapDB", "MapUser", "map", "playtennis");
            model.addAttribute("nCluster", data.getNumberOfExamples());
            session.setAttribute("data", data);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            model.addAttribute("error", "Si è verificato un errore, impossibile eseguire l'operazione");
            return "index";
        }
        return "newkmeans";
    }

    @PostMapping("/result")
    public String goToResult(@ModelAttribute("i") String numCluster, Model model, HttpSession session) {
        try {
            Data data = (Data) session.getAttribute("data");
            session.removeAttribute("data");
            KMeansMiner kMeansMiner = new KMeansMiner(Integer.parseInt(numCluster));
            int numIter=kMeansMiner.kmeans(data);
            kMeansMiner.salva(".//Salvataggi//MapDBplaytennis"+numCluster+".dat");
            model.addAttribute("kmeans", kMeansMiner.getC().getResult(data));
            model.addAttribute("numIter", numIter);

        } catch (Exception e) {
            System.err.println(e.getMessage());
            model.addAttribute("error", "Si è verificato un errore, impossibile eseguire l'operazione");
            return "index";
        }
        return "result";
    }

    @PostMapping("/fileresult")
    public String goToResult(@ModelAttribute("nome") String nomeFile, Model model)
    {
        try {
            KMeansMiner kMeansMiner = new KMeansMiner(".//Salvataggi//"+nomeFile);
            model.addAttribute("clusterSet", kMeansMiner.getC().getResult());
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println(e.getClass().getName());
            model.addAttribute("error", "Si è verificato un errore, impossibile eseguire l'operazione");
            return "index";
        }
        return "fileresult";
    }

    @GetMapping("/filekmeans")
    public String selectFileKmeans(Model model) {
        List<String> nomiFile = new LinkedList<>();

        File cartella = new File("Salvataggi");
        File[] files = cartella.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    nomiFile.add(file.getName());
                }
            }
        }
        if (nomiFile.size()>0) {
            model.addAttribute("nomiFile", nomiFile);
            return "filekmeans";
        }
        else model.addAttribute("error", "Non ci sono file salvati");
        return "index";
    }

    @GetMapping("/error")
    public String goToError(Model model) {
        model.addAttribute("error", "Si è verificato un errore, impossibile eseguire l'operazione");
        return "index";
    }
}

/*

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
*/
