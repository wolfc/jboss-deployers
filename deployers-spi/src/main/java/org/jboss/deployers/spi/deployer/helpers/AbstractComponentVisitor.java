/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.deployers.spi.deployer.helpers;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.spi.DeploymentException;

/**
 * Simple component visitor.
 *
 * @param <T> exact attachment type
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class AbstractComponentVisitor<T> extends ComponentAdapter<T> implements DeploymentVisitor<T>
{
   /**
    * Get attachment name.
    * By default we return visitor type's name.
    *
    * @param attachment the attachment
    * @return the attachment name
    */
   protected String getAttachmentName(T attachment)
   {
      return getVisitorType().getName();
   }

   public void deploy(DeploymentUnit unit, T attachment) throws DeploymentException
   {
      addComponent(unit, attachment);
   }

   public void undeploy(DeploymentUnit unit, T attachment)
   {
      removeComponent(unit, attachment);
   }
}
