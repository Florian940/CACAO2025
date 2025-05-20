// @author  Florian Malveau

package abstraction.eq6Transformateur3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;

public class Transformateur3StratQuantity extends Transformateur3Acteur {

    // Création des contrats acheteur et vendeur (pour les rendres accessibles dans la classe)
    protected List<ExemplaireContratCadre> ContratsAcheteur;
    protected List<ExemplaireContratCadre> ContratsVendeur;

    protected HashMap<IProduit, Double> coutMoyFeves; //estimation du cout de chaque fèves
    protected List<Long> contratTraite;

    //Quantitée de chaque type de fèves reçue au prochain step
    //pour chaque fève, on dispose d'un échéancier sur la quantité total de fèves
	protected HashMap<IProduit, List<Double>> quantityFevesEcheancier;
    // Quantitée de chaque type de choco vendu au prochain step
    protected HashMap<IProduit, List<Double>> quantityChocoEcheancier;
    protected HashMap<IProduit, List<Double>> besoinFeveEcheancier;

    // Analyse des receptions de fèves et livraisons de choco

    //Les chocolats produits
    protected IProduit fraudStrat;
    protected IProduit hypoStrat;
    protected IProduit arnaStrat;
    protected IProduit bolloStrat;

    //Demande de production
    protected double productionMaxStrat = 150000; //Production max de chocolat par step
    protected HashMap<IProduit, Double> DemandeProdChoco; //Demande pour chaque choco en tonnes
    protected HashMap<IProduit, Double> besoinFeveNextStep; //Demande max pour chaque choco en tonnes


    public Transformateur3StratQuantity(){

        //Initialisation des listes de contrats acheteur et vendeur
        this.ContratsAcheteur=new LinkedList<ExemplaireContratCadre>();
        this.ContratsVendeur=new LinkedList<ExemplaireContratCadre>();

        //Initialisation des échanciers de fèves et chocolats
        this.quantityFevesEcheancier = new HashMap<IProduit, List<Double>>();
        this.quantityChocoEcheancier = new HashMap<IProduit, List<Double>>();
        this.besoinFeveEcheancier = new HashMap<IProduit, List<Double>>();
        this.contratTraite = new ArrayList<Long>();

        //Initialisation de la demande de production
        this.DemandeProdChoco = new HashMap<IProduit, Double>();
        this.besoinFeveNextStep = new HashMap<IProduit, Double>();
    }

    public void initialiser() {
        super.initialiser();
        
        // Initialisation des échéanciers de fèves et chocolats
        for(IProduit feve : super.lesFeves){
            this.quantityFevesEcheancier.put(feve, new ArrayList<Double>());
            this.quantityFevesEcheancier.get(feve).add(0.0);
            this.quantityFevesEcheancier.get(feve).add(0.0);
            this.quantityFevesEcheancier.get(feve).add(0.0);
            this.quantityFevesEcheancier.get(feve).add(0.0);
        }

        for(IProduit feve : super.fevesUtiles){
            this.besoinFeveEcheancier.put(feve, new ArrayList<Double>());
            this.besoinFeveEcheancier.get(feve).add(0.0);
            this.besoinFeveEcheancier.get(feve).add(0.0);
            this.besoinFeveEcheancier.get(feve).add(0.0);

            besoinFeveNextStep.put(feve, 0.0);
        }

        for(IProduit choco : super.lesChocolats){
            this.quantityChocoEcheancier.put(choco, new ArrayList<Double>());
            this.quantityChocoEcheancier.get(choco).add(0.0);
            this.quantityChocoEcheancier.get(choco).add(0.0);
            this.quantityChocoEcheancier.get(choco).add(0.0);
            this.quantityChocoEcheancier.get(choco).add(0.0);
        }

        fraudStrat = super.lesChocolats.get(0);
        bolloStrat = super.lesChocolats.get(1);
        arnaStrat = super.lesChocolats.get(2);
        hypoStrat = super.lesChocolats.get(3);

        this.DemandeProdChoco.put(fraudStrat,50000.0);//productionMaxStrat/3);
        this.DemandeProdChoco.put(bolloStrat,1000.0);//productionMaxStrat/3);
        this.DemandeProdChoco.put(arnaStrat,2500.0);//productionMaxStrat/6);
        this.DemandeProdChoco.put(hypoStrat,250.0);//productionMaxStrat/6);

    }

