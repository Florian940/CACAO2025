package abstraction.eq4Transformateur1.contratCadre;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.acteurs.Romu;
import abstraction.eqXRomu.bourseCacao.BourseCacao;
import abstraction.eqXRomu.contratsCadres.*;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve; 
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.Gamme;

import java.util.List;
import java.awt.Color;
import java.util.LinkedList;

/**
 * @author MURY Julien
 */
public class Transformateur1ContratCadreVendeurAcheteur extends Transformateur1ContratCadreVendeur implements IAcheteurContratCadre {
    
    
    protected double qttInitialementVoulue;	
	protected double prixInitialementVoulu;
	protected double epsilon;


	public Transformateur1ContratCadreVendeurAcheteur() {
		super();
		this.mesContratEnTantQuAcheteur=new LinkedList<ExemplaireContratCadre>();
        this.epsilon  = 0.1;

        this.qttInitialementVoulue = 2250000;//On cherche à acheter de quoi remplir ou vendre notre stock à hauteur de 50%

        this.prixInitialementVoulu = 2000.;

	}




	public Echeancier contrePropositionDeLAcheteur(ExemplaireContratCadre contrat) {

        this.qttInitialementVoulue =  (this.qttInitialementVoulue + contrat.getEcheancier().getQuantiteTotale())/2;

		//Détermination du nombre de contrat cadre : 
		int nbContratA = 0;
		for (ExemplaireContratCadre cc : mesContratEnTantQuAcheteur){
			if (cc.getProduit().equals(contrat.getProduit())) nbContratA++;
		}
		
		if (contrat.getEcheancier().getQuantiteTotale()>SuperviseurVentesContratCadre.QUANTITE_MIN_ECHEANCIER){

			//Si la qtt proposée est cohérente avec la quantité que nous voulions initialement, on accepte l'echeancier
			if (Math.abs((this.qttInitialementVoulue - contrat.getEcheancier().getQuantiteTotale())/this.qttInitialementVoulue) <= epsilon){
				return contrat.getEcheancier();
			}

			//Sinon on négocie en partant de l'hypothèse que seulement 75% des fèves que l'on est censés recevvoir vont réellement être livrées
			else{

				//Si Le nombre de contrat cadre est suffisant, on négocie la quantité des autres contrats via la quantité entrante et sortante à chaque step
				if(nbContratA > 3 ){
					double qttSortant = 0.;
					Echeancier e = contrat.getEcheancier();
					for (int step = contrat.getEcheancier().getStepDebut() ; step <= contrat.getEcheancier().getStepFin() ; step++){

						Feve prod = (Feve)contrat.getProduit();
						ChocolatDeMarque cmAssocie;
						int pourcentageCacao;

						//Calcul du manque de la quantité de fève nécessaire pour chacun des steps
						if(prod.getGamme().equals(Gamme.MQ)){
							if (prod.isEquitable()) {
								pourcentageCacao = (int) (Filiere.LA_FILIERE.getParametre("pourcentage min cacao "+ Gamme.MQ).getValeur());
								cmAssocie = new ChocolatDeMarque(Chocolat.C_MQ_E, "LimDt", pourcentageCacao);
								qttSortant = Math.min(this.prodMax.getValeur() * this.repartitionTransfo.get(Chocolat.C_MQ_E).getValeur() / this.pourcentageTransfo.get(prod).get(Chocolat.C_MQ_E), determinerQttSortantChocoAuStep(step, cmAssocie));
								qttSortant += peremption_C_MQ_E_Limdt[11] + peremption_C_MQ_E_Limdt[10] - 0.75*determinerQttEntrantFevesAuStep(step, prod);
							}
							else {
								pourcentageCacao = (int) (Filiere.LA_FILIERE.getParametre("pourcentage min cacao "+ Gamme.MQ).getValeur());
								cmAssocie = new ChocolatDeMarque(Chocolat.C_MQ, "LimDt", pourcentageCacao);
								qttSortant = Math.min(this.prodMax.getValeur() * this.repartitionTransfo.get(Chocolat.C_MQ).getValeur() / this.pourcentageTransfo.get(prod).get(Chocolat.C_MQ), determinerQttSortantChocoAuStep(step, cmAssocie));
								qttSortant +=  peremption_C_MQ_Limdt[11] + peremption_C_MQ_Limdt[10] - 0.75*determinerQttEntrantFevesAuStep(step, prod);
							}
						}

						else if (prod.getGamme().equals(Gamme.BQ)){
							pourcentageCacao = (int) (Filiere.LA_FILIERE.getParametre("pourcentage min cacao "+ Gamme.BQ).getValeur());
							cmAssocie = new ChocolatDeMarque(Chocolat.C_BQ_E, "LimDt", pourcentageCacao);
							qttSortant = Math.min(this.prodMax.getValeur() * this.repartitionTransfo.get(Chocolat.C_BQ_E).getValeur() / this.pourcentageTransfo.get(prod).get(Chocolat.C_BQ_E), determinerQttSortantChocoAuStep(step, cmAssocie));
							qttSortant += peremption_C_BQ_E_Limdt[11] + peremption_C_BQ_E_Limdt[10] - 0.75*determinerQttEntrantFevesAuStep(step, prod);
						}

						else if (prod.getGamme().equals(Gamme.HQ)){
							pourcentageCacao = (int) (Filiere.LA_FILIERE.getParametre("pourcentage min cacao "+ Gamme.HQ).getValeur());
							cmAssocie = new ChocolatDeMarque(Chocolat.C_HQ_BE, "LimDt", pourcentageCacao);
							qttSortant = Math.min(this.prodMax.getValeur() * this.repartitionTransfo.get(Chocolat.C_HQ_BE).getValeur() / this.pourcentageTransfo.get(prod).get(Chocolat.C_HQ_BE), determinerQttSortantChocoAuStep(step, cmAssocie));
							qttSortant += peremption_C_HQ_BE_Limdt[11] + peremption_C_HQ_BE_Limdt[10] - 0.75*determinerQttEntrantFevesAuStep(step, prod);
						}



						/* Mise à jour de l'échéancier selon nos exigeances */
						
						//si qtt sortante est très négatif, c'est que l'on achète plus de fève que l'on ne vent de chocolat, on annule donc le contrat
						if (qttSortant < -10000.){
							return null;
						}
						//Sinon, si on vend suffisamment de chocolat à chaque step et que la proposition est proche de ce que l'on souhaite, on met la qtt sortante à condition qu'elle soit positive
						//Mais il nous faut malgré tout des fèves et on va pour cela ajouter une certaine quantité de fève de secours au cas où on signerait un contrat en tant que vendeur 
						else if (Math.abs(1.5*qttSortant - e.getQuantite(step))/(1.5*qttSortant) < 0.2 && qttSortant>0. ){
							e.set(step, 1.5*qttSortant*0.95 + 0.05*e.getQuantite(step));
						}
						//si la qtt entrante est faible, on vérifie quand meme que celle ci respecte les spécifications sur les CC
						else if (Math.abs(1.1*qttSortant)< 10000. && 1.1*qttSortant > e.getQuantiteTotale()/(10*e.getNbEcheances())){
							e.set(step, 1.1*qttSortant);
						}
						//Sinon, on met directement qtt sortant
						else if (qttSortant > 10000. && qttSortant > e.getQuantiteTotale()/(10*e.getNbEcheances())){
							e.set(step, qttSortant);
						}

					}


					/*Vérification de la conformité de l'échéancier  */


					//Recherche du maximum de l'échancier
					double qttMax = 0.;
					double qttMin = e.getQuantite(e.getStepDebut()+1);


					for(int s = e.getStepDebut() ; s <= e.getStepFin() ; s++){
						if (e.getQuantite(s) > qttMax){
							qttMax = e.getQuantite(s);
						}
						if (e.getQuantite(s) < qttMin){
							qttMin = e.getQuantite(s);
						}
					}


					//On vérifie si l'échéancier nécessite des modifications, c'est le cas si l'une des étapes ne vérifie pas la conditions des distributeurs
					boolean modifNecessaires = false;
					for (int s =e.getStepDebut() ; s<= e.getStepFin() ; s++){
						if (e.getQuantite(s)< e.getQuantiteTotale()/(10*e.getNbEcheances())){
							modifNecessaires = true;
						}

					}

					//Si un des steps est inférieur à la quantité minimale, on met tous les steps à la quantité moyenne
					if (modifNecessaires){
						for (int s = e.getStepDebut() ; s <= e.getStepFin() ; s++){
							e.set(s, (qttMax + qttMin)/2);
						}
					}

					//Si la quantité totale est trop faible, on va se mettre au minimum sur tout le contrat 
					if (e.getQuantiteTotale() < 100.){
						for (int s = e.getStepDebut() ; s <= e.getStepFin() ; s++){
							e.set(s, 1200./e.getNbEcheances());
						}
					}


					/*Vérification de l'échéancier renvoyé */
					qttMin = e.getQuantite(e.getStepDebut()+1);
					for (int s =e.getStepDebut() ; s<= e.getStepFin() ; s++){
						if (e.getQuantite(s) >= qttMax){
							qttMax = e.getQuantite(s);
						}
						if (e.getQuantite(s) <= qttMin){

							qttMin = e.getQuantite(s);
						}
					}
					
					/*Renvoie de l'échéancier modifié */

					this.qttInitialementVoulue = e.getQuantiteTotale();	//On actualise la quantité initialement voulue à la quantité que l'on vient de négocier pour adapter la prochaine offre
					return e;

				}


				//Si on n'a pas suffisamment de contrats actifs, on accepte le contrat proposé seulement si la quantité moyenne par step est assez grande
				else {
					if(contrat.getEcheancier().getQuantiteTotale()/contrat.getEcheancier().getNbEcheances()< 100.){
						return null;
					}

					return contrat.getEcheancier();
				}

			}
		}
		else {
			return null;
		}
	}
	
