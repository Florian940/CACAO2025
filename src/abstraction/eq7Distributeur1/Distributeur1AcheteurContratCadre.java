package abstraction.eq7Distributeur1;
///Maxime GUY///

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.contratsCadres.SuperviseurVentesContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.IProduit;

public class Distributeur1AcheteurContratCadre extends Distributeur1Stock implements IAcheteurContratCadre{

	
	protected List<Double> priceProduct;
	//protected Map<ChocolatDeMarque, Double> priceProduct2;
	protected List<Double> requiredQuantities;
	//protected Map<ChocolatDeMarque, Double> requiredQuantities2;
	protected String name;
	protected Color color;
	protected List<Double> predictionsVentesPourcentage;
	//private HashMap<ChocolatDeMarque,Variable> stock;

	public Distributeur1AcheteurContratCadre() {
		super();

		String name = "EQ7";
		Color color = new Color(162, 207, 238);
		this.name = name;
		this.color = color;
		//this.stock = stock;

		this.priceProduct = new ArrayList<Double>();
		this.requiredQuantities = new ArrayList<Double>();
	
	}

	    public int getInt(ChocolatDeMarque product){
        int idProduct = 0;
        switch(product.getGamme()){
            case BQ : idProduct=0;break;
            case MQ : idProduct=2;break;
            case HQ : idProduct=4;break;
        }
        if (product.isBio()){
            idProduct++;
        }
        if (product.isEquitable()){
            idProduct++;
        }
		if(idProduct == 5)
		{
			return 4;
		}
		if(idProduct == 6)
		{
			return 5;
		}
        return(idProduct);
    }
	
	@Override
	public boolean achete(IProduit produit){
		if (produit instanceof ChocolatDeMarque){
			ChocolatDeMarque chocolat = (ChocolatDeMarque) produit;
			return(requiredQuantities.get(cdmToInt(chocolat))>100); 
		}
		return(false);
	}

	@Override
	public Echeancier contrePropositionDeLAcheteur(ExemplaireContratCadre contrat){
		List<Echeancier> listeEcheancier = contrat.getEcheanciers();
		int tour = 0;
		double valeurtotale = 0;
		ChocolatDeMarque chocolat = (ChocolatDeMarque) contrat.getProduit();
		Echeancier echeancierActuel;
		
		if(chocolat.toString().equals("C_BQ_Fraudolat")){
			return null;
		}
		
		if (listeEcheancier.isEmpty()){
			tour = 0;
			echeancierActuel = new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 12, requiredQuantities.get(cdmToInt(chocolat)));
		} else {
			tour = listeEcheancier.size();
			echeancierActuel = listeEcheancier.get(listeEcheancier.size()-1);
		}
		
