package br.com.alcidesbezerra.bff.generica.security.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Classe utilitária para possibilitar a obtenção de beans do Spring de forma estática, por classes que não estão no
 * contexto do Spring.
 */
@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext CONTEXT;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        CONTEXT = applicationContext;
    }

    /**
     * Busca um bean pelo seu tipo.
     **/
    public static <T> T getBean(final Class<T> beanClass) {
        if (CONTEXT == null) {
            return null;
        }

        return CONTEXT.getBean(beanClass);
    }

}
