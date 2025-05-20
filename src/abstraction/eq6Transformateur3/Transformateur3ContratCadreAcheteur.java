package abstraction.eq6Transformateur3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import abstraction.eqXRomu.filiere.*;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.bourseCacao.BourseCacao;
import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre; 
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.contratsCadres.SuperviseurVentesContratCadre;
import abstraction.eqXRomu.produits.Feve;

// @author Eric Schiltz & Henri Roth & Florian Malveau

public class Transformateur3ContratCadreAcheteur extends Transformateur3ContratCadreVendeur implements IAcheteurContratCadre{
	protected List<ExemplaireContratCadre> contratsObsoletes;
    //protected HashMap<IProduit, Double> coutMoyFeves; //estimation du cout de chaque fèves
	protected HashMap<IProduit, Double> demandeAchatFeve;

	//total des réceptions à faire pour ce step
    protected double qtté_réception_bollo;
    protected double qtté_réception_fraudau;
    protected double qtté_réception_hypo;
    protected double qtté_réception_arna;
	
	public Transformateur3ContratCadreAcheteur() {
		this.ContratsAcheteur=new LinkedList<ExemplaireContratCadre>();
		this.contratsObsoletes =new LinkedList<ExemplaireContratCadre>();
		//on crée la variable/tableau demande_vente qui répertorie les demandes de la ventes 
		//en les différentes fèves pour le step d'après
		//on va commander pour satisfaire cette commande en ventes c'est notre stratégie actuelle.
		demandeAchatFeve = new HashMap<IProduit, Double>();
		//on initialise juste pour les différentes fèves on s'en fout de la valeur initiale
		demandeAchatFeve.put(Feve.F_BQ,0.0);
        demandeAchatFeve.put(Feve.F_BQ_E,0.0);
        demandeAchatFeve.put(Feve.F_MQ,0.0);
        demandeAchatFeve.put(Feve.F_HQ_E,0.0);
		this.qtté_réception_arna= 0; 
        this.qtté_réception_bollo= 0; 
        this.qtté_réception_fraudau= 0; 
        this.qtté_réception_hypo= 0;
	}
	
	//@author Henri Roth
	public Echeancier contrePropositionDeLAcheteur(ExemplaireContratCadre contrat) {
		//on regarde quel produit est concerné par le contrat
        IProduit p = contrat.getProduit();
        //on regarde si on vend/produit bien de ce chocolat
        if(super.fevesUtiles.contains(p)){
            // si oui on calcule de la quantite par step de chocolat que l'on veut livrer
            //on regarde quelle est notre demande de vente du produit 
            double vente = demandeAchatFeve.get(p);
            //on récupère l'échéancier
            Echeancier e = contrat.getEcheancier();
            //on récupère le premier step
            int stepdebut = e.getStepDebut();
            //on rearde la quantité  que l'on peut obtenir proposée
            double QuantiteStep = e.getQuantite(stepdebut);
            // si la quantité que l'on nous propose est inférieure à ce que l'on a besoin on accepte
            if(QuantiteStep < vente){
                //et on met à jour nos capacités de vente max
                return contrat.getEcheancier();
            }
            //sinon
            else{
                //si notre demande est vrmt trop petite : on abandonne 
                if(vente < 100){
                    return null;
                }
                //sinon on refait l'échéancier avec ce que l'on a; 
                else{
                    //et on met à jour nos capacités de vente max
                    int nb = e.getNbEcheances();
                    return new Echeancier(Filiere.LA_FILIERE.getEtape()+1, nb, vente);
                }
            }
        }
        //si on ne le vend/produit pas on ne fait rien 
        return null;
    }
	
