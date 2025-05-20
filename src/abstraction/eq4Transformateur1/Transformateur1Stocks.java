package abstraction.eq4Transformateur1;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List; 

import abstraction.eqXRomu.acteurs.Romu;
import abstraction.eqXRomu.bourseCacao.BourseCacao;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IFabricantChocolatDeMarque;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.IProduit;;

public class Transformateur1Stocks extends Transformateur1Usine implements IFabricantChocolatDeMarque {

	//Des variables qui ne seront au final que des constantes lors de la simulation

	protected double coutStockage; // cout de stockage par tonne et par step
	protected double coutProd; // cout de production unitaire du chocolat produit durant cette step, censé contenir salaires, ingrédients secondaires, et autres couts fixes
	protected double STOCK_MAX_TOTAL_FEVES = 1000000;
	protected double PRIX_MAX = 1000000;

	//Listes regroupant les contrats cadres actifs
	protected List<ExemplaireContratCadre> mesContratEnTantQuAcheteur;
	protected List<ExemplaireContratCadre> mesContratEnTantQueVendeur;

	private List<ChocolatDeMarque> chocosProduits; // la liste de toutes les sortes de ChocolatDeMarque que l'acteur produit et peut vendre



	public Transformateur1Stocks() {
		super();

		this.chocosProduits = new LinkedList<ChocolatDeMarque>();

		this.prixTFeveStockee = new HashMap<Feve, Double>();
		this.prixTChocoBase = new HashMap<Chocolat, Double>();

		this.marges = new HashMap<Chocolat, Double>();
	}





	
	public void initialiser() {
		super.initialiser();

		this.coutStockage = Filiere.LA_FILIERE.getParametre("cout moyen stockage producteur").getValeur()*4;

		this.coutProd = 0; //il s'agit du cout de la production d'une tonne de chocolat, valeur arbitraire censée contenir salaires, ingrédients secondaires, et autres couts fixes


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





	////////////////////////////////////////////////////////
	//      En lien avec la comptabilité et production    //
	////////////////////////////////////////////////////////




	/**
	 * @author ABBASSI Rayene
	 * @author MURY Julien
	 * Cette méthode transforme une partie du stock de fève en chacun des chocolats que l'on a décidé de produire.
	 * Elle détermine et place dans la HashMap prixTChocoBase le prix sans marge des chocolats produits en se basant sur le prix du stock de fèves
	 * @param None
	 * @return None
	 */
	protected void transformation(){

		// La quantité de fèves à transformer
		double transfo=0.;
		this.qttProduiteChoco.setValeur(this, 0.); //On initialise la quantité produite à 0 pour le step en cours

		for (Feve f : lesFeves) {
			for (Chocolat c : lesChocolats) {

				if (this.getQuantiteEnStock(f, this.cryptogramme) > 0. && this.pourcentageTransfo.get(f).get(c) != null){

					//On calcule la quantité de fèves à transformer
					transfo = Math.min(this.getQuantiteEnStock(f, this.cryptogramme), this.prodMax.getValeur() * this.repartitionTransfo.get(c).getValeur() / this.pourcentageTransfo.get(f).get(c));

					if (transfo == this.getQuantiteEnStock(f, this.cryptogramme)) {
						this.journal.ajouter(Color.pink, Color.red, "-> Aïe... On transforme tout notre stock de fèves "+f+" car on n'en achète pas assez");
					}

					//On s'assure que l'on produit quelque chose pour faire nos opérations
					if (transfo > 0.) {

						double pourcentageMarque = 1.0;  //Modifiable
						// La Pourcentage ainsi definie sera stockee sous forme de marquee, la quantité restante sera alors stockee comme non marquee

						//A MODIFIER
						int pourcentageCacao =  (int) (Filiere.LA_FILIERE.getParametre("pourcentage min cacao "+c.getGamme()).getValeur());
						ChocolatDeMarque cm= new ChocolatDeMarque(c, "LimDt", pourcentageCacao);
						
						//calcul du choclat produit
						double nouveauStock = transfo*this.pourcentageTransfo.get(f).get(c);

                        // On met à jour la qtt produite chocolat
						this.qttProduiteChoco.ajouter(this, nouveauStock);

						//Ajout des chocolats produits au stock
						this.retirerDuStock(f, transfo, this.cryptogramme);

						//this.ajouterAuStock(c, nouveauStock * (1.0-pourcentageMarque), this.cryptogramme);
						this.ajouterAuStock(cm, nouveauStock * pourcentageMarque, this.cryptogramme);

						
						//Notification dans le journal
						this.journal.ajouter(Romu.COLOR_LLGRAY, Color.PINK, "Transfo de "+(transfo<10?" "+transfo:transfo)+" T de "+f+" en "+Journal.doubleSur(transfo*this.pourcentageTransfo.get(f).get(c),3,2)+" T de "+c);
						this.journal.ajouter(Romu.COLOR_LLGRAY, Color.BLACK," stock("+f+")->"+this.getQuantiteEnStock(f, this.cryptogramme));
						this.journal.ajouter(Romu.COLOR_LLGRAY, Color.BLACK," stock("+c+")->"+this.getQuantiteEnStock(c, this.cryptogramme));
						this.journal.ajouter(Romu.COLOR_LLGRAY, Color.BLACK," stock("+cm+")->"+this.getQuantiteEnStock(cm, this.cryptogramme));
						this.journal.ajouter("\n");
					}
				}
			}
		}
		this.coutProd = totalCoutsUsineStep/this.qttProduiteChoco.getValeur();
        this.journalCouts.ajouter("Le cout unitaire moyen est "+coutProd);
		this.journalCouts.ajouter("\n");

		
		for (Feve  f : lesFeves) {
			for (Chocolat c : lesChocolats) {
				if (this.getQuantiteEnStock(f, this.cryptogramme) > 0. && this.pourcentageTransfo.get(f).get(c) != null) {
					transfo = Math.min(this.getQuantiteEnStock(f, this.cryptogramme), this.prodMax.getValeur() * this.repartitionTransfo.get(c).getValeur() / this.pourcentageTransfo.get(f).get(c));
					if (transfo > 0.) {
					    double pourcentageMarque = 1.0;  //Modifiable
						// La Pourcentage ainsi definie sera stockee sous forme de marquee, la quantité restante sera alors stockee comme non marquee

						//A MODIFIER
						int pourcentageCacao =  (int) (Filiere.LA_FILIERE.getParametre("pourcentage min cacao "+c.getGamme()).getValeur());
						ChocolatDeMarque cm= new ChocolatDeMarque(c, "LimDt", pourcentageCacao);
					
						//calcul du choclat produit
						double nouveauStock = transfo*this.pourcentageTransfo.get(f).get(c);
						//Détermination du prix de base des chocolats à la tonne en pondérant avec les coûts de la période précédente
						if(prixTChocoBase.containsKey(c) && nouveauStock > 0 ){
							
							double ancienPrix = prixTChocoBase.get(c);
							double nouveauPrix;

							//On vérifie que nos stocks ne sont pas négatifs car sinon on pourrait se retrouver avec des prix négatifs
							if ( this.getQuantiteEnStock(cm, this.cryptogramme) >=0.){
								int indMax = this.getMaxPeremption(c);
								nouveauPrix = ancienPrix * ((this.getQuantiteEnStock(c, this.cryptogramme) + this.getQuantiteEnStock(cm, this.cryptogramme))/ (nouveauStock+this.getQuantiteEnStock(c, this.cryptogramme) + this.getQuantiteEnStock(cm, this.cryptogramme))) + (prixTFeveStockee.get(f) + coutProd + indMax*this.coutStockage) * (pourcentageTransfo.get(f).get(c) * transfo / (nouveauStock+ this.getQuantiteEnStock(c, this.cryptogramme) + this.getQuantiteEnStock(cm, this.cryptogramme)));
							}
							//Si on est en dette de stock, on va garder le même prix qu'au step précédent et on l'augmente pour évite que l'on ne s'enfonce davantage
							else {
								nouveauPrix = 1.15*ancienPrix;
							}
							prixTChocoBase.put(c, nouveauPrix);
							if (c == Chocolat.C_MQ) {
								this.prix_Limdt_MQ.setValeur(this, nouveauPrix);
							} else if (c == Chocolat.C_BQ_E) {
								this.prix_Limdt_BQ_E.setValeur(this, nouveauPrix);
							} else if (c == Chocolat.C_MQ_E) {
								this.prix_Limdt_MQ_E.setValeur(this, nouveauPrix);
							} else if (c == Chocolat.C_HQ_BE) {
								this.prix_Limdt_HQ_BE.setValeur(this, nouveauPrix);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * @author MURY Julien
	 * @author YAOU Reda
	 * Une méthode qui permet de déterminer la quantitié de fèves entrant dans le stock à la période actuelle selon les contrats négociés et achats en bourse
	 * Les résultats sont stockés dans la HashMap qttEntrantes
	 * @param None
	 * @return None
	 */
	public void determinerQttEntrantFeves(){

		//Réinitialisation des qttEntrantes
		for(Feve f : this.lesFeves){
			this.qttEntrantesFeve.put(f, 0.);
		}

		//Quantité entrante de fèves f par contrat cadre
		for (Feve f : pourcentageTransfo.keySet()){
			for (ExemplaireContratCadre cc : mesContratEnTantQuAcheteur){
				if (cc.getProduit() == f){
					if (this.qttEntrantesFeve.containsKey(f)){
						this.qttEntrantesFeve.put(f, cc.getEcheancier().getQuantite(Filiere.LA_FILIERE.getEtape()) +qttEntrantesFeve.get(f));
					}
					else {
						this.qttEntrantesFeve.put(f, cc.getEcheancier().getQuantite(Filiere.LA_FILIERE.getEtape()));
					}
				}
			}
		}


		//Quantité entrante de fèves par achat en bourse
		if (qttEntrantesFeve.containsKey(Feve.F_MQ)){
			double ancienneValeur = this.qttEntrantesFeve.get(Feve.F_MQ);
			this.qttEntrantesFeve.put(Feve.F_MQ, ancienneValeur + qttFevesAcheteesBourse.getValeur());
		} else {
			this.qttEntrantesFeve.put(Feve.F_MQ, qttFevesAcheteesBourse.getValeur());
		}
	}

	/**
	 * @author MURY Julien
	 * Cette méthode retourne la quantité entrante de fève f à un step donné
	 * @param step
	 * @param f
	 * @return quantité de fève f qui entre dans nos stocks
	 */
	public double determinerQttEntrantFevesAuStep(int step, Feve f){

		double qttEntrante = 0.;

		//Quantité entrante de fèves f par contrat cadre
		for (ExemplaireContratCadre cc : mesContratEnTantQuAcheteur){
			if (cc.getProduit() == f){
				qttEntrante+= cc.getEcheancier().getQuantite(step);
			}
		}
	
		//Quantité entrante de fèves par achat en bourse (On approxime par le fait que la quantité de feve achetée en bourse est constante pour les autres step)
		qttEntrante += qttFevesAcheteesBourse.getValeur();

		return qttEntrante;
	}

   /**
    * @author MURY Julien
	* @author YAOU Reda
    */
	public void determinerQttSortantChoco(){

		//Réinitialisation des qttSortantes
		for (Chocolat c : lesChocolats){
			this.qttSortantesChoco.put(c, 0.);
		}

		//Qtt sortante par contrat cadre
		for (Chocolat c : lesChocolats){
			for (ExemplaireContratCadre cc : mesContratEnTantQueVendeur){
				if (cc.getProduit().equals(c) || ((ChocolatDeMarque)cc.getProduit()).getChocolat().equals(c)){
					if (this.qttSortantesChoco.containsKey(c)){

						this.qttSortantesChoco.put(c, cc.getEcheancier().getQuantite(Filiere.LA_FILIERE.getEtape())+qttSortantesChoco.get(c));
					}
					else {
						this.qttSortantesChoco.put(c, cc.getEcheancier().getQuantite(Filiere.LA_FILIERE.getEtape()));
					}
				}
			}
		}

		//Qtt sortante par vente en transaction (enchéres + AO)
		for (Chocolat c : lesChocolats){
			if (this.qttSortantesChoco.containsKey(c)){
				this.qttSortantesChoco.put(c, this.qttSortantesTransactions.get(c) +this.qttSortantesChoco.get(c));
			}
			else {
				this.qttSortantesChoco.put(c, this.qttSortantesTransactions.get(c));
			}

		}
	}

	/**
	 * @author MURY Julien
	 */
	public double determinerQttSortantChocoAuStep(int step, ChocolatDeMarque c){
		double qttSortant = 0.;

		//Chocolat se vendant par contrat cadre
		for (ExemplaireContratCadre cc : mesContratEnTantQueVendeur){
			if (cc.getProduit().equals(c) || ((ChocolatDeMarque)cc.getProduit()).equals(c)){
				qttSortant += cc.getEcheancier().getQuantite(step);
			}
		}

		//Chocolat se vendant par enchères 
		qttSortant += this.qttSortantesTransactions.get(c.getChocolat());
		
		return qttSortant;
	}


	/**
	 * @author MURY Julien
	 * Une méthode qui permet de déterminer les cout en fèves entrantes dans le stock à la période actuelle selon les contrats négociés et achats en bourse
	 * Les résultats sont stockés dans la HashMap qttEntrantes
	 * On amalgame le prix moyen total du stock avec le prix à la tonne des fèves qui entrent dans notre stock
	 * @param None 
	 * @return None
	 */
	public void determinerPrixTFevesStockees(){
		determinerQttEntrantFeves();
		
		for (Feve f : lesFeves){

			//On calcule le prix pour les fèves issues des contrats cadres
			double prix = 0.;
			for (ExemplaireContratCadre cc : mesContratEnTantQuAcheteur){
				if (cc.getProduit() == f){

					//On calcule le prix moyen de ce qui entre en pondérant par la part que représente ce contrat dans la qtt totale entrante (si tant est que le nombre de fèves qui entrent est non nul)
					if (qttEntrantesFeve.get(f) != 0.) prix += cc.getPrix() * (cc.getQuantiteALivrerAuStep()/qttEntrantesFeve.get(f));

				}
			}
			//On calcule le prix pour les ajouts par bourse
			if (f == Feve.F_MQ){
				BourseCacao bourse = (BourseCacao) Filiere.LA_FILIERE.getActeur("BourseCacao");	
				if (qttEntrantesFeve.get(f) != 0.) prix += bourse.getCours(f).getValeur() * (this.qttFevesAcheteesBourse.getValeur() / qttEntrantesFeve.get(f));
			}

			//Calcul du nouveau prix des fèves en stock
			double ancienPrixPondere = 0.;
			if ((getQuantiteEnStock(f, this.cryptogramme)+ qttEntrantesFeve.get(f)) >= 0.){

				//On calcul l'ancien prix que l'on pondère par la quantité que cela représente dans les stocks
				if (lesFeves.contains(f) && this.getQuantiteEnStock(f, cryptogramme)>= 0.){
					ancienPrixPondere = prixTFeveStockee.get(f)*(getQuantiteEnStock(f, this.cryptogramme)/(getQuantiteEnStock(f, this.cryptogramme)+ qttEntrantesFeve.get(f)));
				}
				else {
					ancienPrixPondere = 0.;
				}
				prixTFeveStockee.put(f, ancienPrixPondere + prix*(qttEntrantesFeve.get(f)/(qttEntrantesFeve.get(f)+getQuantiteEnStock(f, this.cryptogramme))));
			}
		}
	}


	////////////////////////////////////////////////////////
	//         En lien avec l'interface graphique         //
	////////////////////////////////////////////////////////

	public void next() {
		super.next();


		/*if (Filiere.LA_FILIERE.getEtape() >= 1) {
			determinerQttEntrantFeves();
			determinerQttSortantChoco();
			if (qttEntrantesFeve.get(Feve.F_MQ)*pourcentageTransfo.get(Feve.F_MQ).get(Chocolat.C_MQ) > qttSortantesChoco.get(Chocolat.C_MQ)) {
				this.qttFevesAcheteesBourse.setValeur(this, 0.9 * this.qttFevesAcheteesBourse.getValeur());
			}
			if (qttEntrantesFeve.get(Feve.F_MQ)*pourcentageTransfo.get(Feve.F_MQ).get(Chocolat.C_MQ) < qttSortantesChoco.get(Chocolat.C_MQ)) {
				this.qttFevesAcheteesBourse.setValeur(this, 1.1 * this.qttFevesAcheteesBourse.getValeur());
			}
		}*/



		this.journalStock.ajouter("\n");
		this.journalPeremptionLimdt.ajouter("\n");
		this.journalPeremptionFeves.ajouter("\n");

	

		//Affichage des stocks de chaque produit dans le journalStock à la période présente 
		this.journal.ajouter("=== STOCKS === ");
		for (Feve f : this.lesFeves) {
			this.journalStock.ajouter(Romu.COLOR_LLGRAY, Color.BLACK, "Stock de "+Journal.texteSurUneLargeurDe(f+"", 15)+" = "+this.getQuantiteEnStock(f, this.cryptogramme));
		}
		this.journalStock.ajouter("\n");

		for (Chocolat c : this.lesChocolats) {
			this.journalStock.ajouter(Romu.COLOR_LLGRAY, Color.BLACK, "Stock de "+Journal.texteSurUneLargeurDe(c+"", 15)+" = "+this.getQuantiteEnStock(c, this.cryptogramme));
		}
		this.journalStock.ajouter("\n");

		for (ChocolatDeMarque cm : this.chocolatsLimDt) {
			this.journalStock.ajouter(Romu.COLOR_LLGRAY, Color.BLACK, "Stock de "+Journal.texteSurUneLargeurDe(cm+"", 15)+" = "+this.getQuantiteEnStock(cm, this.cryptogramme));
		}
		this.journalStock.ajouter("\n");


		// Affichage de l'état de péremption des stocks de chocolat de marque
		afficherPeremption(journalPeremptionLimdt, peremption_C_MQ_Limdt, Chocolat.C_MQ, Color.black);
		afficherPeremption(journalPeremptionLimdt, peremption_C_BQ_E_Limdt, Chocolat.C_BQ_E, Romu.COLOR_GREEN);
		afficherPeremption(journalPeremptionLimdt, peremption_C_MQ_E_Limdt, Chocolat.C_MQ_E, Color.BLUE);
		afficherPeremption(journalPeremptionLimdt, peremption_C_HQ_BE_Limdt, Chocolat.C_HQ_BE, Color.RED);


		// Affichage de l'état de péremption des stocks de fèves
		afficherPeremption(journalPeremptionFeves, peremption_F_MQ, Feve.F_MQ, Color.black);
		afficherPeremption(journalPeremptionFeves, peremption_F_BQ_E, Feve.F_BQ_E, Romu.COLOR_GREEN);
		afficherPeremption(journalPeremptionFeves, peremption_F_MQ_E, Feve.F_MQ_E, Color.BLUE);
		afficherPeremption(journalPeremptionFeves, peremption_F_HQ_BE, Feve.F_HQ_BE, Color.RED);
	


		// Détermination de prix des fèves et transformation de ces dernières

		this.determinerPrixTFevesStockees();
		this.transformation();


		// Respect de la règle de péremption des choco marque après 6 mois soit 12 nexts: on retire du stock ce qui est périmé
		//On met également à jour les prix par rapport à la nouvelle production. Si celui-ci est excessif, on le limite à PRIX_MAX
		for (ChocolatDeMarque cm : chocolatsLimDt){
			switch (cm.getChocolat()){

				case C_MQ : 
				    pertePeremption(peremption_C_MQ_Limdt, cm, Color.black, journalPeremptionLimdt);


					if ((prixTChocoBase.get(Chocolat.C_MQ) + coutProd + this.getMaxPeremption(Chocolat.C_MQ)*this.coutStockage)*marges.get(Chocolat.C_MQ)< PRIX_MAX){
						prix_Limdt_MQ.setValeur(this, (prixTChocoBase.get(Chocolat.C_MQ) + coutProd + this.getMaxPeremption(Chocolat.C_MQ)*this.coutStockage)*marges.get(Chocolat.C_MQ));
					}
					else {
						prix_Limdt_MQ.setValeur(this, PRIX_MAX);
					}


					break;

				case C_BQ_E : 
				    pertePeremption(peremption_C_BQ_E_Limdt, cm, Romu.COLOR_GREEN, journalPeremptionLimdt);


					if ((prixTChocoBase.get(Chocolat.C_BQ_E) + coutProd + this.getMaxPeremption(Chocolat.C_BQ_E)*this.coutStockage)*marges.get(Chocolat.C_BQ_E) < PRIX_MAX){
						prix_Limdt_BQ_E.setValeur(this, (prixTChocoBase.get(Chocolat.C_BQ_E) + coutProd + this.getMaxPeremption(Chocolat.C_BQ_E)*this.coutStockage)*marges.get(Chocolat.C_BQ_E));
					}
					else {
						prix_Limdt_BQ_E.setValeur(this, PRIX_MAX);
					}

					break;

				case C_MQ_E : 
				    pertePeremption(peremption_C_MQ_E_Limdt, cm, Color.blue, journalPeremptionLimdt);

					if ((prixTChocoBase.get(Chocolat.C_MQ_E) + coutProd + this.getMaxPeremption(Chocolat.C_MQ_E)*this.coutStockage)*marges.get(Chocolat.C_MQ_E) < PRIX_MAX){
						prix_Limdt_MQ_E.setValeur(this, (prixTChocoBase.get(Chocolat.C_MQ_E) + coutProd + this.getMaxPeremption(Chocolat.C_MQ_E)*this.coutStockage)*marges.get(Chocolat.C_MQ_E));
					}
					else {
						prix_Limdt_MQ_E.setValeur(this, PRIX_MAX);
					}

					break;

				case C_HQ_BE :
				    pertePeremption(peremption_C_HQ_BE_Limdt, cm, Color.red, journalPeremptionLimdt);


					if ((prixTChocoBase.get(Chocolat.C_HQ_BE) + coutProd + this.getMaxPeremption(Chocolat.C_HQ_BE)*this.coutStockage)*marges.get(Chocolat.C_HQ_BE) < PRIX_MAX){
						prix_Limdt_HQ_BE.setValeur(this, (prixTChocoBase.get(Chocolat.C_HQ_BE) + coutProd + this.getMaxPeremption(Chocolat.C_HQ_BE)*this.coutStockage)*marges.get(Chocolat.C_HQ_BE));
					}
					else {
						prix_Limdt_HQ_BE.setValeur(this, PRIX_MAX);
					}

					break;

				default : 
					this.journalStock.ajouter(Color.pink, Color.BLACK, "Le chocolat " + cm + " ne devrait pas être présent dans notre gammme");
					break;
			}
		}

		// Respect de la règle de péremption des fèves après 4 mois soit 8 nexts: on retire du stock ce qui est périmé
		for (Feve f : lesFeves){
			switch (f){

				case F_MQ : 
				    pertePeremption(peremption_F_MQ, f, Color.black, journalPeremptionFeves);
					break;

				case F_BQ_E : 
				    pertePeremption(peremption_F_BQ_E, f, Romu.COLOR_GREEN, journalPeremptionFeves);
					break;

				case F_MQ_E : 
				    pertePeremption(peremption_F_MQ_E, f, Color.blue, journalPeremptionFeves);
					break;

				case F_HQ_BE :
				    pertePeremption(peremption_F_HQ_BE, f, Color.red, journalPeremptionFeves);
					break;

				default : 
					this.journalStock.ajouter(Color.pink, Color.BLACK, "La fève " + f + " ne devrait pas être présente dans notre gammme");
					break;
			}
		}



		//Calcul des stocks globaux pour payer le cout du stockage
		double totalStocks = 0;
		for (Feve f : this.lesFeves){
			totalStocks += this.getQuantiteEnStock(f, this.cryptogramme);
		}
		for (Chocolat c : lesChocolats){
			totalStocks += this.getQuantiteEnStock(c, this.cryptogramme);
		}
		for (ChocolatDeMarque cm : chocolatsLimDt){
			totalStocks += this.getQuantiteEnStock(cm, this.cryptogramme);
		}

		Filiere.LA_FILIERE.getBanque().payerCout(this, cryptogramme, "Stockage", (totalStocks*this.coutStockage));
	}







	public List<Variable> getIndicateurs(){
		List<Variable> res = super.getIndicateurs();

		res.add(this.stock_F_MQ);
		res.add(this.stock_F_BQ_E);
		res.add(this.stock_F_MQ_E);
		res.add(this.stock_F_HQ_BE);
		res.add(this.stock_C_MQ);
		res.add(this.stock_C_BQ_E);
		res.add(this.stock_C_MQ_E);
		res.add(this.stock_C_HQ_BE);
		res.add(this.stock_C_MQ_Limdt);
		res.add(this.stock_C_BQ_E_Limdt);
		res.add(this.stock_C_MQ_E_Limdt);
		res.add(this.stock_C_HQ_BE_Limdt);
        res.add(this.prix_Limdt_MQ);
		res.add(this.prix_Limdt_BQ_E);
		res.add(this.prix_Limdt_MQ_E);
		res.add(this.prix_Limdt_HQ_BE);
		res.add(this.qttFevesAcheteesBourse);
		res.add(this.nbMachines);
		res.add(this.nbOuvriers);
		res.add(this.prodMax);
		res.add(this.qttProduiteChoco);
		res.add(this.totalSortant);
		for (Chocolat c : lesChocolats){
			res.add(this.repartitionTransfo.get(c));
		}
		return res;
	}



	////////////////////////////////////////////////////////
	//        Pour la creation de filieres de test        //
	////////////////////////////////////////////////////////


	public List<String> getMarquesChocolat() {
		LinkedList<String> marques = new LinkedList<String>();
		marques.add("LimDt");
		return marques;
	}

	public List<ChocolatDeMarque> getChocolatsProduits() {
		if (this.chocosProduits.size()==0) {
			for (Chocolat c : Chocolat.values()) {
				int pourcentageCacao =  (int) (Filiere.LA_FILIERE.getParametre("pourcentage min cacao "+c.getGamme()).getValeur());
				this.chocosProduits.add(new ChocolatDeMarque(c, "LimDt", pourcentageCacao));
			}
		}
		return this.chocosProduits;
	}

    /**
     * @author MURY Julien
	 * @author YAOU Reda : Gestion de la péremption
     * Cette méthode permet l'ajout en stock d'une certaine quantité d'un certain produit
     * @param produit : le produit à ajouter
     * @param quantite : la quantité de produit à ajouter 
     */
	public void ajouterAuStock(IProduit produit, double quantite, int cryptogramme){
		//On vérifie que l'acteur voulant accéder aux stocks est bien le bon 
		if (this.cryptogramme == cryptogramme){

			String type = produit.getType();

			if (type.equals("Feve")){
				switch ((Feve)produit){
					case F_MQ : 
						this.stocksFevesVar.get(Feve.F_MQ).ajouter(this, quantite, cryptogramme);
						this.peremption_F_MQ[0] += quantite;
						break;
					case F_BQ_E : 
						this.stocksFevesVar.get(Feve.F_BQ_E).ajouter(this, quantite, cryptogramme);
						this.peremption_F_BQ_E[0] += quantite;
						break;
					case F_MQ_E : 
						this.stocksFevesVar.get(Feve.F_MQ_E).ajouter(this, quantite, cryptogramme);
						this.peremption_F_MQ_E[0] += quantite;
						break;
					case F_HQ_BE : 
						this.stocksFevesVar.get(Feve.F_HQ_BE).ajouter(this, quantite, cryptogramme);
						this.peremption_F_HQ_BE[0] += quantite;
						break;
					default : 
						journalStock.ajouter(Color.pink, Color.BLACK, "EQ4T : Ce type de fève n'est pas censée entrer dans nos stocks: " + produit);
				}
				
			}
			else if (type.equals("Chocolat")){
				switch ((Chocolat)produit){
					case C_MQ : 
						this.stocksChocoVar.get(Chocolat.C_MQ).ajouter(this, quantite, cryptogramme);
						break;
					case C_BQ_E : 
						this.stocksChocoVar.get(Chocolat.C_BQ_E).ajouter(this, quantite, cryptogramme);
						break;
					case C_MQ_E : 
						this.stocksChocoVar.get(Chocolat.C_MQ_E).ajouter(this, quantite, cryptogramme);
						break;
					case C_HQ_BE : 
						this.stocksChocoVar.get(Chocolat.C_HQ_BE).ajouter(this, quantite, cryptogramme);
						break;
					default : 
						journalStock.ajouter(Color.pink, Color.BLACK,"EQ4T : Ce type de chocolat n'est pas censée entrer dans nos stocks: " + produit);
				}

			}
			else if (type.equals("ChocolatDeMarque")){
				switch (((ChocolatDeMarque)produit).getChocolat()){
					case C_MQ : 
						this.stocksMarqueVar.get(produit).ajouter(this, quantite, cryptogramme);
						this.peremption_C_MQ_Limdt[0] += quantite;
						break;
					case C_BQ_E : 
						this.stocksMarqueVar.get(produit).ajouter(this, quantite, cryptogramme);
						this.peremption_C_BQ_E_Limdt[0] += quantite;
						break;
					case C_MQ_E : 
						this.stocksMarqueVar.get(produit).ajouter(this, quantite, cryptogramme);
						this.peremption_C_MQ_E_Limdt[0] += quantite;
						break;
					case C_HQ_BE : 
						this.stocksMarqueVar.get(produit).ajouter(this, quantite, cryptogramme);
						this.peremption_C_HQ_BE_Limdt[0] += quantite;
						break;
					default : 
						journalStock.ajouter(Color.pink, Color.BLACK,"EQ4T : Ce type de chocolat n'est pas censée entrer dans nos stocks: " + produit);
				}
			}
			else {
				journalStock.ajouter(Color.pink, Color.BLACK,"Ce produit (" + produit + ") n'a pas un type connu ("+ type + ")");
			}
		}
	}


    /**
     * @author MURY Julien
	 * @author YAOU Reda : Gestion de la péremption
     * Cette méthode permet le retrait du stock d'une certaine quantité d'un certain produit
     * @param produit : le produit à ajouter
     * @param quantite : la quantité de produit à ajouter 
     */
	public void retirerDuStock(IProduit produit, double quantite, int cryptogramme){
		//On vérifie que l'acteur voulant accéder aux stocks est bien le bon 
		if (this.cryptogramme == cryptogramme){
			String type = produit.getType();
			if (type.equals("Feve")){
				switch ((Feve)produit){
					case F_MQ : 
						this.stocksFevesVar.get(Feve.F_MQ).retirer(this, quantite, cryptogramme);
						retirerPeremption(this.peremption_F_MQ, quantite);
						break;
					case F_BQ_E : 
						this.stocksFevesVar.get(Feve.F_BQ_E).retirer(this, quantite, cryptogramme);
						retirerPeremption(this.peremption_F_BQ_E, quantite);
						break;
					case F_MQ_E : 
						this.stocksFevesVar.get(Feve.F_MQ_E).retirer(this, quantite, cryptogramme);
						retirerPeremption(this.peremption_F_MQ_E, quantite);
						break;
					case F_HQ_BE : 
						this.stocksFevesVar.get(Feve.F_HQ_BE).retirer(this, quantite, cryptogramme);
						retirerPeremption(this.peremption_F_HQ_BE, quantite);
						break;
					default : 
						journalStock.ajouter(Color.pink, Color.BLACK,"EQ4T : Ce type de fève n'est pas censée entrer dans nos stocks: " + produit);
				}
				
			}
			else if (type.equals("Chocolat")){
				switch ((Chocolat)produit){
					case C_MQ : 
						this.stocksChocoVar.get(Chocolat.C_MQ).retirer(this, quantite, cryptogramme);
						break;
					case C_BQ_E : 
						this.stocksChocoVar.get(Chocolat.C_BQ_E).retirer(this, quantite, cryptogramme);
						break;
					case C_MQ_E : 
						this.stocksChocoVar.get(Chocolat.C_MQ_E).retirer(this, quantite, cryptogramme);
						break;
					case C_HQ_BE : 
						this.stocksChocoVar.get(Chocolat.C_HQ_BE).retirer(this, quantite, cryptogramme);
						break;
					default : 
						journalStock.ajouter(Color.pink, Color.BLACK,"EQ4T : Ce type de chocolat n'est pas censée entrer dans nos stocks: " + produit);
				}
	
			}
			else if (type.equals("ChocolatDeMarque")){
				switch (((ChocolatDeMarque)produit).getChocolat()){
					case C_MQ : 
						this.stocksMarqueVar.get(produit).retirer(this, quantite, cryptogramme);
						retirerPeremption(this.peremption_C_MQ_Limdt, quantite);
						break;
					case C_BQ_E : 
						this.stocksMarqueVar.get(produit).retirer(this, quantite, cryptogramme);
						retirerPeremption(this.peremption_C_BQ_E_Limdt, quantite);
						break;
					case C_MQ_E : 
						this.stocksMarqueVar.get(produit).retirer(this, quantite, cryptogramme);
						retirerPeremption(this.peremption_C_MQ_E_Limdt, quantite);
						break;
					case C_HQ_BE : 
						this.stocksMarqueVar.get(produit).retirer(this, quantite, cryptogramme);
						retirerPeremption(this.peremption_C_HQ_BE_Limdt, quantite);
						break;
					default : 
						journalStock.ajouter(Color.pink, Color.BLACK,"EQ4T : Ce type de chocolat n'est pas censée entrer dans nos stocks: " + produit);
				}
			}
			else {
				journalStock.ajouter(Color.pink, Color.BLACK,"Ce produit (" + produit + ") n'a pas un type connu ("+ type + ")");
			}
		}
	}

	/**
	 * @author YAOU Reda 
	 * Cette méthode permet de gérer la péremption de fèves et de chocos marque lorsqu'on retire du stock
	 */
	private void retirerPeremption(double[] peremptionArray, double quantite) {
		for (int i= peremptionArray.length - 1 ; i>=0; i--){
			if (peremptionArray[i] > 0 && peremptionArray[i] - quantite >= 0){
				peremptionArray[i] -= quantite;
				break;
			}
			else if (peremptionArray[i] > 0 && peremptionArray[i] - quantite < 0){
				quantite -= peremptionArray[i];
				peremptionArray[i] = 0;
			}
		}
	}

	/**
	 * @author YAOU Reda 
	 * Cette méthode nous fait perdre du stock lorsque le produit est périmé
	 */
	private void pertePeremption(double[] peremptionArray, IProduit p, Color color, Journal journalPer) {
		int n = peremptionArray.length - 1;
		if (peremptionArray[n] > 0) {
			journalPer.ajouter(Color.pink, color, "Péremption: On retire "+peremptionArray[n]+ " tonnes de "+p+" de notre stock");
			this.retirerDuStock(p, peremptionArray[n], this.cryptogramme);
		}

		for (int i=n; i>=1; i--) {
			peremptionArray[i] = peremptionArray[i-1];
		}
		peremptionArray[0] = 0;
	}
	/*
	 * @author YAOU Reda 
	 * Cette méthode affiche dans le journal l'état de péremption d'un produit
	 */
	private void afficherPeremption(Journal journalPer, double[] peremptionArray, IProduit p, Color color) {
		journalPer.ajouter(Romu.COLOR_LLGRAY, color, "Péremption : "+p);
		for (int i=0; i<peremptionArray.length; i++) {
			journalPer.ajouter(Romu.COLOR_LLGRAY, color, i+" : "+peremptionArray[i]);
		}
		journalPer.ajouter("\n");
	}

	/**
	 * @author YAOU Reda
	 * Cette méthode permet d'avoir le plus grand indice de l'array de péremption non vide, pour un type de chocolat
	 * Et ce pour déduire à quel point ce chocolat nous a couté pour le stockage et ainsi optimiser son prix
	 */
	private int getMaxPeremption(Chocolat c) {
		double[] peremptionArray = null;
		if (c == Chocolat.C_MQ) {
			peremptionArray = peremption_C_MQ_Limdt;
		} else if (c == Chocolat.C_BQ_E) {
			peremptionArray = peremption_C_BQ_E_Limdt;
		} else if (c == Chocolat.C_MQ_E) {
			peremptionArray = peremption_C_MQ_E_Limdt;
		} else if (c == Chocolat.C_HQ_BE) {
			peremptionArray = peremption_C_HQ_BE_Limdt;
		}
		int max = 0;
		for (int i=0; i<peremptionArray.length; i++) {
			if (peremptionArray[i] > 0) {
				max+= 1;
			}
		}
		return max;
	}

}
