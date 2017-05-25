import es.upm.babel.cclib.*;

public class RoboFabMonitor implements RoboFab {
	
	private Monitor mutex;
	private Monitor.Cond condAvance;
	private boolean avanceSol;
	private int[] pendientes;
	private int pesoContenedor;
	private Monitor.Cond[] condicion;
	//CONSTRUCTOR
	public RoboFabMonitor(){
		mutex = new Monitor();
		condAvance = mutex.newCond();
		avanceSol = false;
		pendientes = new int[Robots.NUM_ROBOTS];
		pesoContenedor = 0;
		condicion= new Monitor.Cond[Robots.NUM_ROBOTS];
		for(int i=0;i<Robots.NUM_ROBOTS;i++){
			condicion[i]= mutex.newCond();
		}
		
	}
	//FUNCIONES
	public void notificarPeso(int i, int p){
		//PRE: p< Peso Maximo Contenedor
		//POST: Roboot[i] carga p
		mutex.enter();
			pendientes[i] = p;
			unlock();
		mutex.leave();
		//Esta funcion notifica al programa principal que el robot i
		//ha recogido el peso p
	}
    public void permisoSoltar(int i){
    	//PRE:PesoContenedor + Robot[i]< Peso Maximo Contenedor
    	//POST: pesoContenedor+=pendiente[i] ^pendiente[i]=0 
    	mutex.enter();
    		if(pendientes[i]+pesoContenedor>Cinta.MAX_P_CONTENEDOR){
    			condicion[i].await();
    		}
    		pesoContenedor+=pendientes[i];
    		pendientes[i] = 0;
    		//FUNCION DESBLOQUEAR
    		unlock();
    	mutex.leave();
    }
    public void solicitarAvance(){
    	//PRE: PesoTotal < Peso Maximo Contenedor
    	//POST:
    	mutex.enter();
    		int pesoMax=Cinta.MAX_P_CONTENEDOR-pesoContenedor;
    		boolean encontrado=false;
    		
    		for(int i=0;i>pendientes.length&&!encontrado;i++){
    			if(pendientes[i]<pesoMax){
    				condAvance.await();
    				encontrado=true;
    			}
    		}
    		unlock();
    	mutex.leave();
    	
    }
    public void contenedorNuevo(){
    	//PRE: Cierto
    	//POST: PesoContenedor =0, avanceSol =false
    	//NO SE CAMBIA []pendientes
    	mutex.enter();
    		pesoContenedor=0;
    		avanceSol=false;
    	unlock();
    	mutex.leave();
    	
    }
    private void unlock(){
    	for(int i=0; i<condicion.length;i++){
    		if(condicion[i].waiting()>0&&pendientes[i]+pesoContenedor<Cinta.MAX_P_CONTENEDOR){
    			condicion[i].signal();
    		}
    	}
    	if(condAvance.waiting()>1){
    		pendientes[i]
    	}
    	
    }

}
