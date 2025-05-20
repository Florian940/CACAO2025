package abstraction.eq6Transformateur3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.contratsCadres.SuperviseurVentesContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.produits.IProduit;
// @author Henri Roth & Eric Schiltz
public class Transformateur3ContratCadreVendeur extends Transformateur3Fabriquant implements IVendeurContratCadre {
    
    //Capacité de vente de chocolat
    protected HashMap<IProduit, Double> capacite_vente_max;

    //total des livraisons à faire pour ce step
    protected double qtté_livraison_bollo;
    protected double qtté_livraison_fraudau;
    protected double qtté_livraison_hypo;
    protected double qtté_livraison_arna;
    
    public Transformateur3ContratCadreVendeur() {
        this.ContratsVendeur=new LinkedList<ExemplaireContratCadre>();
        this.qtté_livraison_arna= 0; 
        this.qtté_livraison_bollo= 0; 
        this.qtté_livraison_fraudau= 0; 
        this.qtté_livraison_hypo= 0; 
	}

    // @author Florin Malveau
    public void initialiser() {
        super.initialiser();
    }

    // @author Florin Malveau
    //Initialisation de la capacité de vente max pour chaque chocolat
    public void initialiserCapaVente() {
        // On initialise la capacité de vente max à 90% de la production max de chocolat
        double f = 0.9;
        capacite_vente_max = new HashMap<IProduit, Double>();

        
        for(IProduit choco : super.lesChocolats){
            // On initialise la capacité de vente max à 90% de la production max de chocolat
            capacite_vente_max.put(choco, super.DemandeProdChoco.get(choco)*f);
        }
        /*
        capacite_vente_max.put(fraud,(productionMax*f)/3);
        capacite_vente_max.put(hypo,(productionMax*f)/6);
        capacite_vente_max.put(arna,(productionMax*f)/6);
        capacite_vente_max.put(bollo,(productionMax*f)/3);
        */
        

        // Enlève la quantité de choco déjà livrée pour le step suivant dans les CC existants
        for(IProduit choco : super.lesChocolats){
            double quantityDejaVendu = 0;
            //on parcourt tous les contrats cadres
            for (ExemplaireContratCadre contrat : ContratsVendeur){
                //pour chaque contrat on regarde si il concerne ce produit 
                if (contrat.getProduit()==choco){
                    //si oui on additionne ce que l'on doit pour l'instant livrer au step suivant 
                    Echeancier e = contrat.getEcheancier();
                    quantityDejaVendu += e.getQuantite(currentStep+1);//getQuantiteALivrerAuStep();
                }
            }
            //on remet alors à jour capacite_vente_max
            capacite_vente_max.replace(choco,capacite_vente_max.get(choco)-quantityDejaVendu);
            //jdb.ajouter("Capacité de vente max de "+choco+" : "+capacite_vente_max.get(choco));
        }
    }

