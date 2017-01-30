import es.eci.utils.GlobalVars;
import hudson.model.*;
import es.eci.utils.clarive.ClariveConnection;

// VARS

def build = Thread.currentThread().executable;
def resolver = build.buildVariableResolver;

GlobalVars gVars = new GlobalVars(build);

ClariveConnection clConn = new ClariveConnection();
clConn.initLogger { println it };

def ECI_PROXY_URL = build.getEnvironment(null).get("ECI_PROXY_URL");
def ECI_PROXY_PORT = build.getEnvironment(null).get("ECI_PROXY_PORT");
def nakedUrl = build.getEnvironment(null).get("CLARIVE_URL");
def api_key_clarive = resolver.resolve("CLARIVE_API_KEY");

def nombre_producto = resolver.resolve("projectArea");
def nombre_subproducto = resolver.resolve("subproducto");
def tipo_corriente = resolver.resolve("tipo_corriente");
def nombre_componente = resolver.resolve("componentName");
def componenteUrbanCode = resolver.resolve("componenteUrbanCode");
def paso = resolver.resolve("tipo_paso");
def resultado = resolver.resolve("resultado");
def metrica_PU = resolver.resolve("metrica_PU");
def metrica_PC = resolver.resolve("metrica_PC");
def version_maven = resolver.resolve("version_maven");
def prov_cod_release = resolver.resolve("codigo_release");
def version = resolver.resolve("builtVersion");
if(version == null || version.trim().equals("")) {
	version = resolver.resolve("version")
}

def prov_id_proceso = gVars.get("id_proceso");
if(prov_id_proceso == null || prov_id_proceso.trim().equals("") || prov_id_proceso.trim().equals("null")) {
	prov_id_proceso = "";
}

def proceso = resolver.resolve("tipo_proceso").toUpperCase();
if(proceso.equals("ADDHOTFIX")) {
	proceso = "HOTFIX";
}

/**
 * Ejecución del servicio SWC01 de Clarive el cual 
 * crea y actualiza el tópico referenciado por el 
 * parámetro "id_proceso".
 */
def ret = [:];

ret = clConn.swc01(
		nakedUrl,
		api_key_clarive,
		ECI_PROXY_URL,
		ECI_PROXY_PORT,
		nombre_producto,
		nombre_subproducto,
		tipo_corriente,
		nombre_componente,
		version,
		version_maven,
		proceso,
		prov_id_proceso,
		paso,
		resultado
		);

def id_proceso = ret.get("id_proceso");

def invalid_prov_codigo_release = prov_cod_release == null || prov_cod_release.trim().equals("") ||
prov_cod_release.trim().equals("null") || prov_cod_release.trim().equals("\${instantanea}");

if(invalid_prov_codigo_release) {
	def cod_release = ret.get("cod_release");
	println("Devuelto el cod_release: \"${cod_release}\"");

	if(cod_release == null || cod_release.trim().equals("") || cod_release.trim().equals("null")) {
		println("El cod_release devuelto por el servicio SWC02 de Clarive no es válido. No establecemos ninguno por ahora.");
	} else {
		// Se pone como variable global cod_release devuelta como "instantanea".
		gVars = new GlobalVars(build);
		gVars.put("instantanea","${cod_release}");
	}
}

// Si ya venía un id_proceso informado, lo usamos e ignoramos
// el devuelto por el servicio SWC01.
// Si no teníamos un id_proceso informado, recogemos el que devuelva
// el servicio SWC01 y lo setea en el job raíz para que esté disponible
// todo el proceso.
if(prov_id_proceso == null || prov_id_proceso.trim().equals("") || prov_id_proceso.trim().equals("null")) {
	println("No hay id_proceso definido en este punto. Usamos el devuelto por SWC01 para todo el proceso.");
	if(id_proceso == null || id_proceso.trim().equals("") || id_proceso.trim().equals("null")) {
		println("El id_proceso devuelto por el servicio SWC01 de Clarive no es válido. No establecemos ninguno por ahora.");
	} else {
		gVars = new GlobalVars(build);
		gVars.put("id_proceso", "${id_proceso}");
	}	

} else {
	println("Ya había un id_proceso definido (${prov_id_proceso}), por lo que será el que usemos.");
}

