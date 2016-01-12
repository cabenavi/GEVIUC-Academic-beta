import ilog.concert.*;
import ilog.cplex.*;

public class EolicUC{
	                  
	//variables binarias o continuas
	boolean var_bin=true;
	
	/****************************** VARIABLES PROBLEMA OPTIMIZACION *************************************************************************/
	//varibles continuas
	IloNumVar[]	Peol;  		//Pel[t]              	//potencia eolica
	IloNumVar[]	CPF;	    //CPF[t]				//control primario de frecuencia
	IloNumVar[] Rg;    		//Rg[t]					//reserva en giro 1
	IloNumVar[] Rg2;    	//Rg2[t]				//reserva en giro 2
	
	/******************************* FIN VARIABLES PROBLEMA OPTIMIZACION ********************************************************************************/
	
	//Atributos: todos variables en el tiempo
	//atributos Eolic
	int[]			Id;
	String[]		Nombre;
	double[]		Viento;
	double[]		PotenciaEolic;
	double[]        FEolic;
	String[]        Propietario;
	double[]		CPFmax;
	double[]		SpinningMax;
	double[]		SpinningMax2;
	int[]			IsERNC;
	int[]			Barra;
	
	//Atributos auxiliares
	double UnitT;
	int H[];
	
	//contructor
	public EolicUC(){
	}
	
	public void InitUC(int[] Id, String[] Nombre, double[] Viento, double[] PotenciaEolic, double[] FEolic, String[] Propietario, double[] CPFmax, double[] SpinningMax, double[] SpinningMax2, int[] IsERNC, int[] Barra){
	
		//atributos
		this.Id				= Id;
		this.Nombre			= Nombre;
		this.Viento			= Viento;
		this.PotenciaEolic	= PotenciaEolic;
		this.FEolic			= FEolic;
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
    	String[] 		nombrePeol     = new String[T];
		String[] 		nombreCPF      = new String[T];
	    String[] 		nombreRg       = new String[T];
	    String[]		nombreRg2	   = new String[T];
   
	    for(int t=0;t<T;t++){
		    nombrePeol[t]   	= "Peol" +"(" + Id[t] +","+(t+1)+")";
			nombreCPF[t]   		= "CPFeol" +"(" + Id[t] +","+(t+1)+")"; 		
			nombreRg[t]   		= "Rgeol" +"(" + Id[t] +","+(t+1)+")"; 		
			nombreRg2[t]   		= "Rg2eol" +"(" + Id[t] +","+(t+1)+")"; 		
		}
		    
	    //variables continuas
		Peol      			= new IloNumVar[T];	
		CPF					= new IloNumVar[T];	
		Rg      			= new IloNumVar[T];	
		Rg2		      		= new IloNumVar[T];	
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		try{
			if(lp==1){
				for(int t=0;t<T;t++){
    	    		Peol[t]   	= cplex.numVar(0,PotenciaEolic[t],nombrePeol[t]);
					CPF[t]   	= cplex.numVar(0,CPFmax[t]*PotenciaEolic[t],nombreCPF[t]);
    	    		Rg[t]   	= cplex.numVar(0,SpinningMax[t]*PotenciaEolic[t],nombreRg[t]);
					Rg2[t]   	= cplex.numVar(0,SpinningMax2[t]*PotenciaEolic[t],nombreRg2[t]);
				}
			}
		}
		catch (IloException e) {
			System.err.println("Concert Definicion de variables '" + e + "' caught");
		}
		
	}
	
	///////////////////////////////////Restricciones/////////////////////////////////////////////////////////////////////////////
		
		//Restriccion pmax
		public void restrpmax(IloCplex cplex, int t){
			try{
				if(CPFmax[t]>0){
					cplex.addLe(cplex.sum(Peol[t],CPF[t]),PotenciaEolic[t],"pmax_eolica_CPF_"+Id[t]+"_"+(t+1));
				}
				if (SpinningMax[t]>0){
					cplex.addLe(cplex.sum(Peol[t],Rg[t]),PotenciaEolic[t],"pmax_eolica_reserva_giro1_"+Id[t]+"_"+(t+1));
				}
				if (SpinningMax2[t]>0){
					cplex.addLe(cplex.sum(Peol[t],Rg2[t]),PotenciaEolic[t],"pmax_eolica_reserva_giro2_"+Id[t]+"_"+(t+1));
				}
			}
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion potencia maxima eolica'" + e + "' caught");
			}
		}
	
}
	