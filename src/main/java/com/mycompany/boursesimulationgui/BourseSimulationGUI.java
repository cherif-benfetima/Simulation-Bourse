package com.mycompany.boursesimulationgui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

// Classe principale pour la GUI
public class BourseSimulationGUI extends JFrame {

    private JLabel soldeLabel;
    private JButton simulerButton;
    private JButton investirButton;
    private JTable produitsTable;
    private Portefeuille portefeuille;
    private ArrayList<Produit> produits;

    /** hola zoli 
     * @Bourse hoho 
     */
    public BourseSimulationGUI() {
        // Initialisation des produits et du portefeuille
        portefeuille = new Portefeuille(1000.0); // Solde initial
        produits = new ArrayList<>();
        produits.add(new Produit("Action A", 100));
        produits.add(new Produit("Action B", 50));
        produits.add(new Produit("Produit C", 200));

        // Configuration de la fenêtre principale
        setTitle("Simulation Boursière");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Section du solde utilisateur
        soldeLabel = new JLabel("Solde: " + portefeuille.getSolde() + " €");
        add(soldeLabel, BorderLayout.NORTH);

        // Tableau des produits
        String[] columnNames = {"Produit", "Valeur actuelle (€)"};
        Object[][] data = new Object[produits.size()][2];
        for (int i = 0; i < produits.size(); i++) {
            data[i][0] = produits.get(i).getNom();
            data[i][1] = produits.get(i).getValeurActuelle();
        }
        produitsTable = new JTable(data, columnNames);
        add(new JScrollPane(produitsTable), BorderLayout.CENTER);

        // Boutons
        JPanel buttonPanel = new JPanel();
        investirButton = new JButton("Investir");
        simulerButton = new JButton("Simuler");
        buttonPanel.add(investirButton);
        buttonPanel.add(simulerButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Actions des boutons
        investirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                investir();
            }
        });

        simulerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                simulerTour();
            }
        });
    }

    private void investir() {
        String produitNom = JOptionPane.showInputDialog("Entrez le nom du produit :");
        String montantStr = JOptionPane.showInputDialog("Montant à investir :");
        try {
            double montant = Double.parseDouble(montantStr);
            Produit produit = trouverProduit(produitNom);
            if (produit != null && portefeuille.investir(produit, montant)) {
                JOptionPane.showMessageDialog(this, "Investissement réussi !");
                miseAJourTableau();
            } else {
                JOptionPane.showMessageDialog(this, "Investissement échoué. Vérifiez vos entrées.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Montant invalide.");
        }
    }

    private void simulerTour() {
        for (Produit produit : produits) {
            produit.simulerVariation();
        }
        miseAJourTableau();
        soldeLabel.setText("Solde: " + portefeuille.getSolde() + " €");
    }

    private Produit trouverProduit(String nom) {
        for (Produit produit : produits) {
            if (produit.getNom().equalsIgnoreCase(nom)) {
                return produit;
            }
        }
        return null;
    }

    private void miseAJourTableau() {
        for (int i = 0; i < produits.size(); i++) {
            produitsTable.setValueAt(produits.get(i).getValeurActuelle(), i, 1);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BourseSimulationGUI().setVisible(true);
        });
    }
}

// Classe Produit
class Produit {
    private String nom;
    private double valeurActuelle;

    public Produit(String nom, double valeurInitiale) {
        this.nom = nom;
        this.valeurActuelle = valeurInitiale;
    }

    public String getNom() {
        return nom;
    }

    public double getValeurActuelle() {
        return valeurActuelle;
    }

    public void simulerVariation() {
        Random random = new Random();
        double variation = (random.nextDouble() * 20) - 10; // Variation entre -10 et +10
        valeurActuelle += variation;
        if (valeurActuelle < 0) valeurActuelle = 0;
    }
}

// Classe Portefeuille
class Portefeuille {
    private double solde;
    private ArrayList<Investissement> investissements;

    public Portefeuille(double soldeInitial) {
        this.solde = soldeInitial;
        this.investissements = new ArrayList<>();
    }

    public double getSolde() {
        return solde;
    }

    public boolean investir(Produit produit, double montant) {
        if (montant <= solde) {
            investissements.add(new Investissement(produit, montant));
            solde -= montant;
            return true;
        }
        return false;
    }
}

// Classe Investissement
class Investissement {
    private Produit produit;
    private double montant;

    public Investissement(Produit produit, double montant) {
        this.produit = produit;
        this.montant = montant;
    }

    public Produit getProduit() {
        return produit;
    }

    public double getMontant() {
        return montant;
    }
}
