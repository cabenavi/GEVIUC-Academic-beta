import ilog.concert.*;
import ilog.cplex.*;

public class CSPUC{
	                  
	//variables binarias o continuas
	boolean var_bin=true;
	
	/****************************** VARIABLES PROBLEMA OPTIMIZACION *************************************************************************/
	//varibles continuas
	IloNumVar[]	Pel1;  		//Pel1[t]              	//potencia electrica inyectada directamente, sin pasar por almacenamiento
	IloNumVar[]	Pel2;  		//Pel2[t]              	//potencia electrica inyectada, extraida de la capacidad de almacenamiento
	IloNumVar[]	Palm;  		//Palm[t]              	//potencia electrica almacenada, obtenida a partir de la radiacion solar
		
	IloNumVar[]	V;	        //V[t]				  	//volumen del estanque en el instante t	 	
	
	//variables binarias
	IloNumVar[]     Bel1;     //Bel1[t]            //variable binaria asociada a la potencia electrica  
	IloNumVar[]     Balm;     //Balm[t]            //variable binaria asociada a la potencia de almacenamiento
	IloNumVar[]		Bg;		  //Bg[t]			   //variable binaria asociada encendido o apagado de CSP
	

	/******************************* FIN VARIABLES PROBLEMA OPTIMIZACION ********************************************************************************/
	
	//Atributos: todos variables en el tiempo
	//Atributos CSP
	int[]			Id;
	String[]		Nombre;
	double[]		PotenciaSolar;
	double[]		PerdidasAlmacenamiento;
	double[]        EficienciaAlmacenamiento;
	double[]        EficienciaInyeccion;
	double[]        Vmin;
	double[]        Vmax;
	String[]        Propietario;
	double[]		Vini;
	double[]		Vfin;
	double[]		Pmin;
	double[]		Pmax;
	int[]			IsERNC;
	
	//Atributos auxiliares
	double UnitT;
	int H[];
	
	//contructor
	public CSPUC(){
	}
	
	public void InitUC(int[] Id, String[] Nombre, double[] PotenciaSolar, double[] PerdidasAlmacenamiento, double[] EficienciaAlmacenamiento, double[] EficienciaInyeccion, double[] Vmin, double[] Vmax, String[] Propietario, double[] Vini, double[] Vfin, double[] Pmin, double[] Pmax, int[] IsERNC){
	
		//atributos
		this.Id							= Id;
		this.Nombre						= Nombre;
		this.PotenciaSolar				= PotenciaSolar;
		this.PerdidasAlmacenamiento		= PerdidasAlmacenamiento;
		this.EficienciaAlmacenamiento	= EficienciaAlmacenamiento;
		this.EficienciaInyeccion		= EficienciaInyeccion;
		this.Vmin						= Vmin;
		this.Vmax						= Vmax;
		this.Propietario				= Propietario;
		this.Vini						= Vini;
		this.Vfin						= Vfin;
		this.Pmin						= Pmin;
		this.Pmax						= Pmax;
		this.IsERNC						= IsERNC;
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
    	String[] 		nombrePel1     = new String[T];
	    String[] 		nombrePel2     = new String[T];
	    String[] 		nombrePalm     = new String[T]; 
		String[]    	nombreBel1     = new String[T];
    	String[]    	nombreBalm     = new String[T];
		String[]        nombreV        = new String[T];
		String[]    	nombreBg       = new String[T];
   
	    for(int t=0;t<T;t++){
		    nombrePel1[t]   = "Pel1" +"(" + Id[t] +","+(t+1)+")";
			nombrePel2[t]   = "Pel2" +"(" + Id[t] +","+(t+1)+")"; 		
	    	nombrePalm[t]   = "Palm" +"(" + Id[t] +","+(t+1)+")"; 		
			nombreV[t]   	= "Vcsp" +"(" + Id[t] +","+(t+1)+")";
	    	nombreBel1[t]   = "Bel1" +"(" + Id[t] +","+(t+1)+")";	
			nombreBalm[t]   = "Balm" +"(" + Id[t] +","+(t+1)+")";	
			nombreBg[t]   	= "Bgcsp"   +"(" + Id[t] +","+(t+1)+")";	
		}
		    
	    //variables continuas
		Pel1      			= new IloNumVar[T];	
		Pel2      			= new IloNumVar[T];	
		Palm      			= new IloNumVar[T];
		V				    = new IloNumVar[T];
	
		// si defino algunas variables binarias como continuas 
		Bel1     			= new IloNumVar[T];
		Balm     			= new IloNumVar[T];
		Bg 	    			= new IloNumVar[T];
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		try{
			if(lp==1){
				for(int t=0;t<T;t++){
    	    		Pel1[t]   = cplex.numVar(0,Double.MAX_VALUE,nombrePel1[t]);
    	    		Pel2[t]   = cplex.numVar(0,Double.MAX_VALUE,nombrePel2[t]);
    	    		Palm[t]   = cplex.numVar(0,Double.MAX_VALUE,nombrePalm[t]);
    	    		V[t]   	  = cplex.numVar(Vmin[t],Vmax[t],nombreV[t]);
    	    		Bel1[t]   = cplex.numVar(0,1,nombreBel1[t]);
					Balm[t]   = cplex.numVar(0,1,nombreBalm[t]);
					Bg[t]    = cplex.numVar(0,1,nombreBg[t]);
				}
			}
		}
		catch (IloException e) {
			System.err.println("Concert Definicion de variables '" + e + "' caught");
		}
		
	}
	
