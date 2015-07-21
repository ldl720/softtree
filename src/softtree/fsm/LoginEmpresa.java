package softtree.fsm;

import java.sql.SQLException;
import java.util.ResourceBundle;
import libClass.conec.ConexionBdg;
import libClass.exc.ConstructorSelectException;
import libClass.exc.DBClosedException;
import libClass.exc.InvalidDateException;
import libClass.exc.NotFoundException;
import libClass.fsm.Empresa;
import libClass.fsm.EmpresaPConexion;
import libClass.fsm.Estado;
import libClass.fsm.Usuario;
import libClass.obj.ObjectApplication;
import libClass.swing.ILogin;
import libClass.util.GenericLogin;
import softtree.Main;
import softtree.ui.MainMDI;
import splashwindow.windows.DefaultSplashWindow;

/**
 *
 * @author López Leandro
 * @version $Revision: 1.9 $; $Date: 2014/02/20 23:03:36 $
 */
public class LoginEmpresa extends GenericLogin implements ILogin {

	public LoginEmpresa(ConexionBdg con, ResourceBundle rb,
		Class aClassForNodePreferences, boolean aRemoteConnection) {

		setConexionDBSistema(con);
		setRemoteConnection(aRemoteConnection);
		setClassForNodePreferences(aClassForNodePreferences);
		setRecursos(rb);
		initWorkWithEnterprise();
		controlAccessOfUser();
	}

	public LoginEmpresa(ConexionBdg con, ResourceBundle rb,
		Class aClassForNodePreferences, boolean aApplet, boolean aRemoteConnection) {

		setApplet(aApplet);
		setConexionDBSistema(con);
		setRemoteConnection(aRemoteConnection);
		setClassForNodePreferences(aClassForNodePreferences);
		setRecursos(rb);
		initWorkWithEnterprise();
		controlAccessOfUser();
	}

	/**
	 * La implementación de este método es la encargada de decirle a la clase que
	 * se debe hacer en el caso de que un curso de acción se haya realizado
	 * correctamente.
	 *
	 * @throws java.sql.SQLException
	 */
	@Override
	public void startApplication() throws ConstructorSelectException,
		NotFoundException, NullPointerException, DBClosedException, SQLException, InvalidDateException {

		if (getEmpresaPConexion() != null) {
			boolean flagConec = initConectToDBEnterprise(getEmpresaPConexion());

			if (flagConec) {
				//Cierro la conexión activa a la base de datos de datos de sistem
				//porque a partir de este punto no se la utiliza mas.
				closeConnectDBSystem();

				ObjectApplication application = new ObjectApplication(
					getConexionDBEmpresa(), getRecursos(), Main.class);

				String login = getEmpresaPConexion().getUsuarioPConexion().getLogin();
				String passwd = getEmpresaPConexion().getUsuarioPConexion().getPassword();

				Usuario aUsr = new Usuario(login, passwd);

				if (aUsr.getEstado() == Estado.ACTIVO) {
					Empresa aEmpresa = new Empresa();
					aEmpresa.setId(1);

					application.setActiveEnterprise(aEmpresa);

					//Ejecuto el select despues de asignar la empresa a la aplicación ya
					//que algunos de los objetos que se ensamblan en la empresa le piden
					//a la aplicación al empresa activa.
					boolean flag = aEmpresa.select();

					if (flag) {
						application.setUsuario(aUsr);
						application.setApplet(isApplet());
						application.startCheckConnection();

						MainMDI mainMDI = new MainMDI(getEmpresaPConexion().getNombre(), getURLFileWallpaper(getEmpresaPConexion()));
						mainMDI.setVisible(true);
					} else {
						throw new ConstructorSelectException("La empresa no se pudo "
							+ "instanciar correctamente.");
					}
				} else {
					throw new InvalidDateException(
						"Usuario inhabilitado para el ingreso al sistema.");
				}
			} else {
				cancelApplication();
			}
		} else {
			cancelApplication();
		}
	}

	@Override
	public void cancelApplication() {
		super.cancelApplication();

		if (DefaultSplashWindow.getInstance() != null) {
			DefaultSplashWindow.getInstance().close();
		}

		System.exit(0);
	}

	@Override
	public void setSelectedEnterprise(EmpresaPConexion aSelectedEnterprise) {
		setEmpresaPConexion(aSelectedEnterprise);
	}

	@Override
	public Object getObjISystemIdentification() {
		return new EmpresaPConexion();
	}
}
