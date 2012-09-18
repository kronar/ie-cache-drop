import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.google.common.base.Preconditions;

public class Runner {

	private static final int DEFAULT_PORT = 9090;

	private static final Logger LOGGER = Logger.getLogger(Runner.class
			.getName());

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		int startPort = DEFAULT_PORT;
		if (args.length > 0) {
			final int port = Integer.parseInt(args[0]);
			Preconditions.checkArgument(port > 0 && port < 65536,
					"port should be in range (1..65536");
			startPort = port;
		}

		Server server = new Server(startPort);
		final ServletContextHandler servletContextHandler = new ServletContextHandler();
		servletContextHandler.addServlet(
				new ServletHolder(new IeCleanServlet()), "/*");

		server.setHandler(servletContextHandler);
		server.start();

	}

	private static final class IeCleanServlet extends HttpServlet {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2321495989904584026L;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			final int ieCleanResult = IeCacheCleaner.doCleanIe();
			if (ieCleanResult == 0) {
				resp.setStatus(HttpServletResponse.SC_OK);
			} else {
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}

	}

	public static final class IeCacheCleaner {
		private static final String CLEAN_IE_BROWSER_CACHE = "RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 255";

		public static int doCleanIe() {
			try {
				final Process exec = Runtime.getRuntime().exec(
						CLEAN_IE_BROWSER_CACHE);
				final int waitFor = exec.waitFor();
				LOGGER.info("Exit code on clean cache operation " + waitFor);
				return waitFor;
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			} catch (InterruptedException e) {
				e.printStackTrace();
				return -1;
			}
		}
	}
}
