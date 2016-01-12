public class DataUCBus{
	
	//atributos demanda
	int[]			Id;
	String[]		Nombre;
	int[]			Bus;
	
	
	//Constructor
	public DataUCBus(){
	}
	//Cargar datos
	void loaddatafromdatabase(String typedb, String namedb, String userdb, String passdb, int[] H, int Duration[],int[] Tini_data, int[] Tfin_data, int object_id) throws Exception{
		
		int T=H.length;
		DataBus databus = new DataBus();
        databus.loaddatafromdatabase(typedb,namedb,userdb,passdb,Tini_data[0],Tfin_data[0],object_id);
		
		Id 				 = new int[T];
		Nombre			 = new String[T];
		Bus 			 = new int[T];
			
		loadint(this.Id,databus.Id, H, Duration);	
	   	loadstring(this.Nombre,databus.Nombre, H, Duration);
		loadint(this.Bus,databus.Bus, H, Duration);
	
	}
	public void loaddouble(double[] attribute1,double[] attribute2, int[] H, int[] Duration) throws Exception{
		
		int ta=0;
		double aux=0;
		int aux2=0;
		for(int t=0;t<H.length;t++){
			for(int tt=ta;tt<ta+H[t];tt++){
				aux=aux+attribute2[tt]*Duration[tt]; //promedio ponderado
				aux2=aux2+Duration[tt];
			}	
		attribute1[t]=aux/aux2;
		//System.out.println("atribute = " + attribute1[t] + " t = " +(t+1));	
		ta=ta+H[t];
		aux=0;
		aux2=0;
		}
	}
	public void loadint(int[] attribute1,int[] attribute2, int[] H, int[] Duration) throws Exception{
		
		int ta=0;
		int aux=0;
		int aux2=0;
		for(int t=0;t<H.length;t++){
			for(int tt=ta;tt<ta+H[t];tt++){
				aux=aux+attribute2[tt]*Duration[tt]; //promedio ponderado
				aux2=aux2+Duration[tt];
			}	
		attribute1[t]=aux/aux2;	
		//System.out.println("atribute = " + attribute1[t] + " t = " +(t+1));	
		ta=ta+H[t];
		aux=0;
		aux2=0;
		}
	}
	public void loadstring(String[] attribute1,String[] attribute2, int[] H, int[] Duration) throws Exception{
		
		int ta=0;
		String aux="";
		for(int t=0;t<H.length;t++){
			for(int tt=ta;tt<ta+H[t];tt++){
				aux=attribute2[tt];
			}	
		attribute1[t]=aux;	
		//System.out.println("atribute = " + attribute1[t] + " t = " +(t+1));	
		ta=ta+H[t];
		}
	}
	


}	