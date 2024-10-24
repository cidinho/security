package br.com.alcidesbezerra.bff.generica.security.domain;

import org.apache.commons.lang3.StringUtils;

import br.com.alcidesbezerra.bff.generica.security.util.ValueUtil;


public final class Cpf extends SensitiveData {

    private static final long serialVersionUID = -7189188677821902502L;
    private static final Integer UNMASK_SIZE = 3;

    public Cpf(final String data) {
        super(data, true);
    }

    public Cpf(final String data, final boolean isEncrypted) {
        super(data, isEncrypted);
    }

    @Override
    public boolean isValid() {
        return StringUtils.isNumeric(get());
    }

    @Override
    public String formatValue(final String cpf) {
        return ValueUtil.getOnlyNumbers(cpf);
    }

    @Override
    public Integer unmaskSize() {
        return UNMASK_SIZE;
    }
}