	//@author Henri Roth & Florian Malveau
	public double contrePropositionPrixAcheteur(ExemplaireContratCadre contrat) {
		//journalCC.ajouter("## NEGOCIATION "+contrat.getNumero()+" - Prix proposé : "+contrat.getPrix());
		double prixBourse = 3000.0;
		List<Double> prixList = contrat.getListePrix();
		
		if(contrat.getProduit() != Feve.F_HQ_E && contrat.getProduit() != Feve.F_BQ_E){
			BourseCacao bourse = (BourseCacao) Filiere.LA_FILIERE.getActeur("BourseCacao");
			prixBourse = bourse.getCours((Feve)contrat.getProduit()).getValeur();
		}

		double prixPropose = 0.75*prixBourse;

		if(prixList.size() > 2){
			prixPropose = prixList.get(prixList.size()-2);
		}

		//jdb.ajouter("#########"+contrat.getNumero()+"######## Prix Bourse"+contrat.getProduit()+" : "+prixBourse);
		
		double prixVendeur = contrat.getPrix();
		if(prixVendeur > prixPropose+500){
			//journalCC.ajouter("## PRIX PROPOSE : "+prixPropose+" - PRIX BOURSE : "+prixBourse*1.2);
			return (prixPropose*1.5+prixVendeur*0.5)/2;
		}
		else{
			return prixVendeur;
		}
		
	}

	/*
	//@author Florian Malveau
	public void initialiser(){
		super.initialiser();
		
		//Initialisation estimation coûts de fèves
		this.coutMoyFeves = new HashMap<IProduit, Double>();
		for(IProduit feve : super.stockFeves.getListProduitSorted()){
			this.coutMoyFeves.put(feve,0.0);
		}
	}
	*/

	//@author Florian Malveau
	//gestion de la suppression des contrats obsolètes
	public void supprCCObsoletes() {
		
		journalCC.ajouter("======= Contrats cadres finis période n°"+Filiere.LA_FILIERE.getEtape()+"=======");
		// On enleve les contrats obsolete (nous pourrions vouloir les conserver pour "archive"...)
		for (ExemplaireContratCadre contrat : this.ContratsAcheteur) {
			if (contrat.getQuantiteRestantALivrer()==0.0 && contrat.getMontantRestantARegler()==0.0) {
				contratsObsoletes.add(contrat);
				journalCC.ajouter("CONTRAT n°"+contrat.getNumero()+" SUPPRIME");
			}
		}
		this.ContratsAcheteur.removeAll(contratsObsoletes);
		for (ExemplaireContratCadre contrat : this.ContratsVendeur) {
			if (contrat.getQuantiteRestantALivrer()==0.0 && contrat.getMontantRestantARegler()==0.0) {
				contratsObsoletes.add(contrat);
			}
		}
		this.ContratsVendeur.removeAll(contratsObsoletes);
		journalCC.ajouter("======================");
	}

	//@author Florian Malveau
	public void initialiserAchatFeve(){

		//On utilise les données de StratQuantity
		// Maj des infos de Strat Quantity
		super.actualiserEcheanciers();
		super.miseAJourEcheanciersBesoins();
		
		for(IProduit feve : super.fevesUtiles){
			// Quantité de fève nécessaire pour la production de chocolat
			double besoinEnFeve = super.besoinFeveEcheancier.get(feve).get(1);
			// Quantité de fève déjà contractée
			double approvisionnementFeve = super.quantityFevesEcheancier.get(feve).get(1);
			// Quantité de fève à acheter
			double quantiteAchatFeve = Math.max(0,besoinEnFeve - approvisionnementFeve);

			demandeAchatFeve.replace(feve, quantiteAchatFeve);
			//jdb.ajouter("Demande de "+feve+" : "+demandeAchatFeve.get(feve));
		}

	}

