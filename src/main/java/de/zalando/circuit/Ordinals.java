package de.zalando.circuit;

final class Ordinals {

    private Ordinals() {
        throw new UnsupportedOperationException();
    }

    static String valueOf(final int i) {
        switch (i % 100) {

            case 11 :
            case 12 :
            case 13 :
                return "th";

            default :
                switch (i % 10) {

                    case 1 :
                        return "st";

                    case 2 :
                        return "nd";

                    case 3 :
                        return "rd";

                    default :
                        return "th";
                }
        }
    }
}
