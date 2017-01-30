import hudson.model.*

def build = Thread.currentThread().executable
def resolver = build.buildVariableResolver

//Parametros
def jobInvoker = resolver.resolve("jobInvoker")
def compJobNumber = resolver.resolve("compJobNumber")
def tool = resolver.resolve("tool")
def stream = resolver.resolve("stream")
def componente = resolver.resolve("component")
def workspaceIC = resolver.resolve("workspaceChecking")
def technology = resolver.resolve("technology")
def publishReportChecking = resolver.resolve("publishReportChecking")
if (technology == null || technology == ""){
	technology = "java"
}

//Variables Inyectados por Jenkins
def IHSurl =  build.getEnvironment(null).get("IHS_URL")
def zipBaseDir =  build.getEnvironment(null).get("CHECKING_REPORTS_PATH")

def jobInvokerSinEspacios = new String(jobInvoker)
def params = []
def publishReportPath = ""
def publishReportZip = ""

jobInvokerSinEspacios = jobInvokerSinEspacios.replace(' ', '').replace('(','').replace(')','').replace('/','-')
println "Sin espacios : $jobInvokerSinEspacios"

java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("ddMMyyyy_HHmmss");
java.util.Date date = new java.util.Date();
def myDate = dateFormat.format(date)

//ruta de jenkins donde dejar el zip de checking
if (publishReportChecking.equals("true")) {
	publishReportPath = "${tool}/${stream}/${componente}".toString()
	publishReportPath = publishReportPath.replaceAll(" ","_")
	publishReportZip = "${compJobNumber}_${myDate}.zip"
}

java.net.URI publishReportURI = java.net.URI.create("${IHSurl}/${publishReportPath}/${publishReportZip}".toString())
def publishReportURL = publishReportURI.toURL()
println "publishReportURL: $publishReportURL"

params.add(new StringParameterValue("stream","$stream"))
params.add(new StringParameterValue("componente",componente))
params.add(new StringParameterValue("workspaceIC", "$workspaceIC"))
params.add(new StringParameterValue("ignorar","no"))
params.add(new StringParameterValue("publishReportChecking",publishReportChecking))
params.add(new StringParameterValue("publishReportPath","${zipBaseDir}/${publishReportPath}"))
params.add(new StringParameterValue("publishReportURL","$publishReportURL"))
params.add(new StringParameterValue("urlCheckin","$publishReportURL"))
params.add(new StringParameterValue("publishReportZip","$publishReportZip"))
params.add(new StringParameterValue("technology","$technology"))
params.add(new StringParameterValue("jobInvokerSinEspacios",jobInvokerSinEspacios))

build.addAction(new ParametersAction(params))