		for (int step = echeancierActuel.getStepDebut(); step<=echeancierActuel.getStepFin() ; step++){

			for (int i = echeancierActuel.getStepDebut(); i <= echeancierActuel.getStepFin(); i++){
				valeurtotale += echeancierActuel.getQuantite(i);
			}
			int nbSteps = echeancierActuel.getStepFin() - echeancierActuel.getStepDebut() + 1;
			double quantiteDemandee = echeancierActuel.getQuantite(step);
			double quantiteVoulue = requiredQuantities.get(cdmToInt(chocolat))/predictionsVentesPourcentage.get(echeancierActuel.getStepDebut()%24)*predictionsVentesPourcentage.get(step%24);
			if (quantiteDemandee > quantiteVoulue*(1+0.02*tour)){
				echeancierActuel.set(step, Math.max(Math.max(100,quantiteVoulue*(1+0.02*tour)), valeurtotale/(10*nbSteps)));
			}
			if (quantiteDemandee < quantiteVoulue*(1-0.02*tour)){
				echeancierActuel.set(step, Math.max(Math.max(100,quantiteVoulue*(1-0.02*tour)), valeurtotale/(10*nbSteps)));
			}
			if (quantiteDemandee < 100){
				echeancierActuel.set(step, Math.max(100, 100 + valeurtotale/(10*nbSteps)));
			}
			valeurtotale = 0;
			
		}
		return(echeancierActuel);
	}

	@Override
	public double contrePropositionPrixAcheteur(ExemplaireContratCadre contrat){
		List<Double> listePrix = contrat.getListePrix();
		ChocolatDeMarque chocolat = (ChocolatDeMarque) contrat.getProduit();
		int tour;
		Double dernierPrix;
		if (listePrix.isEmpty()){
			tour = 0;
			dernierPrix = 2 * priceProduct.get(cdmToInt(chocolat));
		}
		else {
			tour = listePrix.size();
			dernierPrix = listePrix.get(listePrix.size()-1);
		}
		double prixPropose = priceProduct.get(cdmToInt(chocolat))*(0.87+0.04*tour);
		if (tour<6 && dernierPrix>prixPropose){
			return(prixPropose);
		}
		if (dernierPrix<=prixPropose){
			return(listePrix.get(listePrix.size()-1));
		}
		return(priceProduct.get(cdmToInt(chocolat)));
	}

	@Override
	public void initialiser(){
	}

	@Override
	public String getNom(){
		return(this.name);
	}

	@Override
	public Color getColor(){
		return(this.color);
	}

	@Override
	public String getDescription(){
		return("Acheteur contrat cadre de l'equipe 7");
	}

	public void next_cc(){
		SuperviseurVentesContratCadre superviseur = (SuperviseurVentesContratCadre)(Filiere.LA_FILIERE.getActeur("Sup.CCadre"));
		for (int i=0; i<chocolats.size(); i++) {
			List<IVendeurContratCadre> vendeurList = superviseur.getVendeurs(chocolats.get(i));
			if (!vendeurList.isEmpty() && requiredQuantities.get(i)>superviseur.QUANTITE_MIN_ECHEANCIER){
				superviseur.demandeAcheteur(this, vendeurList.get(0), chocolats.get(i), new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 8, requiredQuantities.get(i)), this.cryptogramme, false);
			}
		}
	}

	@Override
	public List<Variable> getIndicateurs(){
		List<Variable> indicateurs = super.getIndicateurs();
		return(indicateurs);
	}

	@Override
	public List<Variable> getParametres(){
		List<Variable> parametres = new ArrayList<Variable>();
		return(parametres);
	}

	@Override
	public List<Journal> getJournaux(){
		List<Journal> journaux = new ArrayList<Journal>();
		return(journaux);
	}


	@Override
	public void notificationFaillite(IActeur acteur){

	}

	@Override
	public void notificationOperationBancaire(double montant){

	}

	@Override
	public List<String> getNomsFilieresProposees(){
		List<String> noms = new ArrayList<String>();
		return(noms);
	}

	@Override
	public Filiere getFiliere(String nom){
		Filiere test = new Filiere(0);
		return(test);
	}

	@Override
	public double getQuantiteEnStock(IProduit p, int cryptogramme ){
		if (this.cryptogramme == cryptogramme){
			if (p instanceof ChocolatDeMarque){
				ChocolatDeMarque chocolat = (ChocolatDeMarque) p;
				if (this.stocksChocolats != null && this.stocksChocolats.containsKey(chocolat)){
					return(this.stocksChocolats.get((chocolat)).getValeur());
				}
			}
			return(0);
		}
		return(0);
	}

	@Override
	public void notificationNouveauContratCadre(ExemplaireContratCadre contrat){
		
	}

	@Override
	public void receptionner(IProduit p, double quantiteEnTonnes, ExemplaireContratCadre contrat){
		this.getStock((ChocolatDeMarque) p).ajouter(this, quantiteEnTonnes);

		
		// journal Alexiho :
		ChocolatDeMarque chocolat = (ChocolatDeMarque) contrat.getProduit();
		
		String str_journal_CC = "";
		str_journal_CC = "Achat en contrat cadre de " + this.stocksChocolats.get(chocolat).getNom()+ " = " + quantiteEnTonnes + " tonne(s);" ;
		str_journal_CC = str_journal_CC.replace("EQ7StockC_", " ");
		journalCC.ajouter(str_journal_CC);
	}
}