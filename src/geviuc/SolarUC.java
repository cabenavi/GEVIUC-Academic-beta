import ilog.concert.*;
import ilog.cplex.*;

public class SolarUC{
	                  
	//variables binarias o continuas
	boolean var_bin=true;
	
	/****************************** VARIABLES PROBLEMA OPTIMIZACION *************************************************************************/
	//varibles continuas
	IloNumVar[]	Psol;  		//Psol[t]              	 //potencia eolica
	IloNumVar[]	CPF;	    //CPF[t] (ex Rg)		 //control primario de frecuencia
	IloNumVar[] Rg;     	//Rg[t]	(ex Rg_sec)	 	 //reserva giro 1
	IloNumVar[] Rg2;		//Rg2[t]				 //reserva giro 2
	
	/******************************* FIN VARIABLES PROBLEMA OPTIMIZACION ********************************************************************************/
	
	//Atributos: todos variables en el tiempo
	//atributos Solar
	int[]			Id;
	String[]		Nombre;
	double[]		PotenciaSolar;
	String[]        Propietario;
	double[]		CPFmax;   		// Control Primario de Frecuencia, valor maximo expresado en 0/1 (ex Rgmax)
	double[]		SpinningMax; 	// Reserva en giro 1, valor maximo expresado en 0/1 
	double[]		SpinningMax2; 	// Reserva en giro 2, valor maximo expresado en 0/1 
	int[]			IsERNC;
	int[]			Barra;
	
	
	//Atributos auxiliares
	double UnitT;
	int H[];
	
	//contructor
	public SolarUC(){
	}
	
	public void InitUC(int[] Id, String[] Nombre, double[] PotenciaSolar, String[] Propietario, double[] CPFmax, double[] SpinningMax, double[] SpinningMax2, int[] IsERNC, int[] Barra){
	
		//atributos
		this.Id				= Id;
		this.Nombre			= Nombre;
		this.PotenciaSolar	= PotenciaSolar;
		this.Propietario	= Propietario;
		this.CPFmax			= CPFmax;
		this.SpinningMax	= SpinningMax;
		this.SpinningMax2	= SpinningMax2;
		this.IsERNC			= IsERNC;
		this.Barra			= Barra;
	}
	
	//se cargan datos auxiliares
	public void InitDataUC( double UnitT){
    	
    	this.UnitT	=UnitT;    
	}
	
	//Inicializacion de variables
	public void InitVariables(int[] H,int lp, IloCplex cplex){
		
		int T	= H.length;
		this.H	= H;
		
		
    	//nombre variables
    	String[] 		nombrePsol     	= new String[T];
	    String[] 		nombreCPF       = new String[T];
	    String[]		nombreRg   		= new String[T];
		 String[]		nombreRg2   	= new String[T];
   
	    for(int t=0;t<T;t++){
		    nombrePsol[t]   	= "Psol" +"(" + Id[t] +","+(t+1)+")";
			nombreCPF[t]   		= "CPFsol" +"(" + Id[t] +","+(t+1)+")"; 		
			nombreRg[t]   		= "Rgsol" +"(" + Id[t] +","+(t+1)+")"; 
			nombreRg2[t]   		= "Rg2sol" +"(" + Id[t] +","+(t+1)+")"; 	
		}
		    
	    //variables continuas
		Psol      			= new IloNumVar[T];	
		CPF      			= new IloNumVar[T];	
		Rg   		   		= new IloNumVar[T];	
		Rg2   		   		= new IloNumVar[T];
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		try{
			if(lp==1){
				for(int t=0;t<T;t++){
    	    		Psol[t]   	= cplex.numVar(0,PotenciaSolar[t],nombrePsol[t]);
    	    		CPF[t]   	= cplex.numVar(0,CPFmax[t]*PotenciaSolar[t],nombreCPF[t]);
					Rg[t]   	= cplex.numVar(0,SpinningMax[t]*PotenciaSolar[t],nombreRg[t]);
					Rg2[t]   	= cplex.numVar(0,SpinningMax2[t]*PotenciaSolar[t],nombreRg2[t]);
				}
			}
		}
		catch (IloException e) {
			System.err.println("Error: Definicion de variables planta solar'" + e + "' caught");
		}
		
	}
	
	///////////////////////////////////Restricciones/////////////////////////////////////////////////////////////////////////////
		
		//Restriccion pmax y tipos de reserva que puede aportar
		public void restrpmax(IloCplex cplex, int t){
			try{
				if(CPFmax[t]>0){
					cplex.addLe(cplex.sum(Psol[t],CPF[t]),PotenciaSolar[t],"pmax_solar_CPF_"+Id[t]+"_"+(t+1));
				}
				if (SpinningMax[t]>0){
					cplex.addLe(cplex.sum(Psol[t],Rg[t]),PotenciaSolar[t],"pmax_solar_reserva_giro1_"+Id[t]+"_"+(t+1));
				}
				if (SpinningMax2[t]>0){
					cplex.addLe(cplex.sum(Psol[t],Rg2[t]),PotenciaSolar[t],"pmax_solar_reserva_giro2_"+Id[t]+"_"+(t+1));
				}
			}
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion potencia maxima solar'" + e + "' caught");
			}
		}
	}
	