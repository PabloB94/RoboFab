import es.upm.babel.cclib.*;

public class RoboFabMonitor implements RoboFab {
	
	private Monitor mutex;
	private Monitor.Cond cRobots;

	private Monitor.Cond cContenedor;
	private boolean avanzando;
	private boolean lleno;
	private int[] pendientes;
	private int pesoContenedor;
	
	//Constructor de la clase RoboFabMonitor
	//Inicializa el monitor con dos condiciones y las variables de control
	//El array pendientes no se llena expl�citamente, porque por la definici�n de la clase int se llena
	//por defecto con 0 en todas las posiciones, lo cual es �til para nosotros.
	public RoboFabMonitor(){
		mutex = new Monitor();
		cRobots = mutex.newCond();
		cContenedor = mutex.newCond();
		avanzando = false;
		lleno = false;

		pendientes = new int[Robots.NUM_ROBOTS];
		pesoContenedor = 0;
		
	}
	//FUNCIONES
	public void notificarPeso(int i, int p){

    //PRE: p< Peso Maximo Contenedor
		//POST: Roboot[i] carga p
		mutex.enter();
		//Esta funcion notifica al programa principal que el robot i
		//ha recogido el peso p
			pendientes[i] = p;
			for(int j = 0; j < Robots.NUM_ROBOTS; j++){
				//Este bucle controla que los robots que esperan tienen todos m�s carga de la que puede transportar
				//el contenedor que est� en ese momento en la cinta
    			if(pesoContenedor + pendientes[j] <= Cinta.MAX_P_CONTENEDOR){
    				lleno = false;
    				break;
    			}
    			lleno = true;
    		}
			if(lleno){
				//Si el bucle de control determina que ning�n robot puede descargar con seguridad, se le otorga
				//permiso a la cinta para cambiar el contenedor
				cContenedor.signal();
			}
		mutex.leave();
    
	}
	
	
    public void permisoSoltar(int i){
      //PRE:PesoContenedor + Robot[i]< Peso Maximo Contenedor
    	//POST: pesoContenedor+=pendiente[i] ^pendiente[i]=0 
    	mutex.enter();
    		while(pesoContenedor + pendientes[i] > Cinta.MAX_P_CONTENEDOR || avanzando){
				//Este bucle controla si el robot puede descargar de forma segura
    			cRobots.await();
    		}	
    		
			//Aqu� se actualiza la variable de control del peso del contenedor y se indica que el robot que ha 
    		//descargado ya no lleva m�s carga
    		pesoContenedor = pesoContenedor + pendientes[i];
    		pendientes[i] = 0;
    		    		
			//Como se controla en bucle si un robot recien despertado puede descargar o no, no es necesario 
    		//un array de colas en el que se despierte a un robot en concreto. Aunque no es la soluci�n m�s
    		//optimizada respecto al tiempo de ejecuci�n, s� lo es en t�rminos de memoria.

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
			//Variable de control del permiso de cambio de contenedor
    		for(int i = 0; i < Robots.NUM_ROBOTS; i++){
				//El bucle comprueba si alguno de los robots puede descargar a�n con seguridad
    			if(pesoContenedor + pendientes[i] <= Cinta.MAX_P_CONTENEDOR){
    				permiso = false;
    				break;
    			}
    		}
    		if(!permiso){
				//Si alguno de los robots puede descargar, el proceso se duerme
    			cContenedor.await();
    		}
			//Al despertarse, indicar� que se procede al cambio de contenedor, lo que evitar� que ning�n robot 
    		//descargue encima de la cinta sin haber contenedor.
    		avanzando = true;
    	mutex.leave();
    }
    
    
    public void contenedorNuevo(){
      	
    	mutex.enter();
    		//Esta funci�n resetea la variable de control del peso del contenedor cuando llega uno nuevo
    			pesoContenedor = 0;
    			//Adem�s, indica que el contenedor est� quieto y se puede descargar con seguridad
    			avanzando = false;
    			//Tras hacer esto, se despierta a uno de los robots para que comience la descarga
    			if(cRobots.waiting()>0){
    				cRobots.signal();
    			}
    	mutex.leave();   	  	

    }

}