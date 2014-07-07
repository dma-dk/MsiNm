/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.msinm.common.config;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Can be used to force injection in classes that do not support CDI
 *
 * Nicked from: http://docs.jboss.org/weld/reference/1.1.0.Final/en-US/html_single/#d0e5286
 *
 * @author peder
 */
public class CdiHelper {

    /**
     * Don't instantiate this class
     */
    private CdiHelper() {
    }

    /**
     * Performs the injection of the given class upon the given injectionObject
     *
     * @param clazz The class of the object to inject upon
     * @param injectionObject the object to inject upon
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> void programmaticInjection(Class clazz, T injectionObject) throws NamingException {
        InitialContext initialContext = new InitialContext();
        Object lookup = initialContext.lookup("java:comp/BeanManager");
        BeanManager beanManager = (BeanManager) lookup;
        AnnotatedType annotatedType = beanManager.createAnnotatedType(clazz);
        InjectionTarget injectionTarget = beanManager.createInjectionTarget(annotatedType);
        CreationalContext creationalContext = beanManager.createCreationalContext(null);
        injectionTarget.inject(injectionObject, creationalContext);
        creationalContext.release();
    }

    /**
     * Looks up a CDI managed bean with the given class
     *
     * @param clazz The class of the object to look up
     * @return the object with the given class
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> clazz) throws NamingException {
        InitialContext initialContext = new InitialContext();
        Object lookup = initialContext.lookup("java:comp/BeanManager");
        BeanManager beanManager = (BeanManager) lookup;
        Bean<T> bean = (Bean<T>)beanManager.getBeans(clazz).iterator().next();
        CreationalContext<T> cc = beanManager.createCreationalContext(bean);
        return (T)beanManager.getReference(bean, clazz, cc);
    }
}
