import ilog.concert.*;
import ilog.cplex.*;

public class LineUC{
	                  
	/****************************** VARIABLES PROBLEMA OPTIMIZACION *************************************************************************/
	//varibles continuas
	IloNumVar[] F;       	 //F[t]               //Flujo por la linea
	IloNumVar[][] Fp;        //Fp[s][t]              //Flujo por la linea linealizado por tramo
	IloNumVar[][] Fn;        //Fn[s][t]              //Flujo por la linea linealizado por tramo
	
	/******************************* FIN VARIABLES PROBLEMA OPTIMIZACION ********************************************************************************/
	
	//Atributos: todos variables en el tiempo
	int[]			ID;
	String[]		Nombre;
	int[]			BusIni;
	int[]			BusFin;
	double[]		Resistencia;
	double[]		Reactancia;
	double[]		Largo;
	double[]		Voltaje;
	double[]		Fmax;
	double[]		Fmin;
	String[]		Propietario;
	double[][]		matriz_alpha;
	int [] 			tramos_maximo;  //tramos maximos para linealizar perdidas 
	String[]		Opera;			//si la linea opera o no opera

	double			Sbase; 
	
	
	//Atributos auxiliares
	double UnitT;
	
	//contructor
	public LineUC(){
	}
	
	public void InitUC(int[] ID, String[] Nombre, int[] BusIni, int[] BusFin, double[] Resistencia, double[] Reactancia, double[] Largo, double[] Voltaje, double[] Fmin, double[] Fmax, String[] Propietario, double[][] matriz_alpha,int[] tramos_maximo,String[]	Opera) {
	
		this.ID					= ID;
		this.Nombre				= Nombre;
		this.BusIni				= BusIni;
		this.BusFin				= BusFin;
		this.Resistencia		= Resistencia;
		this.Reactancia			= Reactancia;
		this.Largo				= Largo;
		this.Voltaje			= Voltaje;
		this.Fmax				= Fmax;
		this.Fmin				= Fmin;
		this.Propietario		= Propietario;
		this.matriz_alpha		= matriz_alpha;
		this.Sbase				= 100;
		this.tramos_maximo		= tramos_maximo;  //tramos maximos para linealizar perdidas 
		this.Opera				= Opera;			//si la linea opera o no opera

	}
	
	//se cargan datos auxiliares
	public void InitDataUC( double UnitT){
    	this.UnitT	=UnitT;    
	}
	
	//Inicializacion de variables
	public void InitVariables(int[] H,int lp, IloCplex cplex){
		
	    int T= H.length;
	   
    	//nombre variables
		String[]		nombreF        = new String[T];
		String[][]		nombreFp       = new String[matriz_alpha.length][T];
		String[][]		nombreFn       = new String[matriz_alpha.length][T];
    	
		for(int t=0;t<T;t++){
			nombreF[t]="F" +"("+ID[t] +","+(t+1)+")"; 
		}
		
		for(int t=0;t<T;t++){
			for (int s=0; s<matriz_alpha.length;s++){
				nombreFp[s][t]="Fp" +"("+ID[t] + ","+(s+1) +","+(t+1)+")";
				nombreFn[s][t]="Fn" +"("+ID[t] + ","+(s+1) +","+(t+1)+")";
			}
		}
		
	    //variables continuas
		F	= new IloNumVar[T];
		Fp	= new IloNumVar[matriz_alpha.length][T];
		Fn	= new IloNumVar[matriz_alpha.length][T];
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		try{
			if(lp==1){
				for(int t=0;t<T;t++){
					F[t]=cplex.numVar(Fmin[t],Fmax[t],nombreF[t]);
				}
				for(int t=0;t<T;t++){
					for (int s=0; s<matriz_alpha.length;s++){
						Fp[s][t]=cplex.numVar(0,Fmax[t]/matriz_alpha.length,nombreFp[s][t]);
						Fn[s][t]=cplex.numVar(0,Fmax[t]/matriz_alpha.length,nombreFn[s][t]);
					}
				}
			}
		}
		catch (IloException e) {
			System.err.println("Concert Definicion de variables '" + e + "' caught");
		}
		
	}
	
	///////////////////////////////////Restricciones/////////////////////////////////////////////////////////////////////////////
	// Sin restricciones individuales
	
	
}
	