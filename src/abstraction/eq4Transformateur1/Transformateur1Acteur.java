package abstraction.eq4Transformateur1;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import abstraction.eqXRomu.acteurs.Romu;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.filiere.IMarqueChocolat;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.general.VariablePrivee;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit; 

public class Transformateur1Acteur implements IActeur, IMarqueChocolat {

	protected Journal journal;	
	protected Journal journalStock;
	protected Journal journalCC;
	protected Journal journalTransactions;
	protected Journal journalPeremptionLimdt;
	protected Journal journalPeremptionFeves;
	protected Journal journalCouts;


	protected List<Feve> lesFeves; 
	protected List<Chocolat> lesChocolats;
	protected List<ChocolatDeMarque> chocolatsLimDt; 

	protected int cryptogramme;

	protected Variable qttProduiteChoco; // quantité de chocolat produite durant cette step: devrait être égale à ProdMax

	//Stock de fèves
	protected Variable stock_F_MQ;
	protected Variable stock_F_BQ_E;
	protected Variable stock_F_MQ_E;
	protected Variable stock_F_HQ_BE;
	protected HashMap<Feve, Variable> stocksFevesVar;

	//Stock de chocolat NON MARQUE
	protected Variable stock_C_MQ;
	protected Variable stock_C_BQ_E;
	protected Variable stock_C_MQ_E;
	protected Variable stock_C_HQ_BE;
	protected HashMap<Chocolat, Variable> stocksChocoVar;

	//Stock de chocolat de marque
	protected Variable stock_C_MQ_Limdt;
	protected Variable stock_C_BQ_E_Limdt;
	protected Variable stock_C_MQ_E_Limdt;
	protected Variable stock_C_HQ_BE_Limdt;
	protected HashMap<ChocolatDeMarque, Variable> stocksMarqueVar;


	// Tableux qui tracent la péremption de nos chocolats de marque
	protected double[] peremption_C_MQ_Limdt = new double[12];
	protected double[] peremption_C_BQ_E_Limdt = new double[12];
	protected double[] peremption_C_MQ_E_Limdt = new double[12];
	protected double[] peremption_C_HQ_BE_Limdt = new double[12];

	//Tableaux qui tracent la péremption de nos fèves
	protected double[] peremption_F_MQ = new double[8];
	protected double[] peremption_F_BQ_E = new double[8];
	protected double[] peremption_F_MQ_E = new double[8];
	protected double[] peremption_F_HQ_BE = new double[8];

	//Listes regroupant les contrats cadres actifs
	protected List<ExemplaireContratCadre> mesContratEnTantQuAcheteur;
	protected List<ExemplaireContratCadre> mesContratEnTantQueVendeur;

	//Informations sur la production de chocolats
	protected HashMap<Feve, HashMap<Chocolat, Double>> pourcentageTransfo; // pour les differentes feves, le chocolat qu'elles peuvent contribuer a produire avec le ratio qttChocoProduit/qttFevesUtilisée
	protected HashMap<Chocolat, Variable> repartitionTransfo; // le pourcentage de prodMax qu'on souhaite produire pour chaque chocolat


	//Des tables de hachages pour connaître l'état des chocolats à une période précise
	protected HashMap<Feve, Double> qttEntrantesFeve; //Contient les quantités entrant dans le stock à la période actuelle
	protected HashMap<Feve, Double> prixTFeveStockee; //Contient les prix moyens des fèves en stock
	protected HashMap<Chocolat, Double> prixTChocoBase; //Contient les prix des chocolats produits en s'appuyant sur le prix du stock de fèves
	protected HashMap<Chocolat, Double> qttSortantesChoco; //Contient les quantités sortantes de chocolat à la période actuelle (CC + enchères + AO)
	protected HashMap<Chocolat, Double> marges; // les marges sur nos produits
	protected Variable qttFevesAcheteesBourse; // La quantité de fèves MQ achetées en bourse
	protected HashMap<Chocolat, Double> qttSortantesTransactions; //Contient les quantités sortantes de chocolat à la période actuelle (enchères + AO)