	//A MODIFIER 
	/*Il faudrait s'appuyer sur le cours de la bourse pour négocier les prix avec les producteurs */
	public double contrePropositionPrixAcheteur(ExemplaireContratCadre contrat) {

		//On détermine des tolérances par rapport au cours de la bourse pour chacune des gammes de fèves
		double tolerance = 1.;
		Feve prod = ((Feve)contrat.getProduit());
		if(prod.getGamme().equals(Gamme.BQ)) tolerance = 2.;
		else if (prod.getGamme().equals(Gamme.MQ)) tolerance = 3.;
		else if (prod.getGamme().equals(Gamme.HQ)) tolerance = 4.; 
		

		// Récupération du cours de la bourse du cacao de basse qualité
		BourseCacao bourse = (BourseCacao)(Filiere.LA_FILIERE.getActeur("BourseCacao"));
		double cours = bourse.getCours(Feve.F_BQ).getValeur();

		//Si le prix est aberrant, on refuse d'office la négociation
        if (contrat.getPrix() > tolerance * cours){
			return tolerance*cours;
		}
		else{
			//On procède par dichotomie sur le prix proposé et notre prix voulu.
			//Si le prix proposé est inférieur à notre prix, on accepte le contrat
			if(contrat.getPrix() < this.prixInitialementVoulu){
				return contrat.getPrix(); 
			}

			//Sinon on vérifie si le prix est cohérent avec le notre d'un seuil epsilon
			if (Math.abs((contrat.getPrix()-prixInitialementVoulu)/prixInitialementVoulu) <= this.epsilon ){
				return contrat.getPrix();
			}
			//Sinon on contre-porpose un prix intermédiaire par rapport au prix proposé
			else{
				if (contrat.getPrix() <= 2* tolerance*cours) return (tolerance*cours + contrat.getPrix()) / 2;
				return -1;
			}
		}
	}

