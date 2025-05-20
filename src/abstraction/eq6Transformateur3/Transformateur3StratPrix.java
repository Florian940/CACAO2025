package abstraction.eq6Transformateur3;

import java.util.HashMap;
import java.util.List;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;

// @author Henri Roth
public class Transformateur3StratPrix {
    
    protected ChocolatDeMarque fraud;
    protected ChocolatDeMarque hypo;
    protected ChocolatDeMarque arna;
    protected ChocolatDeMarque bollo;
    public Transformateur3StratPrix(){
        this.fraud = new ChocolatDeMarque(Chocolat.C_BQ, "Fraudolat", 30);
        this.hypo = new ChocolatDeMarque(Chocolat.C_HQ_E, "Hypocritolat", 100);
        this.arna =  new ChocolatDeMarque(Chocolat.C_MQ, "Arnaquolat", 50);
        this.bollo = new ChocolatDeMarque(Chocolat.C_BQ_E, "Bollorolat", 30);
    }

    /**
     * Methode appelée par l'acteur pour calculer le prix d'achat d'une fève en question.
     * Tout d'abord, on calcule en fonction de l'historique de vente,
     * en prenant les cinq premiers prix les plus
     * intéressants pour nous et on en fait une moyenne puis on compare avec le prix de la bourse.
     * On prend le meilleur des deux avec une petite réduction si c'est pour émettre un contrat cadre.
     * @param prixFeve
     * @param feve
     * @return Retourne une estimation du prix à la tonne auquel on pourrait acheter la fève en question
     */
    public double PrixFeve(HashMap<IProduit, List<Double>> prixFeve, Feve feve){
        List<Double> prix = prixFeve.get(feve);
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
                for(int i = 0; i < step_actuel; i++){
                    meilleurs_prix_histo += prix.get(longueur - i - 1);
                }
                meilleurs_prix_histo = meilleurs_prix_histo/step_actuel;
            }
        }
        if(meilleurs_prix_histo == 0){
            if(feve.equals(Feve.F_BQ)){
                return 3000;
            } else if(feve.equals(Feve.F_BQ_E)){
                return 3500;
            } else if(feve.equals(Feve.F_MQ)){
                return 3500;
            }else if(feve.equals(Feve.F_MQ_E)){
                return 4000;
            } else if(feve.equals(Feve.F_HQ_E)){
                return 4500;
            }
        }
        return meilleurs_prix_histo;
    }
    /**
     * Méthode appelé par l'acteur pour avoir une estimation du prix à la tonne du chocolat en question,
     * on calcule uniquement avec l'historique des ventes
     * @param prixChoco
     * @param Choco
     * @param PrixProd
     * @param CapaProd
     * @return Retourne une estimation du prix à la tonne de choco
     */
    public double PrixChoco(HashMap<IProduit, List<Double>> prixChoco, IProduit Choco, Double CapaProd){
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
                prixinit = 10500.0;
            } else if(Choco.equals(arna)){
                prixinit = 11000.0;
            }else if(Choco.equals(hypo)){
                prixinit = 12000.0;
            }
            return prixinit;
        }
        return meilleurs_prix_histo;

    //public double contrePropal(){}
}
}