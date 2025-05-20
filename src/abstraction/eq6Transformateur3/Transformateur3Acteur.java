package abstraction.eq6Transformateur3;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import abstraction.eqXRomu.filiere.Banque;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;


public class Transformateur3Acteur implements IActeur {
	
	protected int cryptogramme;
	protected int currentStep;
	protected double coutStockage;

	//Récupération des entitées utiles
	protected Banque LaBanque;

	protected Journal jdb;
	protected Journal journalProduction;
	protected Journal journalStock;
	protected Journal journalCC;
	protected Journal journalBourse;
	protected Journal journalAO;
	protected Journal journalStrat;

	protected List<IProduit> lesFeves;
	protected List<IProduit> fevesUtiles; // Liste des fèves qu'on utilise pour la production de chocolat
	protected List<IProduit> lesChocolats;
	protected HashMap<IProduit, Variable> dicoIndicateurFeves;
	protected Transformateur3Stock stockFeves;
	protected Transformateur3Stock stockChoco;

	//Prix
	protected HashMap<IProduit, List<Double>> prixFeve;
	protected HashMap<IProduit, List<Double>> prixChoco;

	protected Transformateur3StratPrix StratPrix;

	//Stratégie

	// Quantitée de chaque type de fèves reçue au prochain step
    // pour chaque fève, in dispose d'un échéancier sur la quantité total de fèves
	//protected HashMap<IProduit, List<Double>> quantityFevesEcheancier;
    // Quantitée de chaque type de choco vendu au prochain step
    //protected HashMap<IProduit, List<Double>> quantityChocoEcheancier;

	protected Variable eq6_Q_MQ_0;
	protected Variable eq6_Q_MQ_1;
	protected Variable eq6_Q_BQ_0;
	protected Variable eq6_Q_BQ_1;
	protected Variable eq6_Q_HQ_1;
	protected Variable eq6_Q_HQ_2;
	protected Variable eq6_Q_Fraudo;
	protected Variable eq6_Q_Bollo;
	protected Variable eq6_Q_Arna;
	protected Variable eq6_Q_Hypo;
	
	public Transformateur3Acteur() {
		// Initialisation des journaux
		this.jdb = new Journal("Journal de bord", this);
		this.journalProduction = new Journal("Journal de production", this);
		this.journalStock = new Journal("Journal des stocks", this);
		this.journalCC = new Journal("Journal des contrats cadre", this);
		this.journalBourse = new Journal("Journal de la Bourse", this);
		this.journalAO = new Journal("Journal des appels d'offre", this);
		this.journalStrat = new Journal("Journal - Stratégie", this);

		// Initialisation des indicateurs
		this.eq6_Q_BQ_0 = new Variable(this.getNom()+": quantité de cacao de BQ non labellisé", this, 0);
		this.eq6_Q_BQ_1 = new Variable(this.getNom()+": quantité de cacao de BQ équitable", this, 0);
		this.eq6_Q_MQ_0 = new Variable(this.getNom()+": quantité de cacao de MQ non labellisé", this, 0);
		this.eq6_Q_MQ_1 = new Variable(this.getNom()+": quantité de cacao de MQ équitable", this, 0);
		this.eq6_Q_HQ_1 = new Variable(this.getNom()+": quantité de cacao de HQ équitable", this, 0);
		this.eq6_Q_HQ_2 = new Variable(this.getNom()+": quantité de cacao de HQ bio & équitable", this, 0);
		this.eq6_Q_Fraudo = new Variable(this.getNom()+": quantité de tablette Fraudolat", this, 0);
		this.eq6_Q_Bollo = new Variable(this.getNom()+": quantité de tablette Bollorolat", this, 0);
		this.eq6_Q_Arna = new Variable(this.getNom()+": quantité de tablette Arnaquolat", this, 0);
		this.eq6_Q_Hypo = new Variable(this.getNom()+": quantité de tablette Hypocritolat", this, 0);

		//Dico d'indicateur fèves
		this.dicoIndicateurFeves = new HashMap<IProduit, Variable>();
		this.dicoIndicateurFeves.put(abstraction.eqXRomu.produits.Feve.F_BQ,eq6_Q_BQ_0);
		this.dicoIndicateurFeves.put(abstraction.eqXRomu.produits.Feve.F_BQ_E,eq6_Q_BQ_1);
		this.dicoIndicateurFeves.put(abstraction.eqXRomu.produits.Feve.F_MQ,eq6_Q_MQ_0);
		this.dicoIndicateurFeves.put(abstraction.eqXRomu.produits.Feve.F_MQ_E,eq6_Q_MQ_1);
		this.dicoIndicateurFeves.put(abstraction.eqXRomu.produits.Feve.F_HQ_E,eq6_Q_HQ_1);
		this.dicoIndicateurFeves.put(abstraction.eqXRomu.produits.Feve.F_HQ_BE,eq6_Q_HQ_2);

		//Bot de prix
		this.StratPrix = new Transformateur3StratPrix();
		this.prixChoco = new HashMap<>();
		this.prixFeve = new HashMap<>();
		for(IProduit feve : dicoIndicateurFeves.keySet()){
			List<Double> Prix = new LinkedList<>();
			this.prixFeve.put(feve, Prix);
		}

	}
	
