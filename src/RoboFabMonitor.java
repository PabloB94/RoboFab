
import es.upm.babel.cclib.*;

public class RoboFabMonitor implements RoboFab {
	
	private Monitor mutex;
	private Monitor.Cond cRobots;
	
	public RoboFabMonitor(){
		mutex = new Monitor();
		cRobots = mutex.newCond();
	}
	
	public void notificarPeso(int i, int p){
		
	}
    public void permisoSoltar(int i){
    	
    }
    public void solicitarAvance(){
    	
    }
    public void contenedorNuevo(){
    	
    }

}