	///////////////////////////////////Restricciones/////////////////////////////////////////////////////////////////////////////

	//////////////////////////////////Restriccion balance almacenamiento///////////////////////////////////////////////////////////////////////////////////////////////////////	
		
		public void restrbala(IloCplex cplex, int t){
			try{
				if(t>0){
					cplex.addEq(cplex.sum(cplex.prod(-1,V[t]),cplex.prod((1-PerdidasAlmacenamiento[t]),V[t-1]),cplex.prod(-H[t]*EficienciaInyeccion[t],Pel2[t]),cplex.prod(H[t]*EficienciaAlmacenamiento[t],Palm[t])),0,"balance_csp"+Id[t]+"_"+(t+1));
				}
				//condicion inicial
				else{
					cplex.addEq(cplex.sum(cplex.prod(-1,V[t]),cplex.prod(-H[t]*EficienciaInyeccion[t],Pel2[t]),cplex.prod(H[t]*EficienciaAlmacenamiento[t],Palm[t])),-Vini[t],"balance_csp"+Id[t]+"_"+(t+1));
				}
			}	
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion balance almacenamiento '" + e + "' caught");
			}
		}	
	
	///////////////////////////////Variables binarias////////////////////////////////////////////////////////////////////////////////////////////////////////	
		public void restrlogi(IloCplex cplex, int t){
	    	
			try{
				cplex.addLe(cplex.sum(Bel1[t],Balm[t]),1,"csp_log"+Id[t]+"_"+(t+1));
			}
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion logica csp'" + e + "' caught");
			}
		}	
	///////////////////////////////Restriccion logica potencia electrica o almacenamiento////////////////////////////////////////////////////////////////////////////////////////////////////////	
		public void restrpot(IloCplex cplex, int t){
	    	
			try{
				cplex.addLe(cplex.diff(Pel1[t],cplex.prod(Bel1[t],PotenciaSolar[t]*EficienciaInyeccion[t])),0,"csp_potencia_electrica"+Id[t]+"_"+(t+1));
				cplex.addLe(cplex.diff(Palm[t],cplex.prod(Balm[t],PotenciaSolar[t]*EficienciaAlmacenamiento[t])),0,"csp_potencia_almacenamiento"+Id[t]+"_"+(t+1));
			}
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion potencia electrica potencia almacenamiento csp'" + e + "' caught");
			}
		}	
		public void restrpmax(IloCplex cplex, int t){
			try{
				cplex.addLe(cplex.sum(Pel1[t],Pel2[t],cplex.prod(-1*Pmax[t],Bg[t])),0,"csp_pmax"+Id[t]+"_"+(t+1));
			}
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion potencia maxima csp'" + e + "' caught");
			}
		}
		public void restrpmin(IloCplex cplex, int t){
			try{
				cplex.addLe(cplex.sum(cplex.prod(-1,Pel1[t]),cplex.prod(-1,Pel2[t]),cplex.prod(Pmin[t],Bg[t])),0,"csp_pmin"+Id[t]+"_"+(t+1));
			}
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion potencia maxima csp'" + e + "' caught");
			}
		}
		public void restrvfin(IloCplex cplex, int t){
			try{
				V[t].setLB(Vfin[t]);
			}
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion potencia maxima csp'" + e + "' caught");
			}
		}
}
	