import ilog.concert.*;
import ilog.cplex.*;

public class LoadUC{
	                  
	/****************************** VARIABLES PROBLEMA OPTIMIZACION *************************************************************************/
	//varibles continuas
	IloNumVar[][] Ens;        //ENS[s][t]               //energia no suministrada por tramo s
	IloNumVar[] Dr;			  //Dr[t]					//demanda real	
	IloNumVar[] Cd;			  //Cd[t]					//coeficiente de ajuste	
	IloNumVar[] Eperd;        //Eperd[t]				//energia perdida
	/******************************* FIN VARIABLES PROBLEMA OPTIMIZACION ********************************************************************************/
	
	//Atributos: todos variables en el tiempo
	//Atributos ENS
	int[]			Id;
	String[]		Nombre;
	double[] 		Load;
	int[]			Bus;
	double[][]		Ensmin;
	double[][]		Ensmax;
	double[][]		Alfa;
	double[][]		Beta;
	double[]        Cdmin;
    double[]        Cdmax;
	double[]        Eperdmin;
	double[]        Eperdmax;
	double[]        Cpperd;
    int[]           Td;  
	int[]           Ns;           
	int n_iv;
	
	//Atributos auxiliares
	double UnitT;
	
	//contructor
	public LoadUC(){
	}
	
	public void InitUC(int[] Id, String[] Nombre, double[] Load, int[] Bus, double[][] Ensmin, double[][] Ensmax, double[][] Alfa, double Beta[][], double[] Cdmin, double[] Cdmax, double[] Eperdmin, double[] Eperdmax, double[] Cpperd, int[] Td, int[] Ns, int n_iv) {
	
		//atributos
		this.Id 			= Id;
		this.Nombre 		= Nombre;
		this.Load           = Load;
		this.Bus			= Bus;
		this.Ensmin 		= Ensmin;
		this.Ensmax 		= Ensmax;
		this.Alfa 			= Alfa;
		this.Beta 			= Beta;
		this.Cdmin			= Cdmin;
		this.Cdmax			= Cdmax;
		this.Eperdmin		= Eperdmin;
		this.Eperdmax		= Eperdmax;
		this.Cpperd			= Cpperd;
		this.Td				= Td;  
		this.Ns 			= Ns;
		this.n_iv			= n_iv;
	}
	
	//se cargan datos auxiliares
	public void InitDataUC(){
    	
	}
	
	//Inicializacion de variables
	public void InitVariables(int[] H,int lp, IloCplex cplex){
		
	    int T= H.length;
	   
    	//nombre variables
		String[][]      nombreEns        = new String[n_iv][T];
    	String[]        nombreDr         = new String[T];
		String[]        nombreCd         = new String[T];
		String[]        nombreEperd      = new String[T];
		
		for(int s=0;s<n_iv;s++){
			for(int t=0;t<T;t++){
				nombreEns[s][t]="PNS" +"("+Id[t] +"," +(s+1)+","+(t+1)+")"; 
			}
		}
		for(int t=0;t<T;t++){
			nombreDr[t]="Dr" +"("+Id[t]+","+(t+1)+")"; 
			nombreCd[t]="Cd" +"("+Id[t]+","+(t+1)+")"; 
			nombreEperd[t]= "Perd" +"("+Id[t]+","+(t+1)+")"; 
			
		}
	    //variables continuas
		Ens		= new IloNumVar[n_iv][T];
	    Dr		= new IloNumVar[T];
		Cd		= new IloNumVar[T];
		Eperd	= new IloNumVar[T];
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		try{
			if(lp==1){
				for(int s=0;s<n_iv;s++){
					for(int t=0;t<T;t++){
						Ens[s][t]=cplex.numVar(0,(Ensmax[s][t]-Ensmin[s][t]),nombreEns[s][t]);
					}
				}
				for(int t=0;t<T;t++){
					Dr[t]=cplex.numVar(0,Double.MAX_VALUE,nombreDr[t]);
					Cd[t]=cplex.numVar(0,Double.MAX_VALUE,nombreCd[t]);
					Eperd[t]=cplex.numVar(Eperdmin[t],Eperdmax[t],nombreEperd[t]);
				}
				
    	    }
		}
		catch (IloException e) {
			System.err.println("Concert Definicion de variables '" + e + "' caught");
		}
		
	}
	
	///////////////////////////////////Restricciones/////////////////////////////////////////////////////////////////////////////
	
	///////////////////////////////////Demanda real//////////////////////////////////////////////////////////////////////////////
	public void restrdrea(IloCplex cplex, int t){
		try{
			if(Cdmin[t] != 1 || Cdmax[t] != 1 ){
				cplex.addEq(cplex.diff(Dr[t],cplex.prod(Load[t],Cd[t])),0,"demanda_real_"+Id[t]+"_"+(t+1));	
			}
		}		
		catch (IloException e) {
			System.err.println("Concert exception: Demanda real'" + e + "' caught");
		}
	
	}
	//////////////////////////////////////////////Cd maximo/////////////////////////////////////////////////////////////////////////////////
	public void restrcmax(IloCplex cplex, int t){
		try{
			if(Cdmin[t] != 1 || Cdmax[t] != 1 ){
				Cd[t].setUB(Cdmax[t]);
			}
		}		
		catch (IloException e) {
			System.err.println("Concert exception: Cd maximo'" + e + "' caught");
		}
	
	}
	//////////////////////////////////////////////Cd minimo/////////////////////////////////////////////////////////////////////////////////
	public void restrcmin(IloCplex cplex, int t){
		try{
			if(Cdmin[t] != 1 || Cdmax[t] != 1 ){
				Cd[t].setLB(Cdmin[t]);
			}
		}		
		catch (IloException e) {
			System.err.println("Concert exception: Cd minimo'" + e + "' caught");
		}
	
	}
}
	