	// Variables indiquant nos prix de vente
	protected Variable prix_Limdt_MQ;
	protected Variable prix_Limdt_BQ_E; 
	protected Variable prix_Limdt_MQ_E;
	protected Variable prix_Limdt_HQ_BE;


	//Quantité vendues par step
	protected Variable totalSortant ;



	/**
	 * Cette classe est la plus haute dans l'arbre d'héritage du transformateur 1
	 * On y définit les différentes variables et types construits qui nous seront nécessaires pour toutes les autres classe héritières
	 * Il ne fait qu'initialiser ces mêmes variables et commence la mise en page des journaux
	 */
	public Transformateur1Acteur() {

		//Initialisation des journaux
		this.journal = new Journal("Journal " + this.getNom(), this);
		this.journalStock = new Journal("Journal Stock " + this.getNom(), this);
		this.journalCC = new Journal("Journal CC " + this.getNom(), this);
		this.journalTransactions = new Journal("Journal Transactions " + this.getNom(), this);
		this.journalPeremptionLimdt = new Journal("Journal Péremption LimDt " + this.getNom(), this);
		this.journalPeremptionFeves = new Journal("Journal Péremption Feves " + this.getNom(), this);
		this.journalCouts = new Journal("Journal Coûts " + this.getNom(), this);


		//On fixe les types de fèves dont on aura besoin
		this.lesFeves = new LinkedList<Feve>();
		this.lesFeves.add(Feve.F_HQ_BE);
		this.lesFeves.add(Feve.F_MQ_E);
		this.lesFeves.add(Feve.F_BQ_E);
		this.lesFeves.add(Feve.F_MQ);


		//On fixe les type de chocolat que l'on va produire
		this.lesChocolats = new LinkedList<Chocolat>();
		this.lesChocolats.add(Chocolat.C_MQ);
		this.lesChocolats.add(Chocolat.C_BQ_E);
		this.lesChocolats.add(Chocolat.C_MQ_E);
		this.lesChocolats.add(Chocolat.C_HQ_BE);




		//Constructions des variables de stocks 
		/**
		 * @author MURY Julien
		 * @author ABBASSI Rayene
		 */
		this.stock_F_MQ =new Variable("F_MQ","<html>Quantite totale de F_MQ en stock</html>", this, 0., 1000000., 5000.);
		this.stock_F_BQ_E = new Variable("F_BQ_E","<html>Quantite totale de F_BQ_E en stock</html>", this, 0., 1000000., 5000.);
		this.stock_F_MQ_E = new Variable("F_MQ_E", "<html>Quantite totale de F_MQ_E en stock</html>", this, 0., 1000000., 5000.);
		this.stock_F_HQ_BE = new Variable("F_HQ_BE", "<html>Quantite totale de F_HQ_BE en stock</html>", this, 0., 1000000., 5000.);

		this.stock_C_MQ=new Variable("EQ4T Stock C_MQ", "<html>Quantite totale de C_MQ en stock</html>",this, 0.0, 1000000.0, 0.0);
		this.stock_C_BQ_E=new Variable("EQ4T Stock C_BQ_E", "<html>Quantite totale de C_BQ_E en stock</html>",this, 0.0, 1000000.0, 0.0);
		this.stock_C_MQ_E=new Variable("EQ4T Stock C_MQ_E", "<html>Quantite totale de C_MQ_E en stock</html>",this, 0.0, 1000000.0, 0.0);
		this.stock_C_HQ_BE=new Variable("EQ4T Stock C_HQ_BE", "<html>Quantite totale de C_HQ_BE en stock</html>",this, 0.0, 1000000.0, 0.0);
		
		this.stock_C_MQ_Limdt=new Variable("EQ4T Stock C_MQ_Limdt", "<html>Quantite totale de C_MQ_Limdt en stock</html>",this, 0.0, 1000000.0, 40000.0);
		this.stock_C_BQ_E_Limdt=new Variable("EQ4T Stock C_BQ_E_Limdt", "<html>Quantite totale de C_BQ_E_Limdt en stock</html>",this, 0.0, 1000000.0, 40000.0);
		this.stock_C_MQ_E_Limdt=new Variable("EQ4T Stock C_MQ_E_Limdt", "<html>Quantite totale de C_MQ_E_Limdt en stock</html>",this, 0.0, 1000000.0, 40000.0);
		this.stock_C_HQ_BE_Limdt=new Variable("EQ4T Stock C_HQ_BE_Limdt", "<html>Quantite totale de C_HQ_BE_Limdt en stock</html>",this, 0.0, 1000000.0, 40000.0);
	

		// Initialisation des variables de péremption
        this.peremption_C_MQ_Limdt = new double[12];
		this.peremption_C_BQ_E_Limdt = new double[12];
		this.peremption_C_MQ_E_Limdt = new double[12];
		this.peremption_C_HQ_BE_Limdt = new double[12];
		this.peremption_F_MQ = new double[8];
		this.peremption_F_BQ_E = new double[8];
		this.peremption_F_MQ_E = new double[8];
		this.peremption_F_HQ_BE = new double[8];

        // Initialisation des hashmaps de stocks
		this.stocksFevesVar = new HashMap<Feve, Variable>();
		this.stocksFevesVar.put(Feve.F_MQ, stock_F_MQ);
		initialiserPeremption(peremption_F_MQ, stock_F_MQ);

		this.stocksFevesVar.put(Feve.F_BQ_E, stock_F_BQ_E);
		initialiserPeremption(peremption_F_BQ_E, stock_F_BQ_E);

		this.stocksFevesVar.put(Feve.F_MQ_E, stock_F_MQ_E);
		initialiserPeremption(peremption_F_MQ_E, stock_F_MQ_E);

		this.stocksFevesVar.put(Feve.F_HQ_BE, stock_F_HQ_BE);
		initialiserPeremption(peremption_F_HQ_BE, stock_F_HQ_BE);

		this.stocksChocoVar= new HashMap<Chocolat, Variable>();
		this.stocksChocoVar.put(Chocolat.C_MQ, stock_C_MQ);
		this.stocksChocoVar.put(Chocolat.C_BQ_E, stock_C_BQ_E);
		this.stocksChocoVar.put(Chocolat.C_MQ_E, stock_C_MQ_E);
		this.stocksChocoVar.put(Chocolat.C_HQ_BE, stock_C_HQ_BE);

		this.stocksMarqueVar = new HashMap<ChocolatDeMarque, Variable>();

		this.qttProduiteChoco = new Variable("Qtt produite chocolat", this, 0., 10000000000., 0.);

		this.pourcentageTransfo = new HashMap<Feve, HashMap<Chocolat, Double>>();

		this.repartitionTransfo = new HashMap<Chocolat, Variable>();
		repartitionTransfo.put(Chocolat.C_MQ, new Variable("pourcentageC_MQ", this, 0, 1, 0.25));
		repartitionTransfo.put(Chocolat.C_BQ_E, new Variable("pourcentageC_BQ_E", this, 0, 1, 0.25));
		repartitionTransfo.put(Chocolat.C_MQ_E, new Variable("pourcentageC_MQ_E", this, 0, 1, 0.25));
		repartitionTransfo.put(Chocolat.C_HQ_BE, new Variable("pourcentageC_HQ_BE", this, 0, 1, 0.25));
		
		this.prixTFeveStockee = new HashMap<Feve, Double>();
		this.prixTChocoBase = new HashMap<Chocolat, Double>();

		this.prix_Limdt_MQ = new Variable("Prix LimDt MQ", "<html>Prix de vente du chocolat de marque MQ</html>", this, 0., 1000000., 0.);
		this.prix_Limdt_BQ_E = new Variable("Prix LimDt BQ_E", "<html>Prix de vente du chocolat de marque BQ_E</html>", this, 0., 1000000., 0.);
		this.prix_Limdt_MQ_E = new Variable("Prix LimDt MQ_E", "<html>Prix de vente du chocolat de marque MQ_E</html>", this, 0., 1000000., 0.);
		this.prix_Limdt_HQ_BE = new Variable("Prix LimDt HQ_BE", "<html>Prix de vente du chocolat de marque HQ_BE</html>", this, 0., 1000000., 0.);

		this.qttEntrantesFeve = new HashMap<Feve, Double>();
		for (Feve f : lesFeves){
			qttEntrantesFeve.put(f, 0.);
		}

		this.qttSortantesChoco = new HashMap<Chocolat, Double>();
		for (Chocolat c : lesChocolats){
			qttSortantesChoco.put(c, 0.);
		}
		this.qttSortantesTransactions = new HashMap<Chocolat, Double>();
		this.qttFevesAcheteesBourse = new Variable("Qtt Feves Achetees Bourse", "<html>Quantité de fèves achetées en bourse</html>", this, 0., 1000000., 0.);

		this.totalSortant = new Variable("Quantité vendue au step", this, 0., 1000000, 0.);

		this.marges = new HashMap<Chocolat, Double>();
	}

