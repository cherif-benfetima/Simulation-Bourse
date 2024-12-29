package com.mycompany.boursesimulationgui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.category.DefaultCategoryDataset;

// Classe principale pour la GUI
public class BourseSimulationGUI extends JFrame {

    private JLabel soldeLabel;
    private JButton simulerButton;
    private JButton investirButton;
    private JButton resetButton;
    private JTable produitsTable;
    private JTable historiqueTable;
    private Portefeuille portefeuille;
    private ArrayList<Produit> produits;
    private DefaultTableModel tableModel;
    private DefaultTableModel historiqueModel;
    private DefaultCategoryDataset dataset;

    public BourseSimulationGUI() {
        // Initialisation des produits et du portefeuille
        portefeuille = new Portefeuille(1000.0); // Solde initial
        produits = new ArrayList<>();
        produits.add(new Produit("Action A", 100));
        produits.add(new Produit("Action B", 50));
        produits.add(new Produit("Produit C", 200));

        // Configuration de la fenêtre principale
        setTitle("Simulation Boursière");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Section du solde utilisateur
        soldeLabel = new JLabel("Solde: " + portefeuille.getSolde() + " €");
        soldeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(soldeLabel, BorderLayout.NORTH);

        // Tableau des produits avec modèle de données
        String[] columnNames = {"Produit", "Valeur actuelle (€)"};
        tableModel = new DefaultTableModel(columnNames, 0);
        produitsTable = new JTable(tableModel);

        // Ajouter des données initiales au tableau
        for (Produit produit : produits) {
            tableModel.addRow(new Object[]{produit.getNom(), produit.getValeurActuelle()});
        }

        // Centrer les cellules et ajuster les colonnes
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        produitsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        produitsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        produitsTable.setRowHeight(25);
        produitsTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        produitsTable.getColumnModel().getColumn(1).setPreferredWidth(150);

        // Historique des gains/pertes par investissement
        String[] historiqueColumns = {"Produit", "Quantité d'Actions", "Prix d'Achat (€)", "Gains/Pertes (€)"};
        historiqueModel = new DefaultTableModel(historiqueColumns, 0);
        historiqueTable = new JTable(historiqueModel);

        // Dataset pour le graphique
        dataset = new DefaultCategoryDataset();
        for (Produit produit : produits) {
            dataset.addValue(produit.getValeurActuelle(), produit.getNom(), "Tour 0");
        }
        JFreeChart chart = ChartFactory.createLineChart(
                "Évolution des valeurs des produits",
                "Tours",
                "Valeur (€)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Positionner la légende en bas
        LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.BOTTOM);
        legend.setHorizontalAlignment(HorizontalAlignment.CENTER);

        ChartPanel chartPanel = new ChartPanel(chart);

        // Mise en page
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(produitsTable), new JScrollPane(historiqueTable));
        splitPane.setDividerLocation(300);

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitPane, chartPanel);
        mainSplitPane.setDividerLocation(500);

        add(mainSplitPane, BorderLayout.CENTER);

        // Boutons
        JPanel buttonPanel = new JPanel();
        investirButton = new JButton("Investir");
        simulerButton = new JButton("Simuler");
        resetButton = new JButton("Réinitialiser");
        buttonPanel.add(investirButton);
        buttonPanel.add(simulerButton);
        buttonPanel.add(resetButton);
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

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reinitialiserSimulation();
            }
        });
    }

    private void investir() {
        String produitNom = JOptionPane.showInputDialog("Entrez le nom du produit :");
        String montantStr = JOptionPane.showInputDialog("Montant à investir (€) :");
        try {
            double montant = Double.parseDouble(montantStr);
            Produit produit = trouverProduit(produitNom);
            if (produit != null) {
                double prixAchat = produit.getValeurActuelle();
                double quantite = montant / prixAchat;
                if (quantite > 0 && portefeuille.investir(produit, montant)) {
                    // Ajouter l'investissement à l'historique
                    historiqueModel.addRow(new Object[]{produitNom, quantite, prixAchat, 0.0});
                    JOptionPane.showMessageDialog(this, "Investissement réussi !");
                    miseAJourTableau();
                } else {
                    JOptionPane.showMessageDialog(this, "Fonds insuffisants ou montant trop faible pour un investissement.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Produit introuvable.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Entrée invalide. Veuillez entrer un montant valide.");
        }
    }

    private void simulerTour() {
        int tour = dataset.getColumnCount(); // Nombre actuel de tours
        for (Produit produit : produits) {
            double ancienneValeur = produit.getValeurActuelle();
            produit.simulerVariation();
            double nouvelleValeur = produit.getValeurActuelle();

            // Mettre à jour les gains/pertes dans l'historique
            for (int i = 0; i < historiqueModel.getRowCount(); i++) {
                if (historiqueModel.getValueAt(i, 0).equals(produit.getNom())) {
                    double quantite = (double) historiqueModel.getValueAt(i, 1);
                    double prixAchat = (double) historiqueModel.getValueAt(i, 2);
                    double gainsOuPertes = (nouvelleValeur - prixAchat) * quantite;
                    historiqueModel.setValueAt(gainsOuPertes, i, 3);
                }
            }

            // Ajouter au graphique
            dataset.addValue(nouvelleValeur, produit.getNom(), "Tour " + tour);

            // Appliquer une couleur pour indiquer les variations
            if (nouvelleValeur > ancienneValeur) {
                produitsTable.setValueAt("<html><span style='color:green;'>" + nouvelleValeur + "</span></html>", produits.indexOf(produit), 1);
            } else if (nouvelleValeur < ancienneValeur) {
                produitsTable.setValueAt("<html><span style='color:red;'>" + nouvelleValeur + "</span></html>", produits.indexOf(produit), 1);
            } else {
                produitsTable.setValueAt(nouvelleValeur, produits.indexOf(produit), 1);
            }
        }
        miseAJourTableau();
        soldeLabel.setText("Solde: " + portefeuille.getSolde() + " €");
    }

    private void reinitialiserSimulation() {
        // Réinitialiser les produits et le portefeuille
        portefeuille = new Portefeuille(1000.0);
        produits.clear();
        produits.add(new Produit("Action A", 100));
        produits.add(new Produit("Action B", 50));
        produits.add(new Produit("Produit C", 200));

        // Réinitialiser les tableaux
        tableModel.setRowCount(0);
        historiqueModel.setRowCount(0);

        for (Produit produit : produits) {
            tableModel.addRow(new Object[]{produit.getNom(), produit.getValeurActuelle()});
        }

        // Réinitialiser le dataset pour le graphique
        dataset.clear();
        for (Produit produit : produits) {
            dataset.addValue(produit.getValeurActuelle(), produit.getNom(), "Tour 0");
        }

        // Réinitialiser le solde affiché
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
