package abstraction.eq6Transformateur3;
// @author Henri Roth & Florian Malveau
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.filiere.IFabricantChocolatDeMarque;
import abstraction.eqXRomu.general.Variable;

public class Transformateur3Fabriquant extends Transformateur3Marques implements IFabricantChocolatDeMarque{
    
    //Gestion des chocolats de marque
    protected List<ChocolatDeMarque> chocolatDeMarques;
	protected HashMap<IProduit, Variable> dicoIndicateurChoco;
    protected ChocolatDeMarque fraud;
    protected ChocolatDeMarque hypo;
    protected ChocolatDeMarque arna;
    protected ChocolatDeMarque bollo;

    //Gestion de la production de chocolat
    private double nbOuvrier = 85400;
    //private double nbMachine = 128;
    
    //private double capacite_machine = 1e3;//1e7*1e-4; // tablette/step
    //private double capacite_ouvrier = 15000; // tab/step

    private double coutIngredient = 450.0; // €/tonnes
    private double salaireOuvrier = 4500.0; // €
    
    protected double coutTotalProd = 0;
    private double quantiteTotaleProduite = 0;

    //Production maximale : 128 000 T x2 = 256 000 T
    //nbMachine*capacite_machine*1.3; // 166 000 T
    protected double productionMax = super.productionMaxStrat;

    public Transformateur3Fabriquant(){
        super();

        //Liste des chocolat de marque (pour matcher avec l'interface)
        this.chocolatDeMarques = new ArrayList<ChocolatDeMarque>();
        this.fraud = new ChocolatDeMarque(Chocolat.C_BQ, "Fraudolat", 30);
        this.hypo = new ChocolatDeMarque(Chocolat.C_HQ_E, "Hypocritolat", 100);
        this.arna =  new ChocolatDeMarque(Chocolat.C_MQ, "Arnaquolat", 50);
        this.bollo = new ChocolatDeMarque(Chocolat.C_BQ_E, "Bollorolat", 30);
        this.chocolatDeMarques.add(fraud);
        this.chocolatDeMarques.add(hypo);
        this.chocolatDeMarques.add(arna);
        this.chocolatDeMarques.add(bollo);

        //Liste de produit
        super.lesChocolats = new ArrayList<IProduit>();
        super.lesChocolats.add(fraud);
        super.lesChocolats.add(bollo);
        super.lesChocolats.add(arna);
        super.lesChocolats.add(hypo);

        //Remplissage de prixChoco
        for(IProduit choco : super.lesChocolats){
            List<Double> Prix = new ArrayList<Double>();
            super.prixChoco.put(choco, Prix);
            }
        
        //Dico indicateur choco
		this.dicoIndicateurChoco = new HashMap<IProduit, Variable>();
        this.dicoIndicateurChoco.put(fraud,super.eq6_Q_Fraudo);
        this.dicoIndicateurChoco.put(hypo,super.eq6_Q_Hypo);
        this.dicoIndicateurChoco.put(arna,super.eq6_Q_Arna);
        this.dicoIndicateurChoco.put(bollo,super.eq6_Q_Bollo);

        //Création du stock de chocolat
		super.stockChoco = new Transformateur3Stock(this, super.journalStock, "chocolat", 25000.0, super.lesChocolats, this.dicoIndicateurChoco);
    

    }

    public void initialiser(){
        super.initialiser();
        //coutStockage = 4*Filiere.LA_FILIERE.getParametre("cout moyen stockage producteur").getValeur();
    }

    public void next(){
        super.next();

        super.jdb.ajouter("NEXT - FABRIQUANT");
        super.journalProduction.ajouter("NEXT - FABRIQUANT");
        
        this.coutTotalProd = 0;
        this.quantiteTotaleProduite = 0;
        //this.productionMax = nbMachine*capacite_machine; on considère productionMax constante
        
        //On limite la demande de production à la capacité des machines/ouvriers
        this.limitProduction();

        //Production des différents types de chocolat
        for(IProduit choco : DemandeProdChoco.keySet()){
            this.produceChocolate((ChocolatDeMarque)choco, DemandeProdChoco.get(choco));
        }
        //this.produceChocolate(arna, 300);

        //Payer les coûts de production
        this.payerProdNext();

        super.journalProduction.ajouter("");
    }

    public List<ChocolatDeMarque> getChocolatsProduits(){
        return this.chocolatDeMarques;
    }

    public void payerProdNext(){
        double prixOuvrier = nbOuvrier*salaireOuvrier;
        double coutFixe = 200.0e6;
        double coutVariable = 0.3*quantiteTotaleProduite/(100.0e-6); // 0.3€ pour 100g
        double prixIngredient = this.coutTotalProd;
        this.coutTotalProd += prixOuvrier + coutFixe + coutVariable;
        //Les coûts de stockage seront considéré en fin de next
        super.LaBanque.payerCout(this, super.cryptogramme, "Coûts de production", coutTotalProd);

        //Afficher les comptes
        double div = 1000.0;
        String suff = " k€";

        super.jdb.ajouter("====> Coût total de production : "+Math.round(this.coutTotalProd/div)+suff);        

        super.journalProduction.ajouter("====> Coût total de production : "+Math.round(this.coutTotalProd/div)+suff);
        super.journalProduction.ajouter("-------------------- + Coût Ingredient : "+Math.round(prixIngredient/div)+suff);
        super.journalProduction.ajouter("-------------------- + Coût Ouvrier... : "+Math.round(prixOuvrier/div)+suff);
        super.journalProduction.ajouter("-------------------- + Coût fixe...... : "+Math.round(coutFixe/div)+suff);
        super.journalProduction.ajouter("-------------------- + Coût variable.. : "+Math.round(coutVariable/div)+suff);
    }

