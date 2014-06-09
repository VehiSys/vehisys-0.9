/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands.control;

import ae.ac.masdar.labs.stevas.adama.Utilities;
import eu.lighthouselabs.obd.commands.ObdCommand;

/**
 * Fuel systems that use conventional oxygen sensor display the commanded open
 * loop equivalence ratio while the system is in open loop. Should report 100%
 * when in closed loop fuel.
 *
 * To obtain the actual air/fuel ratio being commanded, multiply the
 * stoichiometric A/F ratio by the equivalence ratio. For example, gasoline,
 * stoichiometric is 14.64:1 ratio. If the fuel control system was commanded an
 * equivalence ratio of 0.95, the commanded A/F ratio to the engine would be
 * 14.64 * 0.95 = 13.9 A/F.
 */
public class CommandEquivRatioObdCommand extends ObdCommand {

        /*
         * Equivalent ratio (%)
         */
        private double ratio = 0.00;

        /**
         * Default ctor.
         */
        public CommandEquivRatioObdCommand() {
                super("01 44");
        }

        /**
         * Copy ctor.
         *
         * @param other
         */
        public CommandEquivRatioObdCommand(CommandEquivRatioObdCommand other) {
                super(other);
        }

        /**
         *
         */
        @Override
        public String getFormattedResult() {
                String res = getResult();

                if (!"NODATA".equals(res)) {
                        try {
                                // ignore first two bytes [hh hh] of the response
                                int b1 = Utilities.getByteList(buff).get(2) & 0xFF;
                                int b2 = Utilities.getByteList(buff).get(3) & 0xFF;
                                // TODO should we OR the 2 bytes? or are they int,fraction?
                                // don't forget 0xFF
                                ratio = ((int) (b1 << 8) | b2) / 32768;
                                res = String.format("%.1f%s", ratio, "%");
                        } catch (Exception e) {
                                /*
                                 * TODO this must be revised.
                                 */
                                return "NODATA";
                        }
                }

                return res;
        }

        /**
         * @return
         */
        public Double getRatio() {
            String res = getResult();

            if (!"NODATA".equals(res)) {
                    try {
                            int b1 = Utilities.getByteList(buff).get(2) & 0xFF;
                            int b2 = Utilities.getByteList(buff).get(3) & 0xFF;
                            return ((double)((int) (b1 << 8) | b2)) / 32768.0;
                    } catch (Exception e) {
                            return null;
                    }
            }

            return null;
        }

        @Override
        public String getName() {
                return "Command Equivalence Ratio";
        }
}
