import ilog.concert.*;
import ilog.cplex.*;

public class BatteryUC{
	                  
	//variables binarias o continuas
	boolean var_bin=true;
	
	/****************************** VARIABLES PROBLEMA OPTIMIZACION *************************************************************************/
	//varibles continuas
	IloNumVar[]	Ppos;  		//Ppos[t]              	//potencia positiva
	IloNumVar[]	Pneg;  		//Pneg[t]              	//potencia negativa
	IloNumVar[]	Pi;  		//Pi[t]              	//potencia inversor	
	IloNumVar[]	V;	        //V[t]				  	//volumen del estanque en el instante t	 	
	IloNumVar[] Pmin;       //Pmin[t]               //potencia minina, depende del estado de carga
	IloNumVar[][] E;        //E[s][t]               //potencia de cada tramo de la linealizacion de la toma de carga
	IloNumVar[][] B;        //B[s][t]               //variable binaria de cada tramo de la linealizacion de la toma de carga
	IloNumVar[] Ep;         //Ep[t]                 //variable auxiliar para representar el costo por ciclo de la bateria, es positiva
	IloNumVar[] En;         //En[t]                 //variables auxiliar para representar el costo por ciclo de la bateria, es negativa
	
	//variables binarias
	IloNumVar[]     Bpos;     //Bpos[t]            //variable binaria asociada a potencia positiva  
	IloNumVar[]     Bneg;     //Bneg[t]            //variable binaria asociada a potencia negativa
	
	/******************************* FIN VARIABLES PROBLEMA OPTIMIZACION ********************************************************************************/
	
	//Atributos: todos variables en el tiempo
	//Atributos Battery
	int[]			Id;
	String[]		Nombre;
	double[]		Emin;
	double[]		Emax;
	double[]        Pmin_;
	double[]        Pmax;
	double[]        Nid;
	double[]        Nic;
	double[]        Rb;
	String[]        Propietario;
	double[]        Eini;
	double[]        Efin;
	int[]			Ns;
	double[][]      Esocmin;
	double[][]      Esocmax;
	double[][]      Alfa;
    double[][]      Sigma;
	double[]        Pio; 
	double[]        Cb;
	int n_iv;
	
	//Atributos auxiliares
	double UnitT;
	
	//contructor
	public BatteryUC(){
	}
	
	public void InitUC(int[] Id, String[] Nombre, double[] Emin, double[] Emax, double[] Pmin,  double[] Pmax, double[] Nid, double[] Nic, double[] Rb, double[] Eini, double[] Efin, double[][] Esocmin,double[][] Esocmax,double[][] Alfa, double[][] Sigma, double[] Pio,double[] Cb, int[] Ns, int n_iv) {
	
		//atributos
		this.Id 			= Id;
		this.Nombre 		= Nombre;
		this.Emin 			= Emin;
		this.Emax 			= Emax;
		this.Pmin_ 			= Pmin;
		this.Pmax 			= Pmax;
		this.Nid			= Nid;
		this.Nic			= Nic;
		this.Rb				= Rb;
		this.Propietario 	= Propietario;
		this.Eini 			= Eini;
		this.Efin 			= Efin;
		this.Esocmin		= Esocmin;
		this.Esocmax		= Esocmax;
		this.Alfa			= Alfa;
		this.Sigma			= Sigma;
		this.Pio            = Pio;
		this.Cb             = Cb;
		this.Ns				= Ns;
		this.n_iv			= n_iv;
	}
	
	//se cargan datos auxiliares
	public void InitDataUC( double UnitT){
    	
    	this.UnitT	=UnitT;    
	}
	
