package org.jboss.deployers.plugins.internal;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public interface Block<T>
{
   void apply(T t);
}
