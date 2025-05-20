//Nathan
package abstraction.eq5Transformateur2;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;
import java.util.HashMap;
import java.util.List;


public class Stock extends Transformateur2Acteur{
    private HashMap<Feve, Variable> stockFeve;
    private HashMap<Chocolat, Variable> stockChoco;
    private HashMap<ChocolatDeMarque, Variable> stockChocoMarque;
    private Variable stockFeveTotal;
    private Variable stockChocoTotal;
    private Variable stockChocoMarqueTotal;
    private double coutStockageFeve;
    private double coutStockageChoco;


    public Stock() {
        this.coutStockageFeve=30; //par Step et par tonne
        this.coutStockageChoco= coutStockageFeve*2; //par Step et par tonne

        this.stockFeve = new HashMap<Feve,Variable>();          // en tonne
        this.stockChoco = new HashMap<Chocolat, Variable>();    // en unité de tablette
        super.journal.ajouter("stock initialisé");
        
        for (Feve f : Feve.values()) {
            this.stockFeve.put(f, new Variable("Stock Feve " + f, this,500));
        }
        for (Chocolat c : Chocolat.values()) {
            this.stockChoco.put(c, new Variable("Stock Chocolat " + c, this, 0.0));
        }
        
        this.stockFeveTotal = new Variable("EQ5TStockFeve", this, 3000.0);
        this.stockChocoTotal = new Variable("EQ5TStockChoco", this, 0.0);
        this.stockChocoMarqueTotal = new Variable("EQ5TStockChocoMarque", this, 0.0);
    }
    public Variable getstockFeveTotal() {
        return this.stockFeveTotal;
    }

    public Variable getstockChocoTotal() {
        return this.stockChocoTotal;
    }

    public Variable getstock(IProduit p){
        if (p instanceof Feve) {
            Feve f = (Feve) p;
            return this.stockFeve.get(f);
        } else if (p instanceof Chocolat) {
            Chocolat c = (Chocolat) p;
            return this.stockChoco.get(c);
        }
        return null;
    }

    

    public void ajouterStock(IActeur auteur,IProduit p, double quantite, Integer cryptogramme) {
        if (p instanceof Feve) {
            Feve f = (Feve) p;
            this.stockFeve.get(f).ajouter(auteur, quantite, cryptogramme);
        } else if (p instanceof Chocolat) {
            Chocolat c = (Chocolat) p;
            this.stockChoco.get(c).ajouter(auteur, quantite,cryptogramme);
        }
        misAJourstockTotal();
    }



    public void retirerStock(IActeur auteur,IProduit p, double quantite, Integer cryptogramme) {
        if (p instanceof Feve) {
            Feve f = (Feve) p;
            this.stockFeve.get(f).retirer(auteur, quantite, cryptogramme);
        } else if (p instanceof Chocolat) {
            Chocolat c = (Chocolat) p;
            this.stockChoco.get(c).retirer(auteur, quantite, cryptogramme);
        }
        misAJourstockTotal();
    }

    public double getQuantiteStock(IProduit p){
        if (p instanceof Feve) {
            Feve f = (Feve) p;
            return this.stockFeve.get(f).getValeur();
        } else if (p instanceof Chocolat) {
            Chocolat c = (Chocolat) p;
            return this.stockChoco.get(c).getValeur();
        }
        return 0.0;
    }
    
    
    public void misAJourstockTotal(){
        this.stockFeveTotal.setValeur(this,0.0);
        this.stockChocoTotal.setValeur(this,0.0);
        for (Feve f : Feve.values()) {
            this.stockFeveTotal.ajouter(this, this.stockFeve.get(f).getValeur());
        }
        for (Chocolat c : Chocolat.values()) {
            this.stockChocoTotal.ajouter(this, this.stockChoco.get(c).getValeur()/100); // on passe de tablette à tonne
        }
       
    }

    public List<Variable> getIndicateurs() { 
		List<Variable> res = super.getIndicateurs();
		res.add(this.stockChocoTotal);
		res.add(this.stockFeveTotal);
        res.add(this.stockChocoMarqueTotal);    		
        return res;
    }

    public void depenseStockage(){
         Filiere.LA_FILIERE.getBanque().payerCout(this ,super.cryptogramme,  "Coût de stockage",
         this.stockFeveTotal.getValeur()*this.coutStockageFeve+this.stockChocoTotal.getValeur()*this.coutStockageChoco);

    }
    public void next(){
        depenseStockage();
        super.next();
    }


}          