package ch.windmobile.model;

public class ServerException extends WindMobileException {
    private static final long serialVersionUID = 1L;

    public ServerException(CharSequence localizedName, CharSequence message) {
        super(localizedName, message);
    }
}
