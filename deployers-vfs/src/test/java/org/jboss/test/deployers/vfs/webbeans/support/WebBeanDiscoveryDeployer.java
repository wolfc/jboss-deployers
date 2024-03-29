/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.deployers.vfs.webbeans.support;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.classloading.spi.dependency.Module;
import org.jboss.classloading.spi.visitor.ClassFilter;
import org.jboss.classloading.spi.visitor.ResourceContext;
import org.jboss.classloading.spi.visitor.ResourceFilter;
import org.jboss.classloading.spi.visitor.ResourceVisitor;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.vfs.spi.deployer.AbstractOptionalVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.virtual.VirtualFile;

/**
 * WBD deployer.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class WebBeanDiscoveryDeployer extends AbstractOptionalVFSRealDeployer<JBossWebBeansMetaData>
{
   public WebBeanDiscoveryDeployer()
   {
      super(JBossWebBeansMetaData.class);
      addOutput(WebBeanDiscovery.class);
   }

   public void deploy(VFSDeploymentUnit unit, JBossWebBeansMetaData deployment) throws DeploymentException
   {
      VFSDeploymentUnit topUnit = unit.getTopLevel();
      WebBeanDiscoveryImpl wbdi = topUnit.getAttachment(WebBeanDiscovery.class.getName(), WebBeanDiscoveryImpl.class);
      if (wbdi == null)
      {
         wbdi = new WebBeanDiscoveryImpl();
         topUnit.addAttachment(WebBeanDiscovery.class.getName(), wbdi);
      }

      List<URL> urls = new ArrayList<URL>();

      try
      {
         if (deployment != null)
         {
            // do some custom stuff
         }

         Iterable<VirtualFile> classpaths = getClassPaths(unit);
         for (VirtualFile cp : classpaths)
         {
            VirtualFile wbXml = cp.getChild("META-INF/web-beans.xml");
            if (wbXml != null)
            {
               // add url
               wbdi.addWebBeansXmlURL(wbXml.toURL());
               // add classes
               urls.add(cp.toURL());
            }
         }

         // handle war slightly different
         VirtualFile warWbXml = unit.getFile("WEB-INF/web-beans.xml");
         if (warWbXml != null)
         {
            wbdi.addWebBeansXmlURL(warWbXml.toURL());

            VirtualFile classes = unit.getFile("WEB-INF/classes");
            if (classes != null)
               urls.add(classes.toURL());
         }
      }
      catch (Exception e)
      {
         throw DeploymentException.rethrowAsDeploymentException("Cannot deploy WBD.", e);
      }

      if (urls.isEmpty() == false)
      {
         Module module = unit.getAttachment(Module.class);
         if (module == null)
         {
            VFSDeploymentUnit parent = unit.getParent();
            while (parent != null && module == null)
            {
               module = parent.getAttachment(Module.class);
               parent = parent.getParent();
            }
            if (module == null)
               throw new DeploymentException("No module in deployment unit's hierarchy: " + unit.getName());
         }

         WBDiscoveryVisitor visitor = new WBDiscoveryVisitor(wbdi);
         module.visit(visitor, ClassFilter.INSTANCE, null, urls.toArray(new URL[urls.size()]));
      }
   }

   /**
    * Get the matching class paths that belong to this deployment unit.
    *
    * @param unit the deployment unit
    * @return matching class paths
    * @throws Exception for any error
    */
   protected Iterable<VirtualFile> getClassPaths(VFSDeploymentUnit unit) throws Exception
   {
      List<VirtualFile> classpath = unit.getClassPath();
      if (classpath != null && classpath.isEmpty() == false)
      {
         List<VirtualFile> matching = new ArrayList<VirtualFile>();
         VirtualFile root = unit.getRoot();
         for (VirtualFile cp : classpath)
         {
            VirtualFile check = cp;
            while (check != null && check.equals(root) == false)
               check = check.getParent();

            if (check != null)
               matching.add(cp);
         }
         return matching;
      }
      return Collections.emptySet();
   }

   private class WBDiscoveryVisitor implements ResourceVisitor
   {
      private WebBeanDiscoveryImpl wbdi;

      private WBDiscoveryVisitor(WebBeanDiscoveryImpl wbdi)
      {
         this.wbdi = wbdi;
      }

      public ResourceFilter getFilter()
      {
         return ClassFilter.INSTANCE;
      }

      public void visit(ResourceContext resource)
      {
         wbdi.addWebBeanClass(resource.loadClass());
      }
   }
}