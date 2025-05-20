package abstraction.eq7Distributeur1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.awt.Color;
import java.text.DecimalFormat;

import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IDistributeurChocolatDeMarque;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.appelDOffre.IAcheteurAO;
import abstraction.eqXRomu.clients.ClientFinal;

import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.contratsCadres.SuperviseurVentesContratCadre;
import abstraction.eqXRomu.general.Variable;
import com.sun.net.httpserver.Authenticator;

public class Distributeur1 extends Distributeur1AcheteurAppelOffre implements IDistributeurChocolatDeMarque {
	
	// défi 1 et 2 par Alexiho
	protected Journal journal; // Déclaration du journal
	protected Journal journalV;
	protected List<Double> prix;
	protected List<Double> capaciteDeVente;
	protected IAcheteurAO identity;
	protected int step = 0;
	protected String name = "HexaFridge";
	protected Color color = new Color(162, 207, 238);
	


    public Distributeur1() {
		super();
        
        this.journal = new Journal("Journal stock de EQ7", this); // Initialisation du journal
		this.journalV = new Journal("Journal ventes de EQ7", this);
		
		predictionsVentesPourcentage = Arrays.asList(3.6 , 3.6 , 5.0 , 3.6 , 3.6 , 3.6 , 3.6 , 7.0 , 3.6 , 3.6 , 3.6 , 3.6 , 3.6 , 3.6 , 3.6 , 3.6 , 3.6 , 3.6 , 3.6 , 3.6 , 3.6 , 3.6 , 3.6 , 13.0);

		this.prix = new ArrayList<>();
		this.capaciteDeVente = new ArrayList<>();
    }

	@Override
	public void initialiser() // par Alexiho
	{
		this.chocolats = Filiere.LA_FILIERE.getChocolatsProduits();

    // Initialize stocksChocolats map and other lists
    for (int i = 0; i < this.chocolats.size(); i++) {
        ChocolatDeMarque chocolat = chocolats.get(i);

        // Initialize stocksChocolats with a default stock value
        this.stocksChocolats.put(chocolat, new Variable(this.getNom() + "Stock" + chocolat.getNom(), this, 10000.0));

        // Initialize other lists
        this.prix.add(10.0); // Default price
        this.capaciteDeVente.add(900.0); // Default sales capacity
        this.successedSell.add(0); // Default successful sales count
        this.priceProduct.add(1000.0); // Default product price
        this.requiredQuantities.add(1000.0); // Default required quantity
    }

    // Update sales capacity based on stock values
    for (int i = 0; i < this.chocolats.size(); i++) {
        ChocolatDeMarque chocolat = chocolats.get(i);
        this.capaciteDeVente.set(i, this.stocksChocolats.get(chocolat).getValeur());
    }
	}

	@Override
	public double prix(ChocolatDeMarque choco) { // par Alexiho
		//ChocolatDeMarque chocoM = new ChocolatDeMarque(choco.getChocolat(), "Villors", 90);
		int pos= (chocolats.indexOf(choco));
		if (pos<0) {
			return 99999999.0;
		} else {
			//return prix.get(pos);
			double price = 3.5*this.priceProduct.get(pos) - 0.5*(this.stocksChocolats.get(choco).getValeur()/1000)  - 0.5*(this.capaciteDeVente.get(pos)/10000);
			return price;
		}
	}

	@Override
	public void next() // par Alexiho
	{
		//List<Double> requiredQuantities = new ArrayList<>();
		//Distributeur1Stock acteurStock = new Distributeur1Stock();
		int step = Filiere.LA_FILIERE.getEtape(); // Récupération du numéro de l'étape
		journal.ajouter(" ==============  Etape : " + step +  " ====================");
		journalV.ajouter(" ==============  Etape : " + step +  " ====================");
		journalCC.ajouter(" ==============  Etape : " + step +  " ====================");
		journalAO.ajouter(" ==============  Etape : " + step +  " ====================");
		for (int i=0; i< this.chocolats.size(); i++){
			if ("Fraudolat".equals(this.stocksChocolats.get(chocolats.get(i)).getNom())){
				requiredQuantities.set(i,500.0);
			} else{
				if (this.stocksChocolats.get(chocolats.get(i)).getValeur() < 5000) {
					requiredQuantities.set(i, this.VolumetoBuy(chocolats.get(i), this.cryptogramme)*1.1);
				} else {
					requiredQuantities.set(i, 0.0);
				}
			}
	}
		
		if (step%8==0){
			this.next_cc();
			for (int i = 0 ; i<chocolats.size() ; i++){
				requiredQuantities.set(i, Math.max(requiredQuantities.get(i)/5,5));
			}}
		
		//IAcheteurAO acheteurAppelOffre = new Distributeur1AcheteurAppelOffre();
		this.next_ao();
		
		//Ethan - Indicateurs de stocks
		this.stock_C_BQ_E.setValeur(this, 0, cryptogramme);
		this.stock_C_BQ.setValeur(this, 0, cryptogramme);
		this.stock_C_MQ_E.setValeur(this, 0, cryptogramme);
		this.stock_C_MQ.setValeur(this, 0, cryptogramme);
		this.stock_C_HQ_E.setValeur(this, 0, cryptogramme);
		this.stock_C_HQ_BE.setValeur(this, 0, cryptogramme);
		for (int i = 0; i < this.chocolats.size(); i++) {
			if (stocksChocolats.get(chocolats.get(i)).getNom().contains("BQ_E")) {
				this.stock_C_BQ_E.ajouter(this, stocksChocolats.get(chocolats.get(i)).getValeur(), cryptogramme);
			} else if (stocksChocolats.get(chocolats.get(i)).getNom().contains("BQ") && (!stocksChocolats.get(chocolats.get(i)).getNom().contains("BQ_E"))) {
				this.stock_C_BQ.ajouter(this, stocksChocolats.get(chocolats.get(i)).getValeur(), cryptogramme);
			} else if (stocksChocolats.get(chocolats.get(i)).getNom().contains("MQ_E")) {
				this.stock_C_MQ_E.ajouter(this, stocksChocolats.get(chocolats.get(i)).getValeur(), cryptogramme);
			} else if (stocksChocolats.get(chocolats.get(i)).getNom().contains("MQ") && (!stocksChocolats.get(chocolats.get(i)).getNom().contains("MQ_E"))) {
				this.stock_C_MQ.ajouter(this, stocksChocolats.get(chocolats.get(i)).getValeur(), cryptogramme);
			} else if (stocksChocolats.get(chocolats.get(i)).getNom().contains("HQ_E")) {
				this.stock_C_HQ_E.ajouter(this, stocksChocolats.get(chocolats.get(i)).getValeur(), cryptogramme);
			} else if (stocksChocolats.get(chocolats.get(i)).getNom().contains("HQ_BE")) {
				this.stock_C_HQ_BE.ajouter(this, stocksChocolats.get(chocolats.get(i)).getValeur(), cryptogramme);
			}
		}
		//par Ethan
		String str_journal_stock = "";

		for (int i = 0 ; i<chocolats.size() ; i++){
			str_journal_stock = this.stocksChocolats.get(chocolats.get(i)).getNom() + " = " + this.stocksChocolats.get(chocolats.get(i)).getValeur() + ";";
			str_journal_stock = str_journal_stock.replace("EQ7Stock", "Stock ");
			journal.ajouter(str_journal_stock);
		}

		// définition des capacités de ventes

		for (int i=0; i<this.chocolats.size(); i++) {
			this.capaciteDeVente.set(i, stocksChocolats.get(chocolats.get(i)).getValeur()/1.1);
		}

	}

