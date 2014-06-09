/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands.engine;

import ae.ac.masdar.labs.stevas.adama.Utilities;
import eu.lighthouselabs.obd.commands.ObdCommand;

/**
 * TODO put description
 *
 * Mass Air Flow
 */
public class MassAirFlowObdCommand extends ObdCommand {

        private double maf = -9999.0;

        /**
         * Default ctor.
         */
        public MassAirFlowObdCommand() {
                super("01 10");
        }

        /**
         * Copy ctor.
         *
         * @param other
         */
        public MassAirFlowObdCommand(MassAirFlowObdCommand other) {
                super(other);
        }

        /**
         *
         */
        @Override
        public String getFormattedResult() {
                String res = getResult();

                if (!"NODATA".equals(res)) {
                        // ignore first two bytes [hh hh] of the response
                        int b1 = Utilities.getByteList(buff).get(2) & 0xFF;
                        int b2 = Utilities.getByteList(buff).get(3) & 0xFF;
                        maf = (((b1 << 8) | b2) & 0xFFFF) / 100.0f;
                        res = String.format("%.2f%s", maf, "g/s");
                }

                return res;
        }

        /**
         * @return MAF value for further calculus.
         */
        public double getMAF() {
            String res = getResult();

            if (!"NODATA".equals(res)) {
                    // ignore first two bytes [hh hh] of the response
                    int b1 = Utilities.getByteList(buff).get(2) & 0xFF;
                    int b2 = Utilities.getByteList(buff).get(3) & 0xFF;
                    return (((b1 << 8) | b2) & 0xFFFF) / 100.0f;
            }
            return -9999.0;
        }

        public Double getValue() {
            String res = getResult();

            if (!"NODATA".equals(res)) {
                    // ignore first two bytes [01 10] of the response
                    int b1 = Utilities.getByteList(buff).get(2) & 0xFF;
                    int b2 = Utilities.getByteList(buff).get(3) & 0xFF;
                    return (double) (((b1 << 8) | b2) & 0xFFFF) / 100.0f;
            }

            return null;
        }

        @Override
        public String getName() {
                return "Mass Air Flow";
        }
}
