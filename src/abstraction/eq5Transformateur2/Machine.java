//Nils Rossignol
package abstraction.eq5Transformateur2;


import abstraction.eqXRomu.filiere.Filiere;


public class Machine extends Employes{
    
    private double nbrMachineTotal;
    private double capaciteProduction;
    private double prixMachine;
    private double pourcentageRevente;
    
    public Machine(){
        // Chiffres fixés dans le réglement des transformateurs
        super();
        this.nbrMachineTotal=128;
        this.capaciteProduction= 10000000; // par step et en tablette
        this.prixMachine =500000;
        this.pourcentageRevente= 0.6;
    }
    
    public void achatMachine(double nbrMachine){
        // On met à jour le nombre de machines total
        this.nbrMachineTotal += nbrMachine;
        // On met à jour le coût de le compte en banque
        Filiere.LA_FILIERE.getBanque().payerCout(this ,super.cryptogramme,  "Machine",nbrMachineTotal*prixMachine);

    }
    public double getCapaciteProductionMachines(){
        // On modifie la capacité de production totale en fonction du nombre de machines
        return this.capaciteProduction*this.nbrMachineTotal;
    }
    
    
}
