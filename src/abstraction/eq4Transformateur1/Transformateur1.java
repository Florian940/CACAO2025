package abstraction.eq4Transformateur1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;

/**
 * @author YAOU Reda
 */
public class Transformateur1 extends Transformateur1AcheteurAppelDOffre {

    public Transformateur1() {
        super();
    }

    public void next() {
        super.next();
        
        // On met à jour la répartition de prodMax sur les chocolats selon ce qui se vend le plus
        HashMap<Chocolat, Double> map = new HashMap<Chocolat, Double>();
        for (ChocolatDeMarque cm : chocolatsLimDt) {
            map.put(cm.getChocolat(), stocksMarqueVar.get(cm).getValeur());
        }
        List<Map.Entry<Chocolat, Double>> sortedEntries = new ArrayList<>(map.entrySet());
        sortedEntries.sort(Map.Entry.comparingByValue());

        Chocolat minKey = sortedEntries.get(0).getKey();
        Chocolat secondSmallestKey = sortedEntries.get(1).getKey();
        Chocolat secondLargestKey = sortedEntries.get(2).getKey();
        Chocolat maxKey = sortedEntries.get(3).getKey();

        if (repartitionTransfo.get(minKey).getValeur() + 0.1 < 0.999 && repartitionTransfo.get(maxKey).getValeur() - 0.1 > 0.001 && stocksMarqueVar.get(getChocolatDeMarque(minKey)).getValeur() < stocksMarqueVar.get(getChocolatDeMarque(maxKey)).getValeur()) {
            this.repartitionTransfo.get(minKey).setValeur(this, repartitionTransfo.get(minKey).getValeur() + 0.1);
            this.repartitionTransfo.get(maxKey).setValeur(this, repartitionTransfo.get(maxKey).getValeur() - 0.1);
        } 
        if (repartitionTransfo.get(secondSmallestKey).getValeur() + 0.05 < 0.999 && repartitionTransfo.get(secondLargestKey).getValeur() - 0.05 > 0.001 && stocksMarqueVar.get(getChocolatDeMarque(secondSmallestKey)).getValeur() < stocksMarqueVar.get(getChocolatDeMarque(secondLargestKey)).getValeur()) {
            this.repartitionTransfo.get(secondSmallestKey).setValeur(this, repartitionTransfo.get(secondSmallestKey).getValeur() + 0.05);
            this.repartitionTransfo.get(secondLargestKey).setValeur(this, repartitionTransfo.get(secondLargestKey).getValeur() - 0.05);
        } 

        //Mise à jour des qtt sortantes maintenant que nous avons de nouveaux contrats cadres et que toutes les transactions ont été effectuées
        this.determinerQttSortantChoco();

        //Remise à zéro des ventes par transactions : 
        for(Chocolat c : lesChocolats){
            this.qttSortantesTransactions.put(c, 0.);
        }
        
        totalSortant.setValeur(this, 0.);
        for (Chocolat c : lesChocolats){
            totalSortant.ajouter(this, qttSortantesChoco.get(c));
        }

        //on réinitialise coutProd
        this.coutProd = 0;
    }
}