    public void next(){
		super.next();
		super.jdb.ajouter("NEXT - STRATQUANTITY");
        super.journalStrat.ajouter("");
        super.journalStrat.ajouter("NEXT - STRATQUANTITY");

        miseAJourEcheanciers();

        // Traitement nouveaux contrats pour actualiser les échéanciers de fèves et chocolats
        this.quantityFevesEcheancier = traiterContrats(this.ContratsAcheteur, this.quantityFevesEcheancier);
        this.quantityChocoEcheancier = traiterContrats(this.ContratsVendeur, this.quantityChocoEcheancier);

        miseAJourEcheanciersBesoins();

        //if(super.currentStep%4 == 3){
        //    evaluateDemandeProdChoco();
        //}

        // Affichage des échéanciers de fèves et chocolats
        
        //displayEcheancier("Echéancier de chocolats", this.quantityChocoEcheancier, super.lesChocolats);
        //displayEcheancier("Echéancier besoin de fèves", this.besoinFeveEcheancier, super.fevesUtiles);
        //displayEcheancier("Echéancier de fèves", this.quantityFevesEcheancier, super.fevesUtiles);
        }

    // On met à jour les échéanciers de fèves et chocolats en supprimant la première ligne
    public void miseAJourEcheanciers(){
        //On supprime la ligne du next actuel
        for(IProduit feve : super.lesFeves){
            this.quantityFevesEcheancier.get(feve).remove(0);
            if(this.quantityFevesEcheancier.get(feve).size() <= 2){
                this.quantityFevesEcheancier.get(feve).add(0.0);
            }
        }
        for(IProduit choco : super.lesChocolats){
            this.quantityChocoEcheancier.get(choco).remove(0);
            if(this.quantityChocoEcheancier.get(choco).size() <= 2){
                this.quantityChocoEcheancier.get(choco).add(0.0);
            }
        }
    }

    // On traite les contrats acheteur et vendeur pour actualiser les échéanciers de fèves et chocolats
    public void actualiserEcheanciers(){
        this.quantityFevesEcheancier = traiterContrats(this.ContratsAcheteur, this.quantityFevesEcheancier);
        this.quantityChocoEcheancier = traiterContrats(this.ContratsVendeur, this.quantityChocoEcheancier);
    }

    public void miseAJourEcheanciersBesoins(){
        for(IProduit feve : super.fevesUtiles){
            double proportion = 0.0;
            IProduit choco = null;
            if(feve == Feve.F_BQ){
                proportion = 0.3;
                choco = super.lesChocolats.get(0);
            }else if(feve == Feve.F_BQ_E){
                proportion = 0.3;
                choco = super.lesChocolats.get(1);
            }else if(feve == Feve.F_MQ){
                proportion = 0.5;
                choco = super.lesChocolats.get(2);
            }else if(feve == Feve.F_HQ_E){
                proportion = 1.0;
                choco = super.lesChocolats.get(3);
            }
            // On construit les stats sur le besoin en fèves
            for(int i=0; i<3; i++){
                double quantite = this.quantityChocoEcheancier.get(choco).get(i) * proportion;
                this.besoinFeveEcheancier.get(feve).set(i, quantite);
                // Ajout au besoin du prochain step
                if(i==0){
                    besoinFeveNextStep.replace(feve,quantite);
                }
            }
        }
    }