	//Inicializacion de variables
	public void InitVariables(int[] H,int lp, IloCplex cplex){
		
	    int T= H.length;
	   
    	//nombre variables
    	String[] 		nombrePpos     = new String[T];
	    String[] 		nombrePneg     = new String[T];
	    String[] 		nombrePi       = new String[T]; 
		String[]    	nombreBpos     = new String[T];
    	String[]    	nombreBneg     = new String[T];
		String[]        nombreV        = new String[T];
		String[]        nombrePmin     = new String[T];
		String[][]      nombreE        = new String[n_iv][T];
		String[][]      nombreB        = new String[n_iv][T];
		String[]        nombreEp       = new String[T];	
		String[]        nombreEn       = new String[T];	
    		
		
	    for(int t=0;t<T;t++){
		    nombrePpos[t]   ="Ppos" +"(" + Id[t] +","+(t+1)+")"; 	
	    	nombrePneg[t]   ="Pneg" +"(" + Id[t] +","+(t+1)+")"; 	
	    	nombrePi[t]     ="Pi" +"(" + Id[t] +","+(t+1)+")";
			nombrePmin[t]   ="Pmin" +"(" + Id[t] +","+(t+1)+")"; 		
	    	nombreEp[t]     ="Ep" +"(" + Id[t] +","+(t+1)+")"; 
			nombreEn[t]     ="En" +"(" + Id[t] +","+(t+1)+")"; 
			
			nombreV[t]   ="Vb" +"(" + Id[t] +","+(t+1)+")";
	    	nombreBpos[t]  ="Bpos" +"(" + Id[t] +","+(t+1)+")";	
			nombreBneg[t]  ="Bneg" +"(" + Id[t] +","+(t+1)+")";	
		}
		
		for(int s=0;s<n_iv;s++){
			for(int t=0;t<T;t++){
				nombreE[s][t]="Eb" +"("+Id[t] +"," +(s+1)+","+(t+1)+")"; 
				nombreB[s][t]="Bb" +"("+Id[t] +"," +(s+1)+","+(t+1)+")"; 
			}
		}
    	
	    
	    //variables continuas
		Ppos      			= new IloNumVar[T];	
		Pneg      			= new IloNumVar[T];	
		Pi      			= new IloNumVar[T];	
		Pmin      			= new IloNumVar[T];	
		V				    = new IloNumVar[T];
		E					= new IloNumVar[n_iv][T];
	    Ep					= new IloNumVar[T];
		En					= new IloNumVar[T];
		
		// si defino algunas variables binarias como continuas 
		Bpos     			= new IloNumVar[T];
		Bneg     			= new IloNumVar[T];
		B					= new IloNumVar[n_iv][T];
		
		//calculo de cota para la potencia minima de la bateria
		double pmin_lb1=0;
		double pmin_lb2=0;
		double pmin_lb=0;
		
		for(int t=0;t<T;t++){
			for(int s=0;s<n_iv;s++){
				pmin_lb1=Esocmax[s][t]*Alfa[s][t]-Sigma[s][t];
				pmin_lb2=Esocmin[s][t]*Alfa[s][t]-Sigma[s][t];
				if(pmin_lb1<=pmin_lb2){
					if(pmin_lb1<=pmin_lb){
						pmin_lb=pmin_lb1;
					}
				}
				else{
					if(pmin_lb2<pmin_lb){
						pmin_lb=pmin_lb2;
					}
				}
			}
		}
		
		//calculo de cota para la potencia inversor
		double pmininv_lb=0;
		double pmininv_lb1=0;
		double pmaxinv_ub=0;
		double pmaxinv_ub1=0;
		
		for(int t=0;t<T;t++){
			pmininv_lb1=-Pio[t]+pmin_lb; 
			pmaxinv_ub1=-Pio[t]+Pmax[t]; 
			if(pmininv_lb1<pmininv_lb){
				pmininv_lb=pmininv_lb1; 
			}
			if(pmaxinv_ub1>pmaxinv_ub){
				pmaxinv_ub=pmaxinv_ub1; 
			}
		}
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		try{
			if(lp==1){
				for(int t=0;t<T;t++){
    	    		Ppos[t]   =cplex.numVar(0,Double.MAX_VALUE,nombrePpos[t]);
    	    		Pneg[t]   =cplex.numVar(pmin_lb,0,nombrePneg[t]);
    	    		Pi[t]     =cplex.numVar(pmininv_lb1,pmaxinv_ub,nombrePi[t]);
					Pmin[t]   =cplex.numVar(pmin_lb,0,nombrePmin[t]);
					
					V[t]   =cplex.numVar(0,Double.MAX_VALUE,nombreV[t]);
    	    		Bpos[t]  =cplex.numVar(0,1,nombreBpos[t]);
					Bneg[t]  =cplex.numVar(0,1,nombreBneg[t]);
					
					Ep[t]   =cplex.numVar(0,Emax[t],nombreEp[t]);
					En[t]   =cplex.numVar(-Emax[t],0,nombreEn[t]);
				}
				for(int s=0;s<n_iv;s++){
					for(int t=0;t<T;t++){
						E[s][t]=cplex.numVar(0,(Esocmax[s][t]-Esocmin[s][t])*Emax[t],nombreE[s][t]);
						B[s][t]=cplex.numVar(0,1,nombreB[s][t]);
					}
				}
				
    	    }
		}
		catch (IloException e) {
			System.err.println("Concert Definicion de variables '" + e + "' caught");
		}
		
	}
	
