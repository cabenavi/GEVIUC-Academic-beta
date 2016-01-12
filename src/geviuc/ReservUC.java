import ilog.concert.*;
import ilog.cplex.*;

public class ReservUC{
	                  
	//variables binarias o continuas
	boolean var_bin=true;
	
	/****************************** VARIABLES PROBLEMA OPTIMIZACION *************************************************************************/
	//varibles continuas
	IloNumVar[]	V;	        //V[t]				  	//volumen del estanque en el instante t	 	
	IloNumVar[] Vfic;	    //Vfic[t]				//volumen ficticio, holgura para satisfacer vertimiento minimo
	IloNumVar[] Vfic2;	    //Vfic[t]				//volumen ficticio, holgura para satisfacer volumen final
	
	IloNumVar[] Qver;	    //Qver[t]				//caudal vertido por embalse 
	//variables continuas auxiliares para representar restriccion de reserva que puede aportar central asociada a embalse
	IloNumVar[]	V1;	        //V[t]				  	//volumen del estanque entre Vmin y ReserveMinVolume	 	
	IloNumVar[]	V2;	        //V[t]				  	//volumen del estanque entre ReserveMinVolume y Vmax	 	
	
	IloNumVar[]	B1;	        //B1[t]				  	//varibale binaria asociada a cuando embalse esta entre Vmin y ReserveMinVolume	 	
	IloNumVar[]	B2;	        //B2[t]				  	//varibale binaria asociada a cuando embalse esta entre ReserveMinVolume y Vmax	 	
	
	
	/******************************* FIN VARIABLES PROBLEMA OPTIMIZACION ********************************************************************************/
	
	//Atributos: todos variables en el tiempo
	//atributos Embalse
	int[]			Id;
	String[]		Nombre;
	double[]		Vmin;
	double[]		Vmax;
	int[]        	Ctur;
	int[]  		    Cver;
	int[] 		    Cfil;
	double[]		Aflu;
	double[]		Ret;
	double[]		Qvmin;
	double[]		Qvmax;
	String[]		Propietario;
	double[]		Vini;	
	double[]		Vfin;
	double[] 		Vermin; //Vertimiento minimo
    double[]		Vermax; //Vertimiento maximo
	int[]			ID_Central;
    double[] 		ReserveMinVolume;	
	
	//Atributos auxiliares
	double UnitT;
	
	//contructor
	public ReservUC(){
	}
	
	public void InitUC(int[] Id, String[] Nombre, double[] Vmin,double[] Vmax, int[] Ctur, int[] Cver , int[] Cfil, double[] Aflu, double[] Ret, double[] Qvmin,double[] Qvmax,	String[] Propietario,  double[] Vini, double[] Vfin, double[] Vermin, double[] Vermax, int[] ID_Central, double[] ReserveMinVolume) {
	
		//atributos
		this.Id 			= Id;
		this.Nombre 		= Nombre;
		this.Vmin 			= Vmin;
		this.Vmax 			= Vmax;
		this.Ctur           = Ctur;
		this.Cver           = Cver;
		this.Cfil           = Cfil;
		this.Aflu           = Aflu;
		this.Ret            = Ret;
		this.Qvmin          = Qvmin;
		this.Qvmax          = Qvmax;		
		this.Propietario    = Propietario;
		this.Vini           = Vini;	
		this.Vfin           = Vfin;	
		this.Vermin         = Vermin;	
		this.Vermax         = Vermax;
		this.ID_Central		= ID_Central;
		this.ReserveMinVolume = ReserveMinVolume;	
		
	}
	
	//se cargan datos auxiliares
	public void InitDataUC( double UnitT){
    	
    	this.UnitT	=UnitT;    
	}
	
