//Simon
package abstraction.eq5Transformateur2;

import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;
import java.util.ArrayList;
import java.util.List;

class ContratCadreAcheteur extends ContratCadreVendeur implements IAcheteurContratCadre{
    public ContratCadreAcheteur() {
        super();
    }
    /**
	 * Methode appelee par le superviseur afin de savoir si l'acheteur est pret a
	 * faire un contrat cadre sur le produit indique.
	 * 
	 * @param produit
	 * @return Retourne false si l'acheteur ne souhaite pas etablir de contrat a
	 *         cette etape pour ce type de produit (retourne true si il est pret a
	 *         negocier un contrat cadre pour ce type de produit).
	 */
	public boolean achete(IProduit produit){
		if (produit == Feve.F_MQ){
			this.journalContrat.ajouter("On cherche " + produit);
			return true;
		}
		if (produit == Feve.F_MQ_E){
			this.journalContrat.ajouter("on cherche" + produit);
			return true;
		}
		if (produit == Feve.F_HQ_E){
			this.journalContrat.ajouter("On cherche" + produit);
			return true;
		}
		if (produit == Feve.F_HQ_BE){
			this.journalContrat.ajouter("On Cherhce" + produit);
			return true;
		}

        return false;

	}

	/**
	 * Methode appelee par le SuperviseurVentesContratCadre lors des negociations
	 * sur l'echeancier afin de connaitre la contreproposition de l'acheteur. Les
	 * precedentes propositions d'echeancier peuvent etre consultees via un appel a
	 * la methode getEcheanciers() sur le contrat passe en parametre.
	 * 
	 * @param contrat. Notamment, getEcheancier() appelee sur le contrat retourne
	 *                 l'echeancier que le vendeur vient de proposer.
	 * @return Retoune null si l'acheteur souhaite mettre fin aux negociation (et
	 *         abandonner du coup ce contrat). Retourne le meme echeancier que celui
	 *         du contrat (contrat.getEcheancier()) si l'acheteur est d'accord pour
	 *         un tel echeancier. Sinon, retourne un nouvel echeancier que le
	 *         superviseur soumettra au vendeur.
	 */
	public Echeancier contrePropositionDeLAcheteur(ExemplaireContratCadre contrat){
		Echeancier original = contrat.getEcheancier();
		int stepDebut = original.getStepDebut();
		int nbEcheances = original.getNbEcheances();
		double quantiteTotale = original.getQuantiteTotale();

		Feve f = (Feve) contrat.getProduit();
		double prodTotale = this.getProductionTotale()/10000; // en tonnes
		double proportion = this.getProportion(f);

		double quantiteMin = prodTotale * proportion * 0.1;
		double quantiteMax = prodTotale * proportion * 1.1;

		// Si la quantité demandée est dans la plage acceptable, on accepte tel quel
		if (quantiteTotale >= quantiteMin/1000 && quantiteTotale <= quantiteMax*1000) {
			return original;
		}

		// Sinon on fait une contre-proposition sur la limite haute ou basse
		double nouvelleQuantite = Math.max(quantiteMin, Math.min(quantiteMax, quantiteTotale));
		double quantiteParStep = nouvelleQuantite / nbEcheances;

		List<Double> quantites = new ArrayList<>();
		for (int i = 0; i < nbEcheances; i++) {
			quantites.add(quantiteParStep);
		}

		// Création d’un nouvel échéancier
		Echeancier nouveau = new Echeancier(stepDebut, quantites);

		return nouveau;


		
		
    }
	
	/**
	 * Methode appelee par le SuperviseurVentesContratCadre lors des negociations
	 * sur le prix a la tonne afin de connaitre la contreproposition de l'acheteur.
	 * L'acheteur peut consulter les precedentes propositions via un appel a la
	 * methode getListePrix() sur le contrat. En particulier la methode getPrix()
	 * appelee sur contrat indique la derniere proposition faite par le vendeur.
	 * 
	 * @param contrat
	 * @return Retourne un prix negatif ou nul si l'acheteur souhaite mettre fin aux
	 *         negociations (en renoncant a ce contrat). Retourne le prix actuel
	 *         (contrat.getPrix()) si il est d'accord avec ce prix. Sinon, retourne
	 *         un autre prix correspondant a sa contreproposition.
	 */
	public double contrePropositionPrixAcheteur(ExemplaireContratCadre contrat){
        Double prixVendeur= contrat.getPrix();
		Feve f = (Feve) contrat.getProduit();
		if (prixVendeur <= 5000){  
			return prixVendeur;
		}
		else{
			return (5000*0.8 +0.2*prixVendeur);
		}
    }

	/**
	 * Methode appelee par le SuperviseurVentesContratCadre afin de notifier le
	 * l'acheteur de la reussite des negociations sur le contrat precise en
	 * parametre qui a ete initie par le vendeur. Le superviseur veillera a
	 * l'application de ce contrat (des appels a livrer(...) seront effectues
	 * lorsque le vendeur devra livrer afin d'honorer le contrat, et des transferts
	 * d'argent auront lieur lorsque l'acheteur paiera les echeances prevues)..
	 * 
	 * @param contrat
	 */
	public void notificationNouveauContratCadre(ExemplaireContratCadre contrat){
		super.journalContrat.ajouter("nouveau contrat cadre signé"+contrat.toString());
    
    }

	/**
	 * Methode appelee par le SuperviseurVentesContratCadre afin de notifier
	 * l'acheteur de la livraison du lot de produit precise en parametre
	 * (dans le cadre du contrat contrat). Il se peut que la quantitee livree
	 * soit inferieure a la quantite prevue par le contrat si le vendeur est dans 
	 * l'incapacite de la fournir. Dans ce cas, le vendeur aura une penalite 
	 * (un pourcentage de produit a livrer en plus). L'acheteur doit a minima 
	 * mettre ce produit dans son stock.
	 */
	public void receptionner(IProduit p, double quantiteEnTonnes, ExemplaireContratCadre contrat){
        this.ajouterStock(this, p, quantiteEnTonnes, super.cryptogramme);
		this.journalContrat.ajouter("ajout de " + quantiteEnTonnes + " tonnes de " + p + " à notre stock grâce à un contrat cadre");
			
    }

}