	///////////////////////////////////Restricciones/////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////Restriccion Pmin////////////////////////////////////////////////////////////////////////////////////////////
    	public void restrpmin(IloCplex cplex, int t){
	    	
	    	//System.out.println("restriccion pmin");
			try{
				Pneg[t].setLB(-Pmax[t]);
			}		
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion potencia minima '" + e + "' caught");
			}
		}
		
	//////////////////////////////////Restriccion Pmax///////////////////////////////////////////////////////////////////////////////////////////
		public void restrpmax(IloCplex cplex, int t){
	    	
	    	//System.out.println("restriccion pmax");
			try{	
				Ppos[t].setUB(Pmax[t]);
			}
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion potencia maxima '" + e + "' caught");
			}
		}

	///////////////////////////Tramos aux/////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	public void restrtram0(IloCplex cplex, int t){
			try{
				for(int s=0;s<n_iv;s++){
					for(int ss=s+1;ss<n_iv;ss++){
						cplex.addLe(cplex.diff(B[ss][t],B[s][t]),0,"battery_orden_"+Id[t]+"_"+(s+1)+"_"+(ss+1)+"_"+(t+1));
					}
				}
			}		
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion tramos '" + e + "' caught");
			}
	}
	///////////////////////////Tramos2/////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	public void restrtram2(IloCplex cplex, int t){
			try{
				for(int s=0;s<n_iv;s++){
					if((s+1)<n_iv){
						cplex.addLe(cplex.diff(E[s][t],cplex.prod(B[s][t],(Esocmax[s][t]-Esocmin[s][t])*Emax[t])),0,"battery_pmintramo_u"+Id[t]+"_"+(s+1)+"_"+(t+1));
						cplex.addGe(cplex.diff(E[s][t],cplex.prod(B[s+1][t],(Esocmax[s][t]-Esocmin[s][t])*Emax[t])),0,"battery_pmintramo_l"+Id[t]+"_"+(s+1)+"_"+(t+1));
					}
					else{
						cplex.addLe(cplex.diff(E[s][t],cplex.prod(B[s][t],(Esocmax[s][t]-Esocmin[s][t])*Emax[t])),0,"battery_pmintramo_u"+Id[t]+"_"+(s+1)+"_"+(t+1));
						cplex.addGe(E[s][t],0,"battery_pmintramo_l"+Id[t]+"_"+(s+1)+"_"+(t+1));
					}
				}
			}		
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion tramos '" + e + "' caught");
			}
	}	
	//////////////////////////////////Retriccion potencia minima variable/////////////////////////////////////////////////////////////////////////////////////////////////////	
		public void restrdpmi(IloCplex cplex, int t){
			try{
				IloNumExpr sum_b = cplex.constant(0);
				if(t>0){
					for(int s=0;s<n_iv;s++){
						sum_b = cplex.sum(sum_b,cplex.prod(Alfa[s][t]*Math.pow(Emax[t],-1),E[s][t-1]));
					}	
					cplex.addEq(cplex.diff(Pmin[t],sum_b),Alfa[0][t]*Esocmin[0][t]-Sigma[0][t],"battery_defpmin"+Id[t]+"_"+(t+1));
				}
				//condicion inicial, preproceso se podria hacer antes
				else{
					for(int s=0;s<n_iv;s++){
						if(Eini[t]/Emax[t]>=Esocmin[s][t] & Eini[t]/Emax[t]<=Esocmax[s][t]){
							cplex.addEq(Pmin[t],Alfa[s][t]*Eini[t]/Emax[t]-Sigma[s][t],"battery_defpmin"+Id[t]+"_"+(t+1));
							break;
						}
					}
				}
			}	
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion def pmin '" + e + "' caught");
			}
		}
	//////////////////////////////////Retriccion potencia minima variable/////////////////////////////////////////////////////////////////////////////////////////////////////	
		public void restrpmin2(IloCplex cplex, int t){
			try{
				cplex.addGe(cplex.diff(Pneg[t],Pmin[t]),0,"battery_pmin"+Id[t]+"_"+(t+1));
			}	
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion pmin '" + e + "' caught");
			}
		}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		public void restrbala(IloCplex cplex, int t){
			try{
				IloNumExpr sum_b = cplex.constant(0);
				for(int s=0;s<n_iv;s++){
					sum_b=cplex.sum(sum_b,E[s][t]);
				}
				cplex.addEq(cplex.diff(sum_b,V[t]),-Esocmin[0][t]*Emax[t],"battery_balance"+Id[t]+"_"+(t+1));
			}	
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion tramos '" + e + "' caught");
			}
		}	
	///////////////////////////////Restriccion cota positiva //////////////////////////////////////////////////////////////////////////////////////////////////////////
		public void restrppos(IloCplex cplex, int t){
	    	
	    	//System.out.println("restriccion ppos");
			try{
				cplex.addLe(cplex.diff(Ppos[t],cplex.prod(Pmax[t],Bpos[t])),0,"battery_cota_pos"+Id[t]+"_"+(t+1));
			}
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion cota positiva '" + e + "' caught");
			}
		}
	
	///////////////////////////////Restriccion cota negativa //////////////////////////////////////////////////////////////////////////////////////////////////////////	
		public void restrpneg(IloCplex cplex, int t){
	    	
	    	//System.out.println("restriccion pneg");
			try{
				cplex.addGe(cplex.sum(Pneg[t],cplex.prod(1000,Bneg[t])),0,"battery_cota_neg"+Id[t]+"_"+(t+1));
			}
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion cota negativa '" + e + "' caught");
			}
		}
	///////////////////////////////Variables binarias////////////////////////////////////////////////////////////////////////////////////////////////////////	
		public void restrlogi(IloCplex cplex, int t){
	    	
	    	//System.out.println("restriccion bin");
			try{
				cplex.addLe(cplex.sum(Bpos[t],Bneg[t]),1,"battery_log"+Id[t]+"_"+(t+1));
			}
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion logica '" + e + "' caught");
			}
		}	
	///////////////////////////////Restriccion potencia inversor //////////////////////////////////////////////////////////////////////////////////////////////////////////	
		public void restrpinv(IloCplex cplex, int t){
	    	
	    	//System.out.println("restriccion ppos");
			try{
				cplex.addEq(cplex.sum(cplex.prod(-1, Pi[t]),cplex.prod(Nid[t],Ppos[t]),cplex.prod(Math.pow(Nic[t],-1),Pneg[t])),Pio[t],"battery_potencia_inv"+Id[t]+"_"+(t+1));
			}
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion cota negativa '" + e + "' caught");
			}
		}
	//////////////////////////////Restriccion Capacidad Battery//////////////////////////////////////////////////////////////////////////////
		
		public void restrbanc(IloCplex cplex, int t){
			
			try{
				if(Emax[t]>0 & Nid[t]!=0){
					if(t==0){
						cplex.addEq(cplex.sum(V[t],cplex.prod(UnitT,Ppos[t]),cplex.prod(UnitT,Pneg[t])),Eini[t],"battery_"+Id[t]+"_"+(t+1));
				    
					}
					else{
						cplex.addEq(cplex.sum(V[t],cplex.prod(-1,V[t-1]),cplex.prod(UnitT,Ppos[t]),cplex.prod(UnitT,Pneg[t])),0,"battery_"+Id[t]+"_"+(t+1));
					}
					//cotas
					V[t].setLB(Emin[t]);
					V[t].setUB(Emax[t]);
				
				}
    		}	
    		catch (IloException e){
    			System.err.println("Concert exception estanque" + e + "' caught");
    		}
		}
		
	//////////////////////////////////Volumen Final//////////////////////////////////////////////////////////////////////////////////////////////	
		public void restrvfin(IloCplex cplex, int t){
	    	
	    	//System.out.println("restriccion pmax");
			try{	
				V[t].setLB(Efin[t]);				
			}
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion volumen final '" + e + "' caught");
			}
		}
	//////////////////////////////////Ciclo bateria//////////////////////////////////////////////////////////////////////////////////////////////	
		public void restrcicl(IloCplex cplex, int t){
	    	
	    	//System.out.println("restriccion pmax");
			try{
				if(t==0){
					cplex.addEq(cplex.sum(V[t],cplex.prod(-1,Ep[t]),cplex.prod(-1,En[t])),Eini[t],"battery_ciclo_"+Id[t]+"_"+(t+1));
				}
				else{
					cplex.addEq(cplex.sum(cplex.diff(V[t],V[t-1]),cplex.prod(-1,Ep[t]),cplex.prod(-1,En[t])),0,"battery_ciclo_"+Id[t]+"_"+(t+1));
				}			
			}
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion volumen final '" + e + "' caught");
			}
		}
}
	