    public void payerIngredient(double quantity){
        if(quantity > 0.0){
            double prixIngredient = this.coutIngredient*quantity;
            this.coutTotalProd += prixIngredient;
        }
    }

    public void limitProduction(){
        double quantiteTotaleDemande=0.0;
        for(IProduit choco : DemandeProdChoco.keySet()){
            quantiteTotaleDemande += DemandeProdChoco.get(choco);
        }
        if(quantiteTotaleDemande>this.productionMax){
            //On adapte la demande avec un ratio
            for(IProduit choco : DemandeProdChoco.keySet()){
                DemandeProdChoco.put(choco,DemandeProdChoco.get(choco)*this.productionMax/quantiteTotaleDemande);
            }
        }
    }

    public void produceChocolate(ChocolatDeMarque choco, double quantity){
        //Récupération de la liste des fèves triées dans l'ordre décroissant de qualité
        List<IProduit> FevesSorted = super.stockFeves.getListProduitSorted();

        // Étape 1 : Détermine les fèves utilisable pour la production
        int indexLastFeve=-1;
        //int indexFeveMin = 0;

        //Si chocolat est Hypocritolat - C_HQ_E
        if(choco.getGamme()==Gamme.HQ){
            //Indice de la dernière fève compatible pour faire ce chocolat
            indexLastFeve = 1;
        }
        //Si chocolat est Arnaquolat - C_MQ
        else if(choco.getGamme()==Gamme.MQ){
            //Indice de la dernière fève compatible pour faire ce chocolat
            indexLastFeve = 3;
        }
        //Si chocolat est Bollorolat - C_BQ_E ou Fraudolat C_BQ 
        else if(choco.getGamme()==Gamme.BQ){
            //Indice de la dernière fève compatible pour faire ce chocolat
            indexLastFeve = 5;
        }

        //Quantité de chocolat à produire
        double quantityToProduce = quantity;//this.DemandeProdChoco.get(choco);

        //Étape 2 : Production
        /*
         * Principe de production :
         * On produit d'abord du chocolat avec les fèves adapté à ce chocolat
         * Si la quantité produite est insuffisant on complète à partir d'autre fèves
         * à hauteur de moins de 20%
         * => pour l'instant on impose l'utilisation d'un seul type de fève
         * (des fèves plus haut de gamme)
         */
        //Énumération des fèves en allant de la plus basse qualité vers la plus haute
        for(int i=indexLastFeve; indexLastFeve-1<=i; i--){
            Feve feveUtilisee  = (Feve)FevesSorted.get(i);
            
            //On ne produit du chocolat labélisé qu'avec des fèves labélisées et inversement
            if(feveUtilisee.isBio()==choco.isBio() && feveUtilisee.isEquitable()==choco.isEquitable()){
                int pourcentageCacao = choco.getPourcentageCacao();
                double quantityFeve = super.stockFeves.getQuantityOf(feveUtilisee);
                double quantityFeveDemandee = quantityToProduce*pourcentageCacao/100;

                //Si le stock de fève est vide
                if(quantityFeve==0.0){
                    super.journalProduction.ajouter("Pas de production de "+choco.getNom()+" (Stock vide : "+feveUtilisee.toString()+")");
                }

                //Si on dispose d'assez de fèves en stock => on produit
                else if(quantityFeve >= quantityFeveDemandee){
                    double quantityIngredient = quantityToProduce*(100-pourcentageCacao)/100;
                    
                    super.stockFeves.remove(feveUtilisee, quantityFeveDemandee);
                    this.payerIngredient(quantityIngredient);
                    super.stockChoco.addToStock(choco, quantityToProduce);
                    this.quantiteTotaleProduite += quantityToProduce;

                    super.journalProduction.ajouter("Production de "+choco.getNom()+" : "+(int)quantityToProduce+" t ("+feveUtilisee.toString()+")");
                }
                else{

                    //On évalue la quantité maximale de chocolat qu'on peut produire
                    double newQuantityToProduce = quantityFeve/pourcentageCacao*100;
                    double quantityIngredient = newQuantityToProduce*(100-pourcentageCacao)/100;

                    super.stockFeves.remove(feveUtilisee, quantityFeve);
                    this.payerIngredient(quantityIngredient);
                    super.stockChoco.addToStock(choco, newQuantityToProduce);
                    this.quantiteTotaleProduite += newQuantityToProduce;  
                    
                    //Calcul la quantité qui reste à produire
                    quantityToProduce -= newQuantityToProduce;

                    super.journalProduction.ajouter("Production de "+choco.getNom()+" : "+(int)newQuantityToProduce+" t ("+feveUtilisee.toString()+") "+"[Demande disproportionnée]");
                    //+"=> ERROR : demande de fève > stock fève");
                }


            }

        }

    }
}