	public void initialiser() {
		
		//On fixe les chocolats de marque que l'on va produire
		//On ne peut pas le faire dans le constructeur de la classe car pour créer un chocolat de marque on a besoin que la filière soit initialisée.
		this.chocolatsLimDt=new LinkedList<ChocolatDeMarque>();
		for (Chocolat c : lesChocolats) {
			int pourcentageCacao =  (int) (Filiere.LA_FILIERE.getParametre("pourcentage min cacao "+c.getGamme()).getValeur());
			ChocolatDeMarque cm= new ChocolatDeMarque(c, "LimDt", pourcentageCacao);
			this.chocolatsLimDt.add(cm);
			this.qttSortantesTransactions.put(c, 0.);
		}


		//Initialisation des stocks de chocolat de marque
		for (ChocolatDeMarque cm : chocolatsLimDt){
			switch (cm.getChocolat()){

				case C_MQ : 
					stocksMarqueVar.put(cm, stock_C_MQ_Limdt);
					initialiserPeremption(peremption_C_MQ_Limdt, stock_C_MQ_Limdt);
					break;

				case C_BQ_E : 
					stocksMarqueVar.put(cm, stock_C_BQ_E_Limdt);
					initialiserPeremption(peremption_C_BQ_E_Limdt, stock_C_BQ_E);
					break;

				case C_MQ_E : 
					stocksMarqueVar.put(cm, stock_C_MQ_E_Limdt);
					initialiserPeremption(peremption_C_MQ_E_Limdt, stock_C_MQ_E_Limdt);
					break;

				case C_HQ_BE : 
					stocksMarqueVar.put(cm, stock_C_HQ_BE_Limdt);
					initialiserPeremption(peremption_C_HQ_BE_Limdt, stock_C_HQ_BE_Limdt);
					break;

				default : 
					journalStock.ajouter(Color.pink, Color.BLACK,"Le chocolat " + cm + " ne devrait pas être présent dans notre gammme");
					break;
			}
			this.journalStock.ajouter(Romu.COLOR_LLGRAY, Color.BLACK," stock("+cm+")->"+this.stocksMarqueVar.get(cm).getValeur());
			this.journalStock.ajouter("\n");
		}


		//Initialisation des prix de nos stocks de fève
		this.prixTFeveStockee.put(Feve.F_MQ, 2000.);
		this.prixTFeveStockee.put(Feve.F_BQ_E, 2000.);
		this.prixTFeveStockee.put(Feve.F_MQ_E, 2000.);
		this.prixTFeveStockee.put(Feve.F_HQ_BE, 2000.);

		//Initialisation des prix de base des chocolats que l'on veut produire
		this.prixTChocoBase.put(Chocolat.C_MQ, 20000.);
		this.prix_Limdt_MQ.setValeur(this, 20000.);

		this.prixTChocoBase.put(Chocolat.C_BQ_E, 20000.);
		this.prix_Limdt_BQ_E.setValeur(this, 20000.);

		this.prixTChocoBase.put(Chocolat.C_MQ_E, 20000.);
		this.prix_Limdt_MQ_E.setValeur(this, 20000.);

		this.prixTChocoBase.put(Chocolat.C_HQ_BE, 20000.);
		this.prix_Limdt_HQ_BE.setValeur(this, 20000.);
		

		//Initialisation des marges que l'on va faire sur les différents produits
		this.marges.put(Chocolat.C_MQ, 2.5);
		this.marges.put(Chocolat.C_BQ_E, 1.5);
		this.marges.put(Chocolat.C_MQ_E, 1.5);
		this.marges.put(Chocolat.C_HQ_BE, 2.3);


		//Initialisation des pourcentage de conversion fèves vers chocolat
		this.pourcentageTransfo.put(Feve.F_HQ_BE, new HashMap<Chocolat, Double>());
		double conversion = 1.0 + (100.0 - Filiere.LA_FILIERE.getParametre("pourcentage min cacao HQ").getValeur())/100.0;
		this.pourcentageTransfo.get(Feve.F_HQ_BE).put(Chocolat.C_HQ_BE, conversion);// la masse de chocolat obtenue est plus importante que la masse de feve vue l'ajout d'autres ingredients

		this.pourcentageTransfo.put(Feve.F_MQ_E, new HashMap<Chocolat, Double>());
		conversion = 1.0 + (100.0 - Filiere.LA_FILIERE.getParametre("pourcentage min cacao MQ").getValeur())/100.0;
		this.pourcentageTransfo.get(Feve.F_MQ_E).put(Chocolat.C_MQ_E, conversion);

		this.pourcentageTransfo.put(Feve.F_MQ, new HashMap<Chocolat, Double>());
		this.pourcentageTransfo.get(Feve.F_MQ).put(Chocolat.C_MQ, conversion);

		this.pourcentageTransfo.put(Feve.F_BQ_E, new HashMap<Chocolat, Double>());
		conversion = 1.0 + (100.0 - Filiere.LA_FILIERE.getParametre("pourcentage min cacao BQ").getValeur())/100.0;
		this.pourcentageTransfo.get(Feve.F_BQ_E).put(Chocolat.C_BQ_E, conversion);

		this.journalStock.ajouter(Romu.COLOR_LLGRAY, Color.PINK, "Stock initial chocolat de marque : ");

		this.journalCC.ajouter(Color.orange, Color.BLACK, "Les achats seront en marron;");
		this.journalCC.ajouter(Color.orange, Color.BLACK, "Les ventes LimDt en mauve;");
		this.journalCC.ajouter(Color.orange, Color.BLACK, "Et les autres ventes en vert.");
		this.journalCC.ajouter("\n");

		this.journalTransactions.ajouter(Color.orange, Color.BLACK, "Les achats en bourse seront en magenta;");
		this.journalTransactions.ajouter(Color.orange, Color.BLACK, "Les ventes aux enchères en gris foncé;");
		this.journalTransactions.ajouter(Color.orange, Color.BLACK, "Et les ventes AO en rouge.");
		this.journalTransactions.ajouter("\n");

		//Initialisation des quantités de fève entrantes
		this.qttEntrantesFeve.put(Feve.F_MQ, 0.);
		this.qttEntrantesFeve.put(Feve.F_BQ_E, 0.);
		this.qttEntrantesFeve.put(Feve.F_HQ_BE, 0.);
		this.qttEntrantesFeve.put(Feve.F_MQ_E, 0.);



	}


