package br.com.alcidesbezerra.bff.generica.security.exception;

import br.com.alcidesbezerra.bff.generica.security.domain.ErrorType;

public class ServerErrorException extends AbstractErrorException {

    private static final long serialVersionUID = -5809529426540256544L;

    public ServerErrorException(String msg) {
        super(msg);
    }

    public ServerErrorException(String msg, Throwable th) {
        super(msg, th);
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.INTERNAL_ERROR;
    }

}
