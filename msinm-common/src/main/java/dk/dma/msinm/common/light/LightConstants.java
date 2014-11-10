package dk.dma.msinm.common.light;

/**
 * Common constants and functionality for Light classes
 */
public interface LightConstants {

    /**
     * Supported light phases
     */
    enum Phase {
        FFl, LFl, Fl, F, IVQ, VQ, IQ, IUQ, UQ, Q, Iso, Oc, Alt, Mo
    }

    /**
     * Supported light colors
     */
    enum Color {
        W, R, G, B, Y, Am
    }

    /**
     * Returns the telephony code for the given character
     * @param c the character. Valid intervals: a-z and 0-9
     * @return the telephony code or a blank string if invalid
     */
    default String getTelephonyCode(char c) {
        switch (Character.toUpperCase(c)) {
            case 'A': return "Alpha";
            case 'B': return "Bravo";
            case 'C': return "Charlie";
            case 'D': return "Delta";
            case 'E': return "Echo";
            case 'F': return "Foxtrot";
            case 'G': return "Golf";
            case 'H': return "Hotel";
            case 'I': return "India";
            case 'J': return "Juliett";
            case 'K': return "Kilo";
            case 'L': return "Lima";
            case 'M': return "Mike";
            case 'N': return "November";
            case 'O': return "Oscar";
            case 'P': return "Papa";
            case 'Q': return "Quebec";
            case 'R': return "Romeo";
            case 'S': return "Sierra";
            case 'T': return "Tango";
            case 'U': return "Uniform";
            case 'V': return "Victor";
            case 'W': return "Whiskey";
            case 'X': return "Xray";
            case 'Y': return "Yankee";
            case 'Z': return "Zulu";
            case '1': return "One";
            case '2': return "Two";
            case '3': return "Three";
            case '4': return "Four";
            case '5': return "Five";
            case '6': return "Six";
            case '7': return "Seven";
            case '8': return "Eight";
            case '9': return "Nine";
            case '0': return "Zero";
        }
        return "";
    }

}
