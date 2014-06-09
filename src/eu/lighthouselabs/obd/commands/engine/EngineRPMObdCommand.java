/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands.engine;

import ae.ac.masdar.labs.stevas.adama.Utilities;
import eu.lighthouselabs.obd.commands.ObdCommand;

/**
 * Displays the current engine revolutions per minute (RPM).
 */
public class EngineRPMObdCommand extends ObdCommand {

        /**
         * Default ctor.
         */
        public EngineRPMObdCommand() {
                super("01 0C");
        }

        /**
         * Copy ctor.
         *
         * @param other
         */
        public EngineRPMObdCommand(EngineRPMObdCommand other) {
                super(other);
        }

        /**
         * @return the engine RPM per minute
         */
        @Override
        public String getFormattedResult() {
                String res = getResult();
                int value = 0;

                if (!"NODATA".equals(res)) {
                        // ignore first two bytes [01 0C] of the response
                        int b1 = Utilities.getByteList(buff).get(2) & 0xFF;
                        int b2 = Utilities.getByteList(buff).get(3) & 0xFF;
                        value = (((b1 << 8) | b2) & 0xFFFF) / 4;
                }

                return String.format("%d%s", value, "RPM");
        }

        public Double getValue() {
                String res = getResult();

                if (!"NODATA".equals(res)) {
                        // ignore first two bytes [01 0C] of the response
                        int b1 = Utilities.getByteList(buff).get(2) & 0xFF;
                        int b2 = Utilities.getByteList(buff).get(3) & 0xFF;
                        return (double) ((((b1 << 8) | b2) & 0xFFFF) / 4);
                }

                return null;
        }

        @Override
        public String getName() {
                return "Engine RPM";
        }
}
