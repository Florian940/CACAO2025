package abstraction.eq4Transformateur1.contratCadre;


import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import abstraction.eqXRomu.acteurs.Romu;
import abstraction.eqXRomu.contratsCadres.*;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.*;


/*
 * @author MURY Julien
 * Cette calsse, héritant de Transformateur1ContratCadre, décrit le comportement de notre acteur lors de la creation d'un contrat cadre en tant que venndeur
 */

public class Transformateur1ContratCadreVendeur extends TransformateurContratCadre implements IVendeurContratCadre {

	
	protected double partInitialementVoulue;	
	protected double prixInitialementVoulu;
	protected double epsilon;
	
	public Transformateur1ContratCadreVendeur() {

		super();
		this.mesContratEnTantQueVendeur=new LinkedList<ExemplaireContratCadre>();

		this.partInitialementVoulue = 0.3; //A MODIFIER On cherche initialement à vendre 30% du stock du produit dont il est question
		this.epsilon = 0.1;  //A MODIFIER Pourcentage d'erreur entre la quantite voulue et celle du contrat actuel

	}








	//A MODIFIER
	//La stratégie de négociation doit être différenciée selon le produit mais pour la quantité, cela est probablement peu pertinent
	public Echeancier contrePropositionDuVendeur(ExemplaireContratCadre contrat) {

		Feve feveAssociee = Feve.F_BQ_E;

			switch(((ChocolatDeMarque)contrat.getProduit()).getChocolat()){
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
					System.out.println("Ce chocolat ne devrait pas faire partie de la gamme : " + contrat.getProduit());
					break;
			}


		double qttEntrant = qttEntrantesFeve.get(feveAssociee);
		List<IAcheteurContratCadre> acheteurs = supCCadre.getAcheteurs(contrat.getProduit());

		//A MODIFIER 
		//On cherche à vendre une partie de la quantité de chocolat correspondant à la qtt de fèves entrantes
		double qttVoulue = (0.5*qttEntrant/(acheteurs.size()) * contrat.getEcheancier().getNbEcheances() + contrat.getEcheancier().getQuantiteTotale())/2;

		Chocolat chocoVendu = ((ChocolatDeMarque)contrat.getProduit()).getChocolat();
		

		qttEntrant = 0.;

		//A MODIFIER	
		//On vérifie que l'échéancier renvoyé respecte les règles et que la quantité en stock de produit est au moins le quart de la quantité totale
		//Il faudrait dans l'idéal modifier cette condition pour prendre en compte la quantité de chocolat sortante et la quantité produite par step
		if (qttVoulue>= SuperviseurVentesContratCadre.QUANTITE_MIN_ECHEANCIER && 0.1*this.getQuantiteEnStock(contrat.getProduit(), this.cryptogramme) >= contrat.getEcheancier().getQuantiteTotale()/contrat.getEcheancier().getNbEcheances()){

			IProduit produit;
			
			//On vend des chocolat de marque
			if (contrat.getProduit().getType() == "ChocolatDeMarque"){
				produit = ((ChocolatDeMarque)contrat.getProduit());

				if (!this.peutVendre(produit)) {
					return null;
				} //On ne vend pas de ce produit

				Echeancier e = contrat.getEcheancier(); //Récupération de l'échéancier actuel

				//Cas d'acceptation : la quantité totale est légale et proche de la quantité que l'on souhaite vendre à 20% près
				if(e.getNbEcheances()> 8 && e.getQuantiteTotale()> 100. &&( Math.abs(e.getQuantiteTotale()-qttVoulue)/qttVoulue <= 0.2 || Math.abs(e.getQuantiteTotale()-qttVoulue)/e.getQuantiteTotale() <= 0.2)){

					return e;
				}
				//On modifie l'échéancier uniformément pour se rapporcher de nos exigeances
				else{
					for(int s = e.getStepDebut() ; s<=e.getStepFin() ; s++){


						ChocolatDeMarque prod = (ChocolatDeMarque)contrat.getProduit();

						//Détermination de la quantité entrante de chocolat que l'on ne va pas vendre au step s
						if(chocoVendu.getGamme().equals(Gamme.BQ) && chocoVendu.isEquitable()){
								qttEntrant = 0.4*Math.min(determinerQttEntrantFevesAuStep(s, Feve.F_BQ_E) * this.pourcentageTransfo.get(Feve.F_BQ_E).get(Chocolat.C_BQ_E), this.prodMax.getValeur() * this.repartitionTransfo.get(Chocolat.C_BQ_E).getValeur());
								qttEntrant += - determinerQttSortantChocoAuStep(s, prod) ;
						}

						else if(chocoVendu.getGamme().equals(Gamme.MQ) && chocoVendu.isEquitable()) {
							qttEntrant = 0.4*Math.min(determinerQttEntrantFevesAuStep(s, Feve.F_MQ_E) * this.pourcentageTransfo.get(Feve.F_MQ_E).get(Chocolat.C_MQ_E), this.prodMax.getValeur() * this.repartitionTransfo.get(Chocolat.C_MQ_E).getValeur());
							qttEntrant += - determinerQttSortantChocoAuStep(s, prod);
						}
						else if(chocoVendu.getGamme().equals(Gamme.MQ) && !chocoVendu.isEquitable()){
							qttEntrant = 0.4*Math.min(determinerQttEntrantFevesAuStep(s, Feve.F_MQ_E) * this.pourcentageTransfo.get(Feve.F_MQ).get(Chocolat.C_MQ), this.prodMax.getValeur() * this.repartitionTransfo.get(Chocolat.C_MQ).getValeur());
							qttEntrant += - determinerQttSortantChocoAuStep(s, prod) ;
						}
						else if(chocoVendu.getGamme().equals(Gamme.HQ) && chocoVendu.isEquitable() && chocoVendu.isBio()){
							qttEntrant = 0.4*Math.min(determinerQttEntrantFevesAuStep(s, Feve.F_HQ_BE) * this.pourcentageTransfo.get(Feve.F_HQ_BE).get(Chocolat.C_HQ_BE), this.prodMax.getValeur() * this.repartitionTransfo.get(Chocolat.C_HQ_BE).getValeur());
							qttEntrant += - determinerQttSortantChocoAuStep(s, prod) ;
						}
						else{
							System.out.println("Ce chocolat n'est pas censé être vendu : " + prod);
						}
							
						
						//Selon la valeur de qttEntrant, on va agir différemment sur le contrat		
						//si qtt entrant est très négatif, c'est que l'on vend plus de chocolat que l'on ne recoit de fèves, on annule donc le contrat
						if (0.2*qttEntrant < -10000.){
							return null;
						}
						//Sinon, si on a suffisamment de fève qui entrent en stock à chaque step et que la proposition est proche de ce que l'on souhaite, on accepte la proposition sur ce step
						else if (Math.abs(0.2*qttEntrant - e.getQuantite(s))/(0.2*qttEntrant) < 0.1 ){
							e.set(s, e.getQuantite(s));
						}
						//si la qtt entrante est faible, on vérifie quand meme que celle ci respecte les spécifications sur les CC
						else if (0.2*Math.abs(qttEntrant)< 1000. && 0.2*qttEntrant > e.getQuantiteTotale()/(10*e.getNbEcheances())){
							e.set(s, 0.2*Math.abs(qttEntrant));
						}
						//Sinon, on met le double du minimum pour le step
						else {
							e.set(s, e.getQuantiteTotale()/(5*e.getNbEcheances()));

						}
						
					}

					//On vérifie que notre contrat respecte bien les règles des contrats cadres par rapport aux quantité minimale par step

					//Si l'une des échéances est trop faible, on modifie tout l'échéancier
					boolean modifNecessaires = false;
					for (int s = e.getStepDebut() ; s <= e.getStepFin() ; s++){
						if (e.getQuantite(s)<= e.getQuantiteTotale()/(10*e.getNbEcheances())) modifNecessaires = true;
					}

					double qttMax = 0.;
					double qttMin = e.getQuantite(e.getStepDebut()+1);
					for(int s = e.getStepDebut() ; s <= e.getStepFin() ; s++){
						if (e.getQuantite(s) > qttMax) qttMax = e.getQuantite(s);
						if (e.getQuantite(s)< qttMin) qttMin = e.getQuantite(s);
					}

					if (modifNecessaires){
						for (int s = e.getStepDebut() ; s <= e.getStepFin() ; s++){
							e.set(s, (qttMin + qttMax)/2);
						}
					}


					//Si la quantité totale est trop faible, on va se mettre au minimum sur tout le contrat 
					if (e.getQuantiteTotale() < 100.){
						for (int s = e.getStepDebut() ; s <= e.getStepFin() ; s++){
							e.set(s, 1000./e.getNbEcheances());
						}
					}

					

					/*Renvoie de l'échéancier modifié */
					

					return e;
				}

				
			}

			//Vente d'un chocolat non marqué
			else {
				produit = contrat.getProduit();

				if (!this.peutVendre(produit)) return null; //On ne vend pas de ce produit

				Echeancier e = contrat.getEcheancier(); //Récupération de l'échéancier actuel


				//Cas d'acceptation : la quantité totale est légale et proche de la quantité que l'on souhaite vendre à 10% près
				if(e.getQuantiteTotale()> 100. && Math.abs(e.getQuantiteTotale()-qttVoulue)/qttVoulue < 0.1){
					return e;
				}
				//On modifie l'échéancier uniformément pour se rapporcher de nos exigeances
				else{
					for(int s = e.getStepDebut() ; s<e.getStepFin() ; s++){
						double qttActuelle = e.getQuantite(s);
						e.set(s, qttVoulue +  (qttActuelle - qttVoulue)/16);
					}

					//On vérifie que notre contrat respecte bien les règles des contrats cadres par rapport aux quantité minimale par step
					for(int s = e.getStepDebut() ; s<e.getStepFin() ; s++){
						if (e.getQuantite(s) < e.getQuantiteTotale()/(10*e.getNbEcheances())){
							e.set(s, e.getQuantite(s) +  e.getQuantiteTotale()/(10*e.getNbEcheances()));
						}
					}

					return e;
				}
			}
		}
		return null ; //On annule les négociations si le nouveau contrat a une quantité illégale
	}
	







