import es.eci.utils.ParamsHelper;
import rtc.RTCUtils;
import urbanCode.UrbanCodeComponentInfoService
import urbanCode.UrbanCodeExecutor
import es.eci.utils.NexusHelper
import es.eci.utils.ScmCommand;
import es.eci.utils.TmpDir;
import es.eci.utils.JobRootFinder;
import es.eci.utils.pom.MavenCoordinates
import hudson.model.*;


def build = Thread.currentThread().executable;
def resolver = build.buildVariableResolver;

def gitGroup = resolver.resolve("gitGroup");
def stream = resolver.resolve("stream");

def rtcUser = build.getEnvironment(null).get("userRTC");
def rtcPass = resolver.resolve("pwdRTC");
def rtcUrl = build.getEnvironment(null).get("urlRTC");

def udClientCommand = build.getEnvironment(null).get("UDCLIENT_COMMAND");
def urlUrbanCode = build.getEnvironment(null).get("UDCLIENT_URL");
def userUrban = build.getEnvironment(null).get("UDCLIENT_USER");
def passUrban = resolver.resolve("UDCLIENT_PASS");

def componenteUrbanCode = resolver.resolve("componenteUrbanCode");
def nexusUrl = build.getEnvironment(null).get("ROOT_NEXUS_URL");
def component = resolver.resolve("component");
def action = resolver.resolve("action");
def projectArea = resolver.resolve("projectAreaUUID");

def params = [:];

// Se determina el tipo de corriente en base al parametro "action".
if(action.trim().equals("build") || action.trim().equals("release")) {
	params.put("tipo_corriente","DESARROLLO");

} else if(action.trim().equals("addFix")) {
	params.put("tipo_corriente","RELEASE");

} else if(action.trim().equals("addHotfix")) {
	params.put("tipo_corriente","MANTENIMIENTO");
}

// Calculamos el Área de Proyecto en RTC del stream en cuestión (producto)
if(gitGroup == null || gitGroup.trim().equals("")) {
	if(projectArea == null || projectArea.trim().equals("") || projectArea.trim().equals("\${projectArea}")) {
		RTCUtils ru = new RTCUtils();
		def pa = ru.getProjectArea(
				"${stream}",
				"${rtcUser}",
				"${rtcPass}",
				"${rtcUrl}");
		println("AREA DE PROJECTO: ${pa}")
		projectArea = pa;
		params.put("projectArea","${projectArea}");
	}
} else {
	params.put("subproducto","${gitGroup.trim()}");
	def gitGroupParts = gitGroup.split("_");
	if(gitGroupParts.size() <= 2) {
		params.put("projectArea","${gitGroupParts[0]}");
	} else {
		params.put("projectArea", "${gitGroupParts[0]}_${gitGroupParts[1]}");
	}
}

// Calculamos el parámetro "subproducto" a partir del nombre de la "stream".
def streamSuffixes = ["DESARROLLO","RELEASE","MANTENIMIENTO","DEVELOPMENT"];
if(gitGroup != null && !gitGroup.trim().equals("") && !gitGroup.trim().equals("\${gitGroup}")) {
	params.put("subproducto","${gitGroup.trim()}");
		
} else if(stream != null && !stream.trim().equals("") && !stream.trim().equals("\${stream}")) {
	def subproducto = stream;
	for(suffix in streamSuffixes) {
		subproducto = subproducto.split("- ${suffix}")[0].split("-${suffix}")[0];
	}
	params.put("subproducto","${subproducto.trim()}");
}


// Calculamos el parámetro "builtVersion" que en este momento ya estará seteado en el build de componente.
// Buscar si es posible el componente
List<AbstractBuild> fullTree = JobRootFinder.getFullExecutionTree(build);
AbstractBuild componentBuild = build;
// Buscar el job de componente
for (AbstractBuild ancestor: fullTree) {
	if (ancestor.getProject().getName().contains("-COMP-")) {
		componentBuild = ancestor;
	}
}

def compoResolver = componentBuild.buildVariableResolver;
def builtVersion = compoResolver.resolve("builtVersion");
if(builtVersion != null) {
	params.put("builtVersion", "${builtVersion}")
}

// Se calcula la "version_maven" a partir del GAV devuelto por UrbanCode
def version_maven;
try {
	UrbanCodeExecutor urbExe = new UrbanCodeExecutor(udClientCommand,urlUrbanCode,userUrban,passUrban);
	UrbanCodeComponentInfoService compoInfo = new UrbanCodeComponentInfoService(urbExe);
	compoInfo.initLogger { println it };

	if(componenteUrbanCode != null && !componenteUrbanCode.trim().equals("")) {
		MavenCoordinates mvnCoord = compoInfo.getCoordinates("${componenteUrbanCode}");
		if(!builtVersion.endsWith("-SNAPSHOT")) {
			mvnCoord.setVersion("${builtVersion}-SNAPSHOT");
		} else {
			mvnCoord.setVersion("${builtVersion}");
		}
		NexusHelper nexusHelper = new NexusHelper(nexusUrl);
		version_maven = nexusHelper.resolveSnapshot(mvnCoord);
		println("La \"version_maven\" calculada desde Nexus es \"${version_maven}\"");

	} else {
		println("La variable \"componenteUrbanCode\" no viene indicada para el componente \"${component}\". Usamos \"${builtVersion}\".");
		version_maven = "${builtVersion}";
	}

} catch(Exception e) {
	StringWriter errors = new StringWriter();
	e.printStackTrace(new PrintWriter(errors));
	println(errors.toString());
	println("No se ha podido calcular el timestamp de Nexus para la version ${builtVersion}.");
	version_maven = "${builtVersion}";
}

params.put("version_maven","${version_maven}");

// Se añaden finalmente los nuevos parámetros.
ParamsHelper.addParams(build,params);









