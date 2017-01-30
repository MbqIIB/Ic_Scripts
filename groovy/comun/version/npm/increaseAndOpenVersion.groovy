import es.eci.utils.SystemPropertyBuilder
import es.eci.utils.npm.NpmCloseVersionCommand
import es.eci.utils.npm.NpmIncreaseAndOpenVersionCommand;


/**
 * Esta clase modela el entry point para abrir una version 
 * 
 * <br/>
 * Parámetros de entrada:<br/>
 * <br/>
 * --- OBLIGATORIOS<br/>
 * <b>parentWorkspace</b> Directorio de ejecución<br/>
 * <br/>
 * --- OPCIONALES<br/>
 * <b>filename</b> Nombre del fichero de configuración<br/>
 *
*/

SystemPropertyBuilder propertyBuilder = new SystemPropertyBuilder();

NpmIncreaseAndOpenVersionCommand command = new NpmIncreaseAndOpenVersionCommand()

command.initLogger { println it }

propertyBuilder.populate(command);

command.execute();