	public String getNom() {// NE PAS MODIFIER
		return "EQ4T";
	}
	
	public String toString() {// NE PAS MODIFIER
		return this.getNom();
	}

	////////////////////////////////////////////////////////
	//         En lien avec l'interface graphique         //
	////////////////////////////////////////////////////////

	public void next() {
		
		/*
		this.journalStock.ajouter("Stock de fèves : " + this.totalStocksFeves.getValeur(this.cryptogramme));
		this.journalStock.ajouter("Stock de chocolat : " + this.totalStocksChoco.getValeur(this.cryptogramme));
		this.journalStock.ajouter("Stock de chocolat de marque : " + this.totalStocksChocoMarque.getValeur(this.cryptogramme));
		this.journalStock.ajouter("\n");
		*/


		this.journal.ajouter("Solde : " + this.getSolde());
		this.journal.ajouter("\n");


		this.journal.ajouter("##########");
		this.journal.ajouter(Color.yellow, Romu.COLOR_LBLUE, "N° Etape " + Filiere.LA_FILIERE.getEtape());

		this.journalStock.ajouter("\n");
		this.journalStock.ajouter("##########");
		this.journalStock.ajouter(Color.yellow, Romu.COLOR_LBLUE, "N° Etape " + Filiere.LA_FILIERE.getEtape());

		this.journalCC.ajouter("##########");
		this.journalCC.ajouter(Color.yellow, Romu.COLOR_LBLUE, "N° Etape " + Filiere.LA_FILIERE.getEtape());

		this.journalTransactions.ajouter("##########");
		this.journalTransactions.ajouter(Color.yellow, Romu.COLOR_LBLUE, "N° Etape " + Filiere.LA_FILIERE.getEtape());

		this.journalPeremptionLimdt.ajouter("\n");
		this.journalPeremptionLimdt.ajouter("##########");
		this.journalPeremptionLimdt.ajouter(Color.yellow, Romu.COLOR_LBLUE, "N° Etape " + Filiere.LA_FILIERE.getEtape());

		this.journalPeremptionFeves.ajouter("\n");
		this.journalPeremptionFeves.ajouter("##########");
		this.journalPeremptionFeves.ajouter(Color.yellow, Romu.COLOR_LBLUE, "N° Etape " + Filiere.LA_FILIERE.getEtape());

		this.journalCouts.ajouter("##########");
		this.journalCouts.ajouter(Color.yellow, Romu.COLOR_LBLUE, "N° Etape " + Filiere.LA_FILIERE.getEtape());

	}

