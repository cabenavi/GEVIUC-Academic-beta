import ilog.concert.*;
import ilog.cplex.*;

public class PumpUC{
	                  
	//variables binarias o continuas
	boolean var_bin=true;
	
	/****************************** VARIABLES PROBLEMA OPTIMIZACION *************************************************************************/
	//varibles continuas
	IloNumVar[]	P;  		//P[t]              	//potencia en operacion normal
	
	//variables binarias
	IloNumVar[]     Bg;     //Bg[t]
	
	/******************************* FIN VARIABLES PROBLEMA OPTIMIZACION ********************************************************************************/
	
	//Atributos: todos variables en el tiempo
	//Atributos Pump
	int[]			Id;
	String[]		Nombre;
	double[]        PBomba;
	double[]        KBomba;
	String[]        EmbBomba;
	int[]			Barra;	
	
	//Atributos auxiliares
	double UnitT;
	
	//contructor
	public PumpUC(){
	}
	
	public void InitUC(int[] Id, String[] Nombre, double[] PBomba, double[] KBomba, String[] EmbBomba,  int[] Barra){
	
		//atributos
		this.Id 			= Id;
		this.Nombre 		= Nombre;
		this.PBomba         = PBomba;
		this.KBomba         = KBomba;
		this.EmbBomba       = EmbBomba;
		this.Barra          = Barra;
	
	}
	
	//se cargan datos auxiliares
	public void InitDataUC( double UnitT){
    	
    	this.UnitT	=UnitT;    
	}
	
	//Inicializacion de variables
	public void InitVariables(int[] H,int lp, IloCplex cplex){
		
	    int T= H.length;
	   
    	//nombre variables
    	String[] 		nombreP      = new String[T];
	    String[]    	nombreBg     = new String[T];
    		   
	    for(int t=0;t<T;t++){
		    nombreP[t]   ="Pbo" +"(" + Id[t] +","+(t+1)+")"; 	
	    	nombreBg[t]  ="Bgbo" +"(" + Id[t] +","+(t+1)+")";	
	    }
    	
	    
	    //variables continuas
		P      			= new IloNumVar[T];	
			
		// si defino algunas variables binarias como continuas 
		Bg     			= new IloNumVar[T];
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		try{
			if(lp==1){
				for(int t=0;t<T;t++){
    	    		P[t]   =cplex.numVar(0,Double.MAX_VALUE,nombreP[t]);
    	    		Bg[t]  =cplex.numVar(0,1,nombreBg[t]);
    	    	}
    	    }
		}
		catch (IloException e) {
			System.err.println("Concert Definicion de variables '" + e + "' caught");
		}
		
	}
	
}
	