	//Inicializacion de variables
	public void InitVariables(int[] H,int lp, IloCplex cplex){
		
	    int T= H.length;
	   
    	//nombre variables
    	String[]        nombreV      = new String[T];
    	String[]        nombreVfic   = new String[T];
		String[]		nombreQver   = new String[T];
		String[]        nombreV1      = new String[T];
    	String[]        nombreV2      = new String[T];
    	String[]        nombreB1      = new String[T];
    	String[]        nombreB2      = new String[T];
		String[]		nombreVfic2	  = new String[T];
    	
	    
	    for(int t=0;t<T;t++){
	    	nombreV[t]      ="Vemb" +"(" + Id[t] +","+(t+1)+")";
	    	nombreVfic[t]   ="Vfic" +"(" + Id[t] +","+(t+1)+")";
			nombreQver[t]	="Qver" +"(" + Id[t] +","+(t+1)+")";
			nombreV1[t]      ="Vemb1" +"(" + Id[t] +","+(t+1)+")";
			nombreV2[t]      ="Vemb2" +"(" + Id[t] +","+(t+1)+")";
			nombreB1[t]      ="B1" +"(" + Id[t] +","+(t+1)+")";
			nombreB2[t]      ="B2" +"(" + Id[t] +","+(t+1)+")";
			nombreVfic2[t]	 ="Vfic2" +"(" + Id[t] +","+(t+1)+")";
    	}
		
	    
	    //variables continuas
		V				= new IloNumVar[T];
		Vfic			= new IloNumVar[T];
		Vfic2			= new IloNumVar[T];
		Qver			= new IloNumVar[T];
		V1				= new IloNumVar[T];
		V2				= new IloNumVar[T];
		B1				= new IloNumVar[T];
		B2				= new IloNumVar[T];
		
	
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		try{
			if(lp==1){
				for(int t=0;t<T;t++){
    	    		V[t]      = cplex.numVar(Vmin[t],Vmax[t],nombreV[t]);
    	    		Vfic[t]   = cplex.numVar(0,Double.MAX_VALUE,nombreVfic[t]);
					Vfic2[t]  = cplex.numVar(0,Double.MAX_VALUE,nombreVfic2[t]);	
					
					//para embalses con vertimiento minimo distinto de cero se agrega variable de holgura Vfic2 -> Qver puede ser menor que vertimiento minimo
					//version original
					//Qver[t]	  = cplex.numVar(Vermin[t],Vermax[t],nombreQver[t]);		
					//version modificada
					Qver[t]	  = cplex.numVar(0,Vermax[t],nombreQver[t]);		
					
					V1[t]      = cplex.numVar(0,ReserveMinVolume[t]-Vmin[t],nombreV1[t]);
					V2[t]      = cplex.numVar(0,Vmax[t]-ReserveMinVolume[t],nombreV2[t]);
					B1[t]      = cplex.numVar(0,1,nombreB1[t]);
					B2[t]      = cplex.numVar(0,1,nombreB2[t]);
				}			
    	    }
		}
		catch (IloException e) {
			System.err.println("Concert Definicion de variables '" + e + "' caught");
		}
		
	}
	
	///////////////////////////////////Restricciones/////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////Volumen Final//////////////////////////////////////////////////////////////////////////////
		public void restrvfin(IloCplex cplex, int t){
			try{	
				//version original
				V[t].setLB(Vfin[t]);
				//if(Vfin[t]>0)
				//cplex.addGe(cplex.sum(V[t],Vfic2[t]),Vfin[t],"restric_volumen_final"+Id[t]+"_"+(t+1)); -> al final de dejo variable vfic2 solo en balance hidro 
				
			}
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion volumen final '" + e + "' caught");
			}
		}
		/////////////////////////////Volumen ficticio para vertimiento////////////////////////////////////////////////////7//////
		public void restrvfic2(IloCplex cplex, int t){
			try{	
				//version original
				if(Vermin[t]>0)
				cplex.addGe(cplex.sum(Qver[t],Vfic[t]),Vermin[t],"restric_volumen_ficticio2_"+Id[t]+"_"+(t+1));	
			}
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion volumen final '" + e + "' caught");
			}
		}
		///////////////////////////////Restriccion para formular volumen minimo para aportar reserva/////////////////////////////////////////
		public void restrrese(IloCplex cplex, int t){
			try{	
				cplex.addEq(cplex.sum(V[t],cplex.prod(-1,V1[t]),cplex.prod(-1,V2[t])),Vmin[t],"restric_aux_balance_volumen_"+Id[t]+"_"+(t+1));				
				cplex.addLe(cplex.diff(cplex.prod(ReserveMinVolume[t]-Vmin[t],B2[t]),V1[t]),0,"restric_aux_v1_min_"+Id[t]+"_"+(t+1));
				cplex.addLe(cplex.diff(V1[t],cplex.prod(ReserveMinVolume[t]-Vmin[t],B1[t])),0,"restric_aux_v1_max_"+Id[t]+"_"+(t+1));
				cplex.addLe(cplex.diff(V2[t],cplex.prod(Vmax[t]-ReserveMinVolume[t],B2[t])),0,"restric_aux_v2_max_"+Id[t]+"_"+(t+1));		
			}
			catch (IloException e) {
				System.err.println("Concert exception: Restriccion reserva '" + e + "' caught");
			}
		
		}
}
	