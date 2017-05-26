import es.upm.babel.cclib.*;

public class RoboFabMonitor implements RoboFab {
	
	private Monitor mutex;
	private Monitor.Cond cRobots;

	private Monitor.Cond cContenedor;
	private boolean avanzando;
	private int[] pendientes;
	private int pesoContenedor;
	
	public RoboFabMonitor(){
		mutex = new Monitor();
		cRobots = mutex.newCond();
		cContenedor = mutex.newCond();
		avanzando = false;

		pendientes = new int[Robots.NUM_ROBOTS];
		pesoContenedor = 0;
		
	}
	
	//FUNCIONES
	public void notificarPeso(int i, int p){
    		//PRE: p< Peso Maximo Contenedor
		//POST: Roboot[i] carga p
		mutex.enter();
			pendientes[i] = p;
		mutex.leave();
    		//Esta funcion notifica al programa principal que el robot i
		//ha recogido el peso p
	}
    	public void permisoSoltar(int i){
      		//PRE:PesoContenedor + Robot[i]< Peso Maximo Contenedor
    		//POST: pesoContenedor+=pendiente[i] ^pendiente[i]=0 
    		mutex.enter();
    			while(pesoContenedor + pendientes[i] > Cinta.MAX_P_CONTENEDOR || avanzando){
    				if(cRobots.waiting() == Robots.NUM_ROBOTS - 1){
    					cContenedor.signal();
				}
    				cRobots.await();
    			}
    			pesoContenedor = pesoContenedor + pendientes[i];
    			pendientes[i] = 0;
    			if(cRobots.waiting()>0){
    				cRobots.signal();
    			}
    		mutex.leave();
    	}
   	public void solicitarAvance(){
      		//PRE: Cierto
    		//POST: PesoContenedor =0, avanceSol =false
    		//NO SE CAMBIA []pendientes
    		mutex.enter();
    			boolean permiso = true;
			boolean terminado = false;
    			for(int i = 0; i < Robots.NUM_ROBOTS&&!terminado; i++){
    				if(pesoContenedor + pendientes[i] <= Cinta.MAX_P_CONTENEDOR){
    					permiso = false;
    					terminado=true;
    				}
    			}
    			if(!permiso){
    				cContenedor.await();
    			}
    			avanzando = true;
    		mutex.leave();
    	}
    	public void contenedorNuevo(){
    		mutex.enter();
    			pesoContenedor = 0;
    			avanzando = false;
    			if(cRobots.waiting()>0){
    				cRobots.signal();
    			}
    		mutex.leave();   	  	

    	}

}
