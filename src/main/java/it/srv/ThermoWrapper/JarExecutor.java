package it.srv.ThermoWrapper;

import it.srv.ThermoWrapper.exception.JarExecutorException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class JarExecutor {

    private BufferedReader error;
    private BufferedReader op;
    private int exitVal;
    private Process process;

    /**
     * @param jarFileName name of the file to run without the jar extension
     * @throws JarExecutorException when the process is interrupted or happens an IOException
     */
    public void executeJar(String jarFileName) throws JarExecutorException {

        final StringBuilder sb = new StringBuilder();
        sb.append("java -Xms512m -Xmx2g ");
        sb.append("-jar ");
        sb.append(jarFileName);
        sb.append(".jar");
        try {
            final Runtime re = Runtime.getRuntime();
            String command = sb.toString();
            process = re.exec(command);
            this.error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            this.op = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String s;
            while((s = op.readLine()) != null){
                System.out.println(s);
            }
            process.waitFor();
            this.exitVal = process.exitValue();
            if (this.exitVal != 0 && this.exitVal != 1) {
                throw new IOException("Failed to execure jar, " + this.getExecutionLog());
            }
        } catch (final IOException | InterruptedException e) {
            throw new JarExecutorException(e.getMessage());
        }
    }

    /**
     * Fast way to kill the specific running process
     */
    public void destroy(){
        process.destroy();
    }

    public String getExecutionLog() {
        StringBuilder error = new StringBuilder();
        String line;
        try {
            while((line = this.error.readLine()) != null) {
                error.append("\n").append(line);
            }
        } catch (final IOException ignored) { }
        String output = "";
        try {
            while((line = this.op.readLine()) != null) {
                output = output + "\n" + line;
            }
        } catch (final IOException ignored) { }
        try {
            this.error.close();
            this.op.close();
        } catch (final IOException ignored) { }
        return "exitVal: " + this.exitVal + ", error: " + error + ", output: " + output;
    }
}
