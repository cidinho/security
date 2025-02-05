package br.com.alcidesbezerra.bff.generica.security.exception;

import br.com.alcidesbezerra.bff.generica.security.domain.ErrorType;
public class ClientErrorException extends AbstractErrorException {

    private static final long serialVersionUID = 1486297407038116871L;

    private final ErrorType errorType;

    public ClientErrorException(ErrorType errorType, String msg) {
        super(msg);
        this.errorType = errorType;
    }

    public ClientErrorException(ErrorType errorType, String msg, final Throwable th) {
        super(msg, th);
        this.errorType = errorType;
    }

    @Override
    public ErrorType getErrorType() {
        return errorType;
    }

}
