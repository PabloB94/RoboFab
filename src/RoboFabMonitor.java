import es.upm.babel.cclib.*;

public class RoboFabMonitor implements RoboFab {
	
	private Monitor mutex;
	private Monitor.Cond cRobots;
	private boolean avanceSol;
	private int[] pendientes;
	private int pesoContenedor;
	//CONSTRUCTOR
	public RoboFabMonitor(){
		mutex = new Monitor();
		cRobots = mutex.newCond();
		avanceSol = false;
		pendientes = new int[Robots.NUM_ROBOTS];
		pesoContenedor = 0;
		
	}
	//FUNCIONES
	public void notificarPeso(int i, int p){
		//PRE: p< Peso Maximo Contenedor
		//POST: Roboot[i] carga p
		mutex.enter();
		if(p<Cinta.MAX_P_CONTENEDOR){
			pendientes[i] = p;
		}
		mutex.leave();
		//Esta funcion notifica al programa principal que el robot i
		//ha recogido el peso p
	}
    public void permisoSoltar(int i){
    	//PRE:PesoContenedor + Robot[i]< Peso Maximo Contenedor
    	//POST: pesoContenedor+=pendiente[i] ^pendiente[i]=0 
    	mutex.enter();
    		while(pesoContenedor + pendientes[i] > Cinta.MAX_P_CONTENEDOR){
    			cRobots.await();
    		}	
    		pendientes[i] = 0;
    		//
    		if(cRobots.waiting()>0){
    			cRobots.signal();
    		}
    			
    	mutex.leave();
    }
    public void solicitarAvance(){
    	//PRE: PesoTotal < Peso Maximo Contenedor
    	//POST:
    	mutex.enter();
    	int pesoTotal=pesoContenedor;
    	
    	for(int i=0;i<pendientes.length;i++){
    		pesoTotal+=pendientes[i];
    	}
    	if(pesoTotal<Cinta.MAX_P_CONTENEDOR){
    		avanceSol=true;
    	}
    	
    	mutex.leave();
    	
    }
    public void contenedorNuevo(){
    	//PRE: Cierto
    	//POST: PesoContenedor =0, avanceSol =false
    	//NO SE CAMBIA []pendientes
    	mutex.enter();
    	if(avanceSol){
    		Cinta.avance();
    		pesoContenedor=0;
    		avanceSol=false;
    	}
    	mutex.leave();
    	
    }

}
