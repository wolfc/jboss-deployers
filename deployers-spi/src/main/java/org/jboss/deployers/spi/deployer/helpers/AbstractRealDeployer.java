/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

import org.jboss.dependency.spi.ControllerContext;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.structure.spi.DeploymentRegistry;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * AbstractRealDeployer.
 * 
 * @author <a href="adrian@jboss.org">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.org">Ales Justin</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractRealDeployer extends AbstractDeployer
{
   /** Use unit name for controller context name */
   private boolean useUnitName;

   /** The controller context to deployment unit mapping */
   private DeploymentRegistry registry;

   /**
    * Create a new AbstractRealDeployer.
    */
   public AbstractRealDeployer()
   {
      setStage(DeploymentStages.REAL);
   }

   public final void deploy(DeploymentUnit unit) throws DeploymentException
   {
      internalDeploy(unit);

      if (isControllerContextNameCandidate(unit))
      {
         addControllerContextName(unit);
      }
   }

   /**
    * Should we set controller context name on unit?
    *
    * @param unit the dpeloyment unit
    * @return true if we should use unit's name as controller context name
    */
   protected boolean isControllerContextNameCandidate(DeploymentUnit unit)
   {
      return useUnitName && unit.isComponent();
   }

   /**
    * Deploy a deployment
    *
    * @param unit the unit
    * @throws DeploymentException for any error
    */
   protected abstract void internalDeploy(DeploymentUnit unit) throws DeploymentException;

   /**
    * Add controller context name.
    *
    * @param unit the deployment unit
    */
   protected void addControllerContextName(DeploymentUnit unit)
   {
      unit.addControllerContextName(unit.getName());
   }

   /**
    * Remove controller context name.
    *
    * @param unit the deployment unit
    */
   protected void removeControllerContextName(DeploymentUnit unit)
   {
      unit.removeControllerContextName(unit.getName());
   }

   public final void undeploy(DeploymentUnit unit)
   {
      try
      {
         if (isControllerContextNameCandidate(unit))
         {
            removeControllerContextName(unit);
         }
      }
      catch (Throwable t)
      {
         if (log.isTraceEnabled())
            log.trace("Exception while removing unit name: " + t);
      }

      internalUndeploy(unit);
   }

   /**
    * Undeploy an deployment
    *
    * @param unit the unit
    */
   protected void internalUndeploy(DeploymentUnit unit)
   {
      // nothing
   }

   /**
    * Put context to deployment mapping.
    *
    * @param context the context
    * @param unit the deployment
    * @return previous mapping value
    */
   protected DeploymentUnit putContext(ControllerContext context, DeploymentUnit unit)
   {
      return (registry != null) ? registry.putContext(context, unit) : null;
   }

   /**
    * Remove context to deployment mapping.
    *
    * @param context the context
    * @param unit the deployment
    * @return is previous mapping value same as unit param
    */
   protected DeploymentUnit removeContext(ControllerContext context, DeploymentUnit unit)
   {
      return (registry != null) ? registry.removeContext(context, unit) : null;
   }

   /**
    * Should we use unit name for controller context name.
    *
    * @return true if usage is allowed
    */
   public boolean isUseUnitName()
   {
      return useUnitName;
   }

   /**
    * Set use unit name for controller context name.
    *
    * @param useUnitName flag to allow unit name usage
    */
   public void setUseUnitName(boolean useUnitName)
   {
      this.useUnitName = useUnitName;
   }

   /**
    * Set the context to deployment mapping.
    *
    * @param registry the registry
    */
   public void setDeploymentRegistry(DeploymentRegistry registry)
   {
      this.registry = registry;
   }
}
