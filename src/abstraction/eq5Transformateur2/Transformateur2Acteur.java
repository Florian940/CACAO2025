// Nils Rossignol

package abstraction.eq5Transformateur2;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.IProduit;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Transformateur2Acteur implements IActeur {
    protected int cryptogramme;
    protected Journal journal;
    protected Journal journalStock;
    protected Journal journalContrat;
    //protected Stock stock;

    public Transformateur2Acteur() {
        this.journal = new Journal("Journal Equipe 5", this);
        this.journalStock = new Journal("Journal Stock", this);
        this.journalContrat = new Journal("Journal Contrat", this);
    //    this.stock = new Stock();
    }
    
    public void initialiser() {}

    public String getNom() {
        return "EQ5";
    }
    
    public String toString() {
        return this.getNom();
    }

    /*
     * Cette methode est appelee a chaque etape de la simulation. Elle permet de mettre à jour le numéro de l'étape.
     */
    public void next() {
        int etape = Filiere.LA_FILIERE.getEtape();
        this.journal.ajouter("Etape numéro : " + etape);
    }

    public Color getColor() {
        return new Color(165, 235, 195); 
    }

    public String getDescription() {
        return "Un transformateur ethique qui transforme de simples fèves de cacao en produits de qualité supérieure.";
    }

    public List<Variable> getIndicateurs() {
        // On met à jour les indicateurs à chaque étape
        List<Variable> res = new ArrayList<>();
        return res;
    }

    public List<Variable> getParametres() {
        return new ArrayList<>();
    }

    public List<Journal> getJournaux() {
        // On met à jour le journal à chaque étape
        List<Journal> res = new ArrayList<>();
        res.add(journal);
        res.add(journalStock);
        res.add(journalContrat);
        return res;
    }

    public void setCryptogramme(Integer crypto) {
        this.cryptogramme = crypto;
    }

    public void notificationFaillite(IActeur acteur) {}

    public void notificationOperationBancaire(double montant) {}
    
    protected double getSolde() {
        return Filiere.LA_FILIERE.getBanque().getSolde(Filiere.LA_FILIERE.getActeur(getNom()), this.cryptogramme);
    }

    public List<String> getNomsFilieresProposees() {
        return new ArrayList<>();
    }

    public Filiere getFiliere(String nom) {
        return Filiere.LA_FILIERE;
    }

    public double getQuantiteEnStock(IProduit p, int cryptogramme) {
        if (this.cryptogramme == cryptogramme) {
            return 0;
        } else {
            return 0;
        }
    }
}
