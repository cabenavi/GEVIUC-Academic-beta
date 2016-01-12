import ilog.concert.*;
import ilog.cplex.*;

public class BusUC{
	
	//****************************** VARIABLES PROBLEMA OPTIMIZACION *************************************************************************/
	//varibles continuas
	IloNumVar[] Theta;       	 //Theta[t]               //Angulo barra
	
	//****************************** VARIABLES PROBLEMA OPTIMIZACION *************************************************************************/
	
	//atributos demanda
	int[]			Id;
	String[]		Nombre;
	int[]			Bus;
	
	//Atributos auxiliares
	double UnitT;
	
	//contructor
	public BusUC(){
	}
	
	public void InitUC(int[] Id, String[] Nombre, int[] Bus){
	
		this.Id					= Id;
		this.Nombre				= Nombre;
		this.Bus				= Bus;
	
	}
	
	//se cargan datos auxiliares
	public void InitDataUC( double UnitT){
    	this.UnitT	=UnitT;    
	}
	
	//Inicializacion de variables
	public void InitVariables(int[] H,int lp, IloCplex cplex){
		
	    int T= H.length;
	   
    	//nombre variables
		String[]		nombreTheta        = new String[T];
		
		for(int t=0;t<T;t++){
			nombreTheta[t]="Theta" +"("+Id[t] +","+(t+1)+")"; 
		}
		
	    //variables continuas
		Theta	= new IloNumVar[T];
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		try{
			if(lp==1){
				for(int t=0;t<T;t++){
					Theta[t]=cplex.numVar(0,Math.PI*2,nombreTheta[t]);
				}
			}
		}
		catch (IloException e) {
			System.err.println("Concert Definicion de variables in Class BusUC '" + e + "' caught");
		}
		
	}
	
	///////////////////////////////////Restricciones/////////////////////////////////////////////////////////////////////////////
	// Sin restricciones individuales
	
	
}
	