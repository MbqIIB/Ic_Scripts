package clarive

import es.eci.utils.GlobalVars;
import hudson.model.*;
import es.eci.utils.clarive.ClariveConnection;
import es.eci.utils.clarive.ClariveParamsHelper;
import es.eci.utils.Stopwatch;

Long millis = Stopwatch.watch {
	// VARS

	GlobalVars gVars = new GlobalVars();

	ClariveConnection clConn = new ClariveConnection();
	clConn.initLogger { println it };

	def action = build.buildVariableResolver.resolve("action");
	def permisoClarive = build.buildVariableResolver.resolve("permisoClarive");

	if(permisoClarive != null && permisoClarive.trim().equals("true")) {
		
		if(!action.equals("build")) {
			def ECI_PROXY_URL = build.getEnvironment(null).get("ECI_PROXY_URL");
			def ECI_PROXY_PORT = build.getEnvironment(null).get("ECI_PROXY_PORT");
			def nakedUrl = build.getEnvironment(null).get("CLARIVE_URL");
			def api_key_clarive = build.buildVariableResolver.resolve("CLARIVE_API_KEY");
			def nombre_componente = build.buildVariableResolver.resolve("componentName");
			def componenteUrbanCode = build.buildVariableResolver.resolve("componenteUrbanCode");
			def paso = build.buildVariableResolver.resolve("tipo_paso");
			def prov_cod_release = build.buildVariableResolver.resolve("codigo_release");
			def version = build.buildVariableResolver.resolve("builtVersion");
			if(version == null || version.trim().equals("")) {
				version = build.buildVariableResolver.resolve("version")
			}

			def prov_id_proceso = gVars.get(build, "id_proceso");
			if(prov_id_proceso == null || prov_id_proceso.trim().equals("") || prov_id_proceso.trim().equals("null")) {
				prov_id_proceso = "";
			}

			def proceso = build.buildVariableResolver.resolve("tipo_proceso").toUpperCase();
			if(proceso.equals("ADDHOTFIX")) {
				proceso = "HOTFIX";
			}

			ClariveParamsHelper clHelper = new ClariveParamsHelper(build);
			clHelper.initLogger { println it }
			def nombre_producto = build.buildVariableResolver.resolve("aplicacionUrbanCode");
			//		def nombre_producto = clHelper.findArea();
			//		nombre_producto = nombre_producto.replaceAll('\\(RTC\\)','').trim();

			def nombre_subproducto = clHelper.getSubproducto();
			def tipo_corriente = clHelper.getTipoCorriente();
			def resultado = clHelper.getResultado(build);
			def version_maven = clHelper.getVersionMaven(version);

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
					resultado,
					componenteUrbanCode
					);

			def id_proceso = ret.get("id_proceso");
			println("id_proceso devuelta por SWC01 -> ${id_proceso}")

			def invalid_prov_codigo_release = prov_cod_release == null || prov_cod_release.trim().equals("") ||
					prov_cod_release.trim().equals("null") || prov_cod_release.trim().equals("\${instantanea}");

			if(invalid_prov_codigo_release) {
				def cod_release = ret.get("cod_release");
				println("Devuelto el cod_release: \"${cod_release}\"");

				if(cod_release == null || cod_release.trim().equals("") || cod_release.trim().equals("null")) {
					println("El cod_release devuelto por el servicio SWC02 de Clarive no es válido. No establecemos ninguno por ahora.");
				} else {
					// Se pone como variable global cod_release devuelta como "instantanea".
					gVars.put(build, "instantanea","${cod_release}");
				}
			}

			// Si ya venía un id_proceso informado, lo usamos e ignoramos
			// el devuelto por el servicio SWC01.
			// Si no teníamos un id_proceso informado, recogemos el que devuelva
			// el servicio SWC01 y lo setea en el job raíz para que esté disponible
			// todo el proceso.
			if(	prov_id_proceso == null ||
			prov_id_proceso.trim().equals("") ||
			prov_id_proceso.trim().equals("null") ||
			prov_id_proceso.trim().equals("GENERAR_ID")) {

				println("No hay id_proceso definido en este punto. Usamos el devuelto por SWC01 para todo el proceso.");
				if(id_proceso == null || id_proceso.trim().equals("") || id_proceso.trim().equals("null")) {
					println("El id_proceso devuelto por el servicio SWC01 de Clarive no es válido. No establecemos ninguno por ahora.");
				} else {
					gVars.put(build, "id_proceso", "${id_proceso}");
				}

			} else {
				println("Ya había un id_proceso definido (${prov_id_proceso}), por lo que será el que usemos.");
			}

		}
		
	} else {
		println("permisoClarive -> ${permisoClarive}. No llevamos a cabo la llamada al servicio SWC01.");
	}
}

println "Execution time total -> ${millis} mseg."