	public Color getColor() {// NE PAS MODIFIER
		return new Color(229, 243, 157); 
	}

	public String getDescription() {
		return "Transformateur spécialisé dans le chocolat équitable à petits prix";
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
		res.add(this.journal);
		res.add(this.journalStock);
		res.add(this.journalTransactions);
		res.add(this.journalCC);
		res.add(this.journalPeremptionLimdt);
		res.add(this.journalPeremptionFeves);
		res.add(this.journalCouts);

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
		journal.ajouter("Opération sur compte bancaire : " +  montant);
		this.journal.ajouter("\n");
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

	@Override
	public List<String> getMarquesChocolat() {
		LinkedList<String> marques = new LinkedList<String>();
		marques.add("LimDt");
		return marques;
	}



	/**
	 * @author MURY Julien
	 * Cette méthode permet de connaitre notre stock d'un produit bien précis, très utile si on ne veut qu'accéder au stock sans nécessairement faire de modifications.
	 * Si des modifications sont prévues, on utilisera directement les variables.
	 */
	@Override
	public double getQuantiteEnStock(IProduit p, int cryptogramme) {
		if (this.cryptogramme==cryptogramme) { // c'est donc bien un acteur assermente qui demande a consulter la quantite en stock
			if (p instanceof Feve) {
				if (this.lesFeves.contains(p)) {
					return this.stocksFevesVar.get(p).getValeur();
				} else {
					return 0.0;
				}
			} else if (p instanceof Chocolat) {
				if (this.lesChocolats.contains(p)) {
					return this.stocksChocoVar.get(p).getValeur();
				} else {
					return 0.0;
				}
			} 
			else if (p instanceof ChocolatDeMarque){
				if (this.chocolatsLimDt.contains(p)) {
					return this.stocksMarqueVar.get(p).getValeur();
				} else {
					return 0.0;
				}
			}
			else { return 0.;}
		} 
		else {
			return 0; // Les acteurs non assermentes n'ont pas a connaitre notre stock
		}
	}

	/**
	 * @author YAOU Reda
	 * Cette méthode initialise les tableaux de péremption de nos produits
	 */
	public void initialiserPeremption(double[] peremptionArray, Variable stock) {
		peremptionArray[0] = stock.getValeur();
		for (int i=1; i<peremptionArray.length; i++){
			peremptionArray[i] = 0.0;
		}
	}
}