    // On traite les contrats acheteur et vendeur pour actualiser les échéanciers de fèves et chocolats
    public HashMap<IProduit, List<Double>> traiterContrats(List<ExemplaireContratCadre> contratsList, HashMap<IProduit, List<Double>> EcheancierParProduit){
        
        int currentStep = Filiere.LA_FILIERE.getEtape(); // On récupère le step actuel

        for(ExemplaireContratCadre contrat : contratsList){
            // On traite le contrat s'il n'a pas déjà été traité
            if(!this.contratTraite.contains(contrat.getNumero())){

                this.contratTraite.add(contrat.getNumero());
                //super.journalStrat.ajouter("----- Traitement du contrat " + contrat.getNumero()+" -----");
                // On ajoute la quantité de fèves reçue au stock
                IProduit prod = contrat.getProduit();
                Echeancier echeancier = contrat.getEcheancier();
                int debutCC = echeancier.getStepDebut(); // On récupère le step de début de l'échéancier
                int t = debutCC-currentStep; // Translation à appliquer à l'échéancier pour le ramener au step actuel
                //super.journalStrat.ajouter("Produit : " + prod.toString());
                //super.journalStrat.ajouter("Echéancier : " + echeancier.toString());
                for (int i = t; i <= echeancier.getNbEcheances()+t; i++) {
                    // Si la liste d'échéance n'est pas assez grande, on l'agrandi
                    double quantite = echeancier.getQuantite(echeancier.getStepDebut()); // Quantité de fèves reçue
                    if(EcheancierParProduit.get(prod).size() <= i){
                        EcheancierParProduit.get(prod).add(quantite);
                    }else{
                        quantite += EcheancierParProduit.get(prod).get(i);
                        EcheancierParProduit.get(prod).set(i, quantite);
                    }
                }
            }
        }
        return EcheancierParProduit;
    }

    public void traiterContratStat(ExemplaireContratCadre contrat){
        
        int currentStep = Filiere.LA_FILIERE.getEtape(); // On récupère le step actuel
        HashMap<IProduit, List<Double>> EcheancierParProduit;

        // On traite le contrat s'il n'a pas déjà été traité
        if(!this.contratTraite.contains(contrat.getNumero())){
            IProduit prod = contrat.getProduit();

            if(super.lesFeves.contains(prod)){
                EcheancierParProduit = this.quantityFevesEcheancier;
            }else{
                EcheancierParProduit = this.quantityChocoEcheancier;
            }

            this.contratTraite.add(contrat.getNumero());
            //super.journalStrat.ajouter("----- Traitement du contrat " + contrat.getNumero()+" -----");
            // On ajoute la quantité de fèves reçue au stock
            
            Echeancier echeancier = contrat.getEcheancier();
            int debutCC = echeancier.getStepDebut(); // On récupère le step de début de l'échéancier
            int t = debutCC-currentStep; // Translation à appliquer à l'échéancier pour le ramener au step actuel
            //super.journalStrat.ajouter("Produit : " + prod.toString());
            //super.journalStrat.ajouter("Echéancier : " + echeancier.toString());
            for (int i = t; i <= echeancier.getNbEcheances()+t; i++) {
                // Si la liste d'échéance n'est pas assez grande, on l'agrandi
                double quantite = echeancier.getQuantite(echeancier.getStepDebut()); // Quantité de fèves reçue
                if(EcheancierParProduit.get(prod).size() <= i){
                    EcheancierParProduit.get(prod).add(quantite);
                }else{
                    quantite += EcheancierParProduit.get(prod).get(i);
                    EcheancierParProduit.get(prod).set(i, quantite);
                }
            }
        
            // On sauvegarde les infos
            if(super.lesFeves.contains(prod)){
                this.quantityFevesEcheancier = EcheancierParProduit;
            }else{
                this.quantityChocoEcheancier = EcheancierParProduit;
            }
        }
    }