	public List<String> getMarquesChocolat() { // par Alexiho
                @SuppressWarnings("Convert2Diamond")
		List<String> marques = new ArrayList<String>();
		marques.add("Hexafridge");
		return marques;
	}

	@Override
	public double quantiteEnVente(ChocolatDeMarque choco, int crypto) { // par Alexiho
		if (crypto!=this.cryptogramme) {
			journal.ajouter("Quelqu'un essaye de me pirater !");
			return 0.0;
		} else {
			//ChocolatDeMarque chocoM = new ChocolatDeMarque(choco.getChocolat(), "Villors", 90);
			int pos= (chocolats.indexOf(choco));
			//journal.ajouter("pos : " + pos + "chocoval : " + this.getStock(chocoM).getValeur() + "capa : " + capaciteDeVente.get(pos));
			if (pos<0) {
				return 0.0;
			} else {
				return Math.min(capaciteDeVente.get(pos), this.getStock(choco).getValeur());
			}
		}
	}

	@Override
	// On met 10% de ce tout ce qu'on met en vente (on pourrait mettre l'accent sur
	// un produit a promouvoir mais il s'agit ici d'un exemple simpliste
	public double quantiteEnVenteTG(ChocolatDeMarque choco, int crypto) { // par Alexiho
		if (crypto!=this.cryptogramme) {
			journal.ajouter("Quelqu'un essaye de me pirater !");
			return 0.0;
		} else {
			int pos= (chocolats.indexOf(choco));
			if (pos<0) {
				return 0.0;
			} else {
				return Math.min(0,Math.min(capaciteDeVente.get(pos), this.getStock(choco).getValeur()))/10.0;
			}
		}
	}

	@Override
	public void vendre(ClientFinal client, ChocolatDeMarque choco, double quantite, double montant, int crypto) { // par Alexiho
		int pos= (chocolats.indexOf(choco));
		if (pos>=0) {
			this.getStock(choco).retirer(this, quantite);
			journalV.ajouter("Vente de " + quantite + " tonnes de " + choco.getNom());
		}
	}


	@Override
	public Variable getStock(ChocolatDeMarque c) { // par Alexiho
		return this.stocksChocolats.get(c);
	}

	@Override
	public Map<ChocolatDeMarque, Variable> getStocksChocolats() { // par Alexiho
		return this.stocksChocolats;
	}
	@Override
	public double getQuantiteEnStock(IProduit p, int cryptogramme) { // par Alexiho
		if (this.cryptogramme==cryptogramme) {
			for (ChocolatDeMarque c : this.stocksChocolats.keySet()) {
				if (c.equals(p)) {
					return this.stocksChocolats.get(c).getValeur();
				}
			}
			return 0;
		} else {
			return 0;
		}
	}
	@Override
	public List<Variable> getIndicateurs() {
		List<Variable> res = super.getIndicateurs();
		for (int i=0; i<this.chocolats.size(); i++) {
			res.add(this.stocksChocolats.get(chocolats.get(i)));
		}
		return res;
	}

	public void notificationRayonVide(ChocolatDeMarque choco) {
		journal.ajouter(" Aie... j'aurais du mettre davantage de "+choco.getNom()+" en vente");
	}
	@Override
	public void notificationRayonVide(ChocolatDeMarque choco, int crypto)
	{
		journal.ajouter(" Aie... j'aurais du mettre davantage de "+choco.getNom()+" en vente");
	}
	@Override
	// Renvoie les journaux
	public List<Journal> getJournaux() { // par Alexiho
                @SuppressWarnings("Convert2Diamond")
		List<Journal> res=new ArrayList<Journal>();
		res.add(journal);
		res.add(journalV);
		res.add(journalCC);
		res.add(journalAO);
		return res;
	}
	
}

