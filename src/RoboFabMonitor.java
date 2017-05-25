
import es.upm.babel.cclib.*;

public class RoboFabMonitor implements RoboFab {
	
	private Monitor mutex;
	private Monitor.Cond cRobots;
	private Monitor.Cond cContenedor;
	private boolean avanzando;
	private boolean lleno;
	private int[] pendientes;
	private int pesoContenedor;
	
	public RoboFabMonitor(){
		mutex = new Monitor();
		cRobots = mutex.newCond();
		cContenedor = mutex.newCond();
		avanzando = false;
		lleno = false;
		pendientes = new int[Robots.NUM_ROBOTS];
		pesoContenedor = 0;
		
	}
	
	public void notificarPeso(int i, int p){
		mutex.enter();
			pendientes[i] = p;
			for(int j = 0; j < Robots.NUM_ROBOTS; j++){
    			if(pesoContenedor + pendientes[j] <= Cinta.MAX_P_CONTENEDOR){
    				lleno = false;
    				break;
    			}
    			lleno = true;
    		}
			if(lleno){
				cContenedor.signal();
			}
		mutex.leave();
	}
	
	
    public void permisoSoltar(int i){
    	mutex.enter();
    		while(pesoContenedor + pendientes[i] > Cinta.MAX_P_CONTENEDOR || avanzando){
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
    	mutex.enter();
    		boolean permiso = true;
    		for(int i = 0; i < Robots.NUM_ROBOTS; i++){
    			if(pesoContenedor + pendientes[i] <= Cinta.MAX_P_CONTENEDOR){
    				permiso = false;
    				break;
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
