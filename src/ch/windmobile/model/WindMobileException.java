package ch.windmobile.model;

public abstract class WindMobileException extends Exception {
    private static final long serialVersionUID = 1L;

    private final CharSequence localizedName;
    private final boolean isFatal;

    public WindMobileException(CharSequence localizedName, CharSequence message) {
        super(message.toString());
        this.localizedName = localizedName;
        this.isFatal = false;
    }

    public WindMobileException(CharSequence localizedName, CharSequence message, boolean isFatal) {
        super(message.toString());
        this.localizedName = localizedName;
        this.isFatal = isFatal;
    }

    public CharSequence getLocalizedName() {
        return localizedName;
    }

    public boolean isFatal() {
        return isFatal;
    }

    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }
}
