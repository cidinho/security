
import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;


import lombok.AllArgsConstructor;
/**
 * Esta classe é uma abstração para realizar chamadas a serviços soap de forma mais simples. A ideia é que essa classe
 * seja sobrescrita em duas camadas. A primeira por um cada do WebService, que terá o tratamento de erro, e a segunda
 * que é a implementação do método {@code invoke()} que fará a execução da operação do WebService.
 *
 * @param <STUBIN> Stub de entrada da operação
 * @param <STUBOUT> Stub de saída da operação
 * @param <HEADER> Headers para a operação. Pode ser um classe própria ou um simples {@code Map<String, String>}
 *
 */
public abstract class AbstractInvoker<STUBIN, STUBOUT, HEADER> {

    /**
     * Método para setar e preparar o request para a operação do WebService.
     *
     * Caso não seja chamada o {@code header(HEADER)} o valor do header será {@code Optional.empty()}
     *
     * @param request Request de domínio da aplicação
     */
    public <REQUEST> AbstractInvokerPrepareHandler<STUBIN, STUBOUT, HEADER, REQUEST> prepare(final REQUEST request) {
        return new AbstractInvokerPrepareHandler<>(this, request, empty());
    }

    /**
     * Indica qual o valor do header que será passado para o método {@code execute(STUBIN, HEADER}
     *
     * @param header header com valores para a operação
     */
    public AbstractInvokerHeaderHandler<STUBIN, STUBOUT, HEADER> header(final HEADER header) {
        return new AbstractInvokerHeaderHandler<>(this, header);
    }

    /**
     * Faz a chamada para a operação do WebService. Este método faz a chamada para o método @{code execute(STUBIN,
     * HEADER)}
     */
    public AbstractInvokerResponseHandler<STUBOUT> run(final STUBIN stubin) {
        return new AbstractInvokerResponseHandler<>(execute(stubin, empty()));
    }

    /**
     * Lança a exception indicando falha inesperada.
     *
     * @param e Exception que ocorreu
     */
    protected RuntimeException falhaInesperada(final Exception e) {
        return new ServerErrorException(Message.FALHA_INESPERADA.name().replace("_", " "), e);
    }

    /**
     * Faz o encapsulamento da chamada ao método {@code invoke} captudando qualquer exception e chamando o método {@code
     * handleException()} para fazer o seu tratamento.
     *
     * @param stubin Classe de stub da operação
     * @param header Header indicado no método {@code header(HEADER)}. Caso não tenha sido indicado, será empty()
     * @return STUB de response da operação
     */
    private STUBOUT execute(final STUBIN stubin, final Optional<HEADER> header) {
        try {
            return invoke(stubin, header);
        } catch (final Exception e) {
            throw handleException(e);
        }
    }

    /**
     * Executa a chamada ao invoker do webservice.
     *
     * @param stubin Stub de request da operação.
     * @param header Header com valores necessários para execução da operação.
     * @return STUB de response da operação
     * @throws Exception Qualquer exceção que ocorra na chamada ao webservice/operação.
     */
    protected abstract STUBOUT invoke(final STUBIN stubin, final Optional<HEADER> header) throws Exception;

    /**
     * Faz o tratamento da exception que possa ocorrer na chamada ao webservice/operação.
     */
    public abstract RuntimeException handleException(final Exception e);

    /**
     * Classe criada ao iniciar o fluxo com um Header.
     */
    @AllArgsConstructor
    public static final class AbstractInvokerHeaderHandler<STUBIN, STUBOUT, HEADER> {

        private final AbstractInvoker<STUBIN, STUBOUT, HEADER> invoker;
        private final HEADER header;

        /**
         * Seta o request (classe de domínio da aplicação) que deverá ser usado para chamar a operação do WebService.
         *
         * @param request Objeto de request para a operação
         * @return Classe que encapsula o REQUEST para a operação
         */
        public <REQUEST> AbstractInvokerPrepareHandler<STUBIN, STUBOUT, HEADER, REQUEST> prepare(
            final REQUEST request) {
            return new AbstractInvokerPrepareHandler<>(invoker, request, of(header));
        }

        /**
         * Faz a chamada ao invoker da operação passando o {@code STUBIN} passado por parâmetro e o header indicado no
         * método anterior.
         *
         * @param stub STUB de request da operação.
         * @return Classe que encapsula o STUB de response.
         */
        public AbstractInvokerResponseHandler<STUBOUT> run(final STUBIN stub) {
            return new AbstractInvokerResponseHandler<>(invoker.execute(stub, of(header)));
        }
    }

    /**
     * Classe que encapsula o request (classe de domínio da aplicação).
     *
     * @param <STUBIN> STUB de request da operação do WebService
     * @param <STUBOUT>STUB de response da operação do WebService
     * @param <HEADER> Header para a operação
     * @param <REQUEST> Classe de request com domínio da aplicação
     */
    @AllArgsConstructor
    public static final class AbstractInvokerPrepareHandler<STUBIN, STUBOUT, HEADER, REQUEST> {

        private final AbstractInvoker<STUBIN, STUBOUT, HEADER> invoker;
        private final REQUEST request;
        private final Optional<HEADER> header;

        /**
         * Responsável por fazer o map do REQUEST para o STUB de request da operação.
         *
         * @param mapper Função para aplicar o mapper
         * @return Classe que encapsula o request para o WebService
         */
        public AbstractInvokerCallHandler<STUBIN, STUBOUT, HEADER> map(final Function<REQUEST, STUBIN> mapper) {
            return new AbstractInvokerCallHandler<>(invoker, mapper.apply(request), header);
        }
    }

    /**
     * Classe que encapsula a chamada a operação do WebService
     *
     * @param <STUBIN> STUB de request da operação do WebService
     * @param <STUBOUT>STUB de response da operação do WebService
     * @param <HEADER> Header para a operação
     */
    @AllArgsConstructor
    public static final class AbstractInvokerCallHandler<STUBIN, STUBOUT, HEADER> {

        private final AbstractInvoker<STUBIN, STUBOUT, HEADER> invoker;
        private final STUBIN stubin;
        private final Optional<HEADER> header;

        /**
         * Faz a chamada para a operação do WebService e obtém o seu response
         *
         * @return STUB de response da operação do WebService
         */
        private STUBOUT get() {
            return invoker.execute(stubin, header);
        }

        /**
         * Faz a chamada para a operação do WebServive (chamando o {@code get()}) e obtém o retorno da operação.
         *
         * @return Classe que encapsula o STUB de retorno da operação
         */
        public AbstractInvokerResponseHandler<STUBOUT> run() {
            return new AbstractInvokerResponseHandler<>(get());
        }
    }

    /**
     * Classe que encapsula o STUB de response da operação do WebService
     *
     * @param <STUBOUT> STUB de response da operação do WebService
     */
    @AllArgsConstructor
    public static final class AbstractInvokerResponseHandler<STUBOUT> {

        private final STUBOUT stubout;

        /**
         * Retorna o STUB de response da operação
         */
        public STUBOUT get() {
            return this.stubout;
        }

        /**
         * Obtém o response da operação fazendo o mapper para um objeto de domínio da aplicação
         *
         * @param mapper Função para mapear o STUB de reponse para uma classe de domínio da aplicação.
         * @return Resultado da função {@param mapper}
         */
        public <RESPONSE> Supplier<RESPONSE> map(final Function<STUBOUT, RESPONSE> mapper) {
            return () -> mapper.apply(stubout);
        }
    }
}
