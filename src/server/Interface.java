package server;

import java.io.File;
import java.rmi.Remote;

public interface Interface extends Remote {
	public String setPic(File pic) throws Exception;

	public void connect() throws Exception;
}
