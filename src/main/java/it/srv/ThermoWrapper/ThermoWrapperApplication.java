package it.srv.ThermoWrapper;

import it.srv.ThermoWrapper.dao.InfoDAO;
import it.srv.ThermoWrapper.exception.JarExecutorException;
import it.srv.ThermoWrapper.model.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class ThermoWrapperApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThermoWrapperApplication.class, args);
	}

	@Autowired
	private InfoDAO infoDAO;

	private final JarExecutor webExecutor = new JarExecutor();
	private final JarExecutor toolsExecutor = new JarExecutor();
	private final VersionManager versionManager = new VersionManager();

	@Bean
	public void startup(){
		Info info = infoDAO.findLastTemporal();
		Info search = versionManager.searchNewVersion();
		if (info==null){
			versionManager.download(search.getWebeurl(), "ThermoSmartSpring".concat(search.getWebversion()).concat(".jar"));
			versionManager.download(search.getToolsurl(), "ThermoTools".concat(search.getToolsversion()).concat(".jar"));
			search.setLastupdate(LocalDateTime.now());
			infoDAO.save(search);
		} else {
			versionManager.checkVersions(search, info, infoDAO);
		}
		WebRunner wr = new WebRunner();
		ToolsRunner tr = new ToolsRunner();
		wr.start();
		tr.start();
		scheduledRunner();
	}

	public class WebRunner implements Runnable {
		private Thread t;

		@Override
		public void run() {
			String version = infoDAO.findLastTemporal().getWebversion();
			try {
				webExecutor.executeJar("ThermoSmartSpring".concat(version));
			} catch (JarExecutorException e) {
				e.printStackTrace();
			}
		}

		public void start () {
			if (t == null) {
				t = new Thread (this);
				t.start ();
			}
		}
	}

	public class ToolsRunner implements Runnable {
		private Thread t;

		@Override
		public void run() {
			String version = infoDAO.findLastTemporal().getToolsversion();
			try {
				toolsExecutor.executeJar("ThermoTools".concat(version));
			} catch (JarExecutorException e) {
				e.printStackTrace();
			}
		}

		public void start () {
			if (t == null) {
				t = new Thread (this);
				t.start ();
			}
		}
	}

	public class VersionManagerRunner implements Runnable {
		@Override
		public void run() {
			Info newVersion = versionManager.searchNewVersion();
			Info actualVersion = infoDAO.findLastTemporal();
			boolean installed = versionManager.checkVersions(newVersion, actualVersion, infoDAO);
			if (installed){
				if (!newVersion.getWebversion().equalsIgnoreCase(actualVersion.getWebversion())){
					webExecutor.destroy();
					try { Thread.sleep(1500); } catch (InterruptedException ignored) { }
					File old = new File("ThermoSmartSpring".concat(actualVersion.getWebversion()).concat(".jar"));
					if (old.exists() && old.delete())
						System.out.println("Thermo jar deleted");
					else
						System.out.println("Error in Thermo jar deletion");
					WebRunner wr = new WebRunner();
					wr.start();
				}
				if (!newVersion.getToolsversion().equalsIgnoreCase(actualVersion.getToolsversion())) {
					toolsExecutor.destroy();
					try { Thread.sleep(1500); } catch (InterruptedException ignored) { }
					File old = new File("ThermoTools".concat(actualVersion.getToolsversion()).concat(".jar"));
					if (old.exists() && old.delete())
						System.out.println("Tools jar deleted");
					else
						System.out.println("Error in Tools jar deletion");
					ToolsRunner tr = new ToolsRunner();
					tr.start();
				}
			}

		}
	}

	public void scheduledRunner() {
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(new VersionManagerRunner(), 30, 45, TimeUnit.SECONDS);
	}
}