    public boolean demanderUnContratCadreVendeur(IActeur acteur, IProduit produit, double quantite, boolean teteGondole) {

        SuperviseurVentesContratCadre supCCadre = (SuperviseurVentesContratCadre) Filiere.LA_FILIERE.getActeur("Sup.CCadre");
        double a = Filiere.random.nextDouble();
        int b = (int) a*5;
        ExemplaireContratCadre contrat = supCCadre.demandeVendeur((IAcheteurContratCadre)acteur, (IVendeurContratCadre)this,(IProduit) produit, new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 10+b, quantite), cryptogramme, teteGondole);
        // Si un contrat a été créé, on l'ajoute à la liste des contrats du vendeur
        if(contrat != null){
            notificationNouveauContratCadre(contrat);
            //jdb.ajouter("Nouveau contrat cadre Vendeur" +contrat.getProduit());
            //Mettre à jour la capacité de vente max
            initialiserCapaVente();
            return true;
        }
        return false;
    }

    //à chaque next on va proposer des contrats cadres pour vendre du chocolat
    @Override //@author Henri Roth & Eric SCHILTZ
    public void next(){
        super.next();

        //on affiche la quantité livrée durant le step jusque là 
        journalCC.ajouter("quantité livrée à ce step en arna en CC"+qtté_livraison_arna);
        journalCC.ajouter("quantité livrée à ce step en bollo en CC"+qtté_livraison_bollo);
        journalCC.ajouter("quantité livrée à ce step en fraudau en CC"+qtté_livraison_fraudau);
        journalCC.ajouter("quantité livrée à ce step en hypo en CC"+qtté_livraison_hypo);
        //on remet la quantité livrée à chaque step à 0 au début de chaque step
        this.qtté_livraison_arna= 0; 
        this.qtté_livraison_bollo= 0; 
        this.qtté_livraison_fraudau= 0; 
        this.qtté_livraison_hypo= 0;


        //Initialisation de la capacité de vente max
        initialiserCapaVente();

        //on parcourt tous les types de chocolat
        for(IProduit choco : super.lesChocolats){
            //on parcourt les acteurs de la filière
            for (IActeur acteur : Filiere.LA_FILIERE.getActeursSolvables()) {
                //si l'acteur n'est pas nous et si l'acteur achète des contrats cadres et s'il achète
                //du chocolat par contrat cadre
                if (acteur!=this && acteur instanceof IAcheteurContratCadre && ((IAcheteurContratCadre)acteur).achete(choco)) {
                    /*
                    Contrat cadre de 10 step, quantité /step = capacité de production restante 
                    Sans tête de gondole pour hypocritolat
                    et avec tête de gondole pour les autres chocolats
                    */
                    boolean teteGondole = !(choco==hypo);
                    double capa = capacite_vente_max.get(choco);

                    if(capa>10000){
                        capa = capa/5; 
                        if(teteGondole){
                            int a = 0;
                            for (int i=0; i<5; ++i){
                                // 1ere tentative avec tête de gondole
                                boolean marcheConclu = demanderUnContratCadreVendeur(acteur, choco, capa, true);
                                if(!marcheConclu){
                                    a+=1;
                                }
                            }
                            for(int k=0; k<a; ++k){
                                // 2ème tentative sans tête de gondole
                                demanderUnContratCadreVendeur(acteur, choco, capa, false);
                            }       
                        // Contrat sans tête de gondole
                        }
                        else{
                            demanderUnContratCadreVendeur(acteur, choco, capa, false);
                            demanderUnContratCadreVendeur(acteur, choco, capa, false);
                            demanderUnContratCadreVendeur(acteur, choco, capa, false);
                            demanderUnContratCadreVendeur(acteur, choco, capa, false);
                            demanderUnContratCadreVendeur(acteur, choco, capa, false);
                        }
                    }
                    else{
                        if(capa>100){
                            // Contrat avec tête de gondole
                            if(teteGondole){
                                // 1ere tentative avec tête de gondole
                                boolean marcheConclu = demanderUnContratCadreVendeur(acteur, choco, capa, true);
                                if(!marcheConclu){
                                    // 2ème tentative sans tête de gondole
                                    demanderUnContratCadreVendeur(acteur, choco, capa, false);
                                }
                            // Contrat sans tête de gondole
                            }else{
                                demanderUnContratCadreVendeur(acteur, choco, capa, false);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override //@author Eric Schiltz
    public boolean vend(IProduit produit) {
        //On dit oui dès que l'on vend bien de ce produit 
        if(super.lesChocolats.contains(produit)){
            return true;
        }
        else{
            return false;
        }
        //sert à compter les quantités de contrat cadre pour le tour suivant pour chaque produit  
        //double a = 0;
        //for (ExemplaireContratCadre contrat : ContratsVendeur){
        //    if (contrat.getProduit()==produit){
        //        a += contrat.getQuantiteALivrerAuStep();
        //    }
    }

    @Override //@author Eric Schiltz
    public Echeancier contrePropositionDuVendeur(ExemplaireContratCadre contrat) {
        //quand le vendeur propose une tête de gondole on dit toujours oui. On ne le tient 
        //pas en compte. On pourrait les refuser une fois sur 10 par ex mais en fait : 
        //On ne fais pas la différene entre les propositions du vendeur et ses contrepropositions
        //à nos propositions. 
        //Sinon on aurait des problèmes, on risquerait de refuser des contrats 
        //que nous avons nous même émis sans tête de gondole. 
        //on regarde quel produit est concerné par le contrat
        IProduit p = contrat.getProduit();
        //on regarde si on vend/produit bien de ce chocolat
        if(super.lesChocolats.contains(p)){
            // si oui on calcule de la quantite par step de chocolat que l'on veut livrer
            //on regarde quelle est notre capacité de vente du produit 
            double capa = capacite_vente_max.get(p);
            //on récupère l'échéancier
            Echeancier e = contrat.getEcheancier();
            //on récupère le premier step
            int stepdebut = e.getStepDebut();
            //on rearde la quantité à livrer proposée
            double QuantiteStep = e.getQuantite(stepdebut);
            // si la quantité à livrer proposé est dans nos capacités on accepte
            if(QuantiteStep < capa){
                //et on met à jour nos capacités de vente max
                return contrat.getEcheancier();
            }
            //sinon
            else{
                //si elle est vrmt trop petite : on abandonne 
                if(capa < 100){
                    return null;
                }
                //sinon on refait l'échéancier avec ce que l'on a; 
                else{
                    //et on met à jour nos capacités de vente max
                    int nb = e.getNbEcheances();
                    return new Echeancier(Filiere.LA_FILIERE.getEtape()+1, nb, capa);
                }
            }
        }
        //si on ne le vend/produit pas on ne fait rien 
        return null;
    }

    @Override //@author Henri Roth
    public double propositionPrix(ExemplaireContratCadre contrat) {
        // on retourne notre proposition de prix pour chaque 
        IProduit Choco = contrat.getProduit();
        List<Double> prix = prixChoco.get(Choco);
        Integer longueur = prix.size();
        Integer step_actuel = Filiere.LA_FILIERE.getEtape();
        double meilleurs_prix_histo = 0;
        // On évite les erreurs dans les premiers step, en prenant en compte que les premières ventes
        // et ensuite on prend en compte les 5 dernières ventes quand l'étape actuelle dépasse 5
        if(longueur != 0){
            if(longueur > 5){
                for(int i = 0; i < 5; i++){
                    
                    meilleurs_prix_histo += prix.get(longueur - i - 1);
                }
                meilleurs_prix_histo = meilleurs_prix_histo/5;
            }
            else{
                for(int i = 0; i < longueur; i++){
                    meilleurs_prix_histo += prix.get(i);
                }
                meilleurs_prix_histo = meilleurs_prix_histo/step_actuel;
            }
        }
        if(meilleurs_prix_histo == 0){
            Double prixinit = 0.0;
            if(Choco.equals(fraud)){
                prixinit = 10000.0;
            } else if(Choco.equals(bollo)){
                prixinit = 11000.0;
            } else if(Choco.equals(arna)){
                prixinit = 13000.0;
            }else if(Choco.equals(hypo)){
                prixinit = 17000.0;
            }
            return prixinit;
        }
        return meilleurs_prix_histo;
    }

    @Override //@author Henri Roth
    public double contrePropositionPrixVendeur(ExemplaireContratCadre contrat) {
        //à chaque fois on propose 1.2 fois le prix proposé par l'acheteur
        Double prixcontrat = contrat.getPrix();
        IProduit Choco = contrat.getProduit();
        if(prixcontrat > 8000.0){
            return prixcontrat;
        }
        else{
            Double propal = 0.0;
            if(Choco.equals(fraud)){
                propal = 9000.0;
            } else if(Choco.equals(bollo)){
                propal = 9500.0;
            } else if(Choco.equals(arna)){
                propal = 10000.0;
            }else if(Choco.equals(hypo)){
                propal = 11000.0;
            }
            return propal;
        }
    }

    @Override //@author Henri Roth
    // ### CETTE METHODE N'EST PAS UTILISEE DANS LE CODE ###
    public void notificationNouveauContratCadre(ExemplaireContratCadre contrat) {
        journalCC.ajouter("Nouveau contrat cadre Vendeur" +contrat);
        ContratsVendeur.add(contrat);
        IProduit produit = contrat.getProduit();
        double QuantiteAuStep = contrat.getQuantiteALivrerAuStep();
        double c_v = capacite_vente_max.get(produit);
        c_v -= QuantiteAuStep;
        capacite_vente_max.replace(produit, c_v);
        List<Double> listprix =  super.prixChoco.get(produit);
        listprix.add(contrat.getPrix());
        prixChoco.replace(produit, listprix);
    }

    @Override //@author Henri Roth
    public double livrer(IProduit produit, double quantite, ExemplaireContratCadre contrat) {
        double stockActuel = stockChoco.getQuantityOf(produit);
		double aLivre = Math.min(quantite, stockActuel);
        if(produit == bollo) {
            this.qtté_livraison_bollo += aLivre;
        }
        if(produit == fraud) {
            this.qtté_livraison_fraudau += aLivre;
        }
        if(produit == hypo) {
            this.qtté_livraison_hypo += aLivre;
        }
        if(produit == arna) {
            this.qtté_livraison_arna += aLivre;
        }
		journalCC.ajouter("   Livraison de "+aLivre+" T de "+produit+" sur "+quantite+" exigees pour contrat "+contrat.getNumero());
        stockChoco.remove(produit, aLivre);
		return aLivre;
	}
    
}
