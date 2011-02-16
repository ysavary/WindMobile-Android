package ch.windmobile.model;

public class ClientException extends WindMobileException {
    private static final long serialVersionUID = 1L;

    public ClientException(CharSequence localizedName, CharSequence message) {
        super(localizedName, message);
    }

    public ClientException(CharSequence localizedName, CharSequence message, boolean isFatal) {
        super(localizedName, message, isFatal);
    }
}
