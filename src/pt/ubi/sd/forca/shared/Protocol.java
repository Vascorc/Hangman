package pt.ubi.sd.forca.shared;

/**
 * Define as strings padrão utilizadas no protocolo de comunicação
 * entre o Cliente e o Servidor, conforme o enunciado.
 */
public class Protocol {

    // Mensagens enviadas pelo Servidor -> Cliente
    public static final String WELCOME = "WELCOME";      // WELCOME <id> <total>
    public static final String START   = "START";        // START <mask> <attempts> <timeout>
    public static final String ROUND   = "ROUND";        // ROUND <k> <mask> <attempts> <used>
    public static final String STATE   = "STATE";        // STATE <mask> <attempts> <used>
    public static final String END_WIN = "END WIN";      // END WIN <winners> <word>
    public static final String END_LOSE = "END LOSE";    // END LOSE <word>
    public static final String FULL     = "FULL";        // Servidor cheio

    // Mensagens enviadas pelo Cliente -> Servidor
    public static final String GUESS   = "GUESS";        // GUESS <text>
    
}