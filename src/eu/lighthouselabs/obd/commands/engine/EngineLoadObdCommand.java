/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands.engine;

import ae.ac.masdar.labs.stevas.adama.Utilities;
import eu.lighthouselabs.obd.commands.ObdCommand;
import eu.lighthouselabs.obd.commands.PercentageObdCommand;

/**
 * Calculated Engine Load value.
 */
public class EngineLoadObdCommand extends PercentageObdCommand {

        /**
         * @param command
         */
        public EngineLoadObdCommand() {
                super("01 04");
        }

        /**
         * @param other
         */
        public EngineLoadObdCommand(ObdCommand other) {
                super(other);
        }

        /* (non-Javadoc)
         * @see eu.lighthouselabs.obd.commands.ObdCommand#getName()
         */
        @Override
        public String getName() {
                return "Engine Load";
        }
        @Override
        public String getFormattedResult() {
                String res = getResult();

                if (!"NODATA".equals(res)) {
                        // ignore first two bytes [hh hh] of the response
                        int load = buff.get(2) & 0xFF; // unsigned short
                        res = String.format("%.1f%s", (100.0f * load / 255.0f), "%");
                }

                return res;
        }

        public Integer getValue() {
            String res = getResult();

            if (!"NODATA".equals(res)) {
                    // ignore first two bytes [hh hh] of the response
                    return Utilities.getByteList(buff).get(2) & 0xFF; // unsigned short
            }

            return null;
        }


}