	public double propositionPrix(ExemplaireContratCadre contrat) {
		double prixBase = 6000.;
		if (contrat.getProduit().getType() ==  "ChocolatDeMarque"){
			Chocolat prod = ((ChocolatDeMarque)contrat.getProduit()).getChocolat();
			if (prixTChocoBase.get(prod) ==  null){
				return 6000;
			}
			else{
				switch (prod){
					case C_BQ_E : 
						prixBase = prix_Limdt_BQ_E.getValeur();
						break;
					case C_MQ : 
						prixBase = prix_Limdt_MQ.getValeur();
						break;
					case C_MQ_E : 
						prixBase = prix_Limdt_MQ_E.getValeur();
						break;
					case C_HQ_BE : 
						prixBase = prix_Limdt_HQ_BE.getValeur();

				}
				if(contrat.getQuantiteTotale() < 2000){
					return prixBase*(1 - 0.05*contrat.getQuantiteTotale()/2000);
				}
				return 0.95*prixBase;// plus la quantite est elevee, plus le prix est interessant
			}
		}


		else {
			IProduit prod = contrat.getProduit();
			if (prixTChocoBase.get(prod) ==  null){
				return 6000;
			}
			else{
				prixBase = prixTChocoBase.get(prod)*marges.get(prod);
				if(contrat.getQuantiteTotale() < 2000){
					return prixBase*(1 - 0.05*contrat.getQuantiteTotale()/2000);
				}
				return 0.95*prixBase;// plus la quantite est elevee, plus le prix est interessant
			}
		}
	}