	public void initialiser() {
		// Récupération des instances utiles
		this.LaBanque = Filiere.LA_FILIERE.getBanque();
		// Lister les fèves qui existent
		this.lesFeves = new ArrayList<IProduit>();
		for (Feve f : Feve.values()) {
			this.lesFeves.add(f);
		}

		this.fevesUtiles = new ArrayList<IProduit>();
        this.fevesUtiles.add(Feve.F_BQ);
        this.fevesUtiles.add(Feve.F_BQ_E);
        this.fevesUtiles.add(Feve.F_MQ);   
        this.fevesUtiles.add(Feve.F_HQ_E);

		//Création du stock de fèves
		stockFeves = new Transformateur3Stock(this, journalStock, "fèves", 300.0, lesFeves, dicoIndicateurFeves);
	}

	public String getNom() {// NE PAS MODIFIER
		return "EQ6";
	}
	
	public String toString() {// NE PAS MODIFIER
		return this.getNom();
	}

	////////////////////////////////////////////////////////
	//         En lien avec l'interface graphique         //
	////////////////////////////////////////////////////////

	public void next() {
		this.jdb.ajouter("NEXT - TRANSFORMATEUR3ACTEUR");
		currentStep = Filiere.LA_FILIERE.getEtape();
		jdb.ajouter("Acteur Etape " + currentStep);
	}

	public Color getColor() {// NE PAS MODIFIER
		return new Color(158, 242, 226); 
	}

	public String getDescription() {
		return "Bla bla bla";
	}

	// Renvoie les indicateurs
	public List<Variable> getIndicateurs() {
		List<Variable> res = new ArrayList<Variable>();
		return res;
	}

	// Renvoie les parametres
	public List<Variable> getParametres() {
		List<Variable> res=new ArrayList<Variable>();
		return res;
	}

	// Renvoie les journaux
	public List<Journal> getJournaux() {
		List<Journal> res=new ArrayList<Journal>();
		return res;
	}

	////////////////////////////////////////////////////////
	//               En lien avec la Banque               //
	////////////////////////////////////////////////////////

	// Appelee en debut de simulation pour vous communiquer 
	// votre cryptogramme personnel, indispensable pour les
	// transactions.
	public void setCryptogramme(Integer crypto) {
		this.cryptogramme = crypto;
	}

	// Appelee lorsqu'un acteur fait faillite (potentiellement vous)
	// afin de vous en informer.
	public void notificationFaillite(IActeur acteur) {
	}

	// Apres chaque operation sur votre compte bancaire, cette
	// operation est appelee pour vous en informer
	public void notificationOperationBancaire(double montant) {
	}
	
	// Renvoie le solde actuel de l'acteur
	protected double getSolde() {
		return Filiere.LA_FILIERE.getBanque().getSolde(Filiere.LA_FILIERE.getActeur(getNom()), this.cryptogramme);
	}

	////////////////////////////////////////////////////////
	//        Pour la creation de filieres de test        //
	////////////////////////////////////////////////////////

	// Renvoie la liste des filieres proposees par l'acteur
	public List<String> getNomsFilieresProposees() {
		ArrayList<String> filieres = new ArrayList<String>();
		return(filieres);
	}

	// Renvoie une instance d'une filiere d'apres son nom
	public Filiere getFiliere(String nom) {
		return Filiere.LA_FILIERE;
	}

	public double getQuantiteEnStock(IProduit p, int cryptogramme) {
		if (this.cryptogramme==cryptogramme) { // c'est donc bien un acteur assermente qui demande a consulter la quantite en stock
			return 0; // A modifier
		} else {
			return 0; // Les acteurs non assermentes n'ont pas a connaitre notre stock
		}
	}
}