	public void initialiser(){
		super.initialiser();

		//Initialisation du prix initialement voulu au cours actuel du cacao de basse qualité
		this.prixInitialementVoulu = ((BourseCacao)Filiere.LA_FILIERE.getActeur("BourseCacao")).getCours(Feve.F_BQ).getValeur(); //On s'appuie sur le cours actuel de la fève F_BQ pour déterminer le prix à négocier
	
	}









	public void next() {
		super.next();

		//On actualise le prix que l'on veut 
		this.prixInitialementVoulu = ((BourseCacao)Filiere.LA_FILIERE.getActeur("BourseCacao")).getCours(Feve.F_BQ).getValeur(); //On s'appuie sur le cours actuel de la fève F_BQ pour déterminer le prix à négocier
	

		// On enleve les contrats obsolete (nous pourrions vouloir les conserver pour "archive"...)
		List<ExemplaireContratCadre> contratsObsoletes=new LinkedList<ExemplaireContratCadre>();
		for (ExemplaireContratCadre contrat : this.mesContratEnTantQuAcheteur) {
			if (contrat.getQuantiteRestantALivrer()==0.0 && contrat.getMontantRestantARegler()==0.0) {
				contratsObsoletes.add(contrat);
			}
		}
		this.mesContratEnTantQuAcheteur.removeAll(contratsObsoletes);

		

		//On essaie pour chacune des fèves dont on a besoin de négocier un contrat cadre avec tout les vendeurs de cette fève
		for(IProduit produit : this.lesFeves){


			//Détermination du nombre de contrat cadre : 
			int nbContratA = 0;
			for (ExemplaireContratCadre cc : mesContratEnTantQuAcheteur){
				if (cc.getProduit().equals(produit)) nbContratA++;
			}


			//On estime la quantité sortant que si on a suffisamment de contrat en tant qu'acheteur
			if(nbContratA > 3 ){
				//Détermination des carences en approvisionnement de fève
				ChocolatDeMarque cmAssocie;
				int pourcentageCacao;
				Feve prod = (Feve)produit;
				double qttSortant = 0.;
				int step = Filiere.LA_FILIERE.getEtape();

				//Calcul du manque de la quantité de fève nécessaire pour chacun des steps
				if(prod.getGamme().equals(Gamme.MQ)){
					if (prod.isEquitable()) {
						pourcentageCacao = (int) (Filiere.LA_FILIERE.getParametre("pourcentage min cacao "+ Gamme.MQ).getValeur());
						cmAssocie = new ChocolatDeMarque(Chocolat.C_MQ_E, "LimDt", pourcentageCacao);
						qttSortant = Math.min(this.prodMax.getValeur() * this.repartitionTransfo.get(Chocolat.C_MQ_E).getValeur(), qttSortantesChoco.get(cmAssocie.getChocolat())) / this.pourcentageTransfo.get(prod).get(Chocolat.C_MQ_E);
						qttSortant += - qttEntrantesFeve.get(Feve.F_MQ_E);
					}
					else {
						pourcentageCacao = (int) (Filiere.LA_FILIERE.getParametre("pourcentage min cacao "+ Gamme.MQ).getValeur());
						cmAssocie = new ChocolatDeMarque(Chocolat.C_MQ, "LimDt", pourcentageCacao);
						qttSortant = Math.min(this.prodMax.getValeur() * this.repartitionTransfo.get(Chocolat.C_MQ).getValeur() , qttSortantesChoco.get(cmAssocie.getChocolat()))/ this.pourcentageTransfo.get(prod).get(Chocolat.C_MQ);
						qttSortant += - qttEntrantesFeve.get(Feve.F_MQ);
					}
				}

				else if (prod.getGamme().equals(Gamme.BQ)){
					pourcentageCacao = (int) (Filiere.LA_FILIERE.getParametre("pourcentage min cacao "+ Gamme.BQ).getValeur());
					cmAssocie = new ChocolatDeMarque(Chocolat.C_BQ_E, "LimDt", pourcentageCacao);
					qttSortant = Math.min(this.prodMax.getValeur() * this.repartitionTransfo.get(Chocolat.C_BQ_E).getValeur(), qttSortantesChoco.get(cmAssocie.getChocolat()))/ this.pourcentageTransfo.get(prod).get(Chocolat.C_BQ_E);
					qttSortant += - qttEntrantesFeve.get(Feve.F_BQ_E);
				}

				else if (prod.getGamme().equals(Gamme.HQ)){
					pourcentageCacao = (int) (Filiere.LA_FILIERE.getParametre("pourcentage min cacao "+ Gamme.HQ).getValeur());
					cmAssocie = new ChocolatDeMarque(Chocolat.C_HQ_BE, "LimDt", pourcentageCacao);
					qttSortant = Math.min(this.prodMax.getValeur() * this.repartitionTransfo.get(Chocolat.C_HQ_BE).getValeur(), qttSortantesChoco.get(cmAssocie.getChocolat()))  / this.pourcentageTransfo.get(prod).get(Chocolat.C_HQ_BE);
					qttSortant += - qttEntrantesFeve.get(Feve.F_HQ_BE);
				}
			



				if(qttSortant > 0.){
					journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_BROWN, "Recherche d'un vendeur aupres de qui acheter " + produit);

					List<IVendeurContratCadre> vendeurs = supCCadre.getVendeurs(produit);
					if (vendeurs.contains(this)) {
						vendeurs.remove(this);
					}

					if (vendeurs.size()==0) {
						journalCC.ajouter(Color.pink, Romu.COLOR_BROWN, "-->Pas de vendeur potentiel");
						journalCC.ajouter("\n");
					} else {
						journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_BROWN, "Voici les vendeurs potentiels : " + vendeurs);
						journalCC.ajouter("\n");
					}

					for (IVendeurContratCadre vendeur : vendeurs){
						if (vendeur!=null) {
							journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_BROWN, "Demande au superviseur de debuter les negociations pour un contrat cadre de "+produit+" avec le vendeur "+vendeur);
							this.qttInitialementVoulue = STOCK_MAX_TOTAL_FEVES;
							ExemplaireContratCadre cc = supCCadre.demandeAcheteur((IAcheteurContratCadre)this, vendeur, produit, new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 30,1.5*qttSortant/vendeurs.size()), cryptogramme,false);
							if (cc!=null) {
								journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_BROWN, "-->aboutit au contrat "+cc);
								journalCC.ajouter("\n");
								this.mesContratEnTantQuAcheteur.add(cc);
							}
							else {
								journalCC.ajouter(Color.pink, Romu.COLOR_BROWN, "-->Le contrat n'a pas pu aboutir");
								journalCC.ajouter("\n");
							}
						}
					}
				}
			}

			//Sinon, on va initier des contrats à forte quantité
			else {
				journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_BROWN, "Recherche d'un vendeur aupres de qui acheter " + produit);

					List<IVendeurContratCadre> vendeurs = supCCadre.getVendeurs(produit);
					if (vendeurs.contains(this)) {
						vendeurs.remove(this);
					}

					if (vendeurs.size()==0) {
						journalCC.ajouter(Color.pink, Romu.COLOR_BROWN, "-->Pas de vendeur potentiel");
						journalCC.ajouter("\n");
					} else {
						journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_BROWN, "Voici les vendeurs potentiels : " + vendeurs);
						journalCC.ajouter("\n");
					}


					//Initialisation du chocolat associé à la fève
					Chocolat c;
					switch ((Feve)produit) {
						case F_MQ:
							c = Chocolat.C_MQ;
							break;
						case F_MQ_E:
							c = Chocolat.C_MQ_E;
							break;
							
						case F_BQ_E:
							c = Chocolat.C_BQ_E;
							break;

						case F_HQ_BE:
							c = Chocolat.C_HQ_BE;
							break;
					
						default:
							c = null;
							System.out.println("Cette feve ne devrait pas faire partie de la gamme : " + produit);
							break;
					}

					for (IVendeurContratCadre vendeur : vendeurs){
						if (vendeur!=null) {
							journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_BROWN, "Demande au superviseur de debuter les negociations pour un contrat cadre de "+produit+" avec le vendeur "+vendeur);
							this.qttInitialementVoulue = STOCK_MAX_TOTAL_FEVES;
							//On va initier un contrat à forte quantité car on n'a pas de contrat en tant que vendeur
							//Il contiendra à chaque step 50% de la quantité dont on a besoin 
							//A MODIFIER
							//Il faudrait tenir compte de la répartition de la production de chocolat, il faut donc ajouter la création du chocolat associé à la fève
							ExemplaireContratCadre cc = supCCadre.demandeAcheteur((IAcheteurContratCadre)this, vendeur, produit, new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 30, 0.5*repartitionTransfo.get(c).getValeur()*STOCK_MAX_TOTAL_FEVES/(30.*vendeurs.size())), cryptogramme,false);
							if (cc!=null) {
								journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_BROWN, "-->aboutit au contrat "+cc);
								journalCC.ajouter("\n");
								this.mesContratEnTantQuAcheteur.add(cc);
							}
							else {
								journalCC.ajouter(Color.pink, Romu.COLOR_BROWN, "-->Le contrat n'a pas pu aboutir");
								journalCC.ajouter("\n");
							}
						}
					}
			}
		}





		
		// Recherche d'acheteurs de chocolat de marque
		for(ChocolatDeMarque produit : chocolatsLimDt){

			Feve feveAssociee = Feve.F_BQ_E;

			switch(produit.getChocolat()){
				case C_MQ : 
					feveAssociee = Feve.F_MQ;
					break;
				
				case C_MQ_E : 
					feveAssociee = Feve.F_MQ_E;
					break;

				case C_BQ_E : 
					feveAssociee = Feve.F_BQ_E;
					break;

				case C_HQ_BE : 
					feveAssociee = Feve.F_HQ_BE;
					break;

				default : 
					System.out.println("Ce chocolat ne devrait pas faire partie de la gamme : " + produit);
					break;
			}


			//On calcule la quantité de chocolat qu'il est possible de vendre au step suivant : on extrapole cette quantité à tous les futurs steps
			double qttEntrant = Math.min(qttEntrantesFeve.get(feveAssociee) * pourcentageTransfo.get(feveAssociee).get(produit.getChocolat()), this.prodMax.getValeur() * this.repartitionTransfo.get(produit.getChocolat()).getValeur());
			qttEntrant -= qttSortantesChoco.get(produit.getChocolat());


			//On ne cherche des contrats cadres que si l'on a de la matière à vendre 
			if (0.5*(qttEntrant + this.getQuantiteEnStock(produit, this.cryptogramme))> 1000.){
				//détermination du nombre de contrat pour ce produit : 
				int nbContrat = 0;
				for (ExemplaireContratCadre cc : mesContratEnTantQueVendeur){
					if (((ChocolatDeMarque)cc.getProduit()).equals(produit)){
						nbContrat++;
					}
				}
				if (nbContrat<5){
					journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_PURPLE, "Recherche d'un acheteur aupres de qui vendre LimDt " + produit);

					List<IAcheteurContratCadre> acheteurs = supCCadre.getAcheteurs(produit);
					if (acheteurs.contains(this)) {
						acheteurs.remove(this);
					}

					if (acheteurs.size()==0) {
						journalCC.ajouter(Color.pink, Romu.COLOR_PURPLE, "-->Pas d'acheteur potentiel");
						journalCC.ajouter("\n");
					} else {
						journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_PURPLE, "Voici les acheteurs potentiels pour LimDt : " + acheteurs);
						journalCC.ajouter("\n");
					}

					for(IAcheteurContratCadre acheteur : acheteurs){
						if (acheteur!=null) {
							journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_PURPLE, "Demande au superviseur de debuter les negociations pour un contrat cadre de "+produit+" avec l'acheteur "+acheteur);

							if (0.5*qttEntrant > 100.){

								ExemplaireContratCadre cc = supCCadre.demandeVendeur(acheteur, (IVendeurContratCadre)this, produit, new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 25, 0.5*qttEntrant/(acheteurs.size())), cryptogramme, false);
								
								if (cc!=null) {
									journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_PURPLE, "-->aboutit au contrat "+cc);
									journalCC.ajouter("\n");
									this.mesContratEnTantQueVendeur.add(cc);
								}

								else {
									journalCC.ajouter(Color.pink, Romu.COLOR_PURPLE, "-->Le contrat n'a pas pu aboutir");
									journalCC.ajouter("\n");
								}
							}
							
						}
					}
				}

				//Si on n'a pas suffisamment de contrat, on va initier un contrat à forte quantité car on n'a pas de contrat en tant que vendeur
				else {
					journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_PURPLE, "Recherche d'un acheteur aupres de qui vendre LimDt " + produit);

					List<IAcheteurContratCadre> acheteurs = supCCadre.getAcheteurs(produit);
					if (acheteurs.contains(this)) {
						acheteurs.remove(this);
					}

					if (acheteurs.size()==0) {
						journalCC.ajouter(Color.pink, Romu.COLOR_PURPLE, "-->Pas d'acheteur potentiel");
						journalCC.ajouter("\n");
					} else {
						journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_PURPLE, "Voici les acheteurs potentiels pour LimDt : " + acheteurs);
						journalCC.ajouter("\n");
					}

					for(IAcheteurContratCadre acheteur : acheteurs){
						if (acheteur!=null) {
							journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_PURPLE, "Demande au superviseur de debuter les negociations pour un contrat cadre de "+produit+" avec l'acheteur "+acheteur);

							if (0.5*qttEntrant > 100.){

								ExemplaireContratCadre cc = supCCadre.demandeVendeur(acheteur, (IVendeurContratCadre)this, produit, new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 25, 0.5*this.getQuantiteEnStock(produit, this.cryptogramme)/(acheteurs.size())), cryptogramme, false);
								
								if (cc!=null) {
									journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_PURPLE, "-->aboutit au contrat "+cc);
									journalCC.ajouter("\n");
									this.mesContratEnTantQueVendeur.add(cc);
								}

								else {
									journalCC.ajouter(Color.pink, Romu.COLOR_PURPLE, "-->Le contrat n'a pas pu aboutir");
									journalCC.ajouter("\n");
								}
							}
							
						}
					}


				}
			}
		}




		//Recherche d'acheteurs de chocolat non marqué
		for(Chocolat produit : lesChocolats){
			journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_GREEN, "Recherche d'un acheteur aupres de qui vendre " + produit);

			List<IAcheteurContratCadre> acheteurs = supCCadre.getAcheteurs(produit);
			if (acheteurs.contains(this)) {
				acheteurs.remove(this);
			}

			if (acheteurs.size()==0) {
				journalCC.ajouter(Color.pink, Romu.COLOR_GREEN, "-->Pas d'acheteur potentiel");
				journalCC.ajouter("\n");
			} else {
			       journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_GREEN, "Voici les acheteurs potentiels : " + acheteurs);
			       journalCC.ajouter("\n");
			}


			for(IAcheteurContratCadre acheteur : acheteurs){
				if (acheteur!=null) {
					journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_GREEN, "Demande au superviseur de debuter les negociations pour un contrat cadre de "+produit+" avec l'acheteur "+acheteur);
					ExemplaireContratCadre cc = supCCadre.demandeVendeur(acheteur, (IVendeurContratCadre)this, produit, new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 10, (SuperviseurVentesContratCadre.QUANTITE_MIN_ECHEANCIER+10.0)/10), cryptogramme,false);
					if (cc!=null) {
						this.mesContratEnTantQueVendeur.add(cc);
					    journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_GREEN, "-->aboutit au contrat "+cc);
					    journalCC.ajouter("\n");
					}
					else {
					    journalCC.ajouter(Color.pink, Romu.COLOR_GREEN, "-->Le contrat n'a pas pu aboutir");
					    journalCC.ajouter("\n");
					}
				}
			}
		}



		//Affichage dans les journaux des contrats cadres actifs
		journalCC.ajouter("Voici nos contrats actifs en tant qu'acheteur : ");
		journalCC.ajouter("");
		for (ExemplaireContratCadre cc : mesContratEnTantQuAcheteur){
			journalCC.ajouter("Acheteur : " + cc.getAcheteur() );
			journalCC.ajouter("Vendeur : " + cc.getVendeur() );
			journalCC.ajouter("Produit : " + cc.getProduit() );
			journalCC.ajouter("Prix : " + cc.getPrix() + "\n");
			journalCC.ajouter("Echeancier : " + cc.getEcheancier() );
			journalCC.ajouter("");
		}

		//Affichage des contrats vendeurs 
		journalCC.ajouter("Voici nos contrats actifs en tant que vendeur : ");
		journalCC.ajouter("");
		for (ExemplaireContratCadre cc : this.mesContratEnTantQueVendeur){
			journalCC.ajouter("Acheteur : " + cc.getAcheteur() );
			journalCC.ajouter("Vendeur : " + cc.getVendeur() );
			journalCC.ajouter("Produit : " + cc.getProduit() );
			journalCC.ajouter("Prix : " + cc.getPrix() );
			journalCC.ajouter("Echeancier : " + cc.getEcheancier() );
			journalCC.ajouter("");

		}
	}






	public void receptionner(IProduit produit, double quantiteEnTonnes, ExemplaireContratCadre contrat) {
		ajouterAuStock(produit, quantiteEnTonnes, this.cryptogramme);
		
        journalStock.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_BROWN, "Achat CC :");
		journalStock.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_BROWN, "Reception de " + quantiteEnTonnes + "de "+ produit + "(CC avec" + contrat.getVendeur() + ")");
		journalStock.ajouter("\n");
	}







	public boolean achete(IProduit produit) {
		//On n'achète que les fèves nous permettant de produire les chocolats que l'on veut produire
		return lesFeves.contains(produit);
	}




	public String toString() {
		return this.getNom();
	}




	public int fixerPourcentageRSE(IAcheteurContratCadre acheteur, IVendeurContratCadre vendeur, IProduit produit,
			Echeancier echeancier, long cryptogramme, boolean tg) {
		return 5;
	}


}
