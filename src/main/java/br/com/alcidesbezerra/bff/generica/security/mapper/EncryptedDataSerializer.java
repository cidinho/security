package br.com.alcidesbezerra.bff.generica.security.mapper;

import static java.util.Objects.isNull;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import br.com.alcidesbezerra.bff.generica.security.domain.EncryptedData;

public class EncryptedDataSerializer extends StdSerializer<EncryptedData> {

    private static final long serialVersionUID = 3791648591426157822L;

    public EncryptedDataSerializer() {
        super(EncryptedData.class);
    }

    @Override
    public void serialize(final EncryptedData value, final JsonGenerator generator,
        final SerializerProvider serializers)
        throws IOException {
        if (isNull(value)) {
            generator.writeNull();
        } else {
            generator.writeString(value.encrypt());
        }
    }

}
