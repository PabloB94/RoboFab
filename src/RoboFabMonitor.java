import es.upm.babel.cclib.*;

public class RoboFabMonitor implements RoboFab {
	
	private Monitor mutex; //Monitor del recurso
	private Monitor.Cond[] cRobots; //Array de condiciones, una para cada robot
	private Monitor.Cond cContenedor; //Condicion para el contenedor
	private boolean lleno;	//Booleano que controla si el contenedor del recurso esta lleno
	private int[] pendientes;  //Array que guarda los pesos que recogen los robots para
							   //determinar si se pueden descargar o no en el contenedor
	private int pesoContenedor;  //Variable para almacenar el peso del contenedor del recurso
	
	//Constructor de la clase RoboFabMonitor
	public RoboFabMonitor(){
		mutex = new Monitor(); //Inicializacion del monitor
		cRobots = new Monitor.Cond[Robots.NUM_ROBOTS]; //Asignacion de tamano del array de condiciones
		for (int i = 0; i < Robots.NUM_ROBOTS; i++){
			cRobots[i] = mutex.newCond(); //Inicializacion del array de condiciones para los robots
		}
		cContenedor = mutex.newCond(); //Inicializacion de la condicion para el contenedor
		lleno = false;
		pendientes = new int[Robots.NUM_ROBOTS]; //Inicializacion del estado del recurso
		pesoContenedor = 0;		
	}
	
	//Metodo notificarPeso. Actualiza el vector pendientes[] con el peso recogido por cada robot
	public void notificarPeso(int i, int p){ //Recibe como parametros el id del robot que informa y el peso que carga
		mutex.enter(); //Entramos en la seccion critica
			pendientes[i] = p; //Actualizamos el vector pendientes
			for(int j = 0; j < Robots.NUM_ROBOTS; j++){
				//Comprobamos si alguno de los robots puede descargar en la cinta
    			if(pesoContenedor + pendientes[j] <= Cinta.MAX_P_CONTENEDOR){
    				lleno = false; //Si algun robot puede descargar, el contenedor no esta lleno
    				break; //Salimos del bucle para no seguir iterando
    			}
    			lleno = true;//Si ningun robot puede descargar, marcamos el contenedor como lleno
    		}
			if(lleno){
				//Si el contenedor esta lleno, se hace un signal a la cinta para que avance
				cContenedor.signal();
			}
		mutex.leave(); //Salimos de la seccion critica   
	}
	
	//Metodo permisoSoltar. Da paso a los robots para que descarguen en el contenedor y
	//actualiza las variables del recurso (pendientes[] y pesoContenedor) para reflejarlo
    public void permisoSoltar(int i){ //Recibe como parametro el id del robot que pide permiso
    	mutex.enter(); //Entramos en la seccion critica
    		if(pesoContenedor + pendientes[i] > Cinta.MAX_P_CONTENEDOR){
				//Comprobamos si el robot puede descargar, mirando si el 
    			//contenedor tiene capacidad para el peso que carga el robot
    			cRobots[i].await(); //Si no puede descargar, se pone al robot en espera
    		}	
    		pesoContenedor = pesoContenedor + pendientes[i]; //Actualizamos el estado del recurso
    		pendientes[i] = 0;   		    		
    		liberar(); //Se llama al metodo auxiliar liberar, que intentara despertar a uno de los
    				   //robots dormidos
    	mutex.leave(); //Salimos de la seccion critica
    }

    //Metodo solicitarAvance. Da permiso a la cinta para cambiar el contenedor cuando este esta lleno.
    public void solicitarAvance(){
    	mutex.enter(); //Entramos en la seccion critica
    		boolean permiso = true; //Variable auxiliar de control
    		for(int i = 0; i < Robots.NUM_ROBOTS; i++){
				//Comprobamos si alguno de los robots puede descargar
    			if(pesoContenedor + pendientes[i] <= Cinta.MAX_P_CONTENEDOR){
    				permiso = false; //Si al menos un robot puede descargar, cambiamos a false
    								 //la variable de control
    				i = Robots.NUM_ROBOTS; //Actualizamos la variable de control del bucle 
    									   //para no seguir iterando
    			}
    		}
    		if(!permiso){ 
    			//Si la variable de control senala que hay robots descargando, el proceso se duerme
    			cContenedor.await();
    		}
    		//Cuando se recibe el signal se procede al cambio de contenedor
    	mutex.leave(); //Salimos de la seccion critica
    }
    
    //Metodo contenedorNuevo. Actualiza el estado del recurso para indicar que el peso del 
    //contenedor vuelve a estar a 0
    public void contenedorNuevo(){
    	mutex.enter(); //Entramos en la seccion critica
    		pesoContenedor = 0; //Actualizamos el estado del recurso
    		liberar(); //Llamamos al metodo auxiliar para que despierte a uno de los robots    		
    	mutex.leave(); //Salimos de la seccion critica
    }
    
    //Metodo auxiliar liberar. Comprueba si algun robot esta dormido y puede descargar
    //Despierta al primer robot que cumpla estas condiciones
    public void liberar(){
    	for (int i = 0; i < Robots.NUM_ROBOTS; i++){ 
    		if (cRobots[i].waiting() > 0 && pendientes[i] + pesoContenedor <= Cinta.MAX_P_CONTENEDOR){ 
    			//Si se cumple que un robot dormido puede descargar, se hace un signal a su condicion
    			cRobots[i].signal();
    			i = Robots.NUM_ROBOTS; //Se actualiza la variable de control del bucle para no iterar mas
    		}
    	}
    }
}