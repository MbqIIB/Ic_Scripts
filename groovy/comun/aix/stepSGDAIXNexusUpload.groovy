package aix

import aix.SGDAIXNexusUpload;
import es.eci.utils.SystemPropertyBuilder;

/**
 * Ver el funcionamiento en SGDAIXNexusUpload.groovy.
 */

SystemPropertyBuilder propertyBuilder = new SystemPropertyBuilder();

SGDAIXNexusUpload command = new SGDAIXNexusUpload();

command.initLogger { println it }

propertyBuilder.populate(command);

command.execute();