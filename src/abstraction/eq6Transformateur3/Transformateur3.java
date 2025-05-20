// Henri ROTH, Florian MALVEAU
package abstraction.eq6Transformateur3;
import java.util.ArrayList;
import java.util.List;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;

public class Transformateur3 extends Transformateur3AO{

	public Transformateur3() {
	}

	public void initialiser(){
		super.initialiser();
	}

	public void next(){
		super.next();
		super.jdb.ajouter("NEXT - TRANSFORMATEUR3");

		super.actualiserEcheanciers();
		super.displayAllStratQuantityData();
		//stockChoco.addToStock(super.lesChocolats.get(0), 500.0);
		//stockChoco.addToStock(lesChocolats.get(1), 800.0);

		//stockFeves.remove(abstraction.eqXRomu.produits.Feve.F_BQ, 100.0);

		//Récupération des stocks de fèves et choco
		double stockTotal = super.stockFeves.getStockTotal()+super.stockChoco.getStockTotal();
		//On paye les coûts de stockage

		
		super.coutStockage = 4*Filiere.LA_FILIERE.getParametre("cout moyen stockage producteur").getValeur();
		super.LaBanque.payerCout(this, super.cryptogramme, "Coûts de stockage", super.coutStockage*stockTotal);
		
		super.jdb.ajouter("Stock total : "+stockTotal);
		double div = 1000.0;
		String suff = "k€";
		super.jdb.ajouter("Payement Coût stockage : "+Math.round(super.coutStockage*stockTotal/div)+suff);

		//Estimation coût par fèves

		super.stockFeves.display();
		super.stockChoco.display();
		super.jdb.ajouter("");//Saut de ligne de fin de next
	}

	public List<Journal> getJournaux() {
		ArrayList<Journal> res = new ArrayList<Journal>();
		res.add(super.jdb);
		res.add(super.journalProduction);
		res.add(super.journalStock);
		res.add(super.journalCC);
		res.add(super.journalBourse);
		res.add(super.journalAO);
		res.add(super.journalStrat);
		return res;
	}

	public List<Variable> getIndicateurs() {
		List<Variable> res =  new ArrayList<Variable>();
		res.add(super.eq6_Q_BQ_0);
		res.add(super.eq6_Q_BQ_1);
		res.add(super.eq6_Q_MQ_0);
		res.add(super.eq6_Q_MQ_1);
		res.add(super.eq6_Q_HQ_1);
		res.add(super.eq6_Q_HQ_2);
		res.add(super.eq6_Q_Fraudo);
		res.add(super.eq6_Q_Bollo);
		res.add(super.eq6_Q_Hypo);
		res.add(super.eq6_Q_Arna);
		return res;
	}
}