	public double contrePropositionPrixVendeur(ExemplaireContratCadre contrat) {
		IProduit produit = contrat.getProduit();

		//Si le produit vendu est un chocolat MQ, on négocie de manière à avoir de très grandes marges
		if (produit.equals(Chocolat.C_MQ) || (produit.getType()=="ChocolatDeMarque" && ((ChocolatDeMarque)produit).getChocolat().equals(Chocolat.C_MQ))){
			//Si le prix proposé est plus élevé que celui que l'on a calculé, on accepte le contrat

			if (contrat.getPrix() > prix_Limdt_MQ.getValeur()* marges.get(Chocolat.C_MQ)){
				return contrat.getPrix();
			}
			//Si le prix est trop faible, on reste sur le prix minimum auquel on veut vendre
			if (contrat.getPrix()< prix_Limdt_MQ.getValeur()/marges.get(Chocolat.C_MQ)){
				return prix_Limdt_MQ.getValeur()/marges.get(Chocolat.C_MQ);

			}

			//Si le prix du contrat est à un epsilon près de notre prix, on accepte

			double notrePrix = prix_Limdt_MQ.getValeur();

			double diffRelative = Math.abs(contrat.getPrix()- notrePrix)/notrePrix;
			if (diffRelative<epsilon){
				return contrat.getPrix();
			}
			//Sinon on cherche à négocier le prix, vers le bas par dichotomie en notre faveur à 90%
			else{
				double nouveauPrix = contrat.getPrix() * 0.2 + notrePrix * 0.8;

				if (nouveauPrix <0.75* prix_Limdt_MQ.getValeur()/marges.get(Chocolat.C_MQ)){
					return 0.75*prix_Limdt_MQ.getValeur()/marges.get(Chocolat.C_MQ);

				}
				else{
					notrePrix = nouveauPrix;
					return notrePrix;
				}
			}
		}

		//Si le produit vendu est BQ_E, on cherche a des marges réduites ce qui réduit le champ des négociations
		if (produit.equals(Chocolat.C_BQ_E) || (produit.getType()=="ChocolatDeMarque" && ((ChocolatDeMarque)produit).getChocolat().equals(Chocolat.C_BQ_E))){
			//Si le prix proposé est plus élevé que celui que l'on a calculé, on vérifie que le prix n'est pas trop élevé non plus pour maitriser un minimum le prix de vente final
			if (contrat.getPrix() > prix_Limdt_BQ_E.getValeur()){
				return contrat.getPrix();
			}
			//Si le prix est trop faible, on reste sur le prix minimum auquel on veut vendre
			if (contrat.getPrix()< 0.75*prix_Limdt_BQ_E.getValeur()/marges.get(Chocolat.C_BQ_E)){
				return 0.75*prix_Limdt_BQ_E.getValeur()/marges.get(Chocolat.C_BQ_E);
			}
			//Si le prix du contrat est à un epsilon près de notre prix, on accepte
			double notrePrix = prix_Limdt_BQ_E.getValeur();
			double diffRelative = Math.abs(contrat.getPrix()- notrePrix)/notrePrix;
			if (diffRelative<epsilon){
				return contrat.getPrix();
			}
			//Sinon on cherche à négocier le prix, vers le bas par dichotomie en notre faveur à 90%
			else{
				double nouveauPrix = contrat.getPrix() * 0.2 + notrePrix * 0.8;
				if (nouveauPrix < 0.75*prix_Limdt_BQ_E.getValeur()/marges.get(Chocolat.C_BQ_E)){
					return 0.75*prix_Limdt_BQ_E.getValeur()/marges.get(Chocolat.C_BQ_E);
				}
				else{
					notrePrix = nouveauPrix;
					return notrePrix;
				}
			}
		}


		//Idem, si le produit est de moyenne gamme équitable, on veut avoir des marges faibles et controler le prix de vente sans pour autant vendre à perte
		if (produit.equals(Chocolat.C_MQ_E) || (produit.getType()=="ChocolatDeMarque" && ((ChocolatDeMarque)produit).getChocolat().equals(Chocolat.C_MQ_E))){
			//Si le prix proposé est plus élevé que celui que l'on a calculé, on accepte le contrat
			if (contrat.getPrix() > prix_Limdt_MQ_E.getValeur()){
				return contrat.getPrix();
			}
			//Si le prix est trop faible, on reste sur le prix minimum auquel on veut vendre
			if (contrat.getPrix()<0.75* prix_Limdt_MQ_E.getValeur()/marges.get(Chocolat.C_MQ_E)){
				return 0.75*prix_Limdt_MQ_E.getValeur()/marges.get(Chocolat.C_MQ_E);
			}
			//Si le prix du contrat est à un epsilon près de notre prix, on accepte
			double notrePrix = prix_Limdt_MQ_E.getValeur();
			double diffRelative = Math.abs(contrat.getPrix()- notrePrix)/notrePrix;
			if (diffRelative<epsilon){
				return contrat.getPrix();
			}
			//Sinon on cherche à négocier le prix, vers le bas par dichotomie en notre faveur à 90%
			else{
				double nouveauPrix = contrat.getPrix() * 0.2 + notrePrix * 0.8;
				if (nouveauPrix < 0.75*prix_Limdt_MQ_E.getValeur()/marges.get(Chocolat.C_MQ_E)){
					return 0.75*prix_Limdt_MQ_E.getValeur()/marges.get(Chocolat.C_MQ_E);
				}
				else{
					notrePrix = nouveauPrix;
					return notrePrix;
				}
			}
		}


		//Si le produit vendu est HQ_BE, on peut se permettre de prendre des marges plus grandes car un acheteur de haut de gamme sera peu regardant sur le prix, cherchant principalement à se faire plaisir
		if (produit.equals(Chocolat.C_HQ_BE) || (produit.getType()=="ChocolatDeMarque" && ((ChocolatDeMarque)produit).getChocolat().equals(Chocolat.C_HQ_BE))){
			//Si le prix proposé est plus élevé que celui que l'on a calculé, on accepte le contrat
			if (contrat.getPrix() > prix_Limdt_HQ_BE.getValeur()){
				return contrat.getPrix();
			}
			//Si le prix est trop faible, on reste sur le prix minimum auquel on veut vendre
			if (contrat.getPrix()< 0.9*prix_Limdt_HQ_BE.getValeur()/marges.get(Chocolat.C_HQ_BE)){
				return 0.9*prix_Limdt_HQ_BE.getValeur()/marges.get(Chocolat.C_HQ_BE);
			}
			//Si le prix du contrat est à un epsilon près de notre prix, on accepte
			double notrePrix = prix_Limdt_HQ_BE.getValeur();
			double diffRelative = Math.abs(contrat.getPrix()- notrePrix)/notrePrix;
			if (diffRelative<epsilon){
				return contrat.getPrix();
			}
			//Sinon on cherche à négocier le prix, vers le bas par dichotomie en notre faveur à 90%
			else{
				double nouveauPrix = contrat.getPrix() * 0.2 + notrePrix * 0.8;
				if (nouveauPrix < 0.9*prix_Limdt_HQ_BE.getValeur()/marges.get(Chocolat.C_HQ_BE)){
					return 0.9*prix_Limdt_HQ_BE.getValeur()/marges.get(Chocolat.C_HQ_BE);
				}
				else{
					notrePrix = nouveauPrix;
					return notrePrix;
				}
			}
		}

		return -1;
	}