    // On affiche les échéanciers de fèves et chocolats
    public void displayEcheancier(String title, HashMap<IProduit, List<Double>>Echeancier, List<IProduit> prodList, HashMap<IProduit,Double> refData){

        super.journalStrat.ajouter("");
        super.journalStrat.ajouter(title);
        super.journalStrat.ajouter(".................... | .Step +0. | .Step +1.   | .Step +2. | Objectif. .(%). |");
        for(IProduit prod : prodList){

            int pourcentage = (int)Math.round(Echeancier.get(prod).get(0) / refData.get(prod) * 100);

            String prodName = miseEnForme(prod.toString(), 20, true);
            String str1 = miseEnForme(Journal.doubleSur(Echeancier.get(prod).get(0).intValue(),1),9, false);
            String str2 = miseEnForme(Journal.doubleSur(Echeancier.get(prod).get(1).intValue(),1),9, false);
            String str3 = miseEnForme(Journal.doubleSur(Echeancier.get(prod).get(2).intValue(),1),9, false);
			String str4 = miseEnForme(Journal.doubleSur(refData.get(prod).intValue(),1),9, false);
            String str5 = miseEnForme(pourcentage+"",2, false);
            this.journalStrat.ajouter(prodName+" | "+str1+" | "+str2+" | "+str3+" | "+str4+" ("+str5+"%) |");
            //this.journalStrat.ajouter(Journal.doubleSur(123456789.124, 0));
        
        }
    }

    public void displayEcheancier(String title, HashMap<IProduit, List<Double>>Echeancier, List<IProduit> prodList){

            super.journalStrat.ajouter("");
            super.journalStrat.ajouter(title);
            super.journalStrat.ajouter(".................... | .Step +0. | .Step +1.   | .Step +2. |");
            for(IProduit prod : prodList){

                String prodName = miseEnForme(prod.toString(), 20, true);
                String str1 = miseEnForme(Journal.doubleSur(Echeancier.get(prod).get(0).intValue(),1),9, false);
                String str2 = miseEnForme(Journal.doubleSur(Echeancier.get(prod).get(1).intValue(),1),9, false);
                String str3 = miseEnForme(Journal.doubleSur(Echeancier.get(prod).get(2).intValue(),1),9, false);
                this.journalStrat.ajouter(prodName+" | "+str1+" | "+str2+" | "+str3+" |");
                //this.journalStrat.ajouter(Journal.doubleSur(123456789.124, 0));
            
            }
        }

    public void displayAllStratQuantityData(){
        displayEcheancier("Echéancier de chocolats", this.quantityChocoEcheancier, super.lesChocolats, this.DemandeProdChoco);
        displayEcheancier("Echéancier besoin de fèves", this.besoinFeveEcheancier, super.fevesUtiles);
        displayEcheancier("Echéancier de fèves", this.quantityFevesEcheancier, super.fevesUtiles, this.besoinFeveNextStep);
        
    }

    //
    public void evaluateDemandeProdChoco(){
        // On évalue la demande de production de chocolat
        for(IProduit choco : super.lesChocolats){
            double quantiteStep1Choc = this.quantityChocoEcheancier.get(choco).get(1);
            double quantiteDemandeChoc = this.DemandeProdChoco.get(choco);
            double increment = 0.0;
            IProduit feve = null;

            if(choco == fraudStrat){
                feve = Feve.F_BQ;
                increment = 5000.0;
            }else if(choco == bolloStrat){
                feve = Feve.F_BQ_E;
                increment = 500.0;
            }else if(choco == arnaStrat){
                feve = Feve.F_MQ;
                increment = 500.0;
            }else if(choco == hypoStrat){
                feve = Feve.F_HQ_E;
                increment = 100.0;
            }

            double quantiteStep1Feve = this.quantityFevesEcheancier.get(feve).get(1);
            double quantiteDemandeFeve = this.besoinFeveEcheancier.get(feve).get(1);
            
            if(quantiteStep1Feve < quantiteDemandeFeve*0.8){
                this.DemandeProdChoco.replace(choco, quantiteDemandeChoc-increment);
            }else if(quantiteStep1Choc >= quantiteDemandeChoc*0.9){
                this.DemandeProdChoco.replace(choco, quantiteDemandeChoc+increment);
            }



        }
    }
    
    
    public String miseEnForme(String str, int size, Boolean left){
        int nbspace = size-str.length();
        String space = "";
        for(int i=0;i<nbspace;i++){
            space=space+".";
        }
        if(left){
            return str+space;
        }
        else{
            return space+str;
        }
    }

    //Estimation prix fèves

    //Estimation production

    //Estimation stock

}
   