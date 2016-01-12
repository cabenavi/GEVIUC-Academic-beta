import ilog.concert.*;
import ilog.cplex.*;

public class BanderasUC{
	
	//****************************** VARIABLES PROBLEMA OPTIMIZACION *************************************************************************/
	//varibles continuas

	
	//****************************** VARIABLES PROBLEMA OPTIMIZACION *************************************************************************/
	
	//atributos demanda
	int[]			Id_Banderas;
	String[]		Bandera;
	String[]		EstadoBanderas;
	
	//Atributos auxiliares
	double UnitT;
	
	//contructor
	public BanderasUC(){
	}
	
	public void InitUC(int[] Id_Banderas, String[] Bandera, String[] EstadoBanderas){
	
		this.Id_Banderas				= Id_Banderas;
		this.Bandera					= Bandera;
		this.EstadoBanderas				= EstadoBanderas;
	
	}
	
	//se cargan datos auxiliares
	public void InitDataUC( double UnitT){
    	this.UnitT	=UnitT;    
	}
	
	//Inicializacion de variables
	public void InitVariables(int[] H,int lp, IloCplex cplex){
		
	    int T= H.length;
	   
    	//nombre variables
	
		
	    //variables continuas
	
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
		
	}
	
	///////////////////////////////////Restricciones/////////////////////////////////////////////////////////////////////////////
	// Sin restricciones individuales
	
	
}
	