	public void notificationNouveauContratCadre(ExemplaireContratCadre contrat) {
		if(contrat.getAcheteur() == this){
			this.mesContratEnTantQuAcheteur.add(contrat);

			this.journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_BROWN, "Achat: Nouveau contrat cadre obtenu en tant qu'acheteur :");
		    this.journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_BROWN, "Vendeur : " + contrat.getVendeur());
		    this.journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_BROWN, "Produit :  " + contrat.getProduit());
		    this.journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_BROWN, "Echeancier : " + contrat.getEcheancier());
			this.journalCC.ajouter("\n");
		}
		else{
			this.mesContratEnTantQueVendeur.add(contrat);

			this.journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_GREEN, "Vente: Nouveau contrat cadre obtenu en tant que vendeur :");
		    this.journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_GREEN, "Acheteur : " + contrat.getAcheteur());
		    this.journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_GREEN,"Produit :  " + contrat.getProduit());
		    this.journalCC.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_GREEN, "Echeancier : " + contrat.getEcheancier());
			this.journalCC.ajouter("\n");
		}
	}
	








	public void next() {

		super.next();

		List<ExemplaireContratCadre> contratsObsoletes=new LinkedList<ExemplaireContratCadre>();
		for (ExemplaireContratCadre contrat : this.mesContratEnTantQueVendeur) {
			if (contrat.getQuantiteRestantALivrer()==0.0 && contrat.getMontantRestantARegler()==0.0) {
				contratsObsoletes.add(contrat);
			}
		}
		this.mesContratEnTantQueVendeur.removeAll(contratsObsoletes);


		
	}








	//Dans cette méthode, on vérifie qu'il existe des équipes auprès de qui s'approvisionner en feve pour pouvoir vendre des chocolats
	public boolean vend(IProduit produit) {
		if (produit.getType() == "Chocolat"){
			return lesChocolats.contains(produit);
		}
		else if (produit.getType() == "ChocolatDeMarque"){

			return ((ChocolatDeMarque)produit).getMarque() == "LimDt" && chocolatsLimDt.contains(produit) ;
		}
		else{
			return false;
		}
	}










	public double livrer(IProduit produit, double quantite, ExemplaireContratCadre contrat) {
		

			if (produit.getType() == "ChocolatDeMarque"){

				double livre = Math.min(Math.max(this.getQuantiteEnStock(produit, this.cryptogramme), 0.), quantite);

				if (livre > 0.){

					//Retrait du produit concerné par le contrat
					this.retirerDuStock(produit, quantite, this.cryptogramme);


				}
				this.journalStock.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_PURPLE, "Vente CC LimDt :");
				this.journalStock.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_PURPLE, "Retrait de " + livre + "T " + contrat.getProduit() + "(CC avec "+ contrat.getAcheteur() + ")");
				this.journalStock.ajouter("\n");
			
				return livre;
			}
			else{
				double livre = Math.min(getQuantiteEnStock(produit, this.cryptogramme), quantite);
				if (livre>0.0) {
					//AFFICHAGE EN CONSOLE
					journalStock.ajouter(Color.pink, Romu.COLOR_PURPLE, "Le chocolat " + produit + " n'est pas censé sortir du stock, il est de type " + produit.getType());
					
					this.retirerDuStock(produit, quantite, this.cryptogramme);

				}
				this.journalStock.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_GREEN, "Vente CC :");
				this.journalStock.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_GREEN, "Retrait de " + livre + "T " + contrat.getProduit() + "(CC avec "+ contrat.getAcheteur() + ")");
				this.journalStock.ajouter("\n");
			
				return livre;
			}
			
	}








	
	public boolean peutVendre(IProduit produit) {
		//On vérifie que 30% de notre stock est supérieur à 100T
		return this.getQuantiteEnStock(produit, this.cryptogramme) * partInitialementVoulue > 100;

	}






	public String toString() {
		return this.getNom();
	}

}
