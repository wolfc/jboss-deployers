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
package org.jboss.deployers.vfs.plugins.classloader;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractOptionalVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;

/**
 * Integration deployer.
 * Adds integration path to deployment classpath.
 *
 * @param <T> exact output type
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class UrlIntegrationDeployer<T> extends AbstractOptionalVFSRealDeployer<T>
{
   /** Location of integration jar */
   private Set<URL> integrationURLs;

   public UrlIntegrationDeployer(Class<T> input)
   {
      super(input);
      // We have to run before the classloading is setup
      setStage(DeploymentStages.DESCRIBE);
      // Keep things simple having one attachment to control the classloader processing order
      setOutput(ClassLoadingMetaData.class);
   }

   /**
    * Get the integration url.
    *
    * @return the integration url
    */
   public URL getIntegrationURL()
   {
      if (integrationURLs == null || integrationURLs.isEmpty())
         return null;
      else
         return integrationURLs.iterator().next();
   }

   /**
    * Set integration url.
    *
    * @param url the integration url
    */
   public void setIntegrationURL(URL url)
   {
      if (integrationURLs == null)
         integrationURLs = Collections.singleton(url);
      else
         integrationURLs.add(url);
   }

   /**
    * Get integration urls.
    *
    * @return the integration urls
    */
   public Set<URL> getIntegrationURLs()
   {
      return integrationURLs;
   }

   /**
    * Set integration urls.
    *
    * @param integrationURLs the integration urls
    */
   public void setIntegrationURLs(Set<URL> integrationURLs)
   {
      this.integrationURLs = integrationURLs;
   }

   /**
    * Check if integration urls exist.
    */
   public void start()
   {
      if (integrationURLs == null || integrationURLs.isEmpty())
         throw new IllegalArgumentException("No integration urls.");
   }

   @Override
   public void deploy(VFSDeploymentUnit unit, T metaData) throws DeploymentException
   {
      if (isIntegrationDeployment(unit, metaData))
      {
         List<VirtualFile> added = new ArrayList<VirtualFile>();
         try
         {
            for (URL integrationURL : integrationURLs)
            {
               VirtualFile integration = VFS.getRoot(integrationURL);
               unit.addClassPath(integration);
               added.add(integration);
            }
         }
         catch (Throwable t)
         {
            List<VirtualFile> classPath = unit.getClassPath();
            for (int i = added.size() - 1; i >=0; i--)
            {
               classPath.remove(added.get(i));
            }
            throw DeploymentException.rethrowAsDeploymentException("Error adding integration path.", t);
         }
      }
   }

   @Override
   public void undeploy(VFSDeploymentUnit unit, T metaData)
   {
      if (isIntegrationDeployment(unit, metaData))
      {
         List<VirtualFile> classPath = unit.getClassPath();
         for (URL integrationURL : integrationURLs)
         {
            try
            {
               VirtualFile integration = VFS.getRoot(integrationURL);
               classPath.remove(integration);
            }
            catch (Throwable t)
            {
               log.warn("Error removing integration from classpath: " + integrationURL, t);
            }
         }
      }
   }

   /**
    * Is unit integration deployment unit?
    *
    * @param unit the deployment unit
    * @param metaData the meta data
    * @return true if the unit is integration deployment
    */
   protected boolean isIntegrationDeployment(VFSDeploymentUnit unit, T metaData)
   {
      return isIntegrationDeployment(unit);
   }

   /**
    * Is unit integration deployment unit?
    *
    * @param unit the deployment unit
    * @return true if the unit is integration deployment
    */
   protected abstract boolean isIntegrationDeployment(VFSDeploymentUnit unit);
}