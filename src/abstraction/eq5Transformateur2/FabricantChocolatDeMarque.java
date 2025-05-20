// Nils Rossignol
package abstraction.eq5Transformateur2;

import abstraction.eqXRomu.filiere.IFabricantChocolatDeMarque;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import java.util.ArrayList;
import java.util.List;

public class FabricantChocolatDeMarque extends AcheteurBourse  implements IFabricantChocolatDeMarque {

    private List<ChocolatDeMarque> chocolatsProduits;

    public FabricantChocolatDeMarque() {
        super(); 
        this.chocolatsProduits = new ArrayList<>();
        
        // On ajoute les chocolats produits par le fabricant, leur qualité, et leur quantité de cacao
        this.chocolatsProduits.add(new ChocolatDeMarque(Chocolat.C_HQ_BE, "ChocoParfait",100));
        this.chocolatsProduits.add(new ChocolatDeMarque(Chocolat.C_HQ_E, "ChocoHautEq",100));
        this.chocolatsProduits.add(new ChocolatDeMarque(Chocolat.C_MQ_E, "ChocoMoyenEq",100));
        this.chocolatsProduits.add(new ChocolatDeMarque(Chocolat.C_MQ, "ChocoMoyen",100));

    }

    @Override
    public List<ChocolatDeMarque> getChocolatsProduits() {
        return this.chocolatsProduits;
    }
}
