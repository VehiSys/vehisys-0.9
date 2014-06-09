/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands.temperature;

import ae.ac.masdar.labs.stevas.adama.Utilities;

/**
 * Engine Coolant Temperature.
 */
public class EngineCoolantTemperatureObdCommand extends TemperatureObdCommand {

        /**
         *
         */
        public EngineCoolantTemperatureObdCommand() {
                super("01 05");
        }

        /**
         * @param other
         */
        public EngineCoolantTemperatureObdCommand(TemperatureObdCommand other) {
                super(other);
        }

        /*
         * (non-Javadoc)
         *
         * @see eu.lighthouselabs.obd.commands.ObdCommand#getName()
         */
        @Override
        public String getName() {
                return "Engine Coolant Temperature";
        }

        public Integer getValue() {
            String res = getResult();

            if (!"NODATA".equals(res)) {
                    try {
                    	return (((int)Utilities.getByteList(buff).get(2)) & 0xFF)-40;
                    } catch (Exception e) {
                            /*
                             * TODO this must be revised.
                             */
                            return null;
                    }
            }
            return null;
        }
}
