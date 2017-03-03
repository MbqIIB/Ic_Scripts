package vs;

interface OutputParser {
	
	/**
	 * Realiza el parseo de las salidas del compilador y determina
	 * si se ha producido algún error de compilación a partir de las mismas
	 * @param stdout Salida estándar del comando
	 * @param stderr Salida de error del comando
	 */
	boolean validate(String stdout, String stderr);
}