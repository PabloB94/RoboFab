
import es.upm.babel.cclib.*;

public class RoboFabMonitor implements RoboFab {
	
	private Monitor mutex;
	private Monitor.Cond cRobots;
	private boolean avanceSol;
	private int[] pendientes;
	private int pesoContenedor;
	
	public RoboFabMonitor(){
		mutex = new Monitor();
		cRobots = mutex.newCond();
		avanceSol = false;
		pendientes = new int[Robots.NUM_ROBOTS];
		pesoContenedor = 0;
		
	}
	
	public void notificarPeso(int i, int p){
		pendientes[i] = p;
	}
    public void permisoSoltar(int i){
    	mutex.enter();
    		while(pesoContenedor + pendientes[i] > Cinta.MAX_P_CONTENEDOR){
    			cRobots.await();
    		}	
    		pendientes[i] = 0;
    		
    		if(cRobots.waiting()>0){
    			cRobots.signal();
    		}
    			
    	mutex.leave();
    }
    public void solicitarAvance(){
    	
    }
    public void contenedorNuevo(){
    	
    }

}