	//@author Florian Malveau
	public boolean demanderUnContratCadreAcheteur(IActeur acteur, IProduit produit, double quantite) {

        SuperviseurVentesContratCadre supCCadre = (SuperviseurVentesContratCadre) Filiere.LA_FILIERE.getActeur("Sup.CCadre");
		double a = Filiere.random.nextDouble();
        int b = (int) a;
		ExemplaireContratCadre contrat = supCCadre.demandeAcheteur((IAcheteurContratCadre)this, ((IVendeurContratCadre)acteur), produit, new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 10+(b*5), quantite), cryptogramme, false);
		// Si un contrat a été créé, on l'ajoute à la liste des contrats du vendeur
        if(contrat != null){
            notificationNouveauContratCadre(contrat);
            //jdb.ajouter("Nouveau contrat cadre Acheteur" +contrat.getProduit());
            //Mettre à jour la capacité de vente max
            initialiserAchatFeve();
            return true;
        }
        return false;
    }


	public void next() {
		super.next();

		super.jdb.ajouter("NEXT - CONTRAT CADRE");
		super.journalCC.ajouter("");
        super.journalCC.ajouter("NEXT - CONTRAT CADRE");

		//on affiche la quantité livrée durant le step jusque là 
        journalCC.ajouter("quantité réceptionnée à ce step en arna en CC"+qtté_réception_arna);
        journalCC.ajouter("quantité réceptionnée à ce step en bollo en CC"+qtté_réception_bollo);
        journalCC.ajouter("quantité réceptionnée à ce step en fraudau en CC"+qtté_réception_fraudau);
        journalCC.ajouter("quantité réceptionnée à ce step en hypo en CC"+qtté_réception_hypo);
        //on remet la quantité livrée à chaque step à 0 au début de chaque step
        this.qtté_réception_arna= 0; 
        this.qtté_réception_bollo= 0; 
        this.qtté_réception_fraudau= 0; 
        this.qtté_réception_hypo= 0;

		//on supprime les contrats obsolètes
		supprCCObsoletes();

		//on initialise la demande d'achat de fèves
		initialiserAchatFeve();

		// @author Eric Schiltz
		//on parcourt toutes les fèves
		
		/*
		demande_vente.put(Feve.F_BQ,(productionMax*0.9)/3);
        demande_vente.put(Feve.F_BQ_E,(productionMax*0.9)/6);
        demande_vente.put(Feve.F_MQ,(productionMax*0.9)/6);
        demande_vente.put(Feve.F_HQ_E,(productionMax*0.9)/3);
		for(IProduit feve : super.fevesUtiles){
			//on parcourt tous les chocolats et on va regarder pour tous les chocolats que l'on vend
			//et pour chaque on va noter combien de notre fève il faut produire pour remplir la 
			//demande
			//quantité de la fève que l'on doit produire
			double b = 0; 
			for(IProduit choco : super.lesChocolats){
				//somme pour ce chocolat de ce que l'on doit pour l'instant livrer au step suivant
				double a = 0;
				//on parcourt tous les contrats cadres
				for (ExemplaireContratCadre contrat : ContratsVendeur){
					//pour chaque contrat on regarde si il concerne ce produit 
					if (contrat.getProduit()==choco){
						//si oui on additionne ce que l'on doit pour l'instant livrer au step suivant 
						a += contrat.getQuantiteALivrerAuStep();
					}
				}
				//on convertit la somme de chocolat en quantité de fève si il s'agit 
				//bien d'un chocolat qui dépend de notre fève
				//chaque chocolat éta,t rattché à une fève particulière
				//on pourrait faire autrement et ne pas parcourir la liste des chocolats du coup
				//optimisation mineure à faire
				if (choco==bollo && feve == Feve.F_BQ_E){
					b+= a*((15500)/52000);
				}
				
				if (choco==fraud && feve == Feve.F_BQ){
					b+= a*((15500)/52000);
				}
				if (choco==hypo && feve == Feve.F_HQ_E){
					b+= a;
				}
				if (choco==arna && feve == Feve.F_MQ){
					b+= a*((13000)/26000);
				}
			//on met alors à jour demande_vente
			demande_vente.replace(feve,b);
			}
		}*/

		//maintenant on fait des propositions de contrat cadre
        for(IProduit feve : super.fevesUtiles){
            //on parcourt les acteurs de la filière
			// Proposition d'un nouveau contrat a tous les vendeurs possibles
			// Fève utilisée : BQ / BQ_E / MQ / HQ_E
            for (IActeur acteur : Filiere.LA_FILIERE.getActeurs()) {
                //si l'acteur n'est pas nous et si l'acteur achète des contrats cadres et s'il achète
                //des fèves par contrat cadre
                if (acteur!=this && acteur instanceof IVendeurContratCadre && ((IVendeurContratCadre)acteur).vend(feve)) {
                    //Contrat cadre de 10 step au prochain step
					double demande = demandeAchatFeve.get(feve);
					//jdb.ajouter("Demande de "+feve+" : "+demande);
					//besoin de vérifier que la demande est bien strictement supérieur à 0
					if(demande > 100){
                    	demanderUnContratCadreAcheteur(acteur, feve, demande);
					}
                }
            }
        }

		super.journalCC.ajouter("================ FIN NEXT =====================");

		/*/
		//@author Florian Malveau
		//A partir des données des contrats on estime coûts fèves
		for(IProduit feve : coutMoyFeves.keySet()){
			double quantityXprix = 0.0; //numérateur pour moyenne
			double quantitytotale = 0.0; //dénominateur moyenne
			for (ExemplaireContratCadre contrat : this.ContratsAcheteur){
				if(contrat.getProduit() == feve){
					quantityXprix += contrat.getQuantiteTotale()*contrat.getPrix();
					quantitytotale += contrat.getQuantiteTotale();
		}

			this.coutMoyFeves.replace(feve, quantityXprix/quantitytotale);
			}
		}

		//Statistique sur les achats de fèves
		jdb.ajouter("");
		jdb.ajouter("########################################");
		jdb.ajouter("-- COUT ESTIME --");
		for(IProduit feve : coutMoyFeves.keySet()){
			jdb.ajouter("- "+feve+" : "+coutMoyFeves.get(feve));
		}
		*/
	}

	//@author Henri Roth
	public void receptionner(IProduit produit, double quantiteEnTonnes, ExemplaireContratCadre contrat) {
		journalCC.ajouter("Reception de "+quantiteEnTonnes+" de T de " + produit + " en provenance du contrat "+contrat.getNumero());
		super.stockFeves.addToStock(produit, quantiteEnTonnes);
		if(produit == bollo) {
            this.qtté_réception_bollo += quantiteEnTonnes;
        }
        if(produit == fraud) {
            this.qtté_réception_fraudau += quantiteEnTonnes;
        }
        if(produit == hypo) {
            this.qtté_réception_hypo += quantiteEnTonnes;
        }
        if(produit == arna) {
            this.qtté_réception_arna += quantiteEnTonnes;
        }
	}
	//@author Henri Roth
	public boolean achete(IProduit produit) {
		//on vérifie qu'il s'agit bien de la fève que l'on achète
		if(produit == Feve.F_BQ
		|| produit == Feve.F_BQ_E
		|| produit == Feve.F_MQ
		|| produit == Feve.F_HQ_E){
			//on vérifie que l'on en veut bien
				return true; 
		}
		return false;
	}
	//@author Henri Roth
	public String toString() {
		return this.getNom();
	}
	//@author Henri Roth
	public int fixerPourcentageRSE(IAcheteurContratCadre acheteur, IVendeurContratCadre vendeur, IProduit produit,
			Echeancier echeancier, long cryptogramme, boolean tg) {
		return 5;
	}

	@Override //@author Henri Roth & Florian Malveau
	public void notificationNouveauContratCadre(ExemplaireContratCadre contrat) {
		// Trie des contrats cadres en fonction du produit
		if(super.lesFeves.contains(contrat.getProduit())) {
			super.ContratsAcheteur.add(contrat);
			// Récupération des infos du contrat
			IActeur vendeur = contrat.getVendeur();
			Echeancier e = contrat.getEcheancier();
			double quantite = e.getQuantite(e.getStepDebut());
			double prix = contrat.getPrix();
			List<Double> ListePrix = prixFeve.get(contrat.getProduit());
			ListePrix.add(prix);
			prixFeve.replace(contrat.getProduit(), ListePrix);
			journalCC.ajouter(Journal.texteColore(vendeur, vendeur.getNom())+" - New CC Achat "+contrat.getProduit());
			journalCC.ajouter("--> [Prix : "+Math.round(contrat.getPrix())+" € | Quantitee par step : "+quantite+" t | Nb step : "+e.getNbEcheances()+"]");
		
		} else if(super.lesChocolats.contains(contrat.getProduit())) {
			super.ContratsVendeur.add(contrat);

			// Récupération des infos du contrat
			IActeur acheteur = contrat.getAcheteur();
			Echeancier e = contrat.getEcheancier();
			double quantite = e.getQuantite(e.getStepDebut());
			journalCC.ajouter(Journal.texteColore(acheteur, acheteur.getNom())+" - New CC Vente "+contrat.getProduit());
			journalCC.ajouter("--> [Prix : "+Math.round(contrat.getPrix())+" € | Quantitee par step : "+quantite+" t | Nb step : "+e.getNbEcheances()+"]");
		
		} else {
			jdb.ajouter("#### ATTENTION : ON ACHETE N'IMPORTE QUOI ####");
		}

	//ContratsAcheteur.add(contrat);
	}
}
