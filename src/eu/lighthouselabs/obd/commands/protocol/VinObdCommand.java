package eu.lighthouselabs.obd.commands.protocol;

import eu.lighthouselabs.obd.commands.ObdCommand;

public class VinObdCommand extends ObdCommand {

    /**
     * @param command
     */
    public VinObdCommand() {
            super("09 02");
    }

    /**
     * @param other
     */
    public VinObdCommand(ObdCommand other) {
            super(other);
    }

    /*
     * (non-Javadoc)
     *
     * @see eu.lighthouselabs.obd.commands.ObdCommand#getFormattedResult()
     */
    @Override
    public String getFormattedResult() {
            String res = getResult();
            if(res == null || "NODATA".equals(res)) return "NODATA";
            try {
	            StringBuilder sb = new StringBuilder();
	            for (byte b : this.buff)
	                    sb.append((char) b);
//	            return sb.toString().replaceAll("\\s\\d[:]\\s", "\n");
	            String[] sbs = sb.toString().replaceAll("\\s\\d[:]\\s", " ").split("\\W+");
	            byte[] ba = new byte[sbs.length-3];
	            int i = 0; 
	            for(String sbc : sbs) {
            		if(sbc.length() >= 2) {
		            	if(i>3) {
		            		ba[i-4] = (byte)Integer.parseInt(sbc, 16);
		            	}
		            	i++;
            		}
	            }
	            sb = new StringBuilder();
	            for (byte b : ba)
	            	sb.append((char) b);
	            return sb.toString().trim();
            } catch(Exception ex) {
            	return "NODATA";
            }
    }

    @Override
    public String getName() {
            return "Vehicle Identification Number";
    }

}
