package softtree;

import java.io.File;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import libClass.conec.ConexionBdg;
import libClass.conec.ParametrosConexionBdg;
import libClass.obj.ObjectApplication;
import libClass.obj.ObjectURLApplication;
import libClass.swing.UtilityLibraryLookAndFeels;
import libClass.util.ErrorDetection;
import libClass.util.UtilitiesSeveral;
import libClass.util.ui.InitialParams;
import libClass.util.ui.UIConfigInitialParam;
import softtree.fsm.LoginEmpresa;
import splashwindow.windows.DefaultSplashWindow;

/**
 *
 * @author López Leandro
 * @version $Revision: 1.7 $; $Date: 2012/12/05 21:51:51 $
 */
public class Main {

	private InitialParams initialParams = null;

	private ResourceBundle recursos = null;

	private ConexionBdg conexion = null;

	public Main() {
		initMain(false);
	}

	public Main(boolean aApplet) {
		initMain(aApplet);
	}

	public InitialParams getInitialParams() {
		return initialParams;
	}

	public void setInitialParams(InitialParams aInitialParams) {
		initialParams = aInitialParams;
	}

	public ResourceBundle getRecursos() {
		return recursos;
	}

	public void setRecursos(ResourceBundle aRecursos) {
		recursos = aRecursos;
	}

	public ConexionBdg getConexion() {
		return conexion;
	}

	public void setConexion(ConexionBdg aConexion) {
		conexion = aConexion;
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		boolean setup = false;

		if (args.length != 0) {
			for (int i = 0; i < args.length; i++) {
				String param = args[i].trim();

				if (param.equals("setup")) {
					setup = true;
				}
			}
		}

		if (setup) {
			new UIConfigInitialParam(Main.class).setVisible(true);
		} else {
			new Main();
		}
	}

	private void initMain(final boolean aApplet) {
		InitialParams aParams = new InitialParams(this.getClass());
		aParams.readParameter();

		setInitialParams(aParams);

		try {
			initLookAndFeel();
		} catch(Exception e) {
			JOptionPane.showMessageDialog(
				null,
				"Error en la carga de Look&Feel.",
				"ERROR DETECTADO",
				JOptionPane.ERROR_MESSAGE);
		}

		if (getInitialParams().getIpServer() == null) {
			JOptionPane.showMessageDialog(
				null,
				"Controle la configuración inicial del sistema.\n"
				+ "No aparece o esta mal cargado el Host del Servidor.",
				"ERROR DETECTADO",
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (getInitialParams().getIpServer().trim().length() == 0) {
			JOptionPane.showMessageDialog(
				null,
				"Controle la configuración inicial del sistema.\n"
				+ "No aparece o esta mal cargado el Host del Servidor.",
				"ERROR DETECTADO",
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (getInitialParams().getPort() == 0) {
			JOptionPane.showMessageDialog(
				null,
				"Controle la configuración inicial del sistema.\n"
				+ "No aparece o esta mal cargado el Puerto de \n"
				+ "comunicación con el Servidor.",
				"ERROR DETECTADO",
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (getInitialParams().getDbName() == null) {
			JOptionPane.showMessageDialog(
				null,
				"Controle la configuración inicial del sistema.\n"
				+ "No aparece o esta mal cargado el Nombre de la\n"
				+ "Base De Datos.",
				"ERROR DETECTADO",
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (getInitialParams().getDbName().trim().length() == 0) {
			JOptionPane.showMessageDialog(
				null,
				"Controle la configuración inicial del sistema.\n"
				+ "No aparece o esta mal cargado el Nombre de la\n"
				+ "Base De Datos.",
				"ERROR DETECTADO",
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		DefaultSplashWindow splash = new DefaultSplashWindow(5);
		splash.setDelay(1000);

		try {
			initErrorDetection();
			initRecursos();
			initConectDB();

			DefaultSplashWindow.getInstance().setProgressText("Inicializando Aplicación...");
			ObjectURLApplication obj = new ObjectURLApplication(
				this.getClass(), getRecursos().getString("dirFile"), getRecursos().getString("dirXML"));
			DefaultSplashWindow.getInstance().incrementProgressBarValue(1);

			DefaultSplashWindow.getInstance().close();

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					new LoginEmpresa(getConexion(), getRecursos(), this.getClass(), 
						aApplet, getInitialParams().isRemoteConnect());
				}
			});
		} catch(Exception e) {
			UtilitiesSeveral.showErrorDialog("La aplicación no pudo inicial correctamente.", e);
			ObjectApplication.killApplication();
		}
	}

	private void initErrorDetection() throws Exception {
		DefaultSplashWindow.getInstance().setProgressText("Cargando Detección De Errores...");

		URL aUrl = null;

		if (getInitialParams().getLogDir() != null) {
			if (getInitialParams().getLogDir().trim().length() > 0) {
				File aFile = new File(getInitialParams().getLogDir());

				if (aFile.exists()) {
					if (aFile.isDirectory()) {
						aUrl = aFile.toURI().toURL();
					}
				}
			}
		}

		if (aUrl != null) {
			new ErrorDetection(new Locale("es"), "softtree.archivos.errors", aUrl);
		} else {
			new ErrorDetection(new Locale("es"), "softtree.archivos.errors");
		}

		DefaultSplashWindow.getInstance().incrementProgressBarValue(1);
	}

	private void initLookAndFeel() throws Exception {
//		DefaultSplashWindow.getInstance().setProgressText("Cargando Look&Feels...");

		if ((getInitialParams().getNameLookAndFeel() != null) && (getInitialParams().getNameThemeLookAndFeel() != null)) {
			UtilityLibraryLookAndFeels.installLookAndFeel(
				getInitialParams().getNameLookAndFeel(),
				getInitialParams().getNameThemeLookAndFeel());
		} else {
			UtilityLibraryLookAndFeels.installLookAndFeel(
				UtilityLibraryLookAndFeels.LF_METAL,
				UtilityLibraryLookAndFeels.THEME_METAL_OCEAN);
		}

//		DefaultSplashWindow.getInstance().incrementProgressBarValue(1);
	}

	private void initConectDB() throws Exception {

		DefaultSplashWindow.getInstance().setProgressText("Conectando Base De Datos...");

		ParametrosConexionBdg param = new ParametrosConexionBdg();
		param.setTipoConexion("JDBC");
		param.setProveedor(ConexionBdg.DB_MYSQL31);
		param.setSvrName(getInitialParams().getIpServer());
		param.setPuertoDB(getInitialParams().getPort());
		param.setNombreDB(getInitialParams().getDbName());
		param.setNombreUsuario(getInitialParams().getUser());
		param.setPassword(getInitialParams().getPwd());

		ConexionBdg con = new ConexionBdg(param);

		if (!con.isOpen()) {
			throw new Exception("No Se Pudo Inicializar La Conexion A La Base De Datos");
		}

		setConexion(con);

		DefaultSplashWindow.getInstance().incrementProgressBarValue(1);
	}

	private void initRecursos() throws Exception {
		DefaultSplashWindow.getInstance().setProgressText("Cargando Archivo De Recursos...");

		String archivoRecursos = "softtree.archivos.softtree";
		setRecursos(ResourceBundle.getBundle(archivoRecursos));

		DefaultSplashWindow.getInstance().incrementProgressBarValue(1